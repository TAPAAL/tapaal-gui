package dk.aau.cs.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

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

import net.tapaal.gui.petrinet.TAPNLens;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.MessengerImpl;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.Transition;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.model.tapn.SMCUserDefinedDistribution;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Require;
import dk.aau.cs.verification.TAPNComposer;

public class PNMLWriter implements NetWriter {

	private final TimedArcPetriNetNetwork network;
	private TimedArcPetriNet composedNetwork;
	private final HashMap<TimedArcPetriNet, DataLayer> guiModels;
	private final TAPNLens lens;
	private final writeTACPN writeTACPN;

	public PNMLWriter(TimedArcPetriNetNetwork network, HashMap<TimedArcPetriNet, DataLayer> guiModels, TAPNLens lens) {
		this.network = network;
		this.guiModels = guiModels;
		this.lens = lens;
		writeTACPN = new writeTACPN(network);
	}
	
	public ByteArrayOutputStream savePNML() throws ParserConfigurationException, DOMException, TransformerException {
		Document document = null;
		Transformer transformer = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		TAPNComposer composer = new TAPNComposer(new MessengerImpl(), guiModels, lens, true, true);
		composedNetwork = composer.transformModel(network).value1();
		
		// Build a Petri Net XML Document
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		document = builder.newDocument();

		Element pnmlRootNode = document.createElement("pnml"); // PNML Top Level
		document.appendChild(pnmlRootNode);
		pnmlRootNode.setAttribute("xmlns", "http://www.pnml.org/version-2009/grammar/pnml");

		Element netNode = document.createElement("net"); //Net node
		pnmlRootNode.appendChild(netNode);
		netNode.setAttribute("id", composedNetwork.name());
		netNode.setAttribute("type", "http://www.pnml.org/version-2009/grammar/ptnet");

        Element nameNode = document.createElement("name"); //Name of the net
        netNode.appendChild(nameNode);
        Element nameText = document.createElement("text");
        nameNode.appendChild(nameText);
        nameText.setTextContent(composedNetwork.name());

        if (lens.isColored()) {
            writeTACPN.appendDeclarations(document, netNode);
        }
        
        if (lens.isStochastic()) {
            appendCustomDistributions(document, netNode);
        }

		Element pageNode = document.createElement("page"); //Page node
		netNode.appendChild(pageNode);
		pageNode.setAttribute("id", "page0");

		appendPlaces(document, composer.getGuiModel(), pageNode);
		appendTransitions(document, composer.getGuiModel(), pageNode);
		appendArcs(document, composer.getGuiModel(), pageNode);

		document.normalize();
		// Create Transformer with XSL Source File
		transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(document);

		StreamResult result = new StreamResult(os);
		transformer.transform(source, result);
		
		return os;
	}

    private void appendCustomDistributions(Document document, Element root) {
        for (SMCUserDefinedDistribution cd : network.userDefinedDistributions()) {
            Element element = document.createElement("custom_distribution");
            element.setAttribute("name", cd.getName());
            for (Double val : cd.getValues()) {
                Element valElement = document.createElement("value");
                valElement.setTextContent(val.toString());
                element.appendChild(valElement);
            }

            root.appendChild(element);
        }
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

	private void appendPlaces(Document document, DataLayer guiModel, Element NET) {
		Place[] places = guiModel.getPlaces();
		for (Place place : places) {
            if (lens.isColored()){
                NET.appendChild(createColorPlaceElement((TimedPlaceComponent) place, guiModel, document));
            }
            else {
                NET.appendChild(createPlaceElement((TimedPlaceComponent) place, guiModel, document));
            }
		}
	}

	private void appendTransitions(Document document, DataLayer guiModel, Element NET) {
		Transition[] transitions = guiModel.getTransitions();
		for (Transition transition : transitions) {
            if(lens.isColored()) {
                NET.appendChild(createColoredTransitionElement((TimedTransitionComponent) transition, document));
            }
            else {
                NET.appendChild(createTransitionElement((TimedTransitionComponent) transition, document));
            }
		}
	}
	
	private void appendArcs(Document document, DataLayer guiModel, Element NET) {
		Arc[] arcs = guiModel.getArcs();
		for (Arc arc : arcs) {
            if(lens.isColored()) {
                NET.appendChild(createColoredArcElement(arc,guiModel , document));
            }
            else {
                NET.appendChild(createArcElement(arc, guiModel, document));
            }
		}
	}

	private Element createPlaceElement(TimedPlaceComponent inputPlace, DataLayer guiModel, Document document) {
		Require.that(inputPlace != null, "Error: inputPlace was null");
		Require.that(guiModel != null, "Error: guiModel was null");
		Require.that(document != null, "Error: document was null");
		
		Element placeElement = document.createElement("place");
		placeElement.setAttribute("id", (inputPlace.getId() != null ? inputPlace.getId() : inputPlace.getName()));

		Element name = document.createElement("name"); //Name
		placeElement.appendChild(name);
		Element nameGraphics = document.createElement("graphics");
		name.appendChild(nameGraphics);
		Element nameOffset = document.createElement("offset");
		nameGraphics.appendChild(nameOffset);
		nameOffset.setAttribute("x", String.valueOf(Math.round(inputPlace.getNameOffsetX())));
		nameOffset.setAttribute("y", String.valueOf(Math.round(inputPlace.getNameOffsetY())));
		Element nameText = document.createElement("text");
		name.appendChild(nameText);
		nameText.setTextContent(inputPlace.underlyingPlace().name());
		
		Element graphics = document.createElement("graphics");
		placeElement.appendChild(graphics);
		Element offset = document.createElement("position");
		graphics.appendChild(offset);
		offset.setAttribute("x", String.valueOf(Math.round(inputPlace.getPositionX())));
		offset.setAttribute("y", String.valueOf(Math.round(inputPlace.getPositionY())));
		
		Element initialMarking = document.createElement("initialMarking"); //Name
		placeElement.appendChild(name);
		//Seems to break the PNML file
		/*Element initialMarkingGraphics = document.createElement("graphics");
		initialMarking.appendChild(initialMarkingGraphics);*/
		Element initialMarkingText = document.createElement("text");
		initialMarking.appendChild(initialMarkingText);
		initialMarkingText.setTextContent(((Integer) inputPlace.getNumberOfTokens() != null ? String.valueOf((Integer) inputPlace.getNumberOfTokens()) : "0"));
		placeElement.appendChild(initialMarking);
                
		return placeElement;
	}

    private Element createColorPlaceElement(TimedPlaceComponent inputPlace, DataLayer guiModel, Document document) {
        Require.that(inputPlace != null, "Error: inputPlace was null");
        Require.that(guiModel != null, "Error: guiModel was null");
        Require.that(document != null, "Error: document was null");

        Element placeElement = document.createElement("place");
        placeElement.setAttribute("id", (inputPlace.getId() != null ? inputPlace.getId() : inputPlace.getName()));


        Element name = document.createElement("name"); //Name
        placeElement.appendChild(name);
        Element nameText = document.createElement("text");
        name.appendChild(nameText);
        nameText.setTextContent(inputPlace.underlyingPlace().name());
        Element nameGraphics = document.createElement("graphics");
        name.appendChild(nameGraphics);
        Element nameOffset = document.createElement("offset");
        nameGraphics.appendChild(nameOffset);
        nameOffset.setAttribute("x", String.valueOf(Math.round(inputPlace.getNameOffsetX())));
        nameOffset.setAttribute("y", String.valueOf(Math.round(inputPlace.getNameOffsetY())));

        Element graphics = document.createElement("graphics");
        placeElement.appendChild(graphics);
        Element offset = document.createElement("position");
        graphics.appendChild(offset);
        offset.setAttribute("x", String.valueOf(Math.round(inputPlace.getPositionX())));
        offset.setAttribute("y", String.valueOf(Math.round(inputPlace.getPositionY())));

        writeTACPN.appendColoredPlaceDependencies(inputPlace.underlyingPlace(), document, placeElement);
        return placeElement;
    }

	private Element createTransitionElement(TimedTransitionComponent inputTransition, Document document) {
		Require.that(inputTransition != null, "Error: inputTransition was null");
		Require.that(document != null, "Error: document was null");
		
		Element transitionElement = document.createElement("transition");
		transitionElement.setAttribute("id", (inputTransition.getId() != null ? inputTransition.getId()	: "error"));
        inputTransition.underlyingTransition().getDistribution().writeToXml(transitionElement);

		Element name = document.createElement("name"); //Name
		transitionElement.appendChild(name);
		Element nameGraphics = document.createElement("graphics");
		name.appendChild(nameGraphics);
		Element nameOffset = document.createElement("offset");
		nameGraphics.appendChild(nameOffset);
		nameOffset.setAttribute("x", String.valueOf(Math.round(inputTransition.getNameOffsetX())));
		nameOffset.setAttribute("y", String.valueOf(Math.round(inputTransition.getNameOffsetY())));
		Element nameText = document.createElement("text");
		name.appendChild(nameText);
		nameText.setTextContent(inputTransition.underlyingTransition().name());
		
		Element graphics = document.createElement("graphics");
		transitionElement.appendChild(graphics);
		Element offset = document.createElement("position");
		graphics.appendChild(offset);
		offset.setAttribute("x", String.valueOf(Math.round(inputTransition.getPositionX())));
		offset.setAttribute("y", String.valueOf(Math.round(inputTransition.getPositionY())));

		return transitionElement;
	}

    private Element createColoredTransitionElement (TimedTransitionComponent inputTransition, Document document) {
        Require.that(inputTransition != null, "Error: inputTransition was null");
        Require.that(document != null, "Error: document was null");

        Element transitionElement = document.createElement("transition");
        transitionElement.setAttribute("id", (inputTransition.getId() != null ? inputTransition.getId()	: "error"));
        inputTransition.underlyingTransition().getDistribution().writeToXml(transitionElement);

        Element name = document.createElement("name"); //Name
        transitionElement.appendChild(name);
        Element nameGraphics = document.createElement("graphics");
        name.appendChild(nameGraphics);
        Element nameOffset = document.createElement("offset");
        nameGraphics.appendChild(nameOffset);
        nameOffset.setAttribute("x", String.valueOf(Math.round(inputTransition.getNameOffsetX())));
        nameOffset.setAttribute("y", String.valueOf(Math.round(inputTransition.getNameOffsetY())));
        Element nameText = document.createElement("text");
        name.appendChild(nameText);
        nameText.setTextContent(inputTransition.underlyingTransition().name());

        Element graphics = document.createElement("graphics");
        transitionElement.appendChild(graphics);
        Element offset = document.createElement("position");
        graphics.appendChild(offset);
        offset.setAttribute("x", String.valueOf(Math.round(inputTransition.getPositionX())));
        offset.setAttribute("y", String.valueOf(Math.round(inputTransition.getPositionY())));

        writeTACPN.appendColoredTransitionDependencies(inputTransition.underlyingTransition(), document, transitionElement);
        return transitionElement;
    }

	private Element createArcElement(Arc arc, DataLayer guiModel, Document document) {
		Require.that(arc != null, "Error: inputArc was null");
		Require.that(guiModel != null, "Error: guiModel was null");
		Require.that(document != null, "Error: document was null");
		
		Element arcElement = document.createElement("arc");
		
		arcElement.setAttribute("id", (arc.getId() != null ? arc.getId() : "error"));
		arcElement.setAttribute("source", (arc.getSource().getId() != null ? arc.getSource().getId() : ""));
		arcElement.setAttribute("target", (arc.getTarget().getId() != null ? arc.getTarget().getId() : ""));
		
		if (arc instanceof TimedOutputArcComponent && arc.getWeight().value() > 1 ) {
			Element inscription = document.createElement("inscription");
			arcElement.appendChild(inscription);
			Element text = document.createElement("text");
			inscription.appendChild(text);
			text.setTextContent(arc.getWeight().nameForSaving(false)+"");
		} 
		
		if(arc instanceof TimedInhibitorArcComponent){
			arcElement.setAttribute("type", "inhibitor");
		} else {
			arcElement.setAttribute("type", "normal");
		}
		
		
		//ArcPath
		int arcPoints = arc.getArcPath().getArcPathDetails().length;
		if (arcPoints > 2) {
			Element graphics = document.createElement("graphics");
			arcElement.appendChild(graphics);

			String[][] point = arc.getArcPath().getArcPathDetails();
			for (int j = 1; j < arcPoints-1; j++) { // Do not write the first and last point
				graphics.appendChild(createArcPoint(point[j][0],
						point[j][1], point[j][2], document, j));
			}
		}
		
		return arcElement;
	}

    private Element createColoredArcElement(Arc arc, DataLayer guiModel, Document document) {
        Require.that(arc != null, "Error: inputArc was null");
        Require.that(guiModel != null, "Error: guiModel was null");
        Require.that(document != null, "Error: document was null");

        Element arcElement = document.createElement("arc");
        arcElement.setAttribute("id", (arc.getId() != null ? arc.getId() : "error"));
        arcElement.setAttribute("source", (arc.getSource().getId() != null ? arc.getSource().getId() : ""));
        arcElement.setAttribute("target", (arc.getTarget().getId() != null ? arc.getTarget().getId() : ""));

        if (!(arc instanceof TimedInhibitorArcComponent)) {
            writeTACPN.appendColoredArcsDependencies(arc, guiModel, document, arcElement);
        }

        if (arc instanceof TimedOutputArcComponent && arc.getWeight().value() > 1 ) {
            Element inscription = document.createElement("inscription");
            arcElement.appendChild(inscription);
            Element text = document.createElement("text");
            inscription.appendChild(text);
            text.setTextContent(arc.getWeight().nameForSaving(false)+"");
        }

        if(arc instanceof TimedInhibitorArcComponent){
            arcElement.setAttribute("type", "inhibitor");
        } else {
            arcElement.setAttribute("type", "normal");
        }

        //ArcPath
        int arcPoints = arc.getArcPath().getArcPathDetails().length;
        if (arcPoints > 2) {
            Element graphics = document.createElement("graphics");
            arcElement.appendChild(graphics);

            String[][] point = arc.getArcPath().getArcPathDetails();
            for (int j = 1; j < arcPoints-1; j++) { // Do not write the first and last point
                graphics.appendChild(createArcPoint(point[j][0],
                    point[j][1], point[j][2], document, j));
            }
        }
        return arcElement;
    }
	
	private Element createArcPoint(String x, String y, String type, Document document, int id) {
		Require.that(document != null, "Error: document was null");
		Element position = document.createElement("position");
		position.setAttribute("x", x);
		position.setAttribute("y", y);

		return position;
	}
}
