package dk.aau.cs.verification.VerifyTAPN;

import java.util.Vector;
import java.util.function.Function;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.aau.cs.model.CPN.Color;
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
            (place, token) -> marking.add(token)
        );
        
        return marking;
    }

    public static NetworkMarking parseComposedMarking(TimedArcPetriNetNetwork network, Element element, NameMapping nameMapping) {
        NetworkMarking marking = new NetworkMarking();
        parseTokensForPlaces(element,
            place -> {
                Tuple<String, String> originalName = nameMapping.map(place);
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
            }
        );
        
        return marking;
    }
    
    private static void parseTokensForPlaces(Element element,
                                           Function<String, TimedPlace> placeResolver,
                                           Function<String, Color> colorResolver,
                                           Function<Vector<Color>, Color> productColorResolver,
                                           TokenConsumer tokenConsumer) {
        NodeList placeNodes = element.getElementsByTagName("place");
        for (int i = 0; i < placeNodes.getLength(); ++i) {
            Element placeElement = (Element)placeNodes.item(i);
            String placeId = placeElement.getAttribute("id");
            TimedPlace place = placeResolver.apply(placeId);

            NodeList tokenNodes = placeElement.getElementsByTagName("token");
            for (int j = 0; j < tokenNodes.getLength(); ++j) {
                Element tokenElement = (Element)tokenNodes.item(j);
                int count = Integer.parseInt(tokenElement.getAttribute("count"));

                Color color = parseTokenColor(tokenElement, colorResolver, productColorResolver);

                for (int l = 0; l < count; ++l) {
                    TimedToken token = new TimedToken(place, color);
                    tokenConsumer.accept(place, token);
                }
            }
        }
    }
    
    private static Color parseTokenColor(Element tokenElement,
                                       Function<String, Color> colorResolver,
                                       Function<Vector<Color>, Color> productColorResolver) {
        NodeList colorNodes = tokenElement.getElementsByTagName("color");

        if (colorNodes.getLength() == 1) {
            Element colorElement = (Element)colorNodes.item(0);
            String colorName = colorElement.getTextContent();
            return colorResolver.apply(colorName);
        } else {
            Vector<Color> constituents = new Vector<>();   
            for (int k = 0; k < colorNodes.getLength(); ++k) {
                Element colorElement = (Element)colorNodes.item(k);
                String colorName = colorElement.getTextContent();
                Color constituentColor = colorResolver.apply(colorName);
                constituents.add(constituentColor);
            }
            
            return productColorResolver.apply(constituents);
        }
    }
    
    @FunctionalInterface
    private interface TokenConsumer {
        void accept(TimedPlace place, TimedToken token);
    }
}