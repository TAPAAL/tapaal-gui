package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

	private TimedArcPetriNet tapn;
	
	public VerifyTAPNTraceParser(TimedArcPetriNet tapn) {
		this.tapn = tapn;
	}

	public TimedArcPetriNetTrace parseTrace(BufferedReader reader) {
		TimedArcPetriNetTrace trace = new TimedArcPetriNetTrace(true);
		
		Document document = loadDocument(reader);
		
		if(document == null) return null;
		
		NodeList nodeList = document.getElementsByTagName("trace").item(0).getChildNodes();
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
				consumedTokens.add(new TraceToken(place, age, greaterThanOrEqual));
			}
		}
		return new TimedTransitionStep(transition, consumedTokens);
	}

	private TimeDelayStep parseTimeDelay(Element element) {
		return new TimeDelayStep(new BigDecimal(element.getTextContent()));
	}

	private Document loadDocument(BufferedReader reader) {
		try {
			reader.readLine(); // first line is "Trace:", so ignore it
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
		} catch (ParserConfigurationException e) {
			return null;
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
}
