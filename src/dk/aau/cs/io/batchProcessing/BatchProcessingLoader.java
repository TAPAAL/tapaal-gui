package dk.aau.cs.io.batchProcessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
import pipe.dataLayer.TAPNQuery.AlgorithmOption;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.QueryCategory;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.InclusionPlaces;
import pipe.gui.widgets.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.XMLParsing.XMLCTLQueryParser;
import dk.aau.cs.TCTL.XMLParsing.XMLQueryParseException;
import dk.aau.cs.TCTL.visitors.RenameTemplateVisitor;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.io.IdResolver;
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
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public class BatchProcessingLoader {
	private HashMap<Tuple<TimedTransition, Integer>, TimedPlace> presetArcs;
	private HashMap<Tuple<TimedTransition, Integer>, TimedPlace> postsetArcs;
	private HashMap<String, String> placeIDToName;
	private HashMap<String, String> transitionIDToName;
	private HashMap<Tuple<TimedTransition, Integer>, TimeInterval> transportArcsTimeIntervals;

	private IdResolver idResolver = new IdResolver();
	
	private NameGenerator nameGenerator = new NameGenerator();
	
	public BatchProcessingLoader() {
		presetArcs = new HashMap<Tuple<TimedTransition,Integer>, TimedPlace>();
		postsetArcs = new HashMap<Tuple<TimedTransition,Integer>, TimedPlace>();
		placeIDToName = new HashMap<String, String>();
		transitionIDToName = new HashMap<String, String>();
		transportArcsTimeIntervals = new HashMap<Tuple<TimedTransition,Integer>, TimeInterval>();
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

	private LoadedBatchProcessingModel parse(Document doc) throws FormatException {
		ConstantStore constants = new ConstantStore(parseConstants(doc));

		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork(constants);

		parseSharedPlaces(doc, network, constants);
		parseSharedTransitions(doc, network);
		
		parseTemplates(doc, network, constants);
		Collection<TAPNQuery> queries = parseQueries(doc, network);

		network.buildConstraints();
		return new LoadedBatchProcessingModel(network, queries);
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
		}
		
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

	private Collection<TAPNQuery> parseQueries(Document doc, TimedArcPetriNetNetwork network) {
		Collection<TAPNQuery> queries = new ArrayList<TAPNQuery>();
		NodeList queryNodes = doc.getElementsByTagName("query");
		
		ArrayList<Tuple<String, String>> templatePlaceNames = getPlaceNames(network);
		for (int i = 0; i < queryNodes.getLength(); i++) {
			Node q = queryNodes.item(i);

			if (q instanceof Element) {
				TAPNQuery query = parseTAPNQuery((Element) q, network);
				
				if (query != null) {
					if(!doesPlacesUsedInQueryExist(query, templatePlaceNames)) {
						continue;
					}

					queries.add(query);
				}
			}
		}
		
		return queries;
	}

	private boolean doesPlacesUsedInQueryExist(TAPNQuery query, ArrayList<Tuple<String, String>> templatePlaceNames) {
		VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(templatePlaceNames);

		VerifyPlaceNamesVisitor.Context c = nameChecker.verifyPlaceNames(query.getProperty());
		
		return c.getResult();
		
	}

	private ArrayList<Tuple<String, String>> getPlaceNames(TimedArcPetriNetNetwork network) {
		ArrayList<Tuple<String,String>> templatePlaceNames = new ArrayList<Tuple<String,String>>();
		for(TimedArcPetriNet tapn : network.allTemplates()) {
			for(TimedPlace p : tapn.places()) {
				templatePlaceNames.add(new Tuple<String, String>(tapn.name(), p.name()));
			}
		}
		
		for(TimedPlace p : network.sharedPlaces()) {
			templatePlaceNames.add(new Tuple<String, String>("", p.name()));
		}
		return templatePlaceNames;
	}

	private void parseTemplates(Document doc, TimedArcPetriNetNetwork network, ConstantStore constants) throws FormatException {
		NodeList nets = doc.getElementsByTagName("net");
		
		if(nets.getLength() <= 0)
			throw new FormatException("File did not contain any TAPN components.");
		
		for (int i = 0; i < nets.getLength(); i++) {
			parseTimedArcPetriNet(nets.item(i), network, constants);
		}
	}

	private List<Constant> parseConstants(Document doc) {
		List<Constant> constants = new ArrayList<Constant>();
		NodeList constantNodes = doc.getElementsByTagName("constant");
		for (int i = 0; i < constantNodes.getLength(); i++) {
			Node c = constantNodes.item(i);

			if (c instanceof Element) {
				Constant constant = parseAndAddConstant((Element) c);
				constants.add(constant);
			}
		}
		return constants;
	}

	private void parseTimedArcPetriNet(Node tapnNode, TimedArcPetriNetNetwork network, ConstantStore constants) throws FormatException {
		String name = getTAPNName(tapnNode);
		boolean active = getActiveStatus(tapnNode);

		TimedArcPetriNet tapn = new TimedArcPetriNet(name);
		tapn.setActive(active);
		network.add(tapn);
		nameGenerator.add(tapn);

		NodeList nodeList = tapnNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if(node instanceof Element){
				parseElement((Element)node, tapn, network, constants);
			}
		}
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

	private void parseElement(Element element, TimedArcPetriNet tapn, TimedArcPetriNetNetwork network, ConstantStore constants) throws FormatException {
		if ("place".equals(element.getNodeName())) {
			parsePlace(element, network, tapn, constants);
		} else if ("transition".equals(element.getNodeName())) {
			parseTransition(element, network, tapn);
		} else if ("arc".equals(element.getNodeName())) {
			parseAndAddArc(element, tapn, constants);
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

	private void parseTransition(Element transition, TimedArcPetriNetNetwork network, TimedArcPetriNet tapn) {
		String idInput = transition.getAttribute("id");
		String nameInput = transition.getAttribute("name");
		boolean isUrgent = Boolean.parseBoolean(transition.getAttribute("urgent"));
		
		idResolver.add(tapn.name(), idInput, nameInput);
		
		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}
		
		TimedTransition t = new TimedTransition(nameInput);
		t.setUrgent(isUrgent);
		if(network.isNameUsedForShared(nameInput)){
			t.setName(nameGenerator.getNewTransitionName(tapn)); // introduce temporary name to avoid exceptions
			tapn.add(t);
			if(!transitionIDToName.containsKey(idInput))
				transitionIDToName.put(idInput, nameInput);
			network.getSharedTransitionByName(nameInput).makeShared(t);
		}else{
			tapn.add(t);
			transitionIDToName.put(idInput, nameInput);
		}
		nameGenerator.updateIndicesForAllModels(nameInput);
	}

	private void parsePlace(Element place, TimedArcPetriNetNetwork network, TimedArcPetriNet tapn, ConstantStore constants) {
		String idInput = place.getAttribute("id");
		String nameInput = place.getAttribute("name");
		
		
		int initialMarkingInput = Integer.parseInt(place.getAttribute("initialMarking"));
		String invariant = place.getAttribute("invariant");

		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}
		
		if(nameInput.toLowerCase().equals("true") || nameInput.toLowerCase().equals("false")) {
			nameInput = "_" + nameInput;
		}

		idResolver.add(tapn.name(), idInput, nameInput);

		TimedPlace p;
		if(network.isNameUsedForShared(nameInput)){
			p = network.getSharedPlaceByName(nameInput);
			if(!placeIDToName.containsKey(idInput))
				placeIDToName.put(idInput, nameInput);
			tapn.add(p);
		}else{
			p = new LocalTimedPlace(nameInput, TimeInvariant.parse(invariant, constants));
			tapn.add(p);
			placeIDToName.put(idInput, nameInput);
			for (int i = 0; i < initialMarkingInput; i++) {
				network.marking().add(new TimedToken(p));
			}
		}
		nameGenerator.updateIndicesForAllModels(nameInput);
	}

	private void parseAndAddArc(Element arc, TimedArcPetriNet tapn, ConstantStore constants) throws FormatException {
		String sourceId = idResolver.get(tapn.name(), arc.getAttribute("source"));
		String targetId = idResolver.get(tapn.name(), arc.getAttribute("target"));
		String inscription = arc.getAttribute("inscription");
		String type = arc.getAttribute("type");
		
		//Get weight if any
		Weight weight = new IntWeight(1);
		if(arc.hasAttribute("weight")){
			weight = Weight.parseWeight(arc.getAttribute("weight"), constants);
		}

		if (type.equals("tapnInhibitor"))
			parseAndAddTimedInhibitorArc(sourceId, targetId, inscription, tapn, constants, weight);
		else if (type.equals("timed"))
				parseAndAddTimedInputArc(sourceId, targetId, inscription, tapn, constants, weight);
		else if (type.equals("transport"))
			parseAndAddTransportArc(sourceId, targetId, inscription, tapn, constants, weight);
		else
			parseAndAddTimedOutputArc(sourceId, targetId, inscription, tapn, weight);
	}

	private void parseAndAddTimedOutputArc(String sourceId, String targetId, String inscription, TimedArcPetriNet tapn, Weight weight) throws FormatException {
		TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(sourceId));
		TimedPlace place = tapn.getPlaceByName(placeIDToName.get(targetId));

		TimedOutputArc outputArc = new TimedOutputArc(transition, place, weight);
		
		if(tapn.hasArcFromTransitionToPlace(outputArc.source(),outputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}

		tapn.add(outputArc);
	}

	private void parseAndAddTransportArc(String sourceId, String targetId,	String inscription, TimedArcPetriNet tapn, ConstantStore constants, Weight weight) {
		String[] inscriptionSplit = {};
		if (inscription.contains(":")) {
			inscriptionSplit = inscription.split(":");
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

				TransportArc transArc = new TransportArc(sourcePlace, transition, destPlace, interval, weight);
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

				TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, interval, weight);
				tapn.add(transArc);

				presetArcs.remove(hashKey);
				transportArcsTimeIntervals.remove(hashKey);
			} else {
				postsetArcs.put(hashKey, destPlace);
			}
		}
	}

	private void parseAndAddTimedInputArc(String sourceId, String targetId, String inscription, TimedArcPetriNet tapn, ConstantStore constants, Weight weight) throws FormatException {
		TimedPlace place = tapn.getPlaceByName(placeIDToName.get(sourceId));
		TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(targetId));
		TimeInterval interval = TimeInterval.parse(inscription, constants);

		TimedInputArc inputArc = new TimedInputArc(place, transition, interval, weight);

		if(tapn.hasArcFromPlaceToTransition(inputArc.source(), inputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}

		tapn.add(inputArc);
	}

	private void parseAndAddTimedInhibitorArc(String sourceId, String targetId, String inscription, TimedArcPetriNet tapn, ConstantStore constants, Weight weight) {
		TimedPlace place = tapn.getPlaceByName(placeIDToName.get(sourceId));
		TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(targetId));
		
		TimedInhibitorArc inhibArc = new TimedInhibitorArc(place, transition, TimeInterval.ZERO_INF, weight);
		tapn.add(inhibArc);
	}

	private TAPNQuery parseTAPNQuery(Element queryElement, TimedArcPetriNetNetwork network) {
		String comment = getQueryComment(queryElement);
		TraceOption traceOption = getQueryTraceOption(queryElement);
		SearchOption searchOption = getQuerySearchOption(queryElement);
		HashTableSize hashTableSize = getQueryHashTableSize(queryElement);
		ExtrapolationOption extrapolationOption = getQueryExtrapolationOption(queryElement);
		ReductionOption reductionOption = getQueryReductionOption(queryElement);
		int capacity = Integer.parseInt(queryElement.getAttribute("capacity"));
		boolean symmetry = getReductionOption(queryElement, "symmetry", true);
		boolean gcd = getReductionOption(queryElement, "gcd", true);
		boolean timeDarts = getReductionOption(queryElement, "timeDarts", true);
		boolean pTrie = getReductionOption(queryElement, "pTrie", true);
		boolean overApproximation = getReductionOption(queryElement, "overApproximation", false);
		boolean isOverApproximationEnabled = getApproximationOption(queryElement, "enableOverApproximation", false);
		boolean isUnderApproximationEnabled = getApproximationOption(queryElement, "enableUnderApproximation", false);
		int approximationDenominator = getApproximationValue(queryElement, "approximationDenominator", 2);
		boolean discreteInclusion = getDiscreteInclusionOption(queryElement);
		boolean active = getActiveStatus(queryElement);
		InclusionPlaces inclusionPlaces = getInclusionPlaces(queryElement, network);
		boolean reduction = getReductionOption(queryElement, "reduction", true);
		String algorithmOption = queryElement.getAttribute("algorithmOption");
		boolean isCTL = isCTLQuery(queryElement);
		boolean siphontrap = getReductionOption(queryElement, "useSiphonTrapAnalysis", false);
		boolean queryReduction = getReductionOption(queryElement, "useQueryReduction", true);
		boolean stubborn = getReductionOption(queryElement, "useStubbornReduction", true);
                
		TCTLAbstractProperty query;
		if (queryElement.getElementsByTagName("formula").item(0) != null){
			query = parseCTLQueryProperty(queryElement);
		} else {
			query = parseQueryProperty(queryElement.getAttribute("query"));
		}

		if (query != null) {
			TAPNQuery parsedQuery = new TAPNQuery(comment, capacity, query, traceOption, searchOption, reductionOption, symmetry, gcd, timeDarts, pTrie, overApproximation, reduction, hashTableSize, extrapolationOption, inclusionPlaces, isOverApproximationEnabled, isUnderApproximationEnabled, approximationDenominator);
			parsedQuery.setActive(active);
			parsedQuery.setDiscreteInclusion(discreteInclusion);
			parsedQuery.setCategory(TAPNQueryLoader.detectCategory(query, isCTL));
			parsedQuery.setUseSiphontrap(siphontrap);
			parsedQuery.setUseQueryReduction(queryReduction);
			parsedQuery.setUseStubbornReduction(stubborn);
			if (parsedQuery.getCategory() == QueryCategory.CTL && algorithmOption != null){
				parsedQuery.setAlgorithmOption(AlgorithmOption.valueOf(algorithmOption));
//				RenameTemplateVisitor rt = new RenameTemplateVisitor("", 
//		                network.activeTemplates().get(0).name());
//				parsedQuery.getProperty().accept(rt, null);
			}
			return parsedQuery;
		} else
			return null;
	}
	
	private int getApproximationValue(Element queryElement, String attributeName, int defaultValue)
	{
		if(!queryElement.hasAttribute(attributeName)){
			return defaultValue;
		}
		int result;
		try {
			result = Integer.parseInt(queryElement.getAttribute(attributeName));
		} catch(Exception e) {
			result = defaultValue;
		}
		return result;
	}
	
	private boolean getApproximationOption(Element queryElement, String attributeName, boolean defaultValue)
	{
		if(!queryElement.hasAttribute(attributeName)){
			return defaultValue;
		}
		boolean result;
		try {
			result = queryElement.getAttribute(attributeName).equals("true");
		} catch(Exception e) {
			result = defaultValue;
		}
		return result;	
	}
	
	private boolean getDiscreteInclusionOption(Element queryElement) {
		boolean discreteInclusion;
		try {
			discreteInclusion = queryElement.getAttribute("discreteInclusion").equals("true");
		} catch(Exception e) {
			discreteInclusion = false;
		}
		return discreteInclusion;	
	}
	
	private InclusionPlaces getInclusionPlaces(Element queryElement, TimedArcPetriNetNetwork network) {
		List<TimedPlace> places = new ArrayList<TimedPlace>();
		
		String inclusionPlaces;
		try{
			inclusionPlaces = queryElement.getAttribute("inclusionPlaces");
		} catch(Exception e) {
			inclusionPlaces = "*ALL*";
		}

		if(!queryElement.hasAttribute("inclusionPlaces") || inclusionPlaces.equals("*ALL*")) 
			return new InclusionPlaces();
		
		if(inclusionPlaces.isEmpty() || inclusionPlaces.equals("*NONE*")) 
			return new InclusionPlaces(InclusionPlacesOption.UserSpecified, new ArrayList<TimedPlace>());
		
		
		String[] placeNames = inclusionPlaces.split(",");
		
		for(String name : placeNames) {
			if(name.contains(".")) {
				String templateName = name.split("\\.")[0];
				String placeName = name.split("\\.")[1];
				
				// "true" and "false" are reserved keywords and places using these names are renamed to "_true" and "_false" respectively
				if(placeName.equalsIgnoreCase("false") || placeName.equalsIgnoreCase("true"))
					placeName = "_" + placeName;
				
				TimedPlace p = network.getTAPNByName(templateName).getPlaceByName(placeName);
				places.add(p);
			} else { // shared Place
				
				if(name.equalsIgnoreCase("false") || name.equalsIgnoreCase("true"))
					name = "_" + name;
				
				TimedPlace p = network.getSharedPlaceByName(name);
				places.add(p);
			}
		}
		
		return new InclusionPlaces(InclusionPlacesOption.UserSpecified, places);
	}
        
	private boolean isCTLQuery(Element queryElement) {
		if(!queryElement.hasAttribute("type")){
			return false;
		}
		boolean result;
		try {
			result = queryElement.getAttribute("type").equals("CTL");
		} catch(Exception e) {
			result = false;
		}
		return result;
	}
	
	private boolean getReductionOption(Element queryElement, String attributeName, boolean defaultValue) {
		boolean result;
		try {
			result = queryElement.getAttribute(attributeName).equals("true");
		} catch(Exception e) {
			result = defaultValue;
		}
		return result;	
	}
	
	private TCTLAbstractProperty parseCTLQueryProperty(Node queryElement){
		TCTLAbstractProperty query = null;
		
		try {
			query = XMLCTLQueryParser.parse(queryElement);
		} catch (XMLQueryParseException e) {
			System.err.println("No query was specified: " + e.getStackTrace().toString());
		}
		
		return query;
	}

	private TCTLAbstractProperty parseQueryProperty(String queryToParse) {
		TCTLAbstractProperty query = null;

		try {
			query = TAPAALQueryParser.parse(queryToParse);
		} catch (Exception e) {
			System.err.println("No query was specified: " + e.getStackTrace().toString());
		}
		return query;
	}

	private ReductionOption getQueryReductionOption(Element queryElement) {
		ReductionOption reductionOption;
		try {
			reductionOption = ReductionOption.valueOf(queryElement.getAttribute("reductionOption"));
		} catch (Exception e) {
			reductionOption = ReductionOption.VerifyTAPN;
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
}
