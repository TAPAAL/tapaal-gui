package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dk.aau.cs.model.NTA.trace.TraceToken;
import dk.aau.cs.model.tapn.LocalTimedMarking;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.ColoredTransitionStep;
import dk.aau.cs.model.tapn.simulation.PetriNetStep;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;

public class VerifyTAPNTraceParser {
	private final TimedArcPetriNet tapn;
    private final boolean useExplicitSearch;

	private String traceNameToParse;
	
	public VerifyTAPNTraceParser(TimedArcPetriNet tapn) {
		this(tapn, false);
	}

    public VerifyTAPNTraceParser(TimedArcPetriNet tapn, boolean useExplicitSearch) {
        this.tapn = tapn;
        this.useExplicitSearch = useExplicitSearch;
    }

	public TimedArcPetriNetTrace parseTrace(BufferedReader reader) {
		TimedArcPetriNetTrace trace = new TimedArcPetriNetTrace(true);

		Document document = readerToDocument(reader);

        if (document == null) return null;

        NodeList nodeList = null;

		if (traceNameToParse != null) {
            NodeList childNodes = null;
		    Node traceListNode = document.getElementsByTagName("trace-list").item(0);

			if (traceListNode != null) {
				childNodes = traceListNode.getChildNodes();
			}

			if (childNodes != null) {
				for (int i = 0; i < childNodes.getLength(); i++) {
					NamedNodeMap nodeAttribute = childNodes.item(i).getAttributes();
	
					if (nodeAttribute != null && nodeAttribute.item(0).getNodeValue().equals(traceNameToParse)) {
						nodeList = childNodes.item(i).getChildNodes();
						trace.setTraceName(traceNameToParse);
						break;
					}
				}
			}
        }
		
		if (nodeList == null) {
            nodeList = document.getElementsByTagName("trace").item(0).getChildNodes();
        }

        parseTraceNodes(trace, nodeList);
	
		return trace;
	}

    public Map<String, TimedArcPetriNetTrace> parseTraces(BufferedReader reader) {
        Map<String, TimedArcPetriNetTrace> traces = new LinkedHashMap<>();

        Document document = readerToDocument(reader);

        if (document == null) return null;

        NodeList nodeList = document.getElementsByTagName("trace");

        for (int i = 0; i < nodeList.getLength(); ++i){
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element)node;

                String traceName = element.getAttribute("name");
                TimedArcPetriNetTrace trace = new TimedArcPetriNetTrace(true);

                NodeList childNodes = element.getChildNodes();
                parseTraceNodes(trace, childNodes);

                traces.put(traceName, trace);
            }
        }

        return traces;
    }

    private void parseTraceNodes(TimedArcPetriNetTrace trace, NodeList childNodes) {
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node childNode = childNodes.item(j);
            if (childNode instanceof Element) {
                Element childElement = (Element)childNode;
                String tag = childElement.getTagName();
                if (tag.equals("transition")) {
                    PetriNetStep step;
                    if (useExplicitSearch) {
                        step = parseColoredTransitionStep(childElement);
                    } else {
                        step = parseTransitionStep(childElement);
                    }

                    trace.add(step);
                } else if (tag.equals("delay")) {
                    if (childElement.getTextContent().equals("forever")){
                        trace.setTraceType(TraceType.EG_DELAY_FOREVER);
                    } else {
                        TimeDelayStep step = parseTimeDelay(childElement);
                        trace.add(step);
                    }
                } else if (tag.equals("loop")) {
                    trace.nextIsLoop();
                    trace.setTraceType(TraceType.EG_LOOP);
                } else if (tag.equals("marking")) {
                    LocalTimedMarking newMarking = VerifyTAPNMarkingParser.parseMarking(tapn, childElement);
                    ColoredTransitionStep lastStep = (ColoredTransitionStep)trace.getLastStep();
                    if (lastStep != null) {
                        lastStep.setPostMarking(newMarking);
                    }
                }
            }
        }
    }

    private Document readerToDocument(BufferedReader reader) {
        Document document = null;
        Pattern missingQueryIndexPattern = Pattern.compile("Missing query-indexes for query-file \\(which is identified as XML-format\\), assuming only first query is to be verified");
        try {
            StringBuilder allContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Trace")) continue;
                Matcher matcher = missingQueryIndexPattern.matcher(line);
                if (matcher.find()) continue;

                allContent.append(line);
                allContent.append(System.lineSeparator());
            }

            String fullContent = allContent.toString();
            String xml;

            int startTraceList = fullContent.indexOf("<trace-list>");
            int startTrace = fullContent.indexOf("<trace>");
            if (startTraceList != -1) {
                int endTraceList = fullContent.lastIndexOf("</trace-list>");
                if (endTraceList != -1 && endTraceList > startTraceList) {
                    xml = fullContent.substring(startTraceList, endTraceList + 13);
                } else {
                    return null;
                }
            } else if (startTrace != -1) {
                int endTrace = fullContent.lastIndexOf("</trace>");
                if (endTrace != -1 && endTrace > startTrace) {
                    xml = fullContent.substring(startTrace, endTrace + 8);
                } else {
                    return null;
                }
            } else {
                return null;
            }

            document = loadDocument(xml);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return document;
    }

	public void setTraceToParse(String traceName) {
	    this.traceNameToParse = traceName;
    }
    public String getTraceNameToParse() {
	    return this.traceNameToParse;
    }

	private TimedTransitionStep parseTransitionStep(Element element) {
		TimedTransition transition = tapn.getTransitionByName(element.getAttribute("id"));
		NodeList tokenNodes = element.getChildNodes();
		List<TimedToken> consumedTokens = new ArrayList<TimedToken>(tokenNodes.getLength());
		for(int i = 0; i < tokenNodes.getLength(); i++){
			Node node = tokenNodes.item(i);
			if(node instanceof Element){
				Element tokenElement = (Element)node;

				TimedPlace place = tapn.getPlaceByName(tokenElement.getAttribute("place"));
                
				BigDecimal age = new BigDecimal(tokenElement.getAttribute("age"));
				boolean greaterThanOrEqual = Boolean.parseBoolean(tokenElement.getAttribute("greaterThanOrEqual"));
				consumedTokens.add(new TraceToken(place, age, greaterThanOrEqual, ColorType.COLORTYPE_DOT.getFirstColor()));
			}
		}
		return new TimedTransitionStep(transition, consumedTokens);
	}

    private ColoredTransitionStep parseColoredTransitionStep(Element element) {
        ColorBindingParser colorBindingParser = new ColorBindingParser();
        Node bindingsNode = element.getElementsByTagName("bindings").item(0);
        TimedTransition transition = tapn.getTransitionByName(element.getAttribute("id"));
        Map<Variable, Color> bindings = colorBindingParser.parseBindingsForSingleTransition(nodeToString(bindingsNode), transition.name(), tapn.parentNetwork());
        return new ColoredTransitionStep(transition, bindings);
    }

    private static String nodeToString(Node node) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

	private TimeDelayStep parseTimeDelay(Element element) {
		return new TimeDelayStep(new BigDecimal(element.getTextContent()));
	}

	private Document loadDocument(String xml) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xml)));
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
			return null;
		}
	}
}
