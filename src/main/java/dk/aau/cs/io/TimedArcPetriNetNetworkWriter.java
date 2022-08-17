package dk.aau.cs.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.TCTL.visitors.LTLQueryVisitor;
import net.tapaal.gui.petrinet.TAPNLens;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.petrinet.graphicElements.AnnotationNote;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.Transition;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;
import net.tapaal.gui.petrinet.verification.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import dk.aau.cs.util.Require;

public class TimedArcPetriNetNetworkWriter implements NetWriter {

	private final Iterable<Template> templates;
	private final Iterable<TAPNQuery> queries;
	private final Iterable<Constant> constants;
	private final TimedArcPetriNetNetwork network;
    private final writeTACPN writeTACPN;
    private boolean secondTransport = false;
    private int transporCountID = 0;
    private final TAPNLens lens;

    public TimedArcPetriNetNetworkWriter(
			TimedArcPetriNetNetwork network, 
			Iterable<Template> templates,
			Iterable<TAPNQuery> queries,
			Iterable<Constant> constants
    ) {
		this.network = network;
		this.templates = templates;
		this.queries = queries;
		this.constants = constants;
        writeTACPN = new writeTACPN(network);
		this.lens = TAPNLens.Default;
	}

    public TimedArcPetriNetNetworkWriter(
        TimedArcPetriNetNetwork network,
        Iterable<Template> templates,
        Iterable<TAPNQuery> queries,
        Iterable<Constant> constants,
        TAPNLens lens
    ) {
        this.network = network;
        this.templates = templates;
        this.queries = queries;
        this.constants = constants;
        writeTACPN = new writeTACPN(network);
        this.lens = lens;
    }
	
	public ByteArrayOutputStream savePNML() throws ParserConfigurationException, DOMException, TransformerException {
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

        writeTACPN.appendDeclarations(document, pnmlRootNode);

        appendSharedPlaces(document, pnmlRootNode);
		appendSharedTransitions(document, pnmlRootNode);
		appendConstants(document, pnmlRootNode);
		appendTemplates(document, pnmlRootNode);

        appendQueries(document, pnmlRootNode);
		appendFeature(document, pnmlRootNode);

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

	public void savePNML(File file) throws IOException, ParserConfigurationException, DOMException, TransformerException {
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
			e.printStackTrace();
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

    private void appendFeature(Document document, Element root) {
        String isTimed = "true";
        String isGame = "true";
        String isColored = "true";
        if (!lens.isTimed()) {
            isTimed = "false";
        }
        if (!lens.isGame()) {
            isGame = "false";
        }
        if(!lens.isColored()){
            isColored = "false";
        }

        root.appendChild(createFeatureElement(isTimed, isGame, isColored, document));
    }

    private Element createFeatureElement(String isTimed, String isGame, String isColored, Document document) {
        Require.that(document != null, "Error: document was null");
        Element feature = document.createElement("feature");

        feature.setAttribute("isTimed", isTimed);
        feature.setAttribute("isGame", isGame);
        feature.setAttribute("isColored", isColored);

        return feature;
    }
	
	private void appendSharedPlaces(Document document, Element root) {
		for(SharedPlace place : network.sharedPlaces()){
			Element element = document.createElement("shared-place");
			element.setAttribute("invariant", place.invariant().toString());
			element.setAttribute("name", place.name());
			element.setAttribute("initialMarking", String.valueOf(place.numberOfTokens()));
			createColoredInvariants(place, document, element);
            writeTACPN.appendColoredPlaceDependencies(place, document, element);

            root.appendChild(element);
		}
	}

	private void appendSharedTransitions(Document document, Element root) {
		for(SharedTransition transition : network.sharedTransitions()){
			Element element = document.createElement("shared-transition");
			element.setAttribute("name", transition.name());
			element.setAttribute("urgent", transition.isUrgent()?"true":"false");
            element.setAttribute("player", transition.isUncontrollable() ? "1" : "0");
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
			netAttrType.setValue("P/T net");
			NET.setAttributeNode(netAttrType);

			appendAnnotationNotes(document, guiModel, NET);
			appendPlaces(document, guiModel, NET);
			appendTransitions(document, guiModel, NET);
			appendArcs(document, guiModel, NET);
		}
	}

	private void appendAnnotationNotes(Document document, DataLayer guiModel, Element NET) {
		AnnotationNote[] labels = guiModel.getLabels();
		for (AnnotationNote label : labels) {
			NET.appendChild(createAnnotationNoteElement(label, document));
		}
	}

	private void appendPlaces(Document document, DataLayer guiModel, Element NET) {
		Place[] places = guiModel.getPlaces();
		for (Place place : places) {
			NET.appendChild(createPlaceElement((TimedPlaceComponent) place, guiModel, document));
		}
	}

	private void appendTransitions(Document document, DataLayer guiModel, Element NET) {
		Transition[] transitions = guiModel.getTransitions();
		for (Transition transition : transitions) {
			NET.appendChild(createTransitionElement((TimedTransitionComponent) transition, document));
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
			if (query.getCategory() == QueryCategory.LTL){
			    newQuery = createLTLQueryElement(query, document);
            } else {
                newQuery = createCTLQueryElement(query, document);
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
        queryElement.setAttribute("useTarOption", "" + query.isTarOptionEnabled());
        queryElement.setAttribute("partitioning", "" + query.usePartitioning());
        queryElement.setAttribute("colorFixpoint", "" + query.useColorFixpoint());
        queryElement.setAttribute("symmetricVars", "" + query.useSymmetricVars());

		return queryElement;
	}
	
	private Element createCTLQueryElement(TAPNQuery query, Document document) {
		Require.that(query != null, "Error: query was null");
		Require.that(document != null, "Error: document was null");

		Element queryElement = document.createElement("query");

		CTLQueryVisitor ctlQueryVisitor = new CTLQueryVisitor();
		ctlQueryVisitor.buildXMLQuery(query.getProperty(), query.getName(), false);

		Node queryFormula = XMLQueryStringToElement(ctlQueryVisitor.getXMLQuery().toString());
		queryElement.appendChild(document.importNode(queryFormula, true));
		
		queryElement.setAttribute("name", query.getName());
		queryElement.setAttribute("type", query.getCategory().toString());
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
		queryElement.setAttribute("useTarOption", "" + query.isTarOptionEnabled());
        queryElement.setAttribute("useTarjan", "" + query.isTarjan());
		queryElement.setAttribute("partitioning", "" + query.usePartitioning());
		queryElement.setAttribute("colorFixpoint", "" + query.useColorFixpoint());
        queryElement.setAttribute("symmetricVars", "" + query.useSymmetricVars());

        return queryElement;
	}

    private Element createLTLQueryElement(TAPNQuery query, Document document) {
        Require.that(query != null, "Error: query was null");
        Require.that(document != null, "Error: document was null");

        Element queryElement = document.createElement("query");

        LTLQueryVisitor ltlQueryVisitor = new LTLQueryVisitor();
        ltlQueryVisitor.buildXMLQuery(query.getProperty(), query.getName());

        Node queryFormula = XMLQueryStringToElement(ltlQueryVisitor.getXMLQuery().toString());
        queryElement.appendChild(document.importNode(queryFormula, true));

        queryElement.setAttribute("name", query.getName());
        queryElement.setAttribute("type", query.getCategory().toString());
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
        queryElement.setAttribute("useTarOption", "" + query.isTarOptionEnabled());
        queryElement.setAttribute("useTarjan", "" + query.isTarjan());

        return queryElement;
    }
	
	private Node XMLQueryStringToElement(String formulaString){
		
		try {
			return DocumentBuilderFactory
			    .newInstance()
			    .newDocumentBuilder()
			    .parse(new ByteArrayInputStream(formulaString.getBytes()))
			    .getDocumentElement().getElementsByTagName("formula").item(0);
		} catch (SAXException | ParserConfigurationException | IOException e) {
			System.out.println(e + " thrown in savePNML() "
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

		placeElement.setAttribute("positionX", String.valueOf(inputPlace.getOriginalX()));
		placeElement.setAttribute("positionY", String.valueOf(inputPlace.getOriginalY()));
		placeElement.setAttribute("name", inputPlace.underlyingPlace().name());
		placeElement.setAttribute("displayName", (inputPlace.getAttributesVisible() ? "true" : "false"));
		placeElement.setAttribute("id", (inputPlace.getId() != null ? inputPlace.getId() : "error"));
		placeElement.setAttribute("nameOffsetX", String.valueOf(inputPlace.getNameOffsetX()));
		placeElement.setAttribute("nameOffsetY", String.valueOf(inputPlace.getNameOffsetY()));
		placeElement.setAttribute("initialMarking", ((Integer) inputPlace.getNumberOfTokens() != null ? String.valueOf((Integer) inputPlace.getNumberOfTokens()) : "0"));
		placeElement.setAttribute("invariant", inputPlace.underlyingPlace().invariant().toString());
        writeTACPN.appendColoredPlaceDependencies(inputPlace.underlyingPlace(), document, placeElement);

        //In colored nets the "invariant" tag describes the general invariant
        //But we can also have invariants for specific colors
        createColoredInvariants(inputPlace.underlyingPlace(), document, placeElement);
        return placeElement;
	}
    private void createColoredInvariants(TimedPlace inputPlace, Document document, Element placeElement) {
        List<ColoredTimeInvariant> ctiList = inputPlace.getCtiList();

        for (ColoredTimeInvariant coloredTimeInvariant : ctiList) {
            Element invariant = document.createElement("colorinvariant");
            Element inscription = document.createElement("inscription");
            Element colortype = document.createElement("colortype");
            colortype.setAttribute("name", coloredTimeInvariant.getColor().getColorType().getName());
            if (coloredTimeInvariant.equalsOnlyColor(ColoredTimeInvariant.LESS_THAN_INFINITY_AND_STAR)) {
                placeElement.setAttribute("inscription", coloredTimeInvariant.getInvariantString());
            } else {
                if (coloredTimeInvariant.getColor().getTuple() != null) {
                    for (Color color : coloredTimeInvariant.getColor().getTuple()) {
                        Element colorEle = document.createElement("color");
                        colorEle.setAttribute("value", color.getColorName());
                        colortype.appendChild(colorEle);
                    }
                } else {
                    Element colorEle = document.createElement("color");
                    colorEle.setAttribute("value", coloredTimeInvariant.getColor().getColorName());
                    colortype.appendChild(colorEle);
                }
                inscription.setAttribute("inscription", coloredTimeInvariant.getInvariantString());
                invariant.appendChild(inscription);
                invariant.appendChild(colortype);
                placeElement.appendChild(invariant);
            }
        }
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

		transitionElement.setAttribute("positionX", String.valueOf(inputTransition.getOriginalX()));
		transitionElement.setAttribute("positionY",	String.valueOf(inputTransition.getOriginalY()));
		transitionElement.setAttribute("nameOffsetX", String.valueOf(inputTransition.getNameOffsetX()));
		transitionElement.setAttribute("nameOffsetY", String.valueOf(inputTransition.getNameOffsetY()));
		transitionElement.setAttribute("name", inputTransition.underlyingTransition().name());
		transitionElement.setAttribute("displayName", (inputTransition.getAttributesVisible() ? "true" : "false"));
		transitionElement.setAttribute("id", (inputTransition.getId() != null ? inputTransition.getId()	: "error"));
		transitionElement.setAttribute("infiniteServer", "false");
		transitionElement.setAttribute("angle", String.valueOf(inputTransition.getAngle()));
		transitionElement.setAttribute("priority", "0");
		transitionElement.setAttribute("urgent", inputTransition.underlyingTransition().isUrgent()?"true":"false");
        transitionElement.setAttribute("player", inputTransition.underlyingTransition().isUncontrollable() ? "1" : "0");
        writeTACPN.appendColoredTransitionDependencies(inputTransition.underlyingTransition(), document, transitionElement);

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
		arcElement.setAttribute("nameOffsetX", String.valueOf(inputArc.getNameOffsetX()));
		arcElement.setAttribute("nameOffsetY", String.valueOf(inputArc.getNameOffsetY()));
		
		if (inputArc instanceof TimedOutputArcComponent) {
			if (inputArc instanceof TimedInputArcComponent) {
                if (getInputArcTypeAsString((TimedInputArcComponent) inputArc).equals("transport")) {
                    if (secondTransport) {
                        arcElement.setAttribute("transportID", String.valueOf(transporCountID));
                        secondTransport = false;
                    } else {
                        transporCountID++;
                        arcElement.setAttribute("transportID", String.valueOf(transporCountID));
                        secondTransport = true;
                    }
                }
				arcElement.setAttribute("type", getInputArcTypeAsString((TimedInputArcComponent)inputArc));
				arcElement.setAttribute("inscription", getGuardAsString((TimedInputArcComponent)inputArc));	
				arcElement.setAttribute("weight", inputArc.getWeight().nameForSaving(true)+"");
				if(!(inputArc instanceof TimedInhibitorArcComponent)){
                    appendArcIntervals((TimedInputArcComponent)inputArc, document, arcElement);
                }
			} else {
				arcElement.setAttribute("type", "normal");
				arcElement.setAttribute("inscription", "1");
				arcElement.setAttribute("weight", inputArc.getWeight().nameForSaving(true)+"");
			}
		}
		writeTACPN.appendColoredArcsDependencies(inputArc, guiModel, document, arcElement);

		return arcElement;
	}

    private void appendArcIntervals(TimedInputArcComponent inputArc, Document document, Element arcElement) {
        List<ColoredTimeInterval> ctiList;
        if (inputArc instanceof TimedTransportArcComponent) {
            TransportArc arc = ((TimedTransportArcComponent) inputArc).underlyingTransportArc();
            ctiList = arc.getColorTimeIntervals();
        } else {
            ctiList = inputArc.underlyingTimedInputArc().getColorTimeIntervals();
        }

        for (ColoredTimeInterval cti : ctiList) {
            if (cti.equalsOnlyColor(ColoredTimeInterval.ZERO_INF_DYN_COLOR(Color.STAR_COLOR))) {
                arcElement.setAttribute("inscription", cti.getInterval());
            } else {
                Element interval = document.createElement("colorinterval");
                Element inscription = document.createElement("inscription");
                Element colortype = document.createElement("colortype");
                inscription.setAttribute("inscription", cti.getInterval());
                colortype.setAttribute("name", cti.getColor().getColorType().getName());
                if (cti.getColor().getTuple() != null) {
                    for (Color color : cti.getColor().getTuple()) {
                        Element colorEle = document.createElement("color");
                        colorEle.setAttribute("value", color.getColorName());
                        colortype.appendChild(colorEle);
                    }
                } else {
                    Element colorEle = document.createElement("color");
                    colorEle.setAttribute("value", cti.getColor().getColorName());
                    colortype.appendChild(colorEle);
                }
                interval.appendChild(inscription);
                interval.appendChild(colortype);
                arcElement.appendChild(interval);
            }

        }
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
