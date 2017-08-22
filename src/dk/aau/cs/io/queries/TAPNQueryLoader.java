package dk.aau.cs.io.queries;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
import dk.aau.cs.TCTL.StringPosition;
import dk.aau.cs.TCTL.TCTLAUNode;
import dk.aau.cs.TCTL.TCTLAXNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLEUNode;
import dk.aau.cs.TCTL.TCTLEXNode;
import dk.aau.cs.TCTL.TCTLPathToStateConverter;
import dk.aau.cs.TCTL.TCTLStateToPathConverter;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.TCTLPlusListNode;
import dk.aau.cs.TCTL.TCTLTransitionNode;
import dk.aau.cs.TCTL.XMLParsing.XMLCTLQueryParser;
import dk.aau.cs.TCTL.XMLParsing.XMLQueryParseException;
import dk.aau.cs.TCTL.visitors.RenameTemplateVisitor;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.translations.ReductionOption;

public class TAPNQueryLoader extends QueryLoader{

	private Document doc;
	
	public TAPNQueryLoader(Document doc, TimedArcPetriNetNetwork network) {
		super(network);
		this.doc = doc;
	}
	
	protected ArrayList<TAPNQuery> getQueries(){
		NodeList queryNodes = doc.getElementsByTagName("query");
		ArrayList<TAPNQuery> queries = new ArrayList<TAPNQuery>();
		
		for (int i = 0; i < queryNodes.getLength(); i++) {
			Node q = queryNodes.item(i);

			if (q instanceof Element) {
				TAPNQuery query = parseTAPNQuery((Element) q, network);
				
				queries.add(query);
			}
		}
		return queries;
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
		boolean overApproximation = getReductionOption(queryElement, "overApproximation", true);
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
			parsedQuery.setCategory(detectCategory(query, isCTL));
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
	
	public static TAPNQuery.QueryCategory detectCategory(TCTLAbstractProperty query, boolean isCTL){
                if (isCTL) return TAPNQuery.QueryCategory.CTL;
                
                StringPosition[] children = query.getChildren();

                // If query is root and state property
                if(query instanceof TCTLAbstractStateProperty){
                        if(((TCTLAbstractStateProperty) query).getParent() == null){
                                return TAPNQuery.QueryCategory.CTL;
                        }
                }
        
		if(query instanceof TCTLStateToPathConverter ||
				query instanceof TCTLPathToStateConverter){
			return TAPNQuery.QueryCategory.CTL;
		}

		if(query instanceof TCTLEUNode ||
				query instanceof TCTLEXNode ||
				query instanceof TCTLAUNode ||
				query instanceof TCTLAXNode){
			return TAPNQuery.QueryCategory.CTL;
		}
                
                // If query is a fireability query
                if(query instanceof TCTLTransitionNode) {
                    return TAPNQuery.QueryCategory.CTL;
                }
                if(query instanceof TCTLPlusListNode){
                        for(TCTLAbstractStateProperty sp : ((TCTLPlusListNode)query).getProperties()) {
                                if(TAPNQueryLoader.detectCategory(sp, isCTL) == TAPNQuery.QueryCategory.CTL){
                                    return TAPNQuery.QueryCategory.CTL;
                                }
                        }
		}
		
                // If any property has been converted
		for (StringPosition child : children) {
			if(TAPNQueryLoader.detectCategory(child.getObject(), isCTL) == TAPNQuery.QueryCategory.CTL){
				return TAPNQuery.QueryCategory.CTL;
			} 
		}
		return TAPNQuery.QueryCategory.Default;
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
	
	private TCTLAbstractProperty parseCTLQueryProperty(Node queryElement){
		TCTLAbstractProperty query = null;
		
		try {
			query = XMLCTLQueryParser.parse(queryElement);
		} catch (XMLQueryParseException e) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), ERROR_PARSING_QUERY_MESSAGE, "Error Parsing Query", JOptionPane.ERROR_MESSAGE);
		}
		
		return query;
	}

	private TCTLAbstractProperty parseQueryProperty(String queryToParse) {
		TCTLAbstractProperty query = null;

		try {
			query = TAPAALQueryParser.parse(queryToParse);
		} catch (Exception e) {
			if(firstQueryParsingWarning) {
				JOptionPane.showMessageDialog(CreateGui.getApp(), ERROR_PARSING_QUERY_MESSAGE, "Error Parsing Query", JOptionPane.ERROR_MESSAGE);
				firstQueryParsingWarning = false;
			}
			System.err.println("No query was specified: ");
			e.printStackTrace();
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
	
	private boolean getActiveStatus(Element element) {

		String activeString = element.getAttribute("active");
		
		if (activeString == null || activeString.equals(""))
			return true;
		else
			return activeString.equals("true");
	}
}
