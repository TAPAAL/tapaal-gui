package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.ColoredTransitionStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;

public class VerifyPNTraceParser {
	private final TimedArcPetriNet tapn;
	private String traceNameToParse;
	
	public VerifyPNTraceParser(TimedArcPetriNet tapn) {
		this.tapn = tapn;
	}

	public TimedArcPetriNetTrace parseTrace(BufferedReader reader) {
		TimedArcPetriNetTrace trace = new TimedArcPetriNetTrace(false);

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
                TimedArcPetriNetTrace trace = new TimedArcPetriNetTrace(false);

                NodeList childNodes = element.getChildNodes();
                parseTraceNodes(trace, childNodes);

                traces.put(traceName, trace);
            }
        }

        return traces;
    }

    private void parseTraceNodes(TimedArcPetriNetTrace trace, NodeList childNodes) {
        for (int j = 0; j < childNodes.getLength(); ++j){
            Node childNode = childNodes.item(j);
            if (childNode instanceof Element){
                Element childElement = (Element)childNode;
                if (childElement.getTagName().equals("marking")) {
                    parseMarking(childElement);
                } else if (childElement.getTagName().equals("transition")) {
                    trace.add(parseTransitionStep(childElement));
                }
            }
        }
    }

    private Map<String, Map<String, Integer>> parseMarking(Element markingElement) {
        Map<String, Map<String, Integer>> markingMap = new LinkedHashMap<>();
        NodeList placeNodes = markingElement.getElementsByTagName("place");
        for (int i = 0; i < placeNodes.getLength(); ++i) {
            Element placeElement = (Element)placeNodes.item(i);
            String placeId = placeElement.getAttribute("id");
            
            Map<String, Integer> colorCounts = new LinkedHashMap<>();
            markingMap.put(placeId, colorCounts);
            
            NodeList tokenNodes = placeElement.getElementsByTagName("token");
            for (int j = 0; j < tokenNodes.getLength(); j++) {
                Element tokenElement = (Element) tokenNodes.item(j);
                int count = Integer.parseInt(tokenElement.getAttribute("count"));
                NodeList colorNodes = tokenElement.getElementsByTagName("color");
                for (int k = 0; k < colorNodes.getLength(); ++k) {
                    Element colorElement = (Element)colorNodes.item(k);
                    String colorName = colorElement.getTextContent();
                    colorCounts.put(colorName, count);
                }
            }
        }
        
        return markingMap;
    }

    private ColoredTransitionStep parseTransitionStep(Element element) {
        ColorBindingParser colorBindingParser = new ColorBindingParser();
        Node bindingsNode = element.getElementsByTagName("bindings").item(0);
        Map<String, List<String>> bindings = colorBindingParser.parseBindings(nodeToString(bindingsNode));
        TimedTransition transition = tapn.getTransitionByName(element.getAttribute("id"));
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

    private Document readerToDocument(BufferedReader reader) {
        Document document = null;
        Pattern missingQueryIndexPattern = Pattern.compile("Missing query-indexes for query-file \\(which is identified as XML-format\\), assuming only first query is to be verified");
        try {
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Trace")) continue;
                Matcher matcher = missingQueryIndexPattern.matcher(line);
                if (matcher.find()) continue;
                sb.append(line);
                sb.append(System.lineSeparator());
            }

            String xml = sb.toString();
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
	    return traceNameToParse;
    }

	private Document loadDocument(String xml) {
		try {
			// Check if valid xml
			int startTrace = xml.indexOf("<trace>");
			int startTraceList = xml.indexOf("<trace-list>");
			if (startTrace == -1 && startTraceList == -1) return null;

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xml)));
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
			return null;
		}
	}
}

