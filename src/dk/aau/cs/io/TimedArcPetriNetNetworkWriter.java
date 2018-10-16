package dk.aau.cs.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetWriter;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.QueryCategory;
import pipe.dataLayer.Template;
import pipe.gui.graphicElements.AnnotationNote;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import pipe.gui.widgets.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.util.Require;

public class TimedArcPetriNetNetworkWriter implements NetWriter {

	private Iterable<Template> templates;
	private Iterable<TAPNQuery> queries;
	private Iterable<Constant> constants;
	private final TimedArcPetriNetNetwork network;

	public TimedArcPetriNetNetworkWriter(
			TimedArcPetriNetNetwork network, 
			Iterable<Template> templates,
			Iterable<TAPNQuery> queries, 
			Iterable<Constant> constants) {
		this.network = network;
		this.templates = templates;
		this.queries = queries;
		this.constants = constants;
	}
	
	public ByteArrayOutputStream savePNML() throws IOException, ParserConfigurationException, DOMException, TransformerConfigurationException, TransformerException {
		Document document = null;
		Transformer transformer = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		// Build a Petri Net XML Document
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		document = builder.newDocument();

		Element pnmlRootNode = document.createElement("pnml"); // PNML Top Level
		document.appendChild(pnmlRootNode);
		Attr pnmlAttr = document.createAttribute("xmlns"); // PNML "xmlns"
		pnmlAttr.setValue("http://www.informatik.hu-berlin.de/top/pnml/ptNetb");
		pnmlRootNode.setAttributeNode(pnmlAttr);

		appendSharedPlaces(document, pnmlRootNode);
		appendSharedTransitions(document, pnmlRootNode);
		appendConstants(document, pnmlRootNode);
		appendTemplates(document, pnmlRootNode);
		appendQueries(document, pnmlRootNode);
		appendDefaultBound(document, pnmlRootNode);

		document.normalize();
		// Create Transformer with XSL Source File
		transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(document);

		StreamResult result = new StreamResult(os);
		transformer.transform(source, result);
		
		return os;
	}

	public void savePNML(File file) throws IOException, ParserConfigurationException, DOMException, TransformerConfigurationException, TransformerException {
		Require.that(file != null, "Error: file to save to was null");
		
		try {
			ByteArrayOutputStream os = savePNML();
			FileOutputStream fs = new FileOutputStream(file);
			fs.write(os.toByteArray());
			fs.close();
		} catch (ParserConfigurationException e) {
			System.out
					.println("ParserConfigurationException thrown in savePNML() "
							+ ": dataLayerWriter Class : dataLayer Package: filename=\"");
		} catch (DOMException e) {
			System.out
					.println("DOMException thrown in savePNML() "
							+ ": dataLayerWriter Class : dataLayer Package: filename=\""
							+ file.getCanonicalPath() + "\" transformer=\"");
		} catch (TransformerConfigurationException e) {
			System.out
					.println("TransformerConfigurationException thrown in savePNML() "
							+ ": dataLayerWriter Class : dataLayer Package: filename=\""
							+ file.getCanonicalPath()
							+ "\" transformer=\"");
		} catch (TransformerException e) {
			System.out
					.println("TransformerException thrown in savePNML() : dataLayerWriter Class : dataLayer Package: filename=\""
							+ file.getCanonicalPath()
							+ "\"" + e);
		}
	}
	
	private void appendDefaultBound(Document document, Element root) {
		Element element = document.createElement("k-bound");
		element.setAttribute("bound", network.getDefaultBound() + "");
		root.appendChild(element);
	}
	
	private void appendSharedPlaces(Document document, Element root) {
		for(SharedPlace place : network.sharedPlaces()){
			Element element = document.createElement("shared-place");
			element.setAttribute("invariant", place.invariant().toString());
			element.setAttribute("name", place.name());
			element.setAttribute("initialMarking", String.valueOf(place.numberOfTokens()));
			root.appendChild(element);
		}
	}

	private void appendSharedTransitions(Document document, Element root) {
		for(SharedTransition transition : network.sharedTransitions()){
			Element element = document.createElement("shared-transition");
			element.setAttribute("name", transition.name());
			element.setAttribute("urgent", transition.isUrgent()?"true":"false");
			root.appendChild(element);
		}
	}

	private void appendConstants(Document document, Element root) {
		for (Constant constant : constants) {
			Element elem = createConstantElement(constant, document);
			root.appendChild(elem);
		}
	}
	
	private Element createConstantElement(Constant constant, Document document) {
		Require.that(constant != null, "Error: constant was null");
		Require.that(document != null, "Error: document was null");
		
		Element constantElement = document.createElement("constant");
		
		constantElement.setAttribute("name", constant.name());
		constantElement.setAttribute("value", String.valueOf(constant.value()));
	
		return constantElement;
	}

	private void appendTemplates(Document document, Element root) {
		for (Template tapn : templates) {
			DataLayer guiModel = tapn.guiModel();

			Element NET = document.createElement("net");
			root.appendChild(NET);
			Attr netAttrId = document.createAttribute("id");
			netAttrId.setValue(tapn.model().name());
			NET.setAttributeNode(netAttrId);
			
			Attr netAttrActive = document.createAttribute("active");
			netAttrActive.setValue("" + tapn.isActive());
			NET.setAttributeNode(netAttrActive);

			Attr netAttrType = document.createAttribute("type");
			switch (guiModel.netType()) {
			case UNTIMED:
				netAttrType.setValue("Untimed P/T net");
				break;
			default:
				netAttrType.setValue("P/T net");
			}
			NET.setAttributeNode(netAttrType);

			appendAnnotationNotes(document, guiModel, NET);
			appendPlaces(document, guiModel, NET);
			appendTransitions(document, guiModel, NET);
			appendArcs(document, guiModel, NET);
		}
	}

	private void appendAnnotationNotes(Document document, DataLayer guiModel, Element NET) {
		AnnotationNote[] labels = guiModel.getLabels();
		for (int i = 0; i < labels.length; i++) {
			NET.appendChild(createAnnotationNoteElement(labels[i], document));
		}
	}

	private void appendPlaces(Document document, DataLayer guiModel, Element NET) {
		Place[] places = guiModel.getPlaces();
		for (int i = 0; i < places.length; i++) {
			NET.appendChild(createPlaceElement((TimedPlaceComponent) places[i], guiModel, document));
		}
	}

	private void appendTransitions(Document document, DataLayer guiModel, Element NET) {
		Transition[] transitions = guiModel.getTransitions();
		for (int i = 0; i < transitions.length; i++) {
			NET.appendChild(createTransitionElement((TimedTransitionComponent)transitions[i],	document));
		}
	}
	
	private void appendArcs(Document document, DataLayer guiModel, Element NET) {
		Arc[] arcs = guiModel.getArcs();
		for (int i = 0; i < arcs.length; i++) {
			Element newArc = createArcElement(arcs[i], guiModel, document);

			int arcPoints = arcs[i].getArcPath().getArcPathDetails().length;
			String[][] point = arcs[i].getArcPath().getArcPathDetails();
			for (int j = 0; j < arcPoints; j++) {
				newArc.appendChild(createArcPoint(point[j][0],
						point[j][1], point[j][2], document, j));
			}
			NET.appendChild(newArc);
		}
	}
	
	private void appendQueries(Document document, Element root) {
		for (TAPNQuery query : queries) {
			Element newQuery;
			if (query.getCategory() == QueryCategory.CTL){
				newQuery = createCTLQueryElement(query, document);
			} else {
				newQuery = createQueryElement(query, document);
			}
			root.appendChild(newQuery);
		}
	}

	private Element createQueryElement(TAPNQuery query, Document document) {
		Require.that(query != null, "Error: query was null");
		Require.that(document != null, "Error: document was null");
		
		Element queryElement = document.createElement("query");
	
		queryElement.setAttribute("name", query.getName());
		queryElement.setAttribute("capacity", "" + query.getCapacity());
		queryElement.setAttribute("query", query.getProperty().toString());
		queryElement.setAttribute("traceOption", ""	+ query.getTraceOption());
		queryElement.setAttribute("searchOption", "" + query.getSearchOption());
		queryElement.setAttribute("hashTableSize", "" + query.getHashTableSize());
		queryElement.setAttribute("extrapolationOption", "" + query.getExtrapolationOption());
		queryElement.setAttribute("reductionOption", ""	+ query.getReductionOption());
		queryElement.setAttribute("symmetry", "" + query.useSymmetry());
		queryElement.setAttribute("gcd", "" + query.useGCD());
		queryElement.setAttribute("timeDarts", "" + query.useTimeDarts());
		queryElement.setAttribute("pTrie", "" + query.usePTrie());
		queryElement.setAttribute("discreteInclusion", String.valueOf(query.discreteInclusion()));
		queryElement.setAttribute("active", "" + query.isActive());
		queryElement.setAttribute("inclusionPlaces", getInclusionPlacesString(query));
		queryElement.setAttribute("overApproximation", "" + query.useOverApproximation());
		queryElement.setAttribute("reduction", "" + query.useReduction());
		queryElement.setAttribute("enableOverApproximation", "" + query.isOverApproximationEnabled());
		queryElement.setAttribute("enableUnderApproximation", "" + query.isUnderApproximationEnabled());
		queryElement.setAttribute("approximationDenominator", "" + query.approximationDenominator());
		queryElement.setAttribute("useStubbornReduction", "" + query.isStubbornReductionEnabled());

		return queryElement;
	}
	
	private Element createCTLQueryElement(TAPNQuery query, Document document) {
		Require.that(query != null, "Error: query was null");
		Require.that(document != null, "Error: document was null");

		Element queryElement = document.createElement("query");

		Node queryFormula = XMLQueryStringToElement(new CTLQueryVisitor().getXMLQueryFor(query.getProperty(), query.getName()));
		queryElement.appendChild(document.importNode(queryFormula, true));
		
		queryElement.setAttribute("name", query.getName());
		queryElement.setAttribute("type", "CTL");
		queryElement.setAttribute("capacity", "" + query.getCapacity());
		queryElement.setAttribute("traceOption", ""	+ query.getTraceOption());
		queryElement.setAttribute("searchOption", "" + query.getSearchOption());
		queryElement.setAttribute("hashTableSize", "" + query.getHashTableSize());
		queryElement.setAttribute("extrapolationOption", "" + query.getExtrapolationOption());
		queryElement.setAttribute("reductionOption", ""	+ query.getReductionOption());
		queryElement.setAttribute("symmetry", "" + query.useSymmetry());
		queryElement.setAttribute("gcd", "" + query.useGCD());
		queryElement.setAttribute("timeDarts", "" + query.useTimeDarts());
		queryElement.setAttribute("pTrie", "" + query.usePTrie());
		queryElement.setAttribute("discreteInclusion", String.valueOf(query.discreteInclusion()));
		queryElement.setAttribute("active", "" + query.isActive());
		queryElement.setAttribute("inclusionPlaces", getInclusionPlacesString(query));
		queryElement.setAttribute("overApproximation", "" + query.useOverApproximation());
		queryElement.setAttribute("reduction", "" + query.useReduction());
		queryElement.setAttribute("enableOverApproximation", "" + query.isOverApproximationEnabled());
		queryElement.setAttribute("enableUnderApproximation", "" + query.isUnderApproximationEnabled());
		queryElement.setAttribute("approximationDenominator", "" + query.approximationDenominator());
		queryElement.setAttribute("algorithmOption", "" + query.getAlgorithmOption());
		queryElement.setAttribute("useSiphonTrapAnalysis", "" + query.isSiphontrapEnabled());
		queryElement.setAttribute("useQueryReduction", "" + query.isQueryReductionEnabled());
		queryElement.setAttribute("useStubbornReduction", "" + query.isStubbornReductionEnabled());
		
		return queryElement;
	}
	
	private Node XMLQueryStringToElement(String formulaString){
		
		try {
			return DocumentBuilderFactory
			    .newInstance()
			    .newDocumentBuilder()
			    .parse(new ByteArrayInputStream(formulaString.getBytes()))
			    .getDocumentElement().getElementsByTagName("formula").item(0);
		} catch (SAXException e) {
			System.out
			.println(e.toString() + " thrown in savePNML() "
					+ ": dataLayerWriter Class : dataLayer Package: filename=\"");
		} catch (IOException e) {
			System.out
			.println(e.toString() + " thrown in savePNML() "
					+ ": dataLayerWriter Class : dataLayer Package: filename=\"");
		} catch (ParserConfigurationException e) {
			System.out
			.println(e.toString() + " thrown in savePNML() "
					+ ": dataLayerWriter Class : dataLayer Package: filename=\"");
		}
			
		return null;
	}

	private String getInclusionPlacesString(TAPNQuery query) {
		if(!query.discreteInclusion() || (query.inclusionPlaces().inclusionOption() == InclusionPlacesOption.UserSpecified && query.inclusionPlaces().inclusionPlaces().isEmpty()))
			return "*NONE*";
		
		if(query.inclusionPlaces().inclusionOption() == InclusionPlacesOption.AllPlaces)
			return "*ALL*";
		
		boolean first = true;
		StringBuilder s = new StringBuilder();
		for(TimedPlace p : query.inclusionPlaces().inclusionPlaces()) {
			if(!first) s.append(',');
			
			s.append(p.toString());
			if(first) first = false;
		}
		
		return s.toString();
	}

	private Element createPlaceElement(TimedPlaceComponent inputPlace, DataLayer guiModel, Document document) {
		Require.that(inputPlace != null, "Error: inputPlace was null");
		Require.that(guiModel != null, "Error: guiModel was null");
		Require.that(document != null, "Error: document was null");
		
		Element placeElement = document.createElement("place");

		placeElement.setAttribute("positionX", (inputPlace.getPositionXObject() != null ? String.valueOf(inputPlace.getPositionXObject()) : ""));
		placeElement.setAttribute("positionY", (inputPlace.getPositionYObject() != null ? String.valueOf(inputPlace.getPositionYObject())	: ""));
		placeElement.setAttribute("name", inputPlace.underlyingPlace().name());
		placeElement.setAttribute("displayName", (inputPlace.getAttributesVisible() ? "true" : "false"));
		placeElement.setAttribute("id", (inputPlace.getId() != null ? inputPlace.getId() : "error"));
		placeElement.setAttribute("nameOffsetX", (inputPlace.getNameOffsetXObject() != null ? String.valueOf(inputPlace.getNameOffsetXObject()) : ""));
		placeElement.setAttribute("nameOffsetY", (inputPlace.getNameOffsetYObject() != null ? String.valueOf(inputPlace.getNameOffsetYObject()) : ""));
		placeElement.setAttribute("initialMarking", ((Integer) inputPlace.getNumberOfTokens() != null ? String.valueOf((Integer) inputPlace.getNumberOfTokens()) : "0"));
		placeElement.setAttribute("invariant", inputPlace.underlyingPlace().invariant().toString());

		return placeElement;
	}


	private Element createAnnotationNoteElement(AnnotationNote inputLabel, Document document) {
		Require.that(inputLabel != null, "Error: inputLabel was null");
		Require.that(document != null, "Error: document was null");
		
		Element labelElement = document.createElement("labels");

		labelElement.setAttribute("positionX", (inputLabel.getOriginalX() >= 0.0 ? String.valueOf(inputLabel.getOriginalX()) : ""));
		labelElement.setAttribute("positionY", (inputLabel.getOriginalY() >= 0.0 ? String.valueOf(inputLabel.getOriginalY()) : ""));
		labelElement.setAttribute("width", (inputLabel.getNoteWidth() >= 0.0 ? String.valueOf(inputLabel.getNoteWidth()) : ""));
		labelElement.setAttribute("height", (inputLabel.getNoteHeight() >= 0.0 ? String.valueOf(inputLabel.getNoteHeight()) : ""));
		labelElement.setAttribute("border", String.valueOf(inputLabel.isShowingBorder()));
		Text text = document.createTextNode(inputLabel.getNoteText() != null ? inputLabel.getNoteText() : "");
		labelElement.appendChild(text);
		//labelElement.setAttribute("text", );
		
		return labelElement;
	}

	private Element createTransitionElement(TimedTransitionComponent inputTransition, Document document) {
		Require.that(inputTransition != null, "Error: inputTransition was null");
		Require.that(document != null, "Error: document was null");
		
		Element transitionElement = document.createElement("transition");

		transitionElement.setAttribute("positionX", (inputTransition.getPositionXObject() != null ? String.valueOf(inputTransition.getPositionXObject()) : ""));
		transitionElement.setAttribute("positionY",	(inputTransition.getPositionYObject() != null ? String.valueOf(inputTransition.getPositionYObject()) : ""));
		transitionElement.setAttribute("nameOffsetX", (inputTransition.getNameOffsetXObject() != null ? String.valueOf(inputTransition.getNameOffsetXObject()) : ""));
		transitionElement.setAttribute("nameOffsetY", (inputTransition.getNameOffsetYObject() != null ? String.valueOf(inputTransition.getNameOffsetYObject()) : ""));
		transitionElement.setAttribute("name", inputTransition.underlyingTransition().name());
		transitionElement.setAttribute("displayName", (inputTransition.getAttributesVisible() ? "true" : "false"));
		transitionElement.setAttribute("id", (inputTransition.getId() != null ? inputTransition.getId()	: "error"));
		transitionElement.setAttribute("infiniteServer", "false");
		transitionElement.setAttribute("angle", String.valueOf(inputTransition.getAngle()));
		transitionElement.setAttribute("priority", "0");
		transitionElement.setAttribute("urgent", inputTransition.underlyingTransition().isUrgent()?"true":"false");

		return transitionElement;
	}

	private Element createArcElement(Arc inputArc, DataLayer guiModel, Document document) {
		Require.that(inputArc != null, "Error: inputArc was null");
		Require.that(guiModel != null, "Error: guiModel was null");
		Require.that(document != null, "Error: document was null");
		
		Element arcElement = document.createElement("arc");
		
		arcElement.setAttribute("id", (inputArc.getId() != null ? inputArc.getId() : "error"));
		arcElement.setAttribute("source", (inputArc.getSource().getId() != null ? inputArc.getSource().getId() : ""));
		arcElement.setAttribute("target", (inputArc.getTarget().getId() != null ? inputArc.getTarget().getId() : ""));
		arcElement.setAttribute("nameOffsetX", (inputArc.getNameOffsetXObject() != null ? String.valueOf(inputArc.getNameOffsetXObject()) : ""));
		arcElement.setAttribute("nameOffsetY", (inputArc.getNameOffsetYObject() != null ? String.valueOf(inputArc.getNameOffsetYObject()) : ""));
		
		if (inputArc instanceof TimedOutputArcComponent) {
			if (inputArc instanceof TimedInputArcComponent) {
				arcElement.setAttribute("type", getInputArcTypeAsString((TimedInputArcComponent)inputArc));
				arcElement.setAttribute("inscription", getGuardAsString((TimedInputArcComponent)inputArc));	
				arcElement.setAttribute("weight", ((TimedInputArcComponent)inputArc).getWeight().nameForSaving(true)+"");
			} else {
				arcElement.setAttribute("type", "normal");
				arcElement.setAttribute("inscription", "1");
				arcElement.setAttribute("weight", ((TimedOutputArcComponent)inputArc).getWeight().nameForSaving(true)+"");
			}
		} 
		return arcElement;
	}

	private String getInputArcTypeAsString(TimedInputArcComponent inputArc) {
		if (inputArc instanceof TimedTransportArcComponent) {
			return "transport";
		} else if (inputArc instanceof TimedInhibitorArcComponent) {
			return "tapnInhibitor";
		} else {
			return "timed";
		}
	}

	private String getGuardAsString(TimedInputArcComponent inputArc) {
		if(inputArc instanceof TimedTransportArcComponent)
			return inputArc.getGuardAsString() + ":" + ((TimedTransportArcComponent) inputArc).getGroupNr();
		else {
			return inputArc.getGuardAsString();
		}
	}
                
	private Element createArcPoint(String x, String y, String type, Document document, int id) {
		Require.that(document != null, "Error: document was null");
		Element arcPoint = document.createElement("arcpath");
		
		arcPoint.setAttribute("id", String.valueOf(id));
		arcPoint.setAttribute("xCoord", x);
		arcPoint.setAttribute("yCoord", y);
		arcPoint.setAttribute("arcPointType", type);

		return arcPoint;
	}

}
