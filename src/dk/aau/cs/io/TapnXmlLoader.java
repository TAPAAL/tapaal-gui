package dk.aau.cs.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.TabContent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
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
import dk.aau.cs.model.tapn.TimedMarking;
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

	private final NameGenerator nameGenerator = new NameGenerator();
	private boolean firstInhibitorIntervalWarning = true;
	private boolean firstPlaceRenameWarning = true;
	private final IdResolver idResolver = new IdResolver();
    private final Collection<String> messages = new ArrayList<>(10);

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

        parseFeature(doc);
		
		ConstantStore constants = new ConstantStore(parseConstants(doc));

		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork(constants);

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

    private void parseFeature(Document doc) {
        if (doc.getElementsByTagName("feature").getLength() > 0) {
	        NodeList nodeList = doc.getElementsByTagName("feature");

	        hasFeatureTag = true;

            var isTimed = Boolean.parseBoolean(nodeList.item(0).getAttributes().getNamedItem("isTimed").getNodeValue());
            var isGame = Boolean.parseBoolean(nodeList.item(0).getAttributes().getNamedItem("isGame").getNodeValue());

            lens = new TabContent.TAPNLens(isTimed, isGame);
        }
    }

	private void parseSharedPlaces(Document doc, TimedArcPetriNetNetwork network, ConstantStore constants) {
		NodeList sharedPlaceNodes = doc.getElementsByTagName("shared-place");

		for(int i = 0; i < sharedPlaceNodes.getLength(); i++){
			Node node = sharedPlaceNodes.item(i);

			if(node instanceof Element){
				SharedPlace place = parseSharedPlace((Element)node, network.marking(), constants);
				network.add(place);
			}
		}
	}

	private SharedPlace parseSharedPlace(Element element, TimedMarking marking, ConstantStore constants) {
		String name = element.getAttribute("name");
		TimeInvariant invariant = TimeInvariant.parse(element.getAttribute("invariant"), constants);
		int numberOfTokens = Integer.parseInt(element.getAttribute("initialMarking"));

		if(name.toLowerCase().equals("true") || name.toLowerCase().equals("false")) {
			name = "_" + name;
			if(firstPlaceRenameWarning) {
				messages.add(PLACENAME_ERROR_MESSAGE);
				firstPlaceRenameWarning = false;
			}
		}
		
		SharedPlace place = new SharedPlace(name, invariant);
        place.setCurrentMarking(marking);
		place.addTokens(numberOfTokens);


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
		} else if ("arc".equals(element.getNodeName())) {
            parseAndAddArc(element, template, constants);
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
		int positionXInput = (int)Double.parseDouble(transition.getAttribute("positionX"));
		int positionYInput = (int)Double.parseDouble(transition.getAttribute("positionY"));
		String idInput = transition.getAttribute("id");
		String nameInput = transition.getAttribute("name");
		boolean isUrgent = Boolean.parseBoolean(transition.getAttribute("urgent"));

		String player = transition.getAttribute("player");

		idResolver.add(tapn.name(), idInput, nameInput);
		
		int nameOffsetXInput = (int)Double.parseDouble(transition.getAttribute("nameOffsetX"));
		int nameOffsetYInput = (int)Double.parseDouble(transition.getAttribute("nameOffsetY"));
		boolean infiniteServer = transition.getAttribute("infiniteServer").equals("true");
		int angle = Integer.parseInt(transition.getAttribute("angle"));
		int priority = Integer.parseInt(transition.getAttribute("priority"));
		boolean displayName = transition.getAttribute("displayName").equals("false") ? false : true;


		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}

		
		TimedTransition t = new TimedTransition(nameInput);
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
		int positionXInput = (int)Double.parseDouble(place.getAttribute("positionX"));
		int positionYInput = (int)Double.parseDouble(place.getAttribute("positionY"));
		String idInput = place.getAttribute("id");
		String nameInput = place.getAttribute("name");
		int nameOffsetXInput = (int)Double.parseDouble(place.getAttribute("nameOffsetX"));
		int nameOffsetYInput = (int)Double.parseDouble(place.getAttribute("nameOffsetY"));
		int initialMarkingInput = Integer.parseInt(place.getAttribute("initialMarking"));
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
			p = new LocalTimedPlace(nameInput, TimeInvariant.parse(invariant, constants));
			tapn.add(p);
			for (int i = 0; i < initialMarkingInput; i++) {
				network.marking().add(new TimedToken(p));
			}
		}
		nameGenerator.updateIndicesForAllModels(nameInput);
		TimedPlaceComponent placeComponent = new TimedPlaceComponent(positionXInput, positionYInput, idInput, nameOffsetXInput, nameOffsetYInput, lens);
		placeComponent.setUnderlyingPlace(p);

		if (!displayName){
			placeComponent.setAttributesVisible(false);
		}

		return placeComponent;
	}

	private void parseAndAddArc(Element arc, Template template, ConstantStore constants) throws FormatException {
		String idInput = arc.getAttribute("id");
		String sourceInput = arc.getAttribute("source");
		String targetInput = arc.getAttribute("target");
		boolean taggedArc = arc.getAttribute("tagged").equals("true") ? true : false;
		String inscriptionTempStorage = arc.getAttribute("inscription");
		String type = arc.getAttribute("type");
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

		Arc tempArc;

		if (type.equals("tapnInhibitor")) {

			tempArc = parseAndAddTimedInhibitorArc(idInput, taggedArc,
					inscriptionTempStorage, sourceIn, targetIn,
                _endx, _endy,template, constants, weight);

		} else {
			if (type.equals("timed")) {
				tempArc = parseAndAddTimedInputArc(idInput, taggedArc,
						inscriptionTempStorage, sourceIn, targetIn,
                    _endx, _endy, template, constants, weight);

			} else if (type.equals("transport")) {
				tempArc = parseAndAddTransportArc(idInput, taggedArc,
						inscriptionTempStorage, sourceIn, targetIn,
                    _endx, _endy, template, constants, weight);

			} else {
				tempArc = parseAndAddTimedOutputArc(idInput, taggedArc,
						inscriptionTempStorage, sourceIn, targetIn,
                    _endx, _endy, template, weight);
			}

		}
		tempArc.setNameOffsetX(nameOffsetXInput);
		tempArc.setNameOffsetY(nameOffsetYInput);

		parseArcPath(arc, tempArc);
	}

	private TimedOutputArcComponent parseAndAddTimedOutputArc(String idInput, boolean taggedArc,
                                                              String inscriptionTempStorage, PlaceTransitionObject sourceIn,
                                                              PlaceTransitionObject targetIn,
                                                              int _endx, int _endy, Template template, Weight weight) throws FormatException {

		TimedOutputArcComponent tempArc = new TimedOutputArcComponent(sourceIn, targetIn,	(!inscriptionTempStorage.equals("") ? Integer.parseInt(inscriptionTempStorage) : 1), idInput);

		TimedPlace place = template.model().getPlaceByName(targetIn.getName());
		TimedTransition transition = template.model().getTransitionByName(sourceIn.getName());

		TimedOutputArc outputArc = new TimedOutputArc(transition, place, weight);
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
                                                               int _endx, int _endy, Template template, ConstantStore constants, Weight weight) {

		
		String[] inscriptionSplit = {};
		if (inscriptionTempStorage.contains(":")) {
			inscriptionSplit = inscriptionTempStorage.split(":");
		}
		boolean isInPreSet = false;
		if (sourceIn instanceof Place) {
			isInPreSet = true;
		}
		TimedTransportArcComponent tempArc = new TimedTransportArcComponent(new TimedInputArcComponent(new TimedOutputArcComponent(sourceIn, targetIn, 1, idInput)), Integer.parseInt(inscriptionSplit[1]), isInPreSet);


		if (isInPreSet) {
			if (postsetArcs.containsKey(targetIn)) {
				TimedTransportArcComponent postsetTransportArc = postsetArcs.get(targetIn);
				TimedPlace sourcePlace = template.model().getPlaceByName(sourceIn.getName());
				TimedTransition trans = template.model().getTransitionByName(targetIn.getName());
				TimedPlace destPlace = template.model().getPlaceByName(postsetTransportArc.getTarget().getName());
				TimeInterval interval = TimeInterval.parse(inscriptionSplit[0],	constants);

				assert (sourcePlace != null);
				assert (trans != null);
				assert (destPlace != null);

				TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, interval, weight);

				tempArc.setUnderlyingArc(transArc);
				postsetTransportArc.setUnderlyingArc(transArc);
				template.guiModel().addPetriNetObject(tempArc);
				template.guiModel().addPetriNetObject(postsetTransportArc);
				template.model().add(transArc);

				postsetArcs.remove(targetIn);
			} else {
				presetArcs.put((TimedTransitionComponent) targetIn,	tempArc);
				transportArcsTimeIntervals.put(tempArc, TimeInterval.parse(inscriptionSplit[0], constants));
			}
		} else {
			if (presetArcs.containsKey(sourceIn)) {
				TimedTransportArcComponent presetTransportArc = presetArcs.get(sourceIn);
				TimedPlace sourcePlace = template.model().getPlaceByName(presetTransportArc.getSource().getName());
				TimedTransition trans = template.model().getTransitionByName(sourceIn.getName());
				TimedPlace destPlace = template.model().getPlaceByName(targetIn.getName());
				TimeInterval interval = transportArcsTimeIntervals.get(presetTransportArc);

				assert (sourcePlace != null);
				assert (trans != null);
				assert (destPlace != null);

				TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, interval, weight);

				tempArc.setUnderlyingArc(transArc);
				presetTransportArc.setUnderlyingArc(transArc);
				template.guiModel().addPetriNetObject(presetTransportArc);
				template.guiModel().addPetriNetObject(tempArc);
				template.model().add(transArc);

				presetArcs.remove(sourceIn);
				transportArcsTimeIntervals.remove(presetTransportArc);
			} else {
				postsetArcs.put((TimedTransitionComponent) sourceIn, tempArc);
			}
		}
		return tempArc;
	}

	private Arc parseAndAddTimedInputArc(String idInput, boolean taggedArc,
                                         String inscriptionTempStorage, PlaceTransitionObject sourceIn,
                                         PlaceTransitionObject targetIn,
                                         int _endx, int _endy, Template template, ConstantStore constants, Weight weight) throws FormatException {

	    TimedInputArcComponent tempArc = new TimedInputArcComponent(new TimedOutputArcComponent(sourceIn, targetIn, 1, idInput), lens);

		TimedPlace place = template.model().getPlaceByName(sourceIn.getName());
		TimedTransition transition = template.model().getTransitionByName(targetIn.getName());
		TimeInterval interval = TimeInterval.parse(inscriptionTempStorage, constants);

		TimedInputArc inputArc = new TimedInputArc(place, transition, interval, weight);
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
                                             int _endx, int _endy, Template template, ConstantStore constants, Weight weight) {
		TimedInhibitorArcComponent tempArc = new TimedInhibitorArcComponent(
				new TimedInputArcComponent(
						new TimedOutputArcComponent(sourceIn, targetIn, 1, idInput)
				),
				(inscriptionTempStorage != null ? inscriptionTempStorage : ""));
		TimedPlace place = template.model().getPlaceByName(sourceIn.getName());
		TimedTransition transition = template.model().getTransitionByName(targetIn.getName());
		TimeInterval interval = TimeInterval.parse(inscriptionTempStorage, constants);
		
		if(!interval.equals(TimeInterval.ZERO_INF) && firstInhibitorIntervalWarning) {
            messages.add("The chosen model contained inhibitor arcs with unsupported intervals.\n\nTAPAAL only supports inhibitor arcs with intervals [0,inf).\n\nAny other interval on inhibitor arcs will be replaced with [0,inf).");
			firstInhibitorIntervalWarning = false;
		}
		
		TimedInhibitorArc inhibArc = new TimedInhibitorArc(place, transition, interval, weight);

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
						arcPointX += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
						arcPointY += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
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

}
