package dk.aau.cs.model.tapn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pipe.dataLayer.AnnotationNote;
import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Note;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.dataLayer.TimedInhibitorArcComponent;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedOutputArcComponent;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TransportArcComponent;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomable;
import pipe.gui.handler.AnimationHandler;
import pipe.gui.handler.AnnotationNoteHandler;
import pipe.gui.handler.ArcHandler;
import pipe.gui.handler.LabelHandler;
import pipe.gui.handler.PlaceHandler;
import pipe.gui.handler.TAPNTransitionHandler;
import pipe.gui.handler.TimedArcHandler;
import pipe.gui.handler.TransitionHandler;
import pipe.gui.handler.TransportArcHandler;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;

public class TapnXmlLoader {
	private HashMap<TimedTransitionComponent, TransportArcComponent> presetArcs = new HashMap<TimedTransitionComponent, TransportArcComponent>();;
	private HashMap<TimedTransitionComponent, TransportArcComponent> postsetArcs = new HashMap<TimedTransitionComponent, TransportArcComponent>();
	private HashMap<TransportArcComponent, TimeInterval> transportArcsTimeIntervals = new HashMap<TransportArcComponent, TimeInterval>();

	private DrawingSurfaceImpl drawingSurface; // TODO: delete me
	private NameGenerator nameGenerator = new NameGenerator();

	public TapnXmlLoader(DrawingSurfaceImpl drawingSurface) {
		this.drawingSurface = drawingSurface;
	}

	public LoadedModel load(File file) {
		Require.that(file != null && file.exists(), "file must be non-null and exist");

		Document doc = loadDocument(file);
		if(doc == null) return null;
		return parse(doc);
	}

	private Document loadDocument(File file) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(file);
		} catch (ParserConfigurationException e) {
			return null;
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	private LoadedModel parse(Document doc) {
		Map<String, Constant> constants = parseConstants(doc);

		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork(new ConstantStore(constants.values()));

		parseSharedPlaces(doc, network, constants);
		parseSharedTransitions(doc, network);
		
		Collection<Template> templates = parseTemplates(doc, network, constants);
		Collection<TAPNQuery> queries = parseQueries(doc);

		network.buildConstraints();
		return new LoadedModel(network, templates, queries);
	}

	private void parseSharedPlaces(Document doc, TimedArcPetriNetNetwork network, Map<String, Constant> constants) {
		NodeList sharedPlaceNodes = doc.getElementsByTagName("shared-place");

		for(int i = 0; i < sharedPlaceNodes.getLength(); i++){
			Node node = sharedPlaceNodes.item(i);

			if(node instanceof Element){
				SharedPlace place = parseSharedPlace((Element)node, network.marking(), constants);
				network.add(place);
			}
		}
	}

	private SharedPlace parseSharedPlace(Element element, TimedMarking marking, Map<String, Constant> constants) {
		String name = element.getAttribute("name");
		TimeInvariant invariant = TimeInvariant.parse(element.getAttribute("invariant"), constants);
		int numberOfTokens = Integer.parseInt(element.getAttribute("initialMarking"));

		SharedPlace place = new SharedPlace(name, invariant);
		place.setCurrentMarking(marking);
		for(int j = 0; j < numberOfTokens; j++){
			marking.add(new TimedToken(place));
		}
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
		
		return new SharedTransition(name);
	}

	private Collection<TAPNQuery> parseQueries(Document doc) {
		Collection<TAPNQuery> queries = new ArrayList<TAPNQuery>();
		NodeList queryNodes = doc.getElementsByTagName("query");
		for (int i = 0; i < queryNodes.getLength(); i++) {
			Node q = queryNodes.item(i);

			if (q instanceof Element) {
				TAPNQuery query = parseTAPNQuery((Element) q);

				if (query != null)
					queries.add(query);
			}
		}
		return queries;
	}

	private Collection<Template> parseTemplates(Document doc, TimedArcPetriNetNetwork network, Map<String, Constant> constants) {
		Collection<Template> templates = new ArrayList<Template>();
		NodeList nets = doc.getElementsByTagName("net");
		for (int i = 0; i < nets.getLength(); i++) {
			Template template = parseTimedArcPetriNet(nets.item(i), network, constants);
			templates.add(template);
			network.add(template.model());
		}
		return templates;
	}

	private Map<String, Constant> parseConstants(Document doc) {
		TreeMap<String, Constant> constants = new TreeMap<String, Constant>();
		NodeList constantNodes = doc.getElementsByTagName("constant");
		for (int i = 0; i < constantNodes.getLength(); i++) {
			Node c = constantNodes.item(i);

			if (c instanceof Element) {
				Constant constant = parseAndAddConstant((Element) c);
				constants.put(constant.name(), constant);
			}
		}
		return constants;
	}

	private Template parseTimedArcPetriNet(Node tapnNode, TimedArcPetriNetNetwork network, Map<String, Constant> constants) {
		String name = getTAPNName(tapnNode);


		TimedArcPetriNet tapn = new TimedArcPetriNet(name);
		tapn.setMarking(network.marking());

		DataLayer guiModel = new DataLayer();
		Template template = new Template(tapn, guiModel);

		NodeList nodeList = tapnNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if(node instanceof Element){
				parseElement((Element)node, template, network, constants);
			}
		}


		return template;
	}

	private void parseElement(Element element, Template template, TimedArcPetriNetNetwork network, Map<String, Constant> constants) {
		if ("labels".equals(element.getNodeName())) {
			AnnotationNote note = parseAnnotation(element);
			template.guiModel().addPetriNetObject(note);
			addListeners(note, template);
		} else if ("place".equals(element.getNodeName())) {
			TimedPlaceComponent place = parsePlace(element, network, constants);
			template.guiModel().addPetriNetObject(place);
			template.model().add(place.underlyingPlace());
			addListeners(place, template);
		} else if ("transition".equals(element.getNodeName())) {
			TimedTransitionComponent transition = parseTransition(element, network);
			template.guiModel().addPetriNetObject(transition);
			template.model().add(transition.underlyingTransition());
			addListeners(transition, template);
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
			positionXInput = Integer.valueOf(positionXTempStorage).intValue() + 1;
		}

		if (positionYTempStorage.length() > 0) {
			positionYInput = Integer.valueOf(positionYTempStorage).intValue() + 1;
		}

		if (widthTemp.length() > 0) {
			widthInput = Integer.valueOf(widthTemp).intValue() + 1;
		}

		if (heightTemp.length() > 0) {
			heightInput = Integer.valueOf(heightTemp).intValue() + 1;
		}

		if (borderTemp.length() > 0) {
			borderInput = Boolean.valueOf(borderTemp).booleanValue();
		} else {
			borderInput = true;
		}
		AnnotationNote an = new AnnotationNote(text, positionXInput,
				positionYInput, widthInput, heightInput, borderInput);
		return an;
	}

	private TimedTransitionComponent parseTransition(Element transition, TimedArcPetriNetNetwork network) {
		double positionXInput = Double.parseDouble(transition.getAttribute("positionX"));
		double positionYInput = Double.parseDouble(transition.getAttribute("positionY"));
		String idInput = transition.getAttribute("id");
		String nameInput = transition.getAttribute("name");
		double nameOffsetXInput = Double.parseDouble(transition.getAttribute("nameOffsetX"));
		double nameOffsetYInput = Double.parseDouble(transition.getAttribute("nameOffsetY"));
		boolean infiniteServer = transition.getAttribute("infiniteServer").equals("true") ? true : false;
		int angle = Integer.parseInt(transition.getAttribute("angle"));
		int priority = Integer.parseInt(transition.getAttribute("priority"));

		positionXInput = Grid.getModifiedX(positionXInput);
		positionYInput = Grid.getModifiedY(positionYInput);

		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}

		
		TimedTransition t = new TimedTransition(nameInput);
		if(network.isNameUsedForShared(nameInput)){
			network.getSharedTransitionByName(nameInput).makeShared(t);
		}
		TimedTransitionComponent transitionComponent = new TimedTransitionComponent(
				positionXInput, positionYInput, idInput, nameInput,
				nameOffsetXInput, nameOffsetYInput, true,
				infiniteServer, angle, priority);
		transitionComponent.setUnderlyingTransition(t);
		return transitionComponent;
	}

	private TimedPlaceComponent parsePlace(Element place, TimedArcPetriNetNetwork network, Map<String, Constant> constants) {
		double positionXInput = Double.parseDouble(place.getAttribute("positionX"));
		double positionYInput = Double.parseDouble(place.getAttribute("positionY"));
		String idInput = place.getAttribute("id");
		String nameInput = place.getAttribute("name");
		double nameOffsetXInput = Double.parseDouble(place.getAttribute("nameOffsetX"));
		double nameOffsetYInput = Double.parseDouble(place.getAttribute("nameOffsetY"));
		int initialMarkingInput = Integer.parseInt(place.getAttribute("initialMarking"));
		double markingOffsetXInput = Double.parseDouble(place.getAttribute("markingOffsetX"));
		double markingOffsetYInput = Double.parseDouble(place.getAttribute("markingOffsetY"));
		String invariant = place.getAttribute("invariant");

		positionXInput = Grid.getModifiedX(positionXInput);
		positionYInput = Grid.getModifiedY(positionYInput);

		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}
		//if(usedNames.contains(nameInput)) throw new RuntimeException("Cannot contain multiple objects with the same name");

		TimedPlace p;
		if(network.isNameUsedForShared(nameInput)){
			p = network.getSharedPlaceByName(nameInput);
		}else{
			p = new LocalTimedPlace(nameInput, TimeInvariant.parse(invariant, constants));
			for (int i = 0; i < initialMarkingInput; i++) {
				network.marking().add(new TimedToken(p));
			}
		}

		TimedPlaceComponent placeComponent = new TimedPlaceComponent(positionXInput, positionYInput, idInput, nameInput, nameOffsetXInput, nameOffsetYInput, initialMarkingInput, markingOffsetXInput, markingOffsetYInput, 0);
		placeComponent.setUnderlyingPlace(p);

		return placeComponent;
	}

	private void parseAndAddArc(Element arc, Template template, Map<String, Constant> constants) {
		String idInput = arc.getAttribute("id");
		String sourceInput = arc.getAttribute("source");
		String targetInput = arc.getAttribute("target");
		boolean taggedArc = arc.getAttribute("tagged").equals("true") ? true : false;
		String inscriptionTempStorage = arc.getAttribute("inscription");
		String type = arc.getAttribute("type");

		PlaceTransitionObject sourceIn = template.guiModel().getPlaceTransitionObject(sourceInput);
		PlaceTransitionObject targetIn = template.guiModel().getPlaceTransitionObject(targetInput);

		// add the insets and offset
		int _startx = sourceIn.getX() + sourceIn.centreOffsetLeft();
		int _starty = sourceIn.getY() + sourceIn.centreOffsetTop();

		int _endx = targetIn.getX() + targetIn.centreOffsetLeft();
		int _endy = targetIn.getY() + targetIn.centreOffsetTop();

		Arc tempArc;

		if (type.equals("tapnInhibitor")) {

			tempArc = parseAndAddTimedInhibitorArc(idInput, taggedArc,
					inscriptionTempStorage, sourceIn, targetIn, _startx,
					_starty, _endx, _endy,template, constants);

		} else {
			if (type.equals("timed")) {
				tempArc = parseAndAddTimedInputArc(idInput, taggedArc,
						inscriptionTempStorage, sourceIn, targetIn, _startx,
						_starty, _endx, _endy, template, constants);

			} else if (type.equals("transport")) {
				tempArc = parseAndAddTransportArc(idInput, taggedArc,
						inscriptionTempStorage, sourceIn, targetIn, _startx,
						_starty, _endx, _endy, template, constants);

			} else {
				tempArc = parseAndAddTimedOutputArc(idInput, taggedArc,
						inscriptionTempStorage, sourceIn, targetIn, _startx,
						_starty, _endx, _endy, template);
			}

		}

		parseArcPath(arc, tempArc);
	}

	private TimedOutputArcComponent parseAndAddTimedOutputArc(String idInput, boolean taggedArc,
			String inscriptionTempStorage, PlaceTransitionObject sourceIn,
			PlaceTransitionObject targetIn, double _startx, double _starty,
			double _endx, double _endy, Template template) {

		TimedOutputArcComponent tempArc = new TimedOutputArcComponent(_startx, _starty, _endx, _endy, 
				sourceIn, targetIn,	Integer.valueOf(inscriptionTempStorage), idInput, taggedArc);

		TimedPlace place = template.model().getPlaceByName(targetIn.getName());
		TimedTransition transition = template.model().getTransitionByName(sourceIn.getName());

		TimedOutputArc outputArc = new TimedOutputArc(transition, place);
		tempArc.setUnderlyingArc(outputArc);

		if(template.model().hasArcFromTransitionToPlace(outputArc.source(),outputArc.destination())) {
			throw new RuntimeException("Error while loading model:\n - Multiple arcs between a place and a transition is not allowed");
		}

		template.guiModel().addPetriNetObject(tempArc);
		addListeners(tempArc, template);
		template.model().add(outputArc);

		sourceIn.addConnectFrom(tempArc);
		targetIn.addConnectTo(tempArc);
		return tempArc;
	}

	private TransportArcComponent parseAndAddTransportArc(String idInput, boolean taggedArc,
			String inscriptionTempStorage, PlaceTransitionObject sourceIn,
			PlaceTransitionObject targetIn, double _startx, double _starty,
			double _endx, double _endy, Template template, Map<String, Constant> constants) {

		
		String[] inscriptionSplit = {};
		if (inscriptionTempStorage.contains(":")) {
			inscriptionSplit = inscriptionTempStorage.split(":");
		}
		boolean isInPreSet = false;
		if (sourceIn instanceof Place) {
			isInPreSet = true;
		}
		TransportArcComponent tempArc = new TransportArcComponent(new TimedInputArcComponent(
				new TimedOutputArcComponent(_startx, _starty, _endx, _endy,	sourceIn, targetIn, 1, idInput, taggedArc),
				inscriptionSplit[0]), Integer.parseInt(inscriptionSplit[1]), isInPreSet);

		sourceIn.addConnectFrom(tempArc);
		targetIn.addConnectTo(tempArc);

		if (isInPreSet) {
			if (postsetArcs.containsKey((TimedTransitionComponent) targetIn)) {
				TransportArcComponent postsetTransportArc = postsetArcs.get((TimedTransitionComponent) targetIn);
				TimedPlace sourcePlace = template.model().getPlaceByName(sourceIn.getName());
				TimedTransition trans = template.model().getTransitionByName(targetIn.getName());
				TimedPlace destPlace = template.model().getPlaceByName(postsetTransportArc.getTarget().getName());
				TimeInterval interval = TimeInterval.parse(inscriptionSplit[0],	constants);

				assert (sourcePlace != null);
				assert (trans != null);
				assert (destPlace != null);

				TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, interval);

				tempArc.setUnderlyingArc(transArc);
				postsetTransportArc.setUnderlyingArc(transArc);
				template.guiModel().addPetriNetObject(tempArc);
				addListeners(tempArc, template);
				template.guiModel().addPetriNetObject(postsetTransportArc);
				addListeners(postsetTransportArc, template);
				template.model().add(transArc);

				postsetArcs.remove((TimedTransitionComponent) targetIn);
			} else {
				presetArcs.put((TimedTransitionComponent) targetIn,	(TransportArcComponent) tempArc);
				transportArcsTimeIntervals.put((TransportArcComponent) tempArc, TimeInterval.parse(inscriptionSplit[0], constants));
			}
		} else {
			if (presetArcs.containsKey((TimedTransitionComponent) sourceIn)) {
				TransportArcComponent presetTransportArc = presetArcs.get((TimedTransitionComponent) sourceIn);
				TimedPlace sourcePlace = template.model().getPlaceByName(presetTransportArc.getSource().getName());
				TimedTransition trans = template.model().getTransitionByName(sourceIn.getName());
				TimedPlace destPlace = template.model().getPlaceByName(targetIn.getName());
				TimeInterval interval = transportArcsTimeIntervals.get((TransportArcComponent) presetTransportArc);

				assert (sourcePlace != null);
				assert (trans != null);
				assert (destPlace != null);

				TransportArc transArc = new TransportArc(sourcePlace, trans,
						destPlace, interval);

				tempArc.setUnderlyingArc(transArc);
				presetTransportArc.setUnderlyingArc(transArc);
				template.guiModel().addPetriNetObject(presetTransportArc);
				addListeners(presetTransportArc, template);
				template.guiModel().addPetriNetObject(tempArc);
				addListeners(tempArc, template);
				template.model().add(transArc);

				presetArcs.remove((TimedTransitionComponent) sourceIn);
				transportArcsTimeIntervals.remove((TransportArcComponent) presetTransportArc);
			} else {
				postsetArcs.put((TimedTransitionComponent) sourceIn, (TransportArcComponent) tempArc);
			}
		}
		return tempArc;
	}

	private Arc parseAndAddTimedInputArc(String idInput, boolean taggedArc,
			String inscriptionTempStorage, PlaceTransitionObject sourceIn,
			PlaceTransitionObject targetIn, double _startx, double _starty,
			double _endx, double _endy, Template template, Map<String, Constant> constants) {
		Arc tempArc;
		tempArc = new TimedInputArcComponent(new TimedOutputArcComponent(
				_startx, _starty, _endx, _endy, sourceIn, targetIn, 1, idInput,
				taggedArc),
				(inscriptionTempStorage != null ? inscriptionTempStorage : ""));

		TimedPlace place = template.model().getPlaceByName(sourceIn.getName());
		TimedTransition transition = template.model().getTransitionByName(targetIn.getName());
		TimeInterval interval = TimeInterval.parse(inscriptionTempStorage, constants);

		TimedInputArc inputArc = new TimedInputArc(place, transition, interval);
		((TimedInputArcComponent) tempArc).setUnderlyingArc(inputArc);

		if(template.model().hasArcFromPlaceToTransition(inputArc.source(), inputArc.destination())) {
			throw new RuntimeException("Error while loading model:\n - Multiple arcs between a place and a transition is not allowed");
		}

		template.guiModel().addPetriNetObject(tempArc);
		addListeners(tempArc, template);
		template.model().add(inputArc);

		sourceIn.addConnectFrom(tempArc);
		targetIn.addConnectTo(tempArc);
		return tempArc;
	}

	private Arc parseAndAddTimedInhibitorArc(String idInput, boolean taggedArc,
			String inscriptionTempStorage, PlaceTransitionObject sourceIn,
			PlaceTransitionObject targetIn, double _startx, double _starty,
			double _endx, double _endy, Template template, Map<String, Constant> constants) {
		TimedInhibitorArcComponent tempArc = new TimedInhibitorArcComponent(
				new TimedInputArcComponent(
						new TimedOutputArcComponent(_startx, _starty, _endx, _endy,	sourceIn, targetIn, 1, idInput, taggedArc)
				),
				(inscriptionTempStorage != null ? inscriptionTempStorage : ""));
		TimedPlace place = template.model().getPlaceByName(sourceIn.getName());
		TimedTransition transition = template.model().getTransitionByName(targetIn.getName());
		TimeInterval interval = TimeInterval.parse(inscriptionTempStorage, constants);
		TimedInhibitorArc inhibArc = new TimedInhibitorArc(place, transition, interval);

		tempArc.setUnderlyingArc(inhibArc);
		template.guiModel().addPetriNetObject(tempArc);
		addListeners(tempArc, template);
		template.model().add(inhibArc);

		sourceIn.addConnectFrom(tempArc);
		targetIn.addConnectTo(tempArc);
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
						float arcPointX = Float.valueOf(arcTempX).floatValue();
						float arcPointY = Float.valueOf(arcTempY).floatValue();
						arcPointX += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
						arcPointY += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
						boolean arcPointType = Boolean.valueOf(arcTempType).booleanValue();
						tempArc.getArcPath().addPoint(arcPointX, arcPointY,	arcPointType);
					}
				}
			}
		}
	}

	private TAPNQuery parseTAPNQuery(Element queryElement) {
		String comment = getQueryComment(queryElement);
		TraceOption traceOption = getQueryTraceOption(queryElement);
		SearchOption searchOption = getQuerySearchOption(queryElement);
		HashTableSize hashTableSize = getQueryHashTableSize(queryElement);
		ExtrapolationOption extrapolationOption = getQueryExtrapolationOption(queryElement);
		ReductionOption reductionOption = getQueryReductionOption(queryElement);
		int capacity = Integer.parseInt(queryElement.getAttribute("capacity"));

		TCTLAbstractProperty query;
		query = parseQueryProperty(queryElement.getAttribute("query"));

		if (query != null)
			return new TAPNQuery(comment, capacity, query, traceOption,
					searchOption, reductionOption, hashTableSize,
					extrapolationOption);
		else
			return null;
	}

	private TCTLAbstractProperty parseQueryProperty(String queryToParse) {
		TCTLAbstractProperty query = null;
		TAPAALQueryParser queryParser = new TAPAALQueryParser();

		try {
			query = queryParser.parse(queryToParse);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
					CreateGui.getApp(),
					"TAPAAL encountered an error trying to parse one of the queries in the model.\n\nThe queries that could not be parsed will not show up in the query list.",
					"Error Parsing Query", JOptionPane.ERROR_MESSAGE);
			System.err.println("No query was specified: " + e.getStackTrace());
		}
		return query;
	}

	private ReductionOption getQueryReductionOption(Element queryElement) {
		ReductionOption reductionOption;
		try {
			reductionOption = ReductionOption.valueOf(queryElement.getAttribute("reductionOption"));
		} catch (Exception e) {
			reductionOption = ReductionOption.STANDARD;
		}
		return reductionOption;
	}

	private ExtrapolationOption getQueryExtrapolationOption(Element queryElement) {
		ExtrapolationOption extrapolationOption;
		try {
			extrapolationOption = ExtrapolationOption.valueOf(queryElement.getAttribute("extrapolationOption"));
		} catch (Exception e) {
			extrapolationOption = ExtrapolationOption.AUTOMATIC;
		}
		return extrapolationOption;
	}

	private HashTableSize getQueryHashTableSize(Element queryElement) {
		HashTableSize hashTableSize;
		try {
			hashTableSize = HashTableSize.valueOf(queryElement.getAttribute("hashTableSize"));
		} catch (Exception e) {
			hashTableSize = HashTableSize.MB_16;
		}
		return hashTableSize;
	}

	private SearchOption getQuerySearchOption(Element queryElement) {
		SearchOption searchOption;
		try {
			searchOption = SearchOption.valueOf(queryElement.getAttribute("searchOption"));
		} catch (Exception e) {
			searchOption = SearchOption.BFS;
		}
		return searchOption;
	}

	private TraceOption getQueryTraceOption(Element queryElement) {
		TraceOption traceOption;
		try {
			traceOption = TraceOption.valueOf(queryElement.getAttribute("traceOption"));
		} catch (Exception e) {
			traceOption = TraceOption.NONE;
		}
		return traceOption;
	}

	private String getQueryComment(Element queryElement) {
		String comment;
		try {
			comment = queryElement.getAttribute("name");
		} catch (Exception e) {
			comment = "No comment specified";
		}
		return comment;
	}

	private Constant parseAndAddConstant(Element constantElement) {
		String name = constantElement.getAttribute("name");
		int value = Integer.parseInt(constantElement.getAttribute("value"));

		return new Constant(name, value);
	}

	private void addListeners(PetriNetObject newObject, Template template) {
		if (newObject != null) {
			if (newObject.getMouseListeners().length == 0) {
				if (newObject instanceof Place) {
					// XXX - kyrke
					if (newObject instanceof TimedPlaceComponent) {

						LabelHandler labelHandler = new LabelHandler(((Place) newObject).getNameLabel(), (Place) newObject);
						((Place) newObject).getNameLabel().addMouseListener(labelHandler);
						((Place) newObject).getNameLabel().addMouseMotionListener(labelHandler);
						((Place) newObject).getNameLabel().addMouseWheelListener(labelHandler);

						PlaceHandler placeHandler = new PlaceHandler(drawingSurface, (Place) newObject, template.guiModel(), template.model());
						newObject.addMouseListener(placeHandler);
						newObject.addMouseWheelListener(placeHandler);
						newObject.addMouseMotionListener(placeHandler);
					} else {

						LabelHandler labelHandler = new LabelHandler(((Place) newObject).getNameLabel(), (Place) newObject);
						((Place) newObject).getNameLabel().addMouseListener(labelHandler);
						((Place) newObject).getNameLabel().addMouseMotionListener(labelHandler);
						((Place) newObject).getNameLabel().addMouseWheelListener(labelHandler);

						PlaceHandler placeHandler = new PlaceHandler(drawingSurface, (Place) newObject);
						newObject.addMouseListener(placeHandler);
						newObject.addMouseWheelListener(placeHandler);
						newObject.addMouseMotionListener(placeHandler);

					}
				} else if (newObject instanceof Transition) {
					TransitionHandler transitionHandler;
					if (newObject instanceof TimedTransitionComponent) {
						transitionHandler = new TAPNTransitionHandler(drawingSurface, (Transition) newObject, template.guiModel(), template.model());
					} else {
						transitionHandler = new TransitionHandler(drawingSurface, (Transition) newObject);
					}

					LabelHandler labelHandler = new LabelHandler(((Transition) newObject).getNameLabel(), (Transition) newObject);
					((Transition) newObject).getNameLabel().addMouseListener(labelHandler);
					((Transition) newObject).getNameLabel().addMouseMotionListener(labelHandler);
					((Transition) newObject).getNameLabel().addMouseWheelListener(labelHandler);

					newObject.addMouseListener(transitionHandler);
					newObject.addMouseMotionListener(transitionHandler);
					newObject.addMouseWheelListener(transitionHandler);

					newObject.addMouseListener(new AnimationHandler());

				} else if (newObject instanceof Arc) {
					/* CB - Joakim Byg add timed arcs */
					if (newObject instanceof TimedInputArcComponent) {
						if (newObject instanceof TransportArcComponent) {
							TransportArcHandler transportArcHandler = new TransportArcHandler(drawingSurface, (Arc) newObject);
							newObject.addMouseListener(transportArcHandler);
							newObject.addMouseWheelListener(transportArcHandler);
							newObject.addMouseMotionListener(transportArcHandler);
						} else {
							TimedArcHandler timedArcHandler = new TimedArcHandler(drawingSurface, (Arc) newObject);
							newObject.addMouseListener(timedArcHandler);
							newObject.addMouseWheelListener(timedArcHandler);
							newObject.addMouseMotionListener(timedArcHandler);
						}
					} else {
						/* EOC */
						ArcHandler arcHandler = new ArcHandler(drawingSurface,(Arc) newObject);
						newObject.addMouseListener(arcHandler);
						newObject.addMouseWheelListener(arcHandler);
						newObject.addMouseMotionListener(arcHandler);
					}
				} else if (newObject instanceof AnnotationNote) {
					AnnotationNoteHandler noteHandler = new AnnotationNoteHandler(drawingSurface, (AnnotationNote) newObject);
					newObject.addMouseListener(noteHandler);
					newObject.addMouseMotionListener(noteHandler);
					((Note) newObject).getNote().addMouseListener(noteHandler);
					((Note) newObject).getNote().addMouseMotionListener(noteHandler);
				}
				if (newObject instanceof Zoomable) {
					newObject.zoomUpdate(drawingSurface.getZoom());
				}
			}
			newObject.setGuiModel(template.guiModel());
		}
	}

	public class LoadedModel{
		private Collection<Template> templates;
		private Collection<TAPNQuery> queries;
		private TimedArcPetriNetNetwork network; 
		
		public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries){
			this.templates = templates;
			this.network = network;
			this.queries = queries; 
		}

		public Collection<Template> templates(){ return templates; }
		public Collection<TAPNQuery> queries(){ return queries; }
		public TimedArcPetriNetNetwork network(){ return network; }
	}
}
