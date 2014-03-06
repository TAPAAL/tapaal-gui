package dk.aau.cs.io.batchProcessing;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.InclusionPlaces;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.visitors.AddTemplateVisitor;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
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
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public class BatchProcessingLegacyLoader {

	private static final String SYMMETRY = "SYMMETRY";
	private HashMap<Tuple<TimedTransition, Integer>, TimedPlace> presetArcs;
	private HashMap<Tuple<TimedTransition, Integer>, TimedPlace> postsetArcs;
	private HashMap<String, String> placeIDToName;
	private HashMap<String, String> transitionIDToName;
	private HashMap<Tuple<TimedTransition, Integer>, TimeInterval> transportArcsTimeIntervals;
	private TimedArcPetriNet tapn;
	private ArrayList<TAPNQuery> queries;
	private ConstantStore constants;
	private NameGenerator nameGenerator = new NameGenerator();

	public BatchProcessingLegacyLoader() {
		presetArcs = new HashMap<Tuple<TimedTransition,Integer>, TimedPlace>();
		postsetArcs = new HashMap<Tuple<TimedTransition,Integer>, TimedPlace>();
		placeIDToName = new HashMap<String, String>();
		transitionIDToName = new HashMap<String, String>();
		transportArcsTimeIntervals = new HashMap<Tuple<TimedTransition,Integer>, TimeInterval>();
		queries = new ArrayList<TAPNQuery>();
		constants = new ConstantStore();
	}
	
	public LoadedBatchProcessingModel load(File file) throws FormatException {
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

	private LoadedBatchProcessingModel parse(Document tapnDoc) throws FormatException { 
		NodeList constantNodes = tapnDoc.getElementsByTagName("constant");
		for (int i = 0; i < constantNodes.getLength(); i++) {
			Node c = constantNodes.item(i);

			if (c instanceof Element) {
				parseAndAddConstant((Element) c);
			}
		}

		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork(constants);
		NodeList nets = tapnDoc.getElementsByTagName("net");
		
		if(nets.getLength() <= 0)
			throw new FormatException("File did not contain any TAPN components.");
		
		parseTimedArcPetriNetAsOldFormat(nets.item(0), network);
		
		checkThatQueriesUseExistingPlaces(network);
		
		return new LoadedBatchProcessingModel(network, queries);
	}

	private void checkThatQueriesUseExistingPlaces(TimedArcPetriNetNetwork network) {
		ArrayList<TAPNQuery> okQueries = new ArrayList<TAPNQuery>();
		ArrayList<Tuple<String,String>> templatePlaceNames = getTemplatePlaceNames(network);
		for(TAPNQuery query : queries) {
			if(!doesPlacesUsedInQueryExist(query, templatePlaceNames)) {
				continue;
			}
			
			okQueries.add(query);
		}
		
		queries = okQueries;
	}

	private ArrayList<Tuple<String, String>> getTemplatePlaceNames(TimedArcPetriNetNetwork network) {
		ArrayList<Tuple<String,String>> templatePlaceNames = new ArrayList<Tuple<String,String>>();
		for(TimedArcPetriNet tapn : network.allTemplates()) {
			for(TimedPlace p : tapn.places()) {
				templatePlaceNames.add(new Tuple<String, String>(tapn.name(), p.name()));
			}
		}
		
		Collection<SharedPlace> sharedPlaces = network.sharedPlaces();
		for(TimedPlace p : sharedPlaces) {
			templatePlaceNames.add(new Tuple<String, String>("", p.name()));
		}
		return templatePlaceNames;
	}

	private void parseAndAddTimedOutputArc(String sourceId, String targetId, String inscriptionTempStorage) throws FormatException {
		TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(sourceId));
		TimedPlace place = tapn.getPlaceByName(placeIDToName.get(targetId));

		TimedOutputArc outputArc = new TimedOutputArc(transition, place);
		
		if(tapn.hasArcFromTransitionToPlace(outputArc.source(),outputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}
		
		tapn.add(outputArc);
	}

	private void parseAndAddTransportArc(String sourceId, String targetId, String inscriptionTempStorage) {
		
		String[] inscriptionSplit = {};
		if (inscriptionTempStorage.contains(":")) {
			inscriptionSplit = inscriptionTempStorage.split(":");
		}
		boolean isInPreSet = false;
		
		TimedPlace sourcePlace = null;
		if(placeIDToName.containsKey(sourceId))
			sourcePlace = tapn.getPlaceByName(placeIDToName.get(sourceId));
		
		if (sourcePlace != null) {
			isInPreSet = true;
		}
		
		if (isInPreSet) {
			TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(targetId));
			Tuple<TimedTransition, Integer> hashKey = new Tuple<TimedTransition, Integer>(transition, Integer.parseInt(inscriptionSplit[1]));
			if (postsetArcs.containsKey(hashKey)) {
				TimedPlace destPlace = postsetArcs.get(hashKey);
				TimeInterval interval = TimeInterval.parse(inscriptionSplit[0],	constants);

				assert (sourcePlace != null);
				assert (transition != null);
				assert (destPlace != null);

				TransportArc transArc = new TransportArc(sourcePlace, transition, destPlace, interval);
				tapn.add(transArc);

				postsetArcs.remove(hashKey);
			} else {				
				presetArcs.put(hashKey, sourcePlace);
				transportArcsTimeIntervals.put(hashKey, TimeInterval.parse(inscriptionSplit[0], constants));
			}
		} else {
			TimedTransition trans = tapn.getTransitionByName(transitionIDToName.get(sourceId));
			TimedPlace destPlace = tapn.getPlaceByName(placeIDToName.get(targetId));
			Tuple<TimedTransition, Integer> hashKey = new Tuple<TimedTransition, Integer>(trans,  Integer.parseInt(inscriptionSplit[1]));
			
			if (presetArcs.containsKey(hashKey)) {
				sourcePlace = presetArcs.get(hashKey);
				TimeInterval interval = transportArcsTimeIntervals.get(hashKey);

				assert (sourcePlace != null);
				assert (trans != null);
				assert (destPlace != null);

				TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, interval);
				tapn.add(transArc);

				presetArcs.remove(hashKey);
				transportArcsTimeIntervals.remove(hashKey);
			} else {
				postsetArcs.put(hashKey, destPlace);
			}
		}
	}

	private void parseAndAddTimedInputArc(String sourceId, String targetId, String inscriptionTempStorage) throws FormatException {
		TimedPlace place = tapn.getPlaceByName(placeIDToName.get(sourceId));
		TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(targetId));
		TimeInterval interval = TimeInterval.parse(inscriptionTempStorage, constants);

		TimedInputArc inputArc = new TimedInputArc(place, transition, interval);
		
		if(tapn.hasArcFromPlaceToTransition(inputArc.source(), inputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}
		
		tapn.add(inputArc);
	}

	private void parseAndAddTimedInhibitorArc(String sourceId, String targetId, String inscriptionTempStorage) {
		TimedPlace place = tapn.getPlaceByName(placeIDToName.get(sourceId));
		TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(targetId));
		TimeInterval interval = TimeInterval.parse(inscriptionTempStorage, constants);
		
		TimedInhibitorArc inhibArc = new TimedInhibitorArc(place, transition, interval);
		tapn.add(inhibArc);
	}

	private ReductionOption getQueryReductionOption(Element queryElement) {
ReductionOption reductionOption;
		
		String reductionString = queryElement.getAttribute("reductionOption");
		String reductionName = "";
		if(reductionString.contains(SYMMETRY))
			reductionName = reductionString.replace(SYMMETRY, "");
		else {
			reductionName = reductionString;
		}
		
		try {
			reductionOption = ReductionOption.valueOf(reductionName);
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

	private void parseAndAddConstant(Element constantElement) {
		String name = constantElement.getAttribute("name");
		int value = Integer.parseInt(constantElement.getAttribute("value"));

		if (!name.isEmpty() && !name.equals(""))
			constants.add(new Constant(name, value));
	}

	// //////////////////////////////////////////////////////////
	// Legacy support for old format
	// //////////////////////////////////////////////////////////
	private void parseTimedArcPetriNetAsOldFormat(Node tapnNode, TimedArcPetriNetNetwork network) throws FormatException {
		tapn = new TimedArcPetriNet(nameGenerator .getNewTemplateName());
		network.add(tapn);

		Node node = null;
		NodeList nodeList = null;

		nodeList = tapnNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			parseElementAsOldFormat(node, tapn.name(), network.marking());
		}
	}

	private void parseElementAsOldFormat(Node node, String templateName, TimedMarking marking) throws FormatException {
		Element element;
		if (node instanceof Element) {
			element = (Element) node;
			if ("place".equals(element.getNodeName())) {
				parseAndAddPlaceAsOldFormat(element, marking);
			} else if ("transition".equals(element.getNodeName())) {
				parseAndAddTransitionAsOldFormat(element);
			} else if ("arc".equals(element.getNodeName())) {
				parseAndAddArcAsOldFormat(element);
			} else if ("queries".equals(element.getNodeName())) {
				TAPNQuery query = parseQueryAsOldFormat(element);
				
				
				if (query != null) {
					query.getProperty().accept(new AddTemplateVisitor(templateName), null);
					queries.add(query);
				}
			}
		}
	}

	private boolean doesPlacesUsedInQueryExist(TAPNQuery query, ArrayList<Tuple<String, String>> templatePlaceNames) {
		VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(templatePlaceNames);

		VerifyPlaceNamesVisitor.Context c = nameChecker.verifyPlaceNames(query.getProperty());
		
		return c.getResult();
	}

	private void parseAndAddTransitionAsOldFormat(Element element) throws FormatException {
		String idInput = element.getAttribute("id");
		String nameInput = getChildNodesContentOfValueChildNodeAsString(element, "name");
	
		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}

		transitionIDToName.put(idInput, nameInput);
		TimedTransition t = new TimedTransition(nameInput);
		tapn.add(t);
	}

	private void parseAndAddPlaceAsOldFormat(Element element, TimedMarking marking) throws FormatException {
		String idInput = element.getAttribute("id");
		String nameInput = getChildNodesContentOfValueChildNodeAsString(element, "name");
		int initialMarkingInput = getContentOfFirstSpecificChildNodesValueChildNodeAsInt(element, "initialMarking");
		String invariant = getChildNodesContentOfValueChildNodeAsString(element, "invariant");

		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}
		
		if(nameInput.toLowerCase().equals("true") || nameInput.toLowerCase().equals("false")) {
			nameInput = "_" + nameInput;
		}

		placeIDToName.put(idInput, nameInput);
		
		LocalTimedPlace p = new LocalTimedPlace(nameInput, TimeInvariant.parse(invariant, constants));
		tapn.add(p);
		for (int i = 0; i < initialMarkingInput; i++) {
				marking.add(new TimedToken(p, new BigDecimal(0.0)));
		}
	}

	private void parseAndAddArcAsOldFormat(Element inputArcElement) throws FormatException {
		String sourceId = inputArcElement.getAttribute("source");
		String targetId = inputArcElement.getAttribute("target");
		String inscriptionTempStorage = getChildNodesContentOfValueChildNodeAsString(inputArcElement, "inscription");

		
		String type = "normal";
		type = ((Element) getFirstChildNodeByName(inputArcElement, "type")).getAttribute("value");

		if (type.equals("tapnInhibitor")) {

			parseAndAddTimedInhibitorArc(sourceId, targetId, inscriptionTempStorage);

		} else {
			if (type.equals("timed")) {
				parseAndAddTimedInputArc(sourceId, targetId, inscriptionTempStorage);

			} else if (type.equals("transport")) {
				parseAndAddTransportArc(sourceId, targetId, inscriptionTempStorage);

			} else {
				parseAndAddTimedOutputArc(sourceId, targetId, inscriptionTempStorage);
			}

		}
	}

	private TAPNQuery parseQueryAsOldFormat(Element queryElement) throws FormatException {
		String comment = getQueryComment(queryElement);
		TraceOption traceOption = getQueryTraceOption(queryElement);
		SearchOption searchOption = getQuerySearchOption(queryElement);
		HashTableSize hashTableSize = getQueryHashTableSize(queryElement);
		ExtrapolationOption extrapolationOption = getQueryExtrapolationOption(queryElement);
		ReductionOption reductionOption = getQueryReductionOption(queryElement);
		int capacity = getQueryCapacityAsOldFormat(queryElement);
		boolean symmetry = getSymmetryAsOldFormat(queryElement);

		// The following attributes were not supported in the old versions of TAPAAL
		// We just pick the default values.
		boolean discreteInclusion = false;
		boolean active = true;
		InclusionPlaces inclusionPlaces = new InclusionPlaces();

		TCTLAbstractProperty query;
		query = parseQueryPropertyAsOldFormat(queryElement);

		
		if (query != null) {
			TAPNQuery parsedQuery = new TAPNQuery(comment, capacity, query, traceOption,
					searchOption, reductionOption, symmetry, true, true, true, false, false, hashTableSize,
					extrapolationOption,inclusionPlaces);
			parsedQuery.setActive(active);
			parsedQuery.setDiscreteInclusion(discreteInclusion);
			return parsedQuery;
		} else
			return null;
	}
	
	private boolean getSymmetryAsOldFormat(Element queryElement) {
		String reductionString = queryElement.getAttribute("reductionOption");
		
		return reductionString.contains(SYMMETRY);
	}

	private TCTLAbstractProperty parseQueryPropertyAsOldFormat(Element queryElement) throws FormatException {
		TCTLAbstractProperty query = null;

		String queryToParse = getChildNodesContentOfValueChildNodeAsString(queryElement, "query");

		try {
			query = TAPAALQueryParser.parse(queryToParse);
		} catch (Exception e) {
			System.err.println("No query was specified: " + e.getStackTrace().toString());
		}
		return query;
	}

	private int getQueryCapacityAsOldFormat(Element queryElement) throws FormatException {
		return getContentOfFirstSpecificChildNodesValueChildNodeAsInt(queryElement, "capacity");
	}

	private Node getFirstChildNodeByName(Element element, String childNodeName) throws FormatException {
		Node node = element.getElementsByTagName(childNodeName).item(0);

		if (node == null)
			throw new FormatException("TAPAAL could not recognize save format.");

		return node;
	}

	private String getContentOfValueChildNode(Element element) throws FormatException {
		return ((Element) getFirstChildNodeByName(element, "value")).getTextContent();
	}

	private String getChildNodesContentOfValueChildNodeAsString(Element element, String childNodeName) throws FormatException {
		Node node = getFirstChildNodeByName(element, childNodeName);

		if (node instanceof Element) {
			Element e = (Element) node;

			return getContentOfValueChildNode(e);
		}

		return "";
	}

	private int getContentOfFirstSpecificChildNodesValueChildNodeAsInt(Element element, String childNodeName) throws FormatException {
		Node node = getFirstChildNodeByName(element, childNodeName);

		if (node instanceof Element) {
			Element e = (Element) node;

			String value = getContentOfValueChildNode(e);

			if (value.length() > 0)
				return Integer.parseInt(value);
		}

		return 0;
	}
}
