package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dk.aau.cs.model.CPN.ColorType;
<<<<<<< HEAD
import org.w3c.dom.*;
=======
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
>>>>>>> origin/cpn
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import dk.aau.cs.model.NTA.trace.TraceToken;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;


public class VerifyTAPNTraceParser {

	private final TimedArcPetriNet tapn;
<<<<<<< HEAD
	private String traceNameToParse;
=======
>>>>>>> origin/cpn
	
	public VerifyTAPNTraceParser(TimedArcPetriNet tapn) {
		this.tapn = tapn;
	}

	public TimedArcPetriNetTrace parseTrace(BufferedReader reader) {
		TimedArcPetriNetTrace trace = new TimedArcPetriNetTrace(true);
		
		Document document = loadDocument(reader);
		
		if(document == null) return null;
<<<<<<< HEAD

        NodeList nodeList = null;

		if(traceNameToParse != null) {
		    NodeList childNodes = document.getElementsByTagName("trace-list").item(0).getChildNodes();
		    for(int i = 0; i < childNodes.getLength(); i++) {
		        NamedNodeMap nodeAttribute = childNodes.item(i).getAttributes();

                if(nodeAttribute != null && nodeAttribute.item(0).getNodeValue().equals(traceNameToParse)) {
                    nodeList = childNodes.item(i).getChildNodes();
                    trace.setTraceName(traceNameToParse);
                    break;
                }
            }
        }
		
		if(nodeList == null) {
            nodeList = document.getElementsByTagName("trace").item(0).getChildNodes();
        }

=======
		
		NodeList nodeList = document.getElementsByTagName("trace").item(0).getChildNodes();
>>>>>>> origin/cpn
		for(int i = 0; i < nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			if(node instanceof Element){
				Element element = (Element)node;

				if(element.getTagName().equals("transition")){
					TimedTransitionStep step = parseTransitionStep(element);
					trace.add(step);
				}else if(element.getTagName().equals("delay")){
					if(element.getTextContent().equals("forever")){
						trace.setTraceType(TraceType.EG_DELAY_FOREVER);
					} else {
						TimeDelayStep step = parseTimeDelay(element);
						trace.add(step);
					}
				}else if(element.getTagName().equals("loop")){
					trace.nextIsLoop();
					trace.setTraceType(TraceType.EG_LOOP);
				}
			}
		}
	
		return trace;
	}

<<<<<<< HEAD
	public void setTraceToParse(String traceName) {
	    this.traceNameToParse = traceName;
    }
    public String getTraceNameToParse() {
	    return this.traceNameToParse;
    }

=======
>>>>>>> origin/cpn
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

	private TimeDelayStep parseTimeDelay(Element element) {
		return new TimeDelayStep(new BigDecimal(element.getTextContent()));
	}

	private Document loadDocument(BufferedReader reader) {
		try {
<<<<<<< HEAD
			//reader.readLine(); // first line is "Trace:", so ignore it
=======
			reader.readLine(); // first line is "Trace:", so ignore it
>>>>>>> origin/cpn
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler() {
				
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					throw exception;
				}
				
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					throw exception;
				}
				
				@Override
				public void error(SAXParseException exception) throws SAXException {
					throw exception;
				}
			});
			return builder.parse(new InputSource(reader));
		} catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
			return null;
		}
	}
}
