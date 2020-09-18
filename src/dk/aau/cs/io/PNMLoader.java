package dk.aau.cs.io;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.util.Tuple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntWeight;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Require;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;

public class PNMLoader {

    private TabContent.TAPNLens lens;

    enum GraphicsType { Position, Offset }

	private final NameGenerator nameGenerator = new NameGenerator();
	private final IdResolver idResolver = new IdResolver();
	private final HashSet<String> arcs = new HashSet<String>();
	private final HashMap<String, TimedPlace> places = new HashMap<String, TimedPlace>();
	private final HashMap<String, TimedTransition> transitions = new HashMap<String, TimedTransition>();
    private HashMap<String, ColorType> colortypes = new HashMap<String, ColorType>();
    private HashMap<String, Variable> variables = new HashMap<String, Variable>();

	//If the net is too big, do not make the graphics
	private int netSize = 0;
	private final int maxNetSize = 4000;
	private boolean hasPositionalInfo = false;
	
	public PNMLoader() {
	}
	
	public LoadedModel load(File file) throws FormatException{
		try{
			return load(new FileInputStream(file));
		} catch (FileNotFoundException e){
			return null;
		} catch (NullPointerException e){
			throw new FormatException("the PNML file cannot be parsed due to syntax errors");
		}
	}
	
	public LoadedModel load(InputStream file) throws FormatException{
		Document doc = loadDocument(file);
		
		return parse(doc);
	}

	private Document loadDocument(InputStream file) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(file);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			return null;
		}
	}
	
	private LoadedModel parse(Document doc) throws FormatException {
		idResolver.clear();
		
		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
		
		//We assume there is only one net per file (this is what we call a TAPN Network) 
		Node pnmlElement = doc.getElementsByTagName("pnml").item(0);
		Node netNode = getFirstDirectChild(pnmlElement, "net");

        lens = new TabContent.TAPNLens(false, false, getFirstDirectChild(netNode, "declaration") != null);

        String name = getTAPNName(netNode);
		
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);
		tapn.setCheckNames(false);
		network.add(tapn);
		nameGenerator.add(tapn);

		//We assume there is only one page pr. file (this is what we call a net) 
		Template template = new Template(tapn, new DataLayer(), new Zoomer());
	
		parseTimedArcPetriNet(netNode, tapn, template);
		template.setHasPositionalInfo(hasPositionalInfo);
		
		network.setPaintNet(isNetDrawable());
		tapn.setCheckNames(true);
		return new LoadedModel(network, Arrays.asList(template), new ArrayList<TAPNQuery>(), null, lens);
	}

	private String getTAPNName(Node netNode) {
		if(!(netNode instanceof Element)){
			return nameGenerator.getNewTemplateName();
		}
		String result = null;

		Node name =  getFirstDirectChild(netNode, "name");
		if(name != null){
			result = getFirstDirectChild(name, "text").getTextContent();
		}
		
		if(name == null || name.equals("")){
			return nameGenerator.getNewTemplateName();
		}
		
		return NamePurifier.purify(result);
	}

	private void parseTimedArcPetriNet(Node netNode, TimedArcPetriNet tapn, Template template) throws FormatException {
        if (lens.isColored()) {
            Node declarations = getFirstDirectChild(netNode, "declaration");
            parseDeclarations(declarations, tapn, template);
        }

	    //We assume there is only one page pr. file (this is what we call a net)
		Node node = getFirstDirectChild(netNode, "page").getFirstChild();
		Node first = node;
		
		//Calculate netsize
		while(node != null){
			netSize += 1;
			node = node.getNextSibling();
		}
		
		node = first;
		//We parse the places and transitions first
		while(node != null){
			String tag = node.getNodeName();
			if(tag.equals("place")){
				parsePlace(node, tapn, template);
			} else if(tag.equals("transition")){
				parseTransition(node, tapn, template);
			}
			node = node.getNextSibling();
		}
		
		//We parse the transitions last, as we need the places and transitions it refers to
		node = first;
		while(node != null){
			String tag = node.getNodeName();
			if(tag.equals("arc")){
				parseArc(node, template);
			} 
			node = node.getNextSibling();
		}
	}
    //TODO: implement color
	private void parsePlace(Node node, TimedArcPetriNet tapn, Template template) throws FormatException {
		if(!(node instanceof Element)){
			return;
		}
		
		Name name = parseName(getFirstDirectChild(node, "name"));
		if(name == null){
			name = new Name(nameGenerator.getNewPlaceName(template.model()));
		}
		Point position = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Position);
		String id = NamePurifier.purify(((Element) node).getAttribute("id"));

        InitialMarking marking = null;
        ArcExpression colorMarking = null;
        Point markingOffset = null;
        TimedPlace place;

        if (lens.isColored()) {
            Node typeNode = getFirstDirectChild(node, "type");
            ColorType colorType = null;
            if(typeNode != null) {
                colorType = parseUserSort(typeNode);
            }

            Node markingNode = getFirstDirectChild(node, "hlinitialMarking");
            if (markingNode == null) {
                markingOffset = new Point();
                colorMarking = null;
            } else {
                markingOffset = parseGraphics(getFirstDirectChild(markingNode, "graphics"), GraphicsType.Offset);
                Node markingExpression = getFirstDirectChild(markingNode, "structure");
                colorMarking = parseArcExpression(markingExpression);
            }
            place = new LocalTimedPlace(id, colorType);

        } else {
            marking = parseMarking(getFirstDirectChild(node, "initialMarking"));

            place = new LocalTimedPlace(id, new TimeInvariant(false, new Bound.InfBound()), null);
        }
        Require.that(places.put(id, place) == null && !transitions.containsKey(id),
            "The name: " + id + ", was already used");
        tapn.add(place);
		
		if(isNetDrawable()){
			//We parse the id as both the name and id as in tapaal name = id, and name/id has to be unique 
            TimedPlaceComponent placeComponent;
            placeComponent = new TimedPlaceComponent(position.x, position.y, id, name.point.x, name.point.y, lens);
            placeComponent.setUnderlyingPlace(place);
			template.guiModel().addPetriNetObject(placeComponent);
		}
		
		idResolver.add(tapn.name(), id, id);

        if (lens.isColored()) {
            if (colorMarking != null) {
                ExpressionContext context = new ExpressionContext(new HashMap<String, Color>(), colortypes);
                ColorMultiset cm = colorMarking.eval(context);
                for (TimedToken ct : cm.getTokens(place)) {
                    tapn.parentNetwork().marking().add(ct);
                }
            }
        } else {
            for (int i = 0; i < marking.marking; i++) {
                tapn.parentNetwork().marking().add(new TimedToken(place, ColorType.COLORTYPE_DOT.getFirstColor()));
            }
        }
	}

    private void parseDeclarations(Node node, TimedArcPetriNet tapn, Template template) throws FormatException {
        if(node == null || !(node instanceof Element)){
            return;
        }

        Node child = skipWS(node.getFirstChild());
        while(child != null){
            String childName = child.getNodeName();
            if (childName.equals("namedsort")){
                parseNamedSort(child, tapn, template);
            } else if (childName.equals("variabledecl")){
                String id = getAttribute(child, "id").getNodeValue();
                String name = getAttribute(child, "name").getNodeValue();
                ColorType ct = parseUserSort(child);
                Variable var = new Variable(name, id, ct);
                Require.that(variables.put(id, var) == null, "the id " + id + ", was already used");
                tapn.parentNetwork().add(var);
            } else {
                parseDeclarations(child, tapn, template);
            }

            child = skipWS(child.getNextSibling());
        }
    }

    private void parseNamedSort(Node node, TimedArcPetriNet tapn, Template template) throws FormatException {
        Node type = skipWS(node.getFirstChild());
        String typetag = type.getNodeName();

        String id = getAttribute(node, "id").getNodeValue();
        String name = getAttribute(node, "name").getNodeValue();
        if (typetag.equals("productsort")) {
            ProductType pt = new ProductType(name, id);
            Node typechild = skipWS(type.getFirstChild());
            while (typechild != null) {
                if (typechild.getNodeName().equals("usersort")) {
                    String constituent = getAttribute(typechild, "declaration").getNodeValue();
                    pt.addType(colortypes.get(constituent));
                }
                typechild = skipWS(typechild.getNextSibling());
            }
            Require.that(colortypes.put(id, pt) == null, "the name " + name + ", was already used");
            tapn.parentNetwork().add(pt);

        } else {
            ColorType ct = new ColorType(id, name);
            if (typetag.equals("dot")) {
                ct.addColor("dot");
            } else {
                Node typechild = skipWS(type.getFirstChild());
                while (typechild != null) {
                    Node dotId = getAttribute(typechild, "id");
                    if (dotId != null) {
                        ct.addColor(dotId.getNodeValue());
                        typechild = skipWS(typechild.getNextSibling());
                    } else {
                        throw new FormatException(String.format("No id found on %s\n", typechild.getNodeName()));
                    }
                }
            }
            Require.that(colortypes.put(id, ct) == null, "the name " + name + ", was already used");
            tapn.parentNetwork().add(ct);
        }
    }

    private ColorType parseUserSort(Node node) throws FormatException {
        if (node instanceof Element) {
            Node child = skipWS(node.getFirstChild());
            while (child != null) {
                String name = child.getNodeName();
                if (name.equals("usersort")) {
                    Node decl = getAttribute(child, "declaration");
                    return colortypes.get(decl.getNodeValue());
                } else if (name.matches("structure|type|subterm")) {
                    return parseUserSort(child);
                }
                child = skipWS(child.getNextSibling());
            }
        }
        throw new FormatException(String.format("Could not parse %s as an usersort\n", node.getNodeName()));
    }

    private static Node skipWS(Node node) {
        if (node != null && !(node instanceof Element)) {
            return skipWS(node.getNextSibling());
        } else {
            return node;
        }
    }

    private static Node getAttribute(Node node, String attribute) {
        return node.getAttributes().getNamedItem(attribute);
    }

    private ArcExpression parseArcExpression(Node node) throws FormatException {
        String name = node.getNodeName();
        if (name.equals("numberof")) {
            return parseNumberOfExpression(node);
        } else if (name.equals("add")) {
            Vector<ArcExpression> constituents = new Vector<ArcExpression>();

            Node child = skipWS(node.getFirstChild());
            while (child != null) {
                ArcExpression subterm = parseArcExpression(child);
                constituents.add(subterm);
                child = skipWS(child.getNextSibling());
            }
            return new AddExpression(constituents);
        } else if (name.equals("subtract")) {
            Node headchild = skipWS(node.getFirstChild());
            ArcExpression headexp = parseArcExpression(headchild);

            Node nextchild = skipWS(headchild.getNextSibling());
            while (nextchild != null) {
                ArcExpression nextexp = parseArcExpression(nextchild);
                headexp = new SubtractExpression(headexp, nextexp);
                nextchild = skipWS(nextchild.getNextSibling());
            }
            return headexp;
        } else if (name.equals("scalarproduct")) {
            Node scalar = skipWS(node.getFirstChild());
            Integer scalarval = parseNumberConstantExpression(scalar);

            Node child = skipWS(scalar.getNextSibling());
            ArcExpression childexp = parseArcExpression(child);

            return new ScalarProductExpression(scalarval, childexp);

        } else if (name.equals("all")) {
            Node parent = node.getParentNode();
            return parseNumberOfExpression(parent);
        } else if (name.matches("subterm|structure")) {
            Node child = skipWS(node.getFirstChild());
            return parseArcExpression(child);
        } else {
            throw new FormatException(String.format("Could not parse %s as an arc expression\n", name));
        }
    }

    private NumberOfExpression parseNumberOfExpression(Node node) throws FormatException {
        Node number = skipWS(node.getFirstChild());
        //The number constant may be omitted.
        //In that case, this parsing returns null.
        Integer numberval = parseNumberConstantExpression(number);
        Node subnode;
        if (numberval != null) {
            //The subexpression comes after the number constant.
            subnode = skipWS(number.getNextSibling());
        } else {
            //The number we read was actually the subexpression.
            subnode = number;
            numberval = 1;
        }
        //Try to parse subexpression as all expression
        AllExpression subexp = parseAllExpression(subnode);

        if (subexp != null) {
            return new NumberOfExpression(numberval, new Vector<>(Arrays.asList(subexp)));
        } else {
            Vector<ColorExpression> colorexps = new Vector<>();
            while (subnode != null) {
                ColorExpression colorexp = parseColorExpression(subnode);
                colorexps.add(colorexp);
                subnode = skipWS(subnode.getNextSibling());
            }
            return new NumberOfExpression(numberval, colorexps);
        }
    }

    private Integer parseNumberConstantExpression(Node node) {
        String name = node.getNodeName();
        if (name.equals("numberconstant")) {
            String value = getAttribute(node, "value").getNodeValue();
            return Integer.valueOf(value);
        } else if (name.equals("subterm")) {
            Node child = skipWS(node.getFirstChild());
            return parseNumberConstantExpression(child);
        } else {
            return null;
        }
    }

    private AllExpression parseAllExpression(Node node) throws FormatException {
        String name = node.getNodeName();
        if (name.equals("all")) {
            ColorType ct = parseUserSort(node);
            return new AllExpression(ct);
        } else if (name.equals("subterm")) {
            Node child = skipWS(node.getFirstChild());
            return parseAllExpression(child);
        } else {
            return null;
        }
    }

    private ColorExpression parseColorExpression(Node node) throws FormatException {
        String name = node.getNodeName();
        if (name.equals("dotconstant")) {
            return new DotConstantExpression();
        } else if (name.equals("variable")) {
            String varname = getAttribute(node, "refvariable").getNodeValue();
            Variable var = variables.get(varname);
            return new VariableExpression(var);
        } else if (name.equals("useroperator")) {
            String colorname = getAttribute(node, "declaration").getNodeValue();
            Color color = getColor(colorname);
            return new UserOperatorExpression(color);
        } else if (name.equals("successor")) {
            Node child = skipWS(node.getFirstChild());
            ColorExpression childexp = parseColorExpression(child);
            return new SuccessorExpression(childexp);
        } else if (name.equals("predecessor")) {
            Node child = skipWS(node.getFirstChild());
            ColorExpression childexp = parseColorExpression(child);
            return new PredecessorExpression(childexp);
        } else if (name.equals("tuple")) {
            Vector<ColorExpression> colorexps = new Vector<ColorExpression>();

            Node child = skipWS(node.getFirstChild());
            while (child != null) {
                ColorExpression colorexp = parseColorExpression(child);
                colorexps.add(colorexp);
                child = skipWS(child.getNextSibling());
            }
            return new TupleExpression(colorexps);
        } else if (name.matches("subterm|structure")) {
            Node child = skipWS(node.getFirstChild());
            return parseColorExpression(child);
        } else {
            throw new FormatException(String.format("Could not parse %s as an color expression\n", name));
        }
    }

    private Color getColor(String colorname) throws FormatException {
        for (ColorType ct : colortypes.values()) {
            for (Color c : ct) {
                if (c.getName().equals(colorname)) {
                    return c;
                }
            }
        }
        throw new FormatException(String.format("The color \"%s\" was not declared\n", colorname));
    }

    private GuardExpression parseGuardExpression(Node node) throws FormatException {
        String name = node.getNodeName();
        if (name.matches("lt|lessthan")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new LessThanExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("gt|greaterthan")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new GreaterThanExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("leq|lessthanorequal")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new LessThanEqExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("geq|greaterthanorequal")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new GreaterThanEqExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("eq|equality")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new EqualityExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("neq|inequality")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new InequalityExpression(subexps.value1(), subexps.value2());
        } else if (name.equals("not")) {
            Node child = skipWS(node.getFirstChild());
            GuardExpression childexp = parseGuardExpression(child);
            return new NotExpression(childexp);
        } else if (name.equals("and")) {
            Tuple<GuardExpression, GuardExpression> subexps = parseLRGuardExpressions(node);
            return new AndExpression(subexps.value1(), subexps.value2());
        } else if (name.equals("or")) {
            Tuple<GuardExpression, GuardExpression> subexps = parseLRGuardExpressions(node);
            return new OrExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("subterm|structure")) {
            Node child = skipWS(node.getFirstChild());
            return parseGuardExpression(child);
        } else {
            throw new FormatException(String.format("Could not parse %s as a guard expression\n", name));
        }
    }

    private Tuple<ColorExpression,ColorExpression> parseLRColorExpressions(Node node) throws FormatException {
        Node left = skipWS(node.getFirstChild());
        ColorExpression leftexp = parseColorExpression(left);
        Node right = skipWS(left.getNextSibling());
        ColorExpression rightexp = parseColorExpression(right);
        return new Tuple<ColorExpression, ColorExpression>(leftexp, rightexp);
    }

    private Tuple<GuardExpression,GuardExpression> parseLRGuardExpressions(Node node) throws FormatException {
        Node left = skipWS(node.getFirstChild());
        GuardExpression leftexp = parseGuardExpression(left);
        Node right = skipWS(left.getNextSibling());
        GuardExpression rightexp = parseGuardExpression(right);
        return new Tuple<GuardExpression, GuardExpression>(leftexp, rightexp);
    }

    private InitialMarking parseMarking(Node node) {
		if(!(node instanceof Element)){
			return new InitialMarking();
		}
		
		Point offset = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Offset);
		
		int marking = Integer.parseInt(getFirstDirectChild(node, "text").getTextContent());
		
		return new InitialMarking(marking, offset);
	}

	private void parseTransition(Node node, TimedArcPetriNet tapn, Template template) {
		if(!(node instanceof Element)){
			return;
		}
		
		Point position = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Position);
		Name name = parseName(getFirstDirectChild(node, "name"));
		if(name == null){
			name = new Name(nameGenerator.getNewTransitionName(template.model()));
		}
		String id = NamePurifier.purify(((Element) node).getAttribute("id"));
		
		TimedTransition transition = new TimedTransition(id);
		Require.that(transitions.put(id, transition) == null && !places.containsKey(id), 
				"The id: " + id + ", was already used");
		tapn.add(transition);
		
		if(isNetDrawable()){
			TimedTransitionComponent transitionComponent = 
				//We parse the id as both the name and id as in tapaal name = id, and name/id has to be unique 
				new TimedTransitionComponent(position.x, position.y, id, name.point.x, name.point.y, true, false, 0, 0, lens);
			transitionComponent.setUnderlyingTransition(transition);
			template.guiModel().addPetriNetObject(transitionComponent);
		}
		idResolver.add(tapn.name(), id, id);
	}
	
	private void parseArc(Node node, Template template) throws FormatException {
		if(!(node instanceof Element)){
			return;
		}
		
		Element element = (Element) node;
		
		String id = element.getAttribute("id");
		String sourceId = NamePurifier.purify(element.getAttribute("source"));
		String targetId = NamePurifier.purify(element.getAttribute("target"));
		String type = element.getAttribute("type");
		
		String sourceName = idResolver.get(template.model().name(), sourceId);
		String targetName = idResolver.get(template.model().name(), targetId);
		
		TimedPlace sourcePlace = places.get(sourceName);
		TimedPlace targetPlace = places.get(targetName);
		
		TimedTransition sourceTransition = transitions.get(sourceName);
		TimedTransition targetTransition = transitions.get(targetName);
		
		PlaceTransitionObject source = template.guiModel().getPlaceTransitionObject(sourceName);
		PlaceTransitionObject target = template.guiModel().getPlaceTransitionObject(targetName);
		
		//Inscription
		int weight = 1;
		Node inscription  = getFirstDirectChild(node, "inscription");
		if(inscription != null){
			Node text = getFirstDirectChild(inscription, "text");
			if(text != null){
				String weightString = text.getTextContent().trim();
                try {
                    weight = Integer.parseInt(weightString);
                } catch (NumberFormatException ignored) {} //Default values is 1
			}
		}
		
		int _startx = 0, _starty = 0, _endx = 0, _endy = 0;
		
		if(isNetDrawable()){
			// add the insets and offset
			_startx = source.getX() + source.centreOffsetLeft();
			_starty = source.getY() + source.centreOffsetTop();

			_endx = target.getX() + target.centreOffsetLeft();
			_endy = target.getY() + target.centreOffsetTop();
		}
		
		Arc tempArc;
		
		if(type != null && type.equals("inhibitor")) {
			tempArc = parseAndAddTimedInhibitorArc(id, sourcePlace, targetTransition, source, target, weight, _endx, _endy, template);
		} else if(sourcePlace != null && targetTransition != null) {
			tempArc = parseInputArc(id, sourcePlace, targetTransition, source, target, weight, _endx, _endy, template);
		} else if(sourceTransition != null && targetPlace != null) {
			tempArc = parseOutputArc(id,  sourceTransition, targetPlace, source, target, weight, _endx, _endy, template);
		} else {
			throw new FormatException("Arcs must be only between places and transitions");
		}
		
		if(isNetDrawable()) parseArcPath(element, tempArc);
	}
	
	private void parseArcPath(Element arc, Arc tempArc) {
		Element element = (Element) getFirstDirectChild(arc, "graphics");
		if(element == null) return;
		NodeList nodelist = element.getElementsByTagName("position");
		if (nodelist.getLength() > 0) {
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);
				if (node instanceof Element) {
					Element position = (Element) node;
					if ("position".equals(position.getNodeName())) {
						String arcTempX = position.getAttribute("x");
						String arcTempY = position.getAttribute("y");

						double arcPointX = Double.parseDouble(arcTempX);
                        double arcPointY = Double.parseDouble(arcTempY);
						arcPointX += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
						arcPointY += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
						
						//We add the point at i+1 as the starting and end points of 
						//the arc is already in the path as point number 0 and 1
						tempArc.getArcPath().addPoint(i+1,arcPointX, arcPointY, false);
					}
				}
			}
		}
	}

	private Name parseName(Node node){
		if(!(node instanceof Element)){
			return null;
		}
		Point offset = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Offset);
		
		String name = getFirstDirectChild(node, "text").getTextContent();
		if(name == null || name.equals("")){
			return null;
		}
		
		name = NamePurifier.purify(name);
		return new Name(name, offset);
	}
	
	private Point parseGraphics(Node node, GraphicsType type){
		if(!(node instanceof Element)){
			if(type == GraphicsType.Offset)
				return new Point(0, -10);
			else 
				return new Point(100, 100);
		}
		
		hasPositionalInfo = true;
		Element offset = (Element)getFirstDirectChild(node, type == GraphicsType.Offset ? "offset" : "position");
		
		String x = offset.getAttribute("x");
		String y = offset.getAttribute("y");
		
                int xd = Math.round(Float.parseFloat(x));
                int yd = Math.round(Float.parseFloat(y));
               
		return new Point(xd, yd);
	}

	private static class Name{
		String name;
		Point point;
		
		public Name(String newPlaceName) {
			this(newPlaceName, new Point());
		}
		
		public Name(String name, Point p) {
			this.name = name;
			this.point = p;
		}

		@Override
		public String toString() {
			return name + ";" + point;
		}
	}
	
	private static class InitialMarking{
		int marking;
		Point point;
		
		public InitialMarking() {
			this(0, new Point());
		}
			
		
		public InitialMarking(int marking, Point p) {
			this.marking = marking;
			this.point = p;
		}
		
		@Override
		public String toString() {
			return marking + ";" + point;
		}
	}
	
	private TimedInputArcComponent parseInputArc(String arcId, TimedPlace place, TimedTransition transition, PlaceTransitionObject source,
                                                 PlaceTransitionObject target, int weight, int _endx,
                                                 int _endy, Template template) throws FormatException {
        //TODO: Add colors
		TimedInputArc inputArc = new TimedInputArc(place, transition, TimeInterval.ZERO_INF, new IntWeight(weight));
		
		Require.that(places.containsKey(inputArc.source().name()),	"The source place must be part of the petri net.");
		Require.that(transitions.containsKey(inputArc.destination().name()), "The destination transition must be part of the petri net");
		if(!arcs.add(inputArc.source().name() + "-in-" + inputArc.destination().name())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}
		
		TimedInputArcComponent arc = null;
		
		if(isNetDrawable()){
			arc = new TimedInputArcComponent(new TimedOutputArcComponent(source, target, weight, arcId), lens);
			arc.setUnderlyingArc(inputArc);

			template.guiModel().addPetriNetObject(arc);
		}
		
		template.model().add(inputArc);
		
		return arc;

		
	}
	
	private Arc parseOutputArc(String arcId, TimedTransition transition, TimedPlace place, PlaceTransitionObject source,
                               PlaceTransitionObject target, int weight, int _endx,
                               int _endy, Template template) throws FormatException {
		//TODO: Add colors
		TimedOutputArc outputArc = new TimedOutputArc(transition, place, new IntWeight(weight));
		
		Require.that(places.containsKey(outputArc.destination().name()), "The destination place must be part of the petri net.");
		Require.that(transitions.containsKey(outputArc.source().name()), "The source transition must be part of the petri net");
		if(!arcs.add(outputArc.source().name() + "-out-" + outputArc.destination().name())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}
		
		TimedOutputArcComponent arc = null;
		
		if(isNetDrawable()){
			arc = new TimedOutputArcComponent(
                source, target, weight,	arcId);
			arc.setUnderlyingArc(outputArc);

			template.guiModel().addPetriNetObject(arc);

		}
		
		template.model().add(outputArc);
		return arc;
	}
	
	private Arc parseAndAddTimedInhibitorArc(String arcId, TimedPlace place, TimedTransition transition, PlaceTransitionObject source,
                                             PlaceTransitionObject target, int weight, int _endx,
                                             int _endy, Template template) {
		TimedInhibitorArcComponent tempArc = new TimedInhibitorArcComponent(
				new TimedInputArcComponent(
						new TimedOutputArcComponent(source, target, weight, arcId)
				),
				(""));
		//TODO: Add colors
		TimedInhibitorArc inhibArc = new TimedInhibitorArc(place, transition, TimeInterval.ZERO_INF, new IntWeight(weight), null);

		tempArc.setUnderlyingArc(inhibArc);
		template.guiModel().addPetriNetObject(tempArc);
		template.model().add(inhibArc);

		return tempArc;
	}
	
	private boolean isNetDrawable(){
		return netSize <= maxNetSize;
	}
	
	Node getFirstDirectChild(Node parent, String tagName){
		NodeList children = parent.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			if(children.item(i).getNodeName().equals(tagName)){
				return children.item(i);
			}
		}
		return null;
	}

}
