package dk.aau.cs.verification.VerifyTAPN;

import java.util.Vector;
import java.util.function.Function;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.LocalTimedMarking;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;

public class VerifyTAPNMarkingParser {
    public static LocalTimedMarking parseMarking(TimedArcPetriNet tapn, Element element) {
        LocalTimedMarking marking = new LocalTimedMarking();
        
        parseTokensForPlaces(element, 
            placeId -> tapn.getPlaceByName(placeId),
            tapn.parentNetwork()::getColorByName,
            tapn.parentNetwork()::getProductColorByConstituents,
            (place, token) -> marking.add(token),
            tapn.parentNetwork()
        );
        
        return marking;
    }

    public static NetworkMarking parseComposedMarking(TimedArcPetriNetNetwork network, Element element, NameMapping nameMapping) {
        NetworkMarking marking = new NetworkMarking();
        
        parseTokensForPlaces(element,
            place -> {
                Tuple<String, String> originalName = nameMapping.map(place);
                if (originalName.value1().isEmpty()) {
                    return network.getSharedPlaceByName(originalName.value2());
                }

                return network.getTAPNByName(originalName.value1()).getPlaceByName(originalName.value2());
            },
            network::getColorByName,
            network::getProductColorByConstituents,
            (place, token) -> {
                if (!place.isShared()) {
                    LocalTimedPlace localPlace = (LocalTimedPlace)place;
                    TimedArcPetriNet tapn = localPlace.model();
                    LocalTimedMarking localMarking = marking.getMarkingFor(tapn);
                    if (localMarking == null) {
                        marking.addMarking(tapn, new LocalTimedMarking());
                    }
                }
                
                marking.add(token);
            },
            network
        );
        
        return marking;
    }
    
    private static void parseTokensForPlaces(Element element,
                                           Function<String, TimedPlace> placeResolver,
                                           Function<String, Color> colorResolver,
                                           Function<Vector<Color>, Color> productColorResolver,
                                           TokenConsumer tokenConsumer,
                                           TimedArcPetriNetNetwork network) {
        NodeList placeNodes = element.getElementsByTagName("place");
        for (int i = 0; i < placeNodes.getLength(); ++i) {
            Element placeElement = (Element)placeNodes.item(i);
            String placeId = placeElement.getAttribute("id");
            TimedPlace place = placeResolver.apply(placeId);

            NodeList childNodes = placeElement.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node child = childNodes.item(j);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    try {
                        ArcExpression arcExpr = parseSimpleArcExpression(child, network);
                        expandArcExpressionToTokens(arcExpr, place, tokenConsumer, productColorResolver);
                    } catch (Exception e) {
                        System.err.println("Error parsing arc expression for place " + placeId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private static Node skipWS(Node node) {
        while (node != null && node.getNodeType() == Node.TEXT_NODE && node.getNodeValue().trim().isEmpty()) {
            node = node.getNextSibling();
        }
        return node;
    }
    
    private static ArcExpression parseSimpleArcExpression(Node node, TimedArcPetriNetNetwork network) throws Exception {
        String name = node.getNodeName();
        
        if (name.equals("add")) {
            Vector<ArcExpression> constituents = new Vector<>();
            Node child = skipWS(node.getFirstChild());
            while (child != null) {
                ArcExpression subterm = parseSimpleArcExpression(child, network);
                constituents.add(subterm);
                child = skipWS(child.getNextSibling());
            }
            return new AddExpression(constituents);
        } else if (name.equals("numberof")) {
            return parseNumberOfExpression(node, network);
        } else if (name.matches("subterm|structure")) {
            Node child = skipWS(node.getFirstChild());
            return parseSimpleArcExpression(child, network);
        } else {
            throw new Exception("Unexpected arc expression node: " + name);
        }
    }
    
    private static NumberOfExpression parseNumberOfExpression(Node node, TimedArcPetriNetNetwork network) throws Exception {
        Node child = skipWS(node.getFirstChild());
        
        int number = 1;
        if (child != null && child.getNodeName().equals("subterm")) {
            Node numberNode = skipWS(child.getFirstChild());
            if (numberNode != null && numberNode.getNodeName().equals("numberconstant")) {
                String value = numberNode.getAttributes().getNamedItem("value").getNodeValue();
                number = Integer.parseInt(value);
                child = skipWS(child.getNextSibling());
            }
        }
        
        Vector<ColorExpression> colorExprs = new Vector<>();
        while (child != null) {
            ColorExpression colorExpr = parseColorExpression(child, network);
            if (colorExpr != null) {
                colorExprs.add(colorExpr);
            }
            child = skipWS(child.getNextSibling());
        }
        
        return new NumberOfExpression(number, colorExprs);
    }
    
    private static ColorExpression parseColorExpression(Node node, TimedArcPetriNetNetwork network) throws Exception {
        String name = node.getNodeName();
        
        if (name.equals("all")) {
            String sortId = node.getAttributes().getNamedItem("declaration").getNodeValue();
            ColorType colorType = network.getColorTypeByName(sortId);
            return new AllExpression(colorType);
        } else if (name.equals("useroperator")) {
            String colorName = node.getAttributes().getNamedItem("declaration").getNodeValue();
            Color color = network.getColorByName(colorName);
            return new UserOperatorExpression(color);
        } else if (name.equals("tuple")) {
            Vector<ColorExpression> components = new Vector<>();
            Node child = skipWS(node.getFirstChild());
            while (child != null) {
                ColorExpression comp = parseColorExpression(child, network);
                if (comp != null) {
                    components.add(comp);
                }
                child = skipWS(child.getNextSibling());
            }
            return new TupleExpression(components);
        } else if (name.equals("dotconstant")) {
            return new DotConstantExpression();
        } else if (name.matches("subterm|structure")) {
            Node child = skipWS(node.getFirstChild());
            return parseColorExpression(child, network);
        }
        
        return null;
    }
    
    private static void expandArcExpressionToTokens(ArcExpression arcExpr, TimedPlace place, TokenConsumer tokenConsumer,
                                                    Function<Vector<Color>, Color> productColorResolver) {
        if (arcExpr instanceof AddExpression) {
            AddExpression addExpr = (AddExpression) arcExpr;
            for (ArcExpression subExpr : addExpr.getAddExpression()) {
                expandArcExpressionToTokens(subExpr, place, tokenConsumer, productColorResolver);
            }
        } else if (arcExpr instanceof NumberOfExpression) {
            NumberOfExpression numOfExpr = (NumberOfExpression) arcExpr;
            int count = numOfExpr.getNumber();
            Vector<ColorExpression> colorExprs = numOfExpr.getNumberOfExpression();
            
            for (ColorExpression colorExpr : colorExprs) {
                expandColorExpressionToTokens(colorExpr, place, count, tokenConsumer, productColorResolver);
            }
        }
    }
    
    private static void expandColorExpressionToTokens(ColorExpression colorExpr, TimedPlace place, int count, 
                                                      TokenConsumer tokenConsumer, Function<Vector<Color>, Color> productColorResolver) {
        if (colorExpr instanceof AllExpression) {
            AllExpression allExpr = (AllExpression) colorExpr;
            ColorType colorType = allExpr.getColorType();
            
            for (Color color : colorType.getColors()) {
                for (int i = 0; i < count; ++i) {
                    TimedToken token = new TimedToken(place, color);
                    tokenConsumer.accept(place, token);
                }
            }
        } else if (colorExpr instanceof UserOperatorExpression) {
            UserOperatorExpression userOpExpr = (UserOperatorExpression) colorExpr;
            Color color = userOpExpr.getUserOperator();
            
            for (int i = 0; i < count; ++i) {
                TimedToken token = new TimedToken(place, color);
                tokenConsumer.accept(place, token);
            }
        } else if (colorExpr instanceof TupleExpression) {
            TupleExpression tupleExpr = (TupleExpression) colorExpr;
            expandTupleExpressionToTokens(tupleExpr, place, count, tokenConsumer, productColorResolver);
        } else if (colorExpr instanceof DotConstantExpression) {
            ColorType dotType = ColorType.COLORTYPE_DOT;
            Color dotColor = dotType.getColors().get(0);
            
            for (int i = 0; i < count; ++i) {
                TimedToken token = new TimedToken(place, dotColor);
                tokenConsumer.accept(place, token);
            }
        }
    }
    
    private static void expandTupleExpressionToTokens(TupleExpression tupleExpr, TimedPlace place, int count, 
                                                      TokenConsumer tokenConsumer, Function<Vector<Color>, Color> productColorResolver) {
        Vector<ColorExpression> components = tupleExpr.getColors();
        
        boolean hasAll = components.stream().anyMatch(c -> c instanceof AllExpression);
        
        if (hasAll) {
            expandTupleWithAllExpression(components, place, count, tokenConsumer, new Vector<>(), 0, productColorResolver);
        } else {
            Vector<Color> colors = new Vector<>();
            for (ColorExpression compExpr : components) {
                if (compExpr instanceof UserOperatorExpression) {
                    colors.add(((UserOperatorExpression) compExpr).getUserOperator());
                }
            }
            
            if (colors.size() == components.size()) {
                Color tupleColor = productColorResolver.apply(colors);
                for (int i = 0; i < count; ++i) {
                    TimedToken token = new TimedToken(place, tupleColor);
                    tokenConsumer.accept(place, token);
                }
            }
        }
    }
    
    private static void expandTupleWithAllExpression(Vector<ColorExpression> components, TimedPlace place, int count, 
                                                     TokenConsumer tokenConsumer, Vector<Color> currentColors, int index,
                                                     Function<Vector<Color>, Color> productColorResolver) {
        if (index == components.size()) {
            Color tupleColor = productColorResolver.apply(currentColors);
            for (int i = 0; i < count; ++i) {
                TimedToken token = new TimedToken(place, tupleColor);
                tokenConsumer.accept(place, token);
            }
            return;
        }
        
        ColorExpression component = components.get(index);
        if (component instanceof AllExpression) {
            AllExpression allExpr = (AllExpression) component;
            for (Color color : allExpr.getColorType().getColors()) {
                Vector<Color> newColors = new Vector<>(currentColors);
                newColors.add(color);
                expandTupleWithAllExpression(components, place, count, tokenConsumer, newColors, index + 1, productColorResolver);
            }
        } else if (component instanceof UserOperatorExpression) {
            Vector<Color> newColors = new Vector<>(currentColors);
            newColors.add(((UserOperatorExpression) component).getUserOperator());
            expandTupleWithAllExpression(components, place, count, tokenConsumer, newColors, index + 1, productColorResolver);
        }
    }
    
    @FunctionalInterface
    private interface TokenConsumer {
        void accept(TimedPlace place, TimedToken token);
    }
}