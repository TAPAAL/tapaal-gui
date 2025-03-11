package dk.aau.cs.io.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.TCTL.XMLParsing.XMLHyperLTLQueryParser;
import dk.aau.cs.TCTL.XMLParsing.XMLLTLQueryParser;
import dk.aau.cs.verification.SMCSettings;
import dk.aau.cs.verification.SMCTraceType;
import dk.aau.cs.verification.observations.Observation;
import dk.aau.cs.verification.observations.expressions.ObsAdd;
import dk.aau.cs.verification.observations.expressions.ObsConstant;
import dk.aau.cs.verification.observations.expressions.ObsExpression;
import dk.aau.cs.verification.observations.expressions.ObsMultiply;
import dk.aau.cs.verification.observations.expressions.ObsOperator;
import dk.aau.cs.verification.observations.expressions.ObsPlace;
import dk.aau.cs.verification.observations.expressions.ObsSubtract;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.verification.TAPNQuery.AlgorithmOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.ExtrapolationOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.HashTableSize;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.VerificationType;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;
import net.tapaal.gui.petrinet.verification.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.XMLParsing.XMLCTLQueryParser;
import dk.aau.cs.TCTL.XMLParsing.XMLQueryParseException;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.translations.ReductionOption;

public class TAPNQueryLoader extends QueryLoader{

	private final Document doc;
	
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
        boolean coloredReduction = getColoredReductionOption(queryElement);
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
        boolean isCTL = isTypeQuery(queryElement, "CTL");
        boolean isLTL = isTypeQuery(queryElement, "LTL");
        boolean isHyperLTL = isTypeQuery(queryElement, "HyperLTL");
        boolean isSmc = isTypeQuery(queryElement, "SMC") || hasSmcTag(queryElement);

		boolean siphontrap = getReductionOption(queryElement, "useSiphonTrapAnalysis", false);
		boolean queryReduction = getReductionOption(queryElement, "useQueryReduction", true);
		boolean stubborn = getReductionOption(queryElement, "useStubbornReduction", true);
		boolean useTar = getAttributeOption(queryElement, "useTarOption", false);
        boolean useTarjan = getAttributeOption(queryElement, "useTarjan", false);
		boolean partitioning = getUnfoldingOption(queryElement, "partitioning", true);
		boolean colorFixpoint = getUnfoldingOption(queryElement, "colorFixpoint", true);
        boolean symmetricVars = getUnfoldingOption(queryElement, "symmetricVars", true);

        boolean parallel = getReductionOption(queryElement, "parallel", true);
        VerificationType verificationType = VerificationType.fromString(queryElement.getAttribute("verificationType"));

        int numberOfTraces;
        try {
            numberOfTraces = Integer.parseInt(queryElement.getAttribute("numberOfTraces"));
        } catch (NumberFormatException e) {
            numberOfTraces = 1;
        }

        SMCTraceType smcTraceType = new SMCTraceType(queryElement.getAttribute("smcTraceType"));

        SMCSettings smcSettings = SMCSettings.Default();

		TCTLAbstractProperty query;
        ArrayList<String> tracesArr = new ArrayList<String>();
        if(isSmc) {
            NodeList smcTagList = queryElement.getElementsByTagName("smc");
            if(smcTagList.getLength() > 0) {
                Element settingsNode = (Element) smcTagList.item(0);
                smcSettings = parseSmcSettings(settingsNode);
                NodeList observationsList = queryElement.getElementsByTagName("observations");
                if (observationsList.getLength() > 0) {
                    Element observationsNode = (Element)observationsList.item(0);
                    NodeList watchList = observationsNode.getElementsByTagName("watch");

                    List<Observation> observations = new ArrayList<>();
                    for (int i = 0; i < watchList.getLength(); ++i) {
                        Element watch = (Element)watchList.item(i);
                        String name = watch.getAttribute("name");                        

                        Element root = getFirstElementChild(watch);
                        Observation observation = new Observation(name, parseObsExpression(root));
                        
                        if (watch.hasAttribute("isEnabled")) {
                            observation.setEnabled(watch.getAttribute("isEnabled").equals("true"));
                        }

                        observations.add(observation);
                    }

                    smcSettings.setObservations(observations);
                }
            } else {
                smcSettings = SMCSettings.Default();
            }

            query = parseLTLQueryProperty(queryElement);
        } else if (queryElement.hasAttribute("type") && queryElement.getAttribute("type").equals("LTL")) {
            query = parseLTLQueryProperty(queryElement);
        } else if (queryElement.hasAttribute("type") && queryElement.getAttribute("type").equals("HyperLTL")){
		    query = parseHyperLTLQueryProperty(queryElement);
            if(queryElement.hasAttribute("traces")) {
                tracesArr.addAll(getTraces(queryElement));
            }
        } else if (queryElement.getElementsByTagName("formula").item(0) != null){
			query = parseCTLQueryProperty(queryElement);
		} else {
			query = parseQueryProperty(queryElement.getAttribute("query"));
		}

		if (query != null) {
			TAPNQuery parsedQuery = new TAPNQuery(comment, capacity, query, traceOption, searchOption, reductionOption, symmetry, gcd, timeDarts, pTrie, overApproximation, reduction, hashTableSize, extrapolationOption, inclusionPlaces, isOverApproximationEnabled, isUnderApproximationEnabled, approximationDenominator, partitioning, colorFixpoint, symmetricVars, network.isColored(), coloredReduction);
			parsedQuery.setActive(active);
			parsedQuery.setDiscreteInclusion(discreteInclusion);
			parsedQuery.setCategory(detectCategory(query, isCTL, isLTL, isHyperLTL, isSmc));
			parsedQuery.setUseSiphontrap(siphontrap);
			parsedQuery.setUseQueryReduction(queryReduction);
			parsedQuery.setUseStubbornReduction(stubborn);
            parsedQuery.setUseTarOption(useTar);
            parsedQuery.setUseTarjan(useTarjan);
			if (parsedQuery.getCategory() == QueryCategory.CTL){
				parsedQuery.setAlgorithmOption(AlgorithmOption.valueOf(algorithmOption));
			} else if(parsedQuery.getCategory() == QueryCategory.HyperLTL) {
                parsedQuery.setTraceList(tracesArr);
            } else if(parsedQuery.getCategory() == QueryCategory.SMC) {
                parsedQuery.setSmcSettings(smcSettings);
                parsedQuery.setParallel(parallel);
                parsedQuery.setVerificationType(verificationType);
                parsedQuery.setNumberOfTraces(numberOfTraces);
                parsedQuery.setSmcTraceType(smcTraceType);
            }
			return parsedQuery;
		} else
			return null;
	}

    public static boolean hasSmcTag(Node queryElement) {
        NodeList children = queryElement.getChildNodes();
        for(int i = 0 ; i < children.getLength() ; i++) {
            Node child = children.item(i);
            if(child.getNodeName().equals("smc")) return true;
        }
        return false;
    }

    private Element getFirstElementChild(Element parent) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return (Element)child;
            }
        }

        return null;
    }

    private Element getSecondElementChild(Element parent) {
        NodeList children = parent.getChildNodes();
        int elementCount = 0;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                ++elementCount;
                if (elementCount == 2) {
                    return (Element)child;
                }
            }
        }

        return null;
    }

    private ObsExpression parseObsExpression(Element element) {
        String tagName = element.getTagName();
    
        switch (tagName) {
            case "integer-sum":
                return createOperatorExpression(element, ObsAdd::new);
            case "integer-difference":
                return createOperatorExpression(element, ObsSubtract::new);
            case "integer-product":
                return createOperatorExpression(element, ObsMultiply::new);
            case "integer-constant":
                return new ObsConstant(Integer.parseInt(element.getTextContent()));
            case "place":
                return createPlaceExpression(element);
            default:
                throw new IllegalArgumentException("Unknown expression type: " + tagName);
        }
    }

    private ObsExpression createOperatorExpression(Element element, BiFunction<ObsExpression, ObsExpression, ObsOperator> constructor) {
        ObsExpression left = parseObsExpression(getFirstElementChild(element));
        ObsExpression right = parseObsExpression(getSecondElementChild(element));
        ObsOperator operator = constructor.apply(left, right);

        left.setParent(operator);
        right.setParent(operator);

        return operator;
    }    

    private ObsExpression createPlaceExpression(Element element) {
        String name = element.getTextContent();
        String[] parts = name.split("_", 2);
        String templateName = parts[0];
        String placeName = parts[1];
    
        return new ObsPlace(templateName, placeName, network);
    }

    public static SMCSettings parseSmcSettings(Element smcTag) {
        SMCSettings settings = SMCSettings.Default();
        if(smcTag.hasAttribute("time-bound"))
            settings.timeBound = Integer.parseInt(smcTag.getAttribute("time-bound"));
        if(smcTag.hasAttribute("step-bound"))
            settings.stepBound = Integer.parseInt(smcTag.getAttribute("step-bound"));
        if(smcTag.hasAttribute("compare-to")) {
            settings.compareToFloat = true;
            settings.geqThan = Float.parseFloat(smcTag.getAttribute("compare-to"));
        }
        if(smcTag.hasAttribute("confidence"))
            settings.confidence = Float.parseFloat(smcTag.getAttribute("confidence"));
        if(smcTag.hasAttribute("interval-width"))
            settings.estimationIntervalWidth = Float.parseFloat(smcTag.getAttribute("interval-width"));
        if(smcTag.hasAttribute("false-positives"))
            settings.falsePositives = Float.parseFloat(smcTag.getAttribute("false-positives"));
        if(smcTag.hasAttribute("false-negatives"))
            settings.falsePositives = Float.parseFloat(smcTag.getAttribute("false-negatives"));
        if(smcTag.hasAttribute("indifference"))
            settings.indifferenceWidth = Float.parseFloat(smcTag.getAttribute("indifference"));
        return settings;
    }

	public static TAPNQuery.QueryCategory detectCategory(TCTLAbstractProperty query, boolean isCTL, boolean isLTL, boolean isHyperLTL, boolean isSmc){
        if (isCTL) return TAPNQuery.QueryCategory.CTL;
        if (isLTL) return QueryCategory.LTL;
        if (isHyperLTL) return QueryCategory.HyperLTL;
        if (isSmc) return QueryCategory.SMC;

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

		if(query instanceof LTLGNode ||
                query instanceof LTLFNode ||
                query instanceof LTLUNode ||
                query instanceof LTLXNode ||
                query instanceof LTLANode ||
                query instanceof LTLENode){
            return TAPNQuery.QueryCategory.LTL;
        } else if(query instanceof TCTLEUNode ||
                query instanceof TCTLEXNode ||
				query instanceof TCTLAUNode ||
				query instanceof TCTLAXNode) {
            return TAPNQuery.QueryCategory.CTL;
        } else if(query instanceof HyperLTLPathScopeNode){
		    return QueryCategory.HyperLTL;
        }

        // If query is a fireability query
        if(query instanceof TCTLTransitionNode) {
            return TAPNQuery.QueryCategory.CTL;
        }
        if(query instanceof TCTLPlusListNode){
                for(TCTLAbstractStateProperty sp : ((TCTLPlusListNode)query).getProperties()) {
                        if(TAPNQueryLoader.detectCategory(sp, isCTL, isLTL, isHyperLTL, isSmc) == TAPNQuery.QueryCategory.CTL){
                            return TAPNQuery.QueryCategory.CTL;
                        }
                }
		}
		
                // If any property has been converted
		for (StringPosition child : children) {
			if(TAPNQueryLoader.detectCategory(child.getObject(), isCTL, isLTL, isHyperLTL, isSmc) == TAPNQuery.QueryCategory.CTL){
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
        
	private boolean isTypeQuery(Element queryElement, String type) {
		if(!queryElement.hasAttribute("type")){
		    if (type.equals("CTL")) {
		        return XMLQueryLoader.canBeCTL(queryElement) && !XMLQueryLoader.canBeLTL(queryElement);
            } else if (type.equals("LTL")) {
                return !XMLQueryLoader.canBeCTL(queryElement) && XMLQueryLoader.canBeLTL(queryElement);
            }
            return false;
		}
		boolean result;
		try {
			result = queryElement.getAttribute("type").equals(type);
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

    private boolean getUnfoldingOption(Element queryElement, String attributeName, boolean defaultValue) {
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

	private boolean getAttributeOption(Element queryElement, String attributeName, boolean defaultValue) {
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
            messages.add(ERROR_PARSING_QUERY_MESSAGE);
		}
		
		return query;
	}

    private TCTLAbstractProperty parseLTLQueryProperty(Node queryElement){
        TCTLAbstractProperty query = null;
        try {
            query = XMLLTLQueryParser.parse(queryElement);
        } catch (XMLQueryParseException e) {
            messages.add(ERROR_PARSING_QUERY_MESSAGE);
        }

        return query;
    }

    private TCTLAbstractProperty parseHyperLTLQueryProperty(Node queryElement){
        TCTLAbstractProperty query = null;
        try {
            query = XMLHyperLTLQueryParser.parse(queryElement);
        } catch (XMLQueryParseException e) {
            messages.add(ERROR_PARSING_QUERY_MESSAGE);
        }

        return query;
    }

	private TCTLAbstractProperty parseQueryProperty(String queryToParse) {
		TCTLAbstractProperty query = null;
		try {
			query = TAPAALQueryParser.parse(queryToParse);
		} catch (Exception e) {
			if(firstQueryParsingWarning) {
                messages.add(ERROR_PARSING_QUERY_MESSAGE);
                firstQueryParsingWarning = false;
			}
		}
		return query;
	}

	private ReductionOption getQueryReductionOption(Element queryElement) {
		ReductionOption reductionOption;
        var redName = queryElement.getAttribute("reductionOption");
		try {
            if (redName.equals("VerifyTAPNdiscreteVerification")) {
                //Verifydtapn was know as VerifyTAPNdiscreteVerification in the older versions
                reductionOption = ReductionOption.VerifyDTAPN;
            } else {
                reductionOption = ReductionOption.valueOf(redName);
            }
		} catch (Exception e) {
			throw new RuntimeException("Unknown Query reduction option: " + redName);
		}
		return reductionOption;
	}

    private boolean getColoredReductionOption(Element queryElement) {
        boolean coloredReduction;
        try {
            coloredReduction = queryElement.getAttribute("coloredReduction").equals("true");
        } catch(Exception e) {
            coloredReduction = false;
        }
        return coloredReduction;
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

	private List<String> getTraces(Element element) {
        List<String> traces = new ArrayList<String>();


        try {
            String[] tracesArr = element.getAttribute("traces").split(",");
            traces = Arrays.asList(tracesArr);

        } catch (Exception e) {
            traces.add("T1");
        }

	    return traces;
    }
}
