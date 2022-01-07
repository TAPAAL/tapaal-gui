package dk.aau.cs.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import kotlin.Pair;
import dk.aau.cs.gui.TabContent;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;
import pipe.gui.Constants;
import pipe.gui.canvas.Zoomer;
import pipe.gui.graphicElements.*;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.io.queries.TAPNQueryLoader;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.IntWeight;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
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
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.model.tapn.Weight;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Require;

public class TapnXmlLoader {
	private static final String PLACENAME_ERROR_MESSAGE = "The keywords \"true\" and \"false\" are reserved and can not be used as place names.\nPlaces with these names will be renamed to \"_true\" and \"_false\" respectively.\n\n Note that any queries using these places may not be parsed correctly.";

	private final HashMap<TimedTransitionComponent, TimedTransportArcComponent> presetArcs = new HashMap<TimedTransitionComponent, TimedTransportArcComponent>();
	private final HashMap<TimedTransitionComponent, TimedTransportArcComponent> postsetArcs = new HashMap<TimedTransitionComponent, TimedTransportArcComponent>();
	private final HashMap<TimedTransportArcComponent, TimeInterval> transportArcsTimeIntervals = new HashMap<TimedTransportArcComponent, TimeInterval>();
    private final HashMap<TimedTransportArcComponent, List<ColoredTimeInterval>> coloredTransportArcsTimeIntervals = new HashMap<TimedTransportArcComponent, List<ColoredTimeInterval>>();
    private ArcExpression transportExpr;

    private final NameGenerator nameGenerator = new NameGenerator();
	private boolean firstInhibitorIntervalWarning = true;
	private boolean firstPlaceRenameWarning = true;
	private final IdResolver idResolver = new IdResolver();
    private final Collection<String> messages = new ArrayList<>(10);
    int groupPlaceHolder = 1;
    private LoadTACPN loadTACPN = new LoadTACPN();
    boolean hasFeatureTag = false;
    private TabContent.TAPNLens lens = TabContent.TAPNLens.Default;

	public TapnXmlLoader() {

	}

    public TabContent.TAPNLens loadLens(InputStream file) throws FormatException {
        Require.that(file != null, "file must be non-null and exist");

        Document doc = loadDocument(file);
        if(doc == null) return null;

        idResolver.clear();
        parseFeature(doc);

        if (hasFeatureTag) {
            return lens;
        }
        return null;
    }

	public LoadedModel load(InputStream file) throws Exception {
		Require.that(file != null, "file must be non-null and exist");

		Document doc = loadDocument(file);
		if(doc == null) return null;
		try {
            return parse(doc);
        } catch (FormatException | NullPointerException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("One or more necessary attributes were not found\n  - One or more attribute values have an incorrect type");
        }
	}
	
	public LoadedModel load(File file) throws Exception {
		Require.that(file != null && file.exists(), "file must be non-null and exist");

		Document doc = loadDocument(file);
		if(doc == null) return null;
        try {
            return parse(doc);
        } catch (FormatException | NullPointerException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("One or more necessary attributes were not found\n  - One or more attribute values have an incorrect type");
        }
	}
	
	
	private Document loadDocument(InputStream file) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(file);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			return null;
		}
	}

	private Document loadDocument(File file) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(file);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			return null;
		}
	}

	private LoadedModel parse(Document doc) throws FormatException {
		idResolver.clear();


		ConstantStore constants = new ConstantStore(parseConstants(doc));
		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork(constants, new ArrayList<>());
        NodeList declarations = doc.getElementsByTagName("declaration");

        if (declarations.getLength() > 0) {
            //throw new FormatException("File did not contain any declarations components.");

            for (int i = 0; i < declarations.getLength(); i++) {
                Node node = declarations.item(i);
                if (node.getNodeName().equals("declaration")) {
                    loadTACPN.parseDeclarations(node, network);
                }
            }
            for(String message : loadTACPN.getMessages()){
                messages.add(message);
            }
        } else{
            network.add(ColorType.COLORTYPE_DOT);
        }
		parseSharedPlaces(doc, network, constants);
		parseSharedTransitions(doc, network);
		
		Collection<Template> templates = parseTemplates(doc, network, constants);
		LoadedQueries loadedQueries = new TAPNQueryLoader(doc, network).parseQueries();

		if (loadedQueries != null) {
            for (String message : loadedQueries.getMessages()) {
                messages.add(message);
            }
        }
		network.buildConstraints();
		
		parseBound(doc, network);

		parseFeature(doc, network);

        if (hasFeatureTag) {
            return new LoadedModel(network, templates, loadedQueries.getQueries(), messages, lens);
        } else {
            return new LoadedModel(network, templates, loadedQueries.getQueries(), messages, null);
        }
	}

	private void parseBound(Document doc, TimedArcPetriNetNetwork network){
		if(doc.getElementsByTagName("k-bound").getLength() > 0){
			int i = Integer.parseInt(doc.getElementsByTagName("k-bound").item(0).getAttributes().getNamedItem("bound").getNodeValue());
			network.setDefaultBound(i);
		}
	}

    private void parseFeature(Document doc, TimedArcPetriNetNetwork network) {
        if (doc.getElementsByTagName("feature").getLength() > 0) {
	        NodeList nodeList = doc.getElementsByTagName("feature");

	        hasFeatureTag = true;

            var isTimedElement = nodeList.item(0).getAttributes().getNamedItem("isTimed");
            boolean isTimed = isTimedElement == null ? network.isTimed() : Boolean.parseBoolean(isTimedElement.getNodeValue());

            var isGameElement = nodeList.item(0).getAttributes().getNamedItem("isGame");
            boolean isGame = isGameElement == null ? network.hasUncontrollableTransitions() : Boolean.parseBoolean(isGameElement.getNodeValue());

            var isColoredElement = nodeList.item(0).getAttributes().getNamedItem("isColored");
            boolean isColored = isColoredElement == null ? network.isColored() : Boolean.parseBoolean(isColoredElement.getNodeValue());

            lens = new TabContent.TAPNLens(isTimed, isGame, isColored);
        }
    }

    private void parseFeature(Document doc) {
        if (doc.getElementsByTagName("feature").getLength() > 0) {
            NodeList nodeList = doc.getElementsByTagName("feature");

            hasFeatureTag = true;

            var isTimed = Boolean.parseBoolean(nodeList.item(0).getAttributes().getNamedItem("isTimed").getNodeValue());
            var isGame = Boolean.parseBoolean(nodeList.item(0).getAttributes().getNamedItem("isGame").getNodeValue());
            var isColored = Boolean.parseBoolean(nodeList.item(0).getAttributes().getNamedItem("isColored").getNodeValue());

            lens = new TabContent.TAPNLens(isTimed, isGame, isColored);
        }
    }

	private void parseSharedPlaces(Document doc, TimedArcPetriNetNetwork network, ConstantStore constants) {
		NodeList sharedPlaceNodes = doc.getElementsByTagName("shared-place");

		for(int i = 0; i < sharedPlaceNodes.getLength(); i++){
			Node node = sharedPlaceNodes.item(i);

			if(node instanceof Element){
				SharedPlace place = parseSharedPlace((Element)node, network, constants);
				network.add(place);
			}
		}
	}

	private SharedPlace parseSharedPlace(Element element, TimedArcPetriNetNetwork network, ConstantStore constants) {
		String name = element.getAttribute("name");
		TimeInvariant invariant = TimeInvariant.parse(element.getAttribute("invariant"), constants);
		//int numberOfTokens = Integer.parseInt(element.getAttribute("initialMarking"));

		if(name.toLowerCase().equals("true") || name.toLowerCase().equals("false")) {
			name = "_" + name;
			if(firstPlaceRenameWarning) {
				messages.add(PLACENAME_ERROR_MESSAGE);
				firstPlaceRenameWarning = false;
			}
		}
		SharedPlace place = new SharedPlace(name, invariant);
        place.setCurrentMarking(network.marking());
        place.setColorType(parsePlaceColorType(element));
		//place.addTokens(numbesrOfTokens);
        addColoredDependencies(place,element, network, constants);


        return place;
	}

	private void parseSharedTransitions(Document doc, TimedArcPetriNetNetwork network) {
		NodeList sharedTransitionNodes = doc.getElementsByTagName("shared-transition");

		for(int i = 0; i < sharedTransitionNodes.getLength(); i++){
			Node node = sharedTransitionNodes.item(i);

			if(node instanceof Element){
				SharedTransition transition = parseSharedTransition((Element)node);
				network.add(transition);
			}
		}
	}

	private SharedTransition parseSharedTransition(Element element) {
		String name = element.getAttribute("name");
		boolean urgent = Boolean.parseBoolean(element.getAttribute("urgent"));
        boolean isUncontrollable = element.getAttribute("player").equals("1");
		
		SharedTransition st = new SharedTransition(name);
		st.setUrgent(urgent);
		st.setUncontrollable(isUncontrollable);
		return st;
	}

	private Collection<Template> parseTemplates(Document doc, TimedArcPetriNetNetwork network, ConstantStore constants) throws FormatException {
		Collection<Template> templates = new ArrayList<Template>();
		NodeList nets = doc.getElementsByTagName("net");
		
		if(nets.getLength() <= 0)
			throw new FormatException("File did not contain any TAPN components.");
		
		for (int i = 0; i < nets.getLength(); i++) {
			Template template = parseTimedArcPetriNet(nets.item(i), network, constants);
            template.setHasPositionalInfo(true); //We assume that all templates have positional info
			templates.add(template);
		}
		return templates;
	}

	private List<Constant> parseConstants(Document doc) {
		List<Constant> constants = new ArrayList<Constant>();
		NodeList constantNodes = doc.getElementsByTagName("constant");
		for (int i = 0; i < constantNodes.getLength(); i++) {
			Node c = constantNodes.item(i);

			if (c instanceof Element) {
				Constant constant = parseConstant((Element) c);
				constants.add(constant);
			}
		}
		return constants;
	}

	private Template parseTimedArcPetriNet(Node tapnNode, TimedArcPetriNetNetwork network, ConstantStore constants) throws FormatException {
        String name = getTAPNName(tapnNode);

		boolean active = getActiveStatus(tapnNode);

		TimedArcPetriNet tapn = new TimedArcPetriNet(name);
		tapn.setActive(active);
		network.add(tapn);
		nameGenerator.add(tapn);
		
		DataLayer guiModel = new DataLayer();
		Template template = new Template(tapn, guiModel, new Zoomer());

		NodeList nodeList = tapnNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {

			Node node = nodeList.item(i);
			if(node instanceof Element){
				parseElement((Element)node, template, network, constants);
			}
		}

		return template;
	}

	private boolean getActiveStatus(Node tapnNode) {
        if (tapnNode instanceof Element) {
            Element element = (Element)tapnNode;
            String activeString = element.getAttribute("active");

            if (activeString == null || activeString.equals(""))
                return true;
            else
                return activeString.equals("true");
        } else {
            return true;
        }
    }

	private void parseElement(Element element, Template template, TimedArcPetriNetNetwork network, ConstantStore constants) throws FormatException {
		if ("labels".equals(element.getNodeName())) {
			AnnotationNote note = parseAnnotation(element);
			template.guiModel().addPetriNetObject(note);
		} else if ("place".equals(element.getNodeName())) {
			TimedPlaceComponent place = parsePlace(element, network, template.model(), constants);
			template.guiModel().addPetriNetObject(place);
		} else if ("transition".equals(element.getNodeName())) {
			TimedTransitionComponent transition = parseTransition(element, network, template.model());
			template.guiModel().addPetriNetObject(transition);
		} else if (element.getNodeName().matches("arc|outputArc|inputArc|inhibitorArc|transportArc")) {
            parseAndAddArc(element, template, constants, network);
        }
	}

	private boolean isNameAllowed(String name) {
		Require.that(name != null, "name was null");

		return !name.isEmpty() && java.util.regex.Pattern.matches("[a-zA-Z]([_a-zA-Z0-9])*", name);
	}


	private String getTAPNName(Node tapnNode) {
		if (tapnNode instanceof Element) {
			Element element = (Element)tapnNode;
			String name = element.getAttribute("name");

			if (name == null || name.equals(""))
				name = element.getAttribute("id");

			if(!isNameAllowed(name)){
				name = nameGenerator.getNewTemplateName();
			}
			nameGenerator.updateTemplateIndex(name);
			return name;
		} else {
			return nameGenerator.getNewTemplateName();
		}
	}

	private AnnotationNote parseAnnotation(Element annotation) {
		int positionXInput = 0;
		int positionYInput = 0;
		int widthInput = 0;
		int heightInput = 0;
		boolean borderInput = true;

		String positionXTempStorage = annotation.getAttribute("positionX");
		String positionYTempStorage = annotation.getAttribute("positionY");
		String widthTemp = annotation.getAttribute("width");
		String heightTemp = annotation.getAttribute("height");
		String borderTemp = annotation.getAttribute("border");

		String text = annotation.getTextContent();

		if (positionXTempStorage.length() > 0) {
			positionXInput = Integer.parseInt(positionXTempStorage) + 1;
		}

		if (positionYTempStorage.length() > 0) {
			positionYInput = Integer.parseInt(positionYTempStorage) + 1;
		}

		if (widthTemp.length() > 0) {
			widthInput = Integer.parseInt(widthTemp) + 1;
		}

		if (heightTemp.length() > 0) {
			heightInput = Integer.parseInt(heightTemp) + 1;
		}

		if (borderTemp.length() > 0) {
			borderInput = Boolean.parseBoolean(borderTemp);
		} else {
			borderInput = true;
		}
        return new AnnotationNote(text, positionXInput, positionYInput, widthInput, heightInput, borderInput);
	}

	private TimedTransitionComponent parseTransition(Element transition, TimedArcPetriNetNetwork network, TimedArcPetriNet tapn) {
		String posX = transition.getAttribute("positionX");
		String posY = transition.getAttribute("positionY");
		String nameOffsetX = transition.getAttribute("nameOffsetX");
		String nameOffsetY = transition.getAttribute("nameOffsetY");
		String angleStr = transition.getAttribute("angle");
		String priorityStr = transition.getAttribute("priority");
	    int positionXInput = 0;
		int positionYInput = 0;
		int nameOffsetXInput = 0;
		int nameOffsetYInput = 0;
		int angle = 0;
        int priority = 0;
		if(!posX.isEmpty()){
		    positionXInput = (int)Double.parseDouble(posX);
        }
		if(!posY.isEmpty()){
		    positionYInput = (int)Double.parseDouble(posY);
        }
		if(!nameOffsetX.isEmpty()){
		    nameOffsetXInput = (int)Double.parseDouble(nameOffsetX);
        }
		if(!nameOffsetY.isEmpty()){
		    nameOffsetYInput = (int)Double.parseDouble(nameOffsetY);
        }
		if(!angleStr.isEmpty()){
		    angle = Integer.parseInt(angleStr);
        }
		if(!priorityStr.isEmpty()){
		    priority = Integer.parseInt(priorityStr);
        }
		String idInput = transition.getAttribute("id");
		String nameInput = transition.getAttribute("name");
		boolean isUrgent = Boolean.parseBoolean(transition.getAttribute("urgent"));

		String player = transition.getAttribute("player");

		idResolver.add(tapn.name(), idInput, nameInput);

		boolean infiniteServer = transition.getAttribute("infiniteServer").equals("true");

		boolean displayName = transition.getAttribute("displayName").equals("false") ? false : true;


		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}
        GuardExpression guardExpr = null;

        Node conditionNode = getFirstDirectChild(transition, "condition");
        if (conditionNode != null) {
            try {
                guardExpr = loadTACPN.parseGuardExpression(getFirstDirectChild(conditionNode, "structure"));
            } catch (FormatException e) {
                e.printStackTrace();
            }
        }
		
		TimedTransition t = new TimedTransition(nameInput, guardExpr);
		t.setUrgent(isUrgent);
		t.setUncontrollable(player.equals("1"));
		if(network.isNameUsedForShared(nameInput)){
			t.setName(nameGenerator.getNewTransitionName(tapn)); // introduce temporary name to avoid exceptions
			tapn.add(t);
			network.getSharedTransitionByName(nameInput).makeShared(t);
		}else{
			tapn.add(t);
		}
		nameGenerator.updateIndicesForAllModels(nameInput);
		TimedTransitionComponent transitionComponent = new TimedTransitionComponent(
				positionXInput, positionYInput, idInput,
				nameOffsetXInput, nameOffsetYInput, true,
				infiniteServer, angle, priority, lens);
		transitionComponent.setUnderlyingTransition(t);
		
		if (!displayName){
			transitionComponent.setAttributesVisible(false);
		}
		return transitionComponent;
	}

	private TimedPlaceComponent parsePlace(Element place, TimedArcPetriNetNetwork network, TimedArcPetriNet tapn, ConstantStore constants) {
        String placePosX = place.getAttribute("positionX");
        String placePosY = place.getAttribute("positionY");
        String nameOffsetX = place.getAttribute("nameOffsetX");
        String nameOffsetY = place.getAttribute("nameOffsetY");
        int positionXInput = 0;
        int positionYInput = 0;
	    if(!placePosX.isEmpty()){
	        positionXInput = (int)Double.parseDouble(placePosX);
        }
	    if(!placePosY.isEmpty()){
            positionYInput = (int)Double.parseDouble(placePosY);
        }
		String idInput = place.getAttribute("id");
		String nameInput = place.getAttribute("name");

		int nameOffsetXInput = 0;
        int nameOffsetYInput = 0;
		if(!nameOffsetX.isEmpty()){
		    nameOffsetXInput = (int)Double.parseDouble(nameOffsetX);
        }
		if(!nameOffsetY.isEmpty()){
		    nameOffsetYInput = (int)Double.parseDouble(nameOffsetY);
        }

		String invariant = place.getAttribute("invariant");
		boolean displayName = place.getAttribute("displayName").equals("false") ? false : true;

		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}

		if(nameInput.toLowerCase().equals("true") || nameInput.toLowerCase().equals("false")) {
			nameInput = "_" + nameInput;
			if(firstPlaceRenameWarning) {
                messages.add(PLACENAME_ERROR_MESSAGE);
				firstPlaceRenameWarning = false;
			}
		}

		idResolver.add(tapn.name(), idInput, nameInput);

		TimedPlace p;
		if(network.isNameUsedForShared(nameInput)){
			p = network.getSharedPlaceByName(nameInput);
            tapn.add(p);
		}else{
		    p = new LocalTimedPlace(nameInput, TimeInvariant.parse(invariant, constants), parsePlaceColorType(place));
		    tapn.add(p);
		    addColoredDependencies(p,place, network, constants);

		}
		nameGenerator.updateIndicesForAllModels(nameInput);
		TimedPlaceComponent placeComponent = new TimedPlaceComponent(positionXInput, positionYInput, idInput, nameOffsetXInput, nameOffsetYInput, lens);
		placeComponent.setUnderlyingPlace(p);

		if (!displayName){
			placeComponent.setAttributesVisible(false);
		}

        return placeComponent;
	}

	private ColorType parsePlaceColorType(Element element){
        ColorType ct = ColorType.COLORTYPE_DOT;
        Node typeNode = element.getElementsByTagName("type").item(0);
        if (typeNode != null) {
            try {
                ct = loadTACPN.parseUserSort(typeNode);
            } catch (FormatException e) {
                e.printStackTrace();
            }
        }
        return ct;
    }

	private void addColoredDependencies(TimedPlace p, Element place, TimedArcPetriNetNetwork network, ConstantStore constants){
        List<ColoredTimeInvariant> ctiList = new ArrayList<ColoredTimeInvariant>();
        int initialMarkingInput = Integer.parseInt(place.getAttribute("initialMarking"));

        ArcExpression colorMarking = null;
        NodeList nodes = place.getElementsByTagName("colorinvariant");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Pair<String, Vector<Color>> pair = parseColorInvariant((Element) nodes.item(i), network);
                ColoredTimeInvariant cti = ColoredTimeInvariant.parse(pair.getFirst(), constants, pair.getSecond());
                ctiList.add(cti);
            }
        }
        if (place.getAttribute("inscription").length() > 0) {
            ctiList.add(ColoredTimeInvariant.parse(place.getAttribute("inscription"), constants, new Vector<Color>() {{
                add(Color.STAR_COLOR);
            }}));
        }
        Node hlInitialMarkingNode = place.getElementsByTagName("hlinitialMarking").item(0);

        if (hlInitialMarkingNode != null && hlInitialMarkingNode instanceof Element) {
            try {
                colorMarking = loadTACPN.parseArcExpression(((Element)hlInitialMarkingNode).getElementsByTagName("structure").item(0));
            } catch (FormatException e) {
                e.printStackTrace();
            }
        }


	    p.setCtiList(ctiList);
        ExpressionContext context = new ExpressionContext(new HashMap<String, Color>(), loadTACPN.getColortypes());
        if(colorMarking!= null){
            ColorMultiset cm = colorMarking.eval(context);

            p.setTokenExpression(loadTACPN.constructCleanAddExpression(p.getColorType(),cm));


            for (TimedToken ctElement : cm.getTokens(p)) {
                network.marking().add(ctElement);
                //p.addToken(ctElement);
            }

        } else {
            for (int i = 0; i < initialMarkingInput; i++) {
                //Regular tokens will just be dotconstant
                network.marking().add(new TimedToken(p, ColorType.COLORTYPE_DOT.getFirstColor()));
            }
            if(initialMarkingInput > 1) {
                Vector<ColorExpression> v = new Vector<>();
                v.add(new DotConstantExpression());
                Vector<ArcExpression> numbOfExpression = new Vector<>();
                numbOfExpression.add(new NumberOfExpression(initialMarkingInput, v));
                p.setTokenExpression(new AddExpression(numbOfExpression));
            }
        }
    }

	private void parseAndAddArc(Element arc, Template template, ConstantStore constants, TimedArcPetriNetNetwork network) throws FormatException {
		String idInput = arc.getAttribute("id");
		String sourceInput = arc.getAttribute("source");
		String targetInput = arc.getAttribute("target");
		boolean taggedArc = arc.getAttribute("tagged").equals("true") ? true : false;
		String inscriptionTempStorage = arc.getAttribute("inscription");
		String type = arc.getAttribute("type");
		if(type.isEmpty()){
		    if(arc.getNodeName().equals("transportArc")){
		        type = "transport";
            } else if (arc.getNodeName().equals("inhibitorArc")){
		        type = "inhibitor";
            } else if (arc.getNodeName().equals("inputArc")){
		        type = "timed";
            } else {
		        type = "";
            }
        }
		int nameOffsetXInput;
		int nameOffsetYInput;
		
		//This check is done, as arcs in nets saved before this change do not have a nameOffset
		if(!arc.getAttribute("nameOffsetX").equals("") && !arc.getAttribute("nameOffsetY").equals("")) {
			nameOffsetXInput = (int) Double.parseDouble(arc.getAttribute("nameOffsetX"));
			nameOffsetYInput = (int) Double.parseDouble(arc.getAttribute("nameOffsetY"));
		} else {
			nameOffsetXInput = 0;
			nameOffsetYInput = 0;
		}
		
		sourceInput = idResolver.get(template.model().name(), sourceInput);
		targetInput = idResolver.get(template.model().name(), targetInput);
		
		PlaceTransitionObject sourceIn = template.guiModel().getPlaceTransitionObject(sourceInput);
		PlaceTransitionObject targetIn = template.guiModel().getPlaceTransitionObject(targetInput);

		// add the insets and offset
		int _startx = sourceIn.getX() + sourceIn.centreOffsetLeft();
		int _starty = sourceIn.getY() + sourceIn.centreOffsetTop();

		int _endx = targetIn.getX() + targetIn.centreOffsetLeft();
		int _endy = targetIn.getY() + targetIn.centreOffsetTop();

		//Get weight if any
		Weight weight = new IntWeight(1);
		if(arc.hasAttribute("weight")){
			weight = Weight.parseWeight(arc.getAttribute("weight"), constants);
		}
        ArcExpression arcExpr = null;
        List<ColoredTimeInterval> ctiList = new ArrayList<ColoredTimeInterval>();
        Node hlInscription = getFirstDirectChild(arc, "hlinscription");
        if (hlInscription != null)
            hlInscription = getFirstDirectChild(hlInscription, "structure");
        if (hlInscription != null)
            arcExpr = loadTACPN.parseArcExpression(hlInscription);

        NodeList intervalNodes = arc.getElementsByTagName("colorinterval");
        if (intervalNodes != null) {
            for (int i = 0; i < intervalNodes.getLength(); i++) {
                if (intervalNodes.item(i) instanceof  Element) {
                    Element interval = (Element) intervalNodes.item(i);
                    Pair<String, Vector<Color>> pair =  parseColorInvariant(interval, network);
                    ColoredTimeInterval coloredinterval = ColoredTimeInterval.parse(pair.getFirst(), constants, pair.getSecond());
                    ctiList.add(coloredinterval);
                }
            }
        }

		Arc tempArc;
        Arc tempArc2 = null;

		if (type.equals("tapnInhibitor") || type.equals("inhibitor")) {

			tempArc = parseAndAddTimedInhibitorArc(idInput, taggedArc,
					inscriptionTempStorage, sourceIn, targetIn,
                _endx, _endy,template, constants, weight, arcExpr);

		} else {
			if (type.equals("timed")) {
				tempArc = parseAndAddTimedInputArc(idInput, taggedArc,
						inscriptionTempStorage, sourceIn, targetIn,
                    _endx, _endy, template, constants, weight, ctiList, arcExpr);

			} else if (type.equals("transport")) {
                String transition = arc.getAttribute("transition");
                if(transition.isEmpty()){
                    tempArc = parseAndAddTransportArc(idInput, taggedArc,
                        inscriptionTempStorage, sourceIn, targetIn,
                        _endx, _endy, template, constants, weight, ctiList, arcExpr);
                } else {
                    transition = idResolver.get(template.model().name(), transition);
                    PlaceTransitionObject transitionIn = template.guiModel().getPlaceTransitionObject(transition);

                    tempArc = parseAndAddTransportArc(idInput, taggedArc,
                        inscriptionTempStorage, sourceIn, transitionIn,
                        _endx, _endy, template, constants, weight, ctiList, arcExpr);

                    tempArc2 = parseAndAddTransportArc(idInput, taggedArc,
                        inscriptionTempStorage, transitionIn, targetIn,
                        _endx, _endy, template, constants, weight, ctiList, arcExpr);
                }
			} else {
				tempArc = parseAndAddTimedOutputArc(idInput, taggedArc,
						inscriptionTempStorage, sourceIn, targetIn,
                    _endx, _endy, template, weight, arcExpr);
			}

		}
		tempArc.setNameOffsetX(nameOffsetXInput);
		tempArc.setNameOffsetY(nameOffsetYInput);

		parseArcPath(arc, tempArc);
		if(tempArc2 != null){
            tempArc2.setNameOffsetX(nameOffsetXInput);
            tempArc2.setNameOffsetY(nameOffsetYInput);

            parseArcPath(arc, tempArc2);
        }
	}

	private TimedOutputArcComponent parseAndAddTimedOutputArc(String idInput, boolean taggedArc,
                                                              String inscriptionTempStorage, PlaceTransitionObject sourceIn,
                                                              PlaceTransitionObject targetIn,
                                                              int _endx, int _endy, Template template, Weight weight,ArcExpression expr) throws FormatException {

		TimedOutputArcComponent tempArc = new TimedOutputArcComponent(sourceIn, targetIn,	(!inscriptionTempStorage.equals("") ? Integer.parseInt(inscriptionTempStorage) : 1), idInput);

		TimedPlace place = template.model().getPlaceByName(targetIn.getName());
		TimedTransition transition = template.model().getTransitionByName(sourceIn.getName());

		TimedOutputArc outputArc = new TimedOutputArc(transition, place, weight, expr);
		tempArc.setUnderlyingArc(outputArc);

		if(template.model().hasArcFromTransitionToPlace(outputArc.source(),outputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}

		template.guiModel().addPetriNetObject(tempArc);
		template.model().add(outputArc);

		return tempArc;
	}

	private TimedTransportArcComponent parseAndAddTransportArc(String idInput, boolean taggedArc,
                                                               String inscriptionTempStorage, PlaceTransitionObject sourceIn,
                                                               PlaceTransitionObject targetIn,
                                                               int _endx, int _endy, Template template, ConstantStore constants, Weight weight, List<ColoredTimeInterval> ctiList, ArcExpression expr) {

		
		String[] inscriptionSplit = {};
		if (inscriptionTempStorage.contains(":")) {
			inscriptionSplit = inscriptionTempStorage.split(":");
		} else{
		    inscriptionSplit = new String[]{inscriptionTempStorage};
        }
		boolean isInPreSet = false;
		if (sourceIn instanceof Place) {
			isInPreSet = true;
		}

		TimedTransportArcComponent tempArc = new TimedTransportArcComponent(new TimedInputArcComponent(new TimedOutputArcComponent(sourceIn, targetIn, 1, idInput)), -1, isInPreSet);


		if (isInPreSet) {
			if (postsetArcs.containsKey(targetIn)) {
				TimedTransportArcComponent postsetTransportArc = postsetArcs.get(targetIn);
				TimedPlace sourcePlace = template.model().getPlaceByName(sourceIn.getName());
				TimedTransition trans = template.model().getTransitionByName(targetIn.getName());
				TimedPlace destPlace = template.model().getPlaceByName(postsetTransportArc.getTarget().getName());

                TimeInterval timeInterval = TimeInterval.parse(inscriptionSplit[0],	constants);

				assert (sourcePlace != null);
				assert (trans != null);
				assert (destPlace != null);
				int groupNr = 1;
                if(inscriptionSplit.length < 2){
                    for (Object pt : tempArc.getTarget().getPostset()) {
                        if (pt instanceof TimedTransportArcComponent) {
                            if (((TimedTransportArcComponent) pt).getGroupNr() > groupNr) {
                                groupNr = ((TimedTransportArcComponent) pt).getGroupNr();
                            }
                        }
                    }
                } else {
                    groupNr = Integer.parseInt(inscriptionSplit[1]);
                }
                tempArc.setGroupNr(groupNr);
                postsetTransportArc.setGroupNr(groupNr);

				TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, timeInterval, weight,expr,transportExpr);
				transArc.setColorTimeIntervals(ctiList);

				tempArc.setUnderlyingArc(transArc);
				postsetTransportArc.setUnderlyingArc(transArc);
				template.guiModel().addPetriNetObject(tempArc);
				template.guiModel().addPetriNetObject(postsetTransportArc);
				template.model().add(transArc);

				postsetArcs.remove(targetIn);
			} else {
                transportExpr = expr;
                presetArcs.put((TimedTransitionComponent) targetIn,	tempArc);
				transportArcsTimeIntervals.put(tempArc, TimeInterval.parse(inscriptionSplit[0], constants));
                coloredTransportArcsTimeIntervals.put(tempArc, ctiList);

            }
		} else {
			if (presetArcs.containsKey(sourceIn)) {
				TimedTransportArcComponent presetTransportArc = presetArcs.get(sourceIn);
				TimedPlace sourcePlace = template.model().getPlaceByName(presetTransportArc.getSource().getName());
				TimedTransition trans = template.model().getTransitionByName(sourceIn.getName());
				TimedPlace destPlace = template.model().getPlaceByName(targetIn.getName());
				TimeInterval interval = transportArcsTimeIntervals.get(presetTransportArc);
                List<ColoredTimeInterval> timeIntervals = coloredTransportArcsTimeIntervals.get(presetTransportArc);

                assert (sourcePlace != null);
				assert (trans != null);
				assert (destPlace != null);
                int groupNr = 1;
                if(inscriptionSplit.length < 2){
                    for (Object pt : tempArc.getSource().getPostset()) {
                        if (pt instanceof TimedTransportArcComponent) {
                            if (((TimedTransportArcComponent) pt).getGroupNr() > groupNr) {
                                groupNr = ((TimedTransportArcComponent) pt).getGroupNr();
                            }
                        }
                    }
                } else {
                    groupNr = Integer.parseInt(inscriptionSplit[1]);
                }
                tempArc.setGroupNr(groupNr);
                presetTransportArc.setGroupNr(groupNr);

				TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, interval, weight,transportExpr,expr);
				transArc.setColorTimeIntervals(timeIntervals);
                transportExpr = null;

				tempArc.setUnderlyingArc(transArc);
				presetTransportArc.setUnderlyingArc(transArc);
				template.guiModel().addPetriNetObject(presetTransportArc);
				template.guiModel().addPetriNetObject(tempArc);
				template.model().add(transArc);

				presetArcs.remove(sourceIn);
				transportArcsTimeIntervals.remove(presetTransportArc);
			} else {
				postsetArcs.put((TimedTransitionComponent) sourceIn, tempArc);
				transportExpr = expr;
			}
		}
		return tempArc;
	}

	private Arc parseAndAddTimedInputArc(String idInput, boolean taggedArc,
                                         String inscriptionTempStorage, PlaceTransitionObject sourceIn,
                                         PlaceTransitionObject targetIn,
                                         int _endx, int _endy, Template template, ConstantStore constants, Weight weight,List<ColoredTimeInterval> ctiList, ArcExpression expr ) throws FormatException {
        TimedInputArcComponent tempArc = new TimedInputArcComponent(new TimedOutputArcComponent(sourceIn, targetIn, 1, idInput), lens);




		TimedPlace place = template.model().getPlaceByName(sourceIn.getName());
		TimedTransition transition = template.model().getTransitionByName(targetIn.getName());

		TimeInterval timeInterval = TimeInterval.parse(inscriptionTempStorage, constants);
        //ctiList.add(ColoredTimeInterval.parse(inscriptionTempStorage, constants, new Vector<Color>(){{add(Color.STAR_COLOR);}}));


        TimedInputArc inputArc = new TimedInputArc(place, transition, timeInterval, weight, expr);
        inputArc.setColorTimeIntervals(ctiList);
		tempArc.setUnderlyingArc(inputArc);

		if(template.model().hasArcFromPlaceToTransition(inputArc.source(), inputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}

		template.guiModel().addPetriNetObject(tempArc);
		template.model().add(inputArc);

		return tempArc;
	}

	private Arc parseAndAddTimedInhibitorArc(String idInput, boolean taggedArc,
                                             String inscriptionTempStorage, PlaceTransitionObject sourceIn,
                                             PlaceTransitionObject targetIn,
                                             int _endx, int _endy, Template template, ConstantStore constants, Weight weight, ArcExpression arcExpr) {
		TimedPlace place = template.model().getPlaceByName(sourceIn.getName());
		TimedTransition transition = template.model().getTransitionByName(targetIn.getName());
		TimeInterval interval = TimeInterval.parse(inscriptionTempStorage, constants);
		
		if(!interval.equals(TimeInterval.ZERO_INF) && firstInhibitorIntervalWarning) {
            messages.add("The chosen model contained inhibitor arcs with unsupported intervals.\n\nTAPAAL only supports inhibitor arcs with intervals [0,inf).\n\nAny other interval on inhibitor arcs will be replaced with [0,inf).");
			firstInhibitorIntervalWarning = false;
		}

		TimedInhibitorArc inhibArc = new TimedInhibitorArc(place, transition, interval, weight, arcExpr);
        TimedInhibitorArcComponent tempArc = new TimedInhibitorArcComponent((TimedPlaceComponent)sourceIn, (TimedTransitionComponent)targetIn, inhibArc);
		tempArc.setUnderlyingArc(inhibArc);
		template.guiModel().addPetriNetObject(tempArc);
		template.model().add(inhibArc);

		return tempArc;
	}

	private void parseArcPath(Element arc, Arc tempArc) {
		NodeList nodelist = arc.getElementsByTagName("arcpath");
		if (nodelist.getLength() > 0) {
			tempArc.getArcPath().purgePathPoints();
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);
				if (node instanceof Element) {
					Element element = (Element) node;
					if ("arcpath".equals(element.getNodeName())) {
						String arcTempX = element.getAttribute("xCoord");
						String arcTempY = element.getAttribute("yCoord");

						// Wierd naming convention in pipe: this represents if
						// the arc point is a curve point or not
						String arcTempType = element.getAttribute("arcPointType");
						double arcPointX = Double.parseDouble(arcTempX);
						double arcPointY = Double.parseDouble(arcTempY);
						arcPointX += Constants.ARC_CONTROL_POINT_CONSTANT + 1;
						arcPointY += Constants.ARC_CONTROL_POINT_CONSTANT + 1;
						boolean arcPointType = Boolean.parseBoolean(arcTempType);
						tempArc.getArcPath().addPoint(arcPointX, arcPointY,	arcPointType);
					}
				}
			}
		}
	}

	private Constant parseConstant(Element constantElement) {
		String name = constantElement.getAttribute("name");
		int value = Integer.parseInt(constantElement.getAttribute("value"));

		return new Constant(name, value);
	}

    private Pair<String, Vector<Color>> parseColorInvariant(Element colorinvariant, TimedArcPetriNetNetwork network)  {
        String inscription, colorTypeName;
        Vector<Color> colors = new Vector<Color>();
        Element colorTypeELe = (Element) colorinvariant.getElementsByTagName("colortype").item(0);
        inscription = colorinvariant.getElementsByTagName("inscription").item(0).getAttributes().getNamedItem("inscription").getNodeValue();
        colorTypeName = colorTypeELe.getAttributes().getNamedItem("name").getNodeValue();
        if (network.isNameUsedForColorType(colorTypeName)) {
            NodeList colorNodeList = colorTypeELe.getElementsByTagName("color");
            String colorName;
            ColorType ct = network.getColorTypeByName(colorTypeName);
            for (int i = 0; i < colorNodeList.getLength(); i++) {
                colorName = colorNodeList.item(i).getAttributes().getNamedItem("value").getNodeValue();
                colors.add(new Color(ct, 0, colorName));
            }
        } else {
            try {
                throw new FormatException("The color type used for an invariant does not exist");
            } catch (FormatException e) {
                e.printStackTrace();
            }
        }

        Pair<String, Vector<Color>> pair = new Pair<String, Vector<Color>>(inscription, colors);
        return pair;
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
