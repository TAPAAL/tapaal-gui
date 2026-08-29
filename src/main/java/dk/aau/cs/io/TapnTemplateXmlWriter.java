package dk.aau.cs.io;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TransportArc;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.petrinet.dataLayer.DataLayer;
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
import dk.aau.cs.util.Require;

/**
 * Writes template-local native XML, including the legacy diagram payload.
 *
 * <p>This is intentionally an adapter around the current {@link Template}
 * representation. The template object still combines a domain net and a
 * {@link DataLayer}; isolating the adapter here keeps that coupling out of the
 * document-level writer and gives the eventual ID-based view split one home.</p>
 */
final class TapnTemplateXmlWriter {
    private final Iterable<Template> templates;
    private final TAPNLens lens;
    private final writeTACPN writeTACPN;
    private boolean secondTransport;
    private int transportCountId;
    private boolean saveConstantNames;

    TapnTemplateXmlWriter(Iterable<Template> templates, TAPNLens lens, writeTACPN writeTACPN) {
        this.templates = templates;
        this.lens = lens;
        this.writeTACPN = writeTACPN;
    }

    void appendTemplates(Document document, Element root, boolean saveConstantNames) {
        this.saveConstantNames = saveConstantNames;
        secondTransport = false;
        transportCountId = 0;

        for (Template template : templates) {
            DataLayer guiModel = template.guiModel();

            Element net = document.createElement("net");
            root.appendChild(net);
            net.setAttribute("id", template.model().name());
            net.setAttribute("active", String.valueOf(template.isActive()));
            net.setAttribute("type", "P/T net");

            appendAnnotationNotes(document, guiModel, net);
            appendPlaces(document, guiModel, net);
            appendTransitions(document, guiModel, net);
            appendArcs(document, guiModel, net);
        }
    }

    private void appendAnnotationNotes(Document document, DataLayer guiModel, Element net) {
        for (AnnotationNote label : guiModel.getLabels()) {
            net.appendChild(createAnnotationNoteElement(label, document));
        }
    }

    private void appendPlaces(Document document, DataLayer guiModel, Element net) {
        for (Place place : guiModel.getPlaces()) {
            net.appendChild(createPlaceElement((TimedPlaceComponent) place, guiModel, document));
        }
    }

    private void appendTransitions(Document document, DataLayer guiModel, Element net) {
        for (Transition transition : guiModel.getTransitions()) {
            net.appendChild(createTransitionElement((TimedTransitionComponent) transition, document));
        }
    }

    private void appendArcs(Document document, DataLayer guiModel, Element net) {
        for (Arc arc : guiModel.getArcs()) {
            Element arcElement = createArcElement(arc, guiModel, document);
            String[][] points = arc.getArcPath().getArcPathDetails();
            for (int i = 0; i < points.length; i++) {
                arcElement.appendChild(createArcPoint(points[i][0], points[i][1], points[i][2], document, i));
            }
            net.appendChild(arcElement);
        }
    }

    private Element createPlaceElement(TimedPlaceComponent inputPlace, DataLayer guiModel, Document document) {
        Require.that(inputPlace != null, "Error: inputPlace was null");
        Require.that(guiModel != null, "Error: guiModel was null");
        Require.that(document != null, "Error: document was null");

        Element placeElement = document.createElement("place");
        placeElement.setAttribute("positionX", String.valueOf(inputPlace.getOriginalX()));
        placeElement.setAttribute("positionY", String.valueOf(inputPlace.getOriginalY()));
        placeElement.setAttribute("name", inputPlace.underlyingPlace().name());
        placeElement.setAttribute("displayName", inputPlace.getAttributesVisible() ? "true" : "false");
        placeElement.setAttribute("id", inputPlace.getId() != null ? inputPlace.getId() : "error");
        placeElement.setAttribute("nameOffsetX", String.valueOf(inputPlace.getNameOffsetX()));
        placeElement.setAttribute("nameOffsetY", String.valueOf(inputPlace.getNameOffsetY()));
        placeElement.setAttribute("initialMarking", String.valueOf(inputPlace.getNumberOfTokens()));
        writeInitialMarkingAges(inputPlace.underlyingPlace(), placeElement);
        placeElement.setAttribute("invariant", inputPlace.underlyingPlace().invariant().toString());
        writeTACPN.appendColoredPlaceDependencies(inputPlace.underlyingPlace(), document, placeElement);
        createColoredInvariants(inputPlace.underlyingPlace(), document, placeElement);
        return placeElement;
    }

    private void writeInitialMarkingAges(TimedPlace place, Element element) {
        List<TimedToken> tokens = place.tokens();
        if (tokens.stream().allMatch(token -> token.age().signum() == 0)) {
            return;
        }

        Element markingAge = element.getOwnerDocument().createElement("initialMarkingAge");
        for (TimedToken token : tokens) {
            if (token.age().signum() == 0) {
                continue;
            }

            Element tokenElement = element.getOwnerDocument().createElement("token");
            if (lens.isColored()) {
                tokenElement.setAttribute("color", token.color().toString());
            }
            tokenElement.setAttribute("age", token.age().toPlainString());
            markingAge.appendChild(tokenElement);
        }
        element.appendChild(markingAge);
    }

    private void createColoredInvariants(TimedPlace inputPlace, Document document, Element placeElement) {
        for (ColoredTimeInvariant coloredTimeInvariant : inputPlace.getCtiList()) {
            Element invariant = document.createElement("colorinvariant");
            Element inscription = document.createElement("inscription");
            Element colorType = document.createElement("colortype");
            colorType.setAttribute("name", coloredTimeInvariant.getColor().getColorType().getName());
            if (coloredTimeInvariant.equalsOnlyColor(ColoredTimeInvariant.LESS_THAN_INFINITY_AND_STAR)) {
                placeElement.setAttribute("inscription", coloredTimeInvariant.getInvariantString(saveConstantNames));
                continue;
            }

            if (coloredTimeInvariant.getColor().getTuple() != null) {
                for (Color color : coloredTimeInvariant.getColor().getTuple()) {
                    Element colorElement = document.createElement("color");
                    colorElement.setAttribute("value", color.getColorName());
                    colorType.appendChild(colorElement);
                }
            } else {
                Element colorElement = document.createElement("color");
                colorElement.setAttribute("value", coloredTimeInvariant.getColor().getColorName());
                colorType.appendChild(colorElement);
            }

            inscription.setAttribute("inscription", coloredTimeInvariant.getInvariantString(saveConstantNames));
            invariant.appendChild(inscription);
            invariant.appendChild(colorType);
            placeElement.appendChild(invariant);
        }
    }

    private Element createAnnotationNoteElement(AnnotationNote inputLabel, Document document) {
        Require.that(inputLabel != null, "Error: inputLabel was null");
        Require.that(document != null, "Error: document was null");

        Element labelElement = document.createElement("labels");
        labelElement.setAttribute("positionX", inputLabel.getOriginalX() >= 0.0 ? String.valueOf(inputLabel.getOriginalX()) : "");
        labelElement.setAttribute("positionY", inputLabel.getOriginalY() >= 0.0 ? String.valueOf(inputLabel.getOriginalY()) : "");
        labelElement.setAttribute("width", inputLabel.getNoteWidth() >= 0.0 ? String.valueOf(inputLabel.getNoteWidth()) : "");
        labelElement.setAttribute("height", inputLabel.getNoteHeight() >= 0.0 ? String.valueOf(inputLabel.getNoteHeight()) : "");
        labelElement.setAttribute("border", String.valueOf(inputLabel.isShowingBorder()));
        labelElement.appendChild(document.createTextNode(inputLabel.getNoteText() != null ? inputLabel.getNoteText() : ""));
        return labelElement;
    }

    private Element createTransitionElement(TimedTransitionComponent inputTransition, Document document) {
        Require.that(inputTransition != null, "Error: inputTransition was null");
        Require.that(document != null, "Error: document was null");

        Element transitionElement = document.createElement("transition");
        transitionElement.setAttribute("positionX", String.valueOf(inputTransition.getOriginalX()));
        transitionElement.setAttribute("positionY", String.valueOf(inputTransition.getOriginalY()));
        transitionElement.setAttribute("nameOffsetX", String.valueOf(inputTransition.getNameOffsetX()));
        transitionElement.setAttribute("nameOffsetY", String.valueOf(inputTransition.getNameOffsetY()));
        transitionElement.setAttribute("name", inputTransition.underlyingTransition().name());
        transitionElement.setAttribute("displayName", inputTransition.getAttributesVisible() ? "true" : "false");
        transitionElement.setAttribute("id", inputTransition.getId() != null ? inputTransition.getId() : "error");
        transitionElement.setAttribute("infiniteServer", "false");
        transitionElement.setAttribute("angle", String.valueOf(inputTransition.getAngle()));
        transitionElement.setAttribute("priority", "0");
        transitionElement.setAttribute("urgent", inputTransition.underlyingTransition().isUrgent() ? "true" : "false");
        transitionElement.setAttribute("player", inputTransition.underlyingTransition().isUncontrollable() ? "1" : "0");
        transitionElement.setAttribute("weight", inputTransition.underlyingTransition().getWeight().nameForSaving(saveConstantNames));
        transitionElement.setAttribute("firingMode", inputTransition.underlyingTransition().getFiringMode().toString());
        inputTransition.underlyingTransition().getDistribution().writeToXml(transitionElement, saveConstantNames);
        writeTACPN.appendColoredTransitionDependencies(inputTransition.underlyingTransition(), document, transitionElement);
        return transitionElement;
    }

    private Element createArcElement(Arc inputArc, DataLayer guiModel, Document document) {
        Require.that(inputArc != null, "Error: inputArc was null");
        Require.that(guiModel != null, "Error: guiModel was null");
        Require.that(document != null, "Error: document was null");

        Element arcElement = document.createElement("arc");
        arcElement.setAttribute("id", inputArc.getId() != null ? inputArc.getId() : "error");
        arcElement.setAttribute("source", inputArc.getSource().getId() != null ? inputArc.getSource().getId() : "");
        arcElement.setAttribute("target", inputArc.getTarget().getId() != null ? inputArc.getTarget().getId() : "");
        arcElement.setAttribute("nameOffsetX", String.valueOf(inputArc.getNameOffsetX()));
        arcElement.setAttribute("nameOffsetY", String.valueOf(inputArc.getNameOffsetY()));

        if (inputArc instanceof TimedOutputArcComponent) {
            if (inputArc instanceof TimedInputArcComponent) {
                TimedInputArcComponent input = (TimedInputArcComponent) inputArc;
                if (getInputArcTypeAsString(input).equals("transport")) {
                    if (secondTransport) {
                        arcElement.setAttribute("transportID", String.valueOf(transportCountId));
                        secondTransport = false;
                    } else {
                        transportCountId++;
                        arcElement.setAttribute("transportID", String.valueOf(transportCountId));
                        secondTransport = true;
                    }
                }
                arcElement.setAttribute("type", getInputArcTypeAsString(input));
                arcElement.setAttribute("inscription", getGuardAsString(input));
                arcElement.setAttribute("weight", inputArc.getWeight().nameForSaving(true) + "");
                if (!(inputArc instanceof TimedInhibitorArcComponent)) {
                    appendArcIntervals(input, document, arcElement);
                }
            } else {
                arcElement.setAttribute("type", "normal");
                arcElement.setAttribute("inscription", "1");
                arcElement.setAttribute("weight", inputArc.getWeight().nameForSaving(true) + "");
            }
        }

        if (!(inputArc instanceof TimedInhibitorArcComponent)) {
            writeTACPN.appendColoredArcsDependencies(inputArc, guiModel, document, arcElement);
        }
        return arcElement;
    }

    private void appendArcIntervals(TimedInputArcComponent inputArc, Document document, Element arcElement) {
        List<ColoredTimeInterval> intervals;
        if (inputArc instanceof TimedTransportArcComponent) {
            TransportArc arc = ((TimedTransportArcComponent) inputArc).underlyingTransportArc();
            intervals = arc.getColorTimeIntervals();
        } else {
            intervals = inputArc.underlyingTimedInputArc().getColorTimeIntervals();
        }

        for (ColoredTimeInterval interval : intervals) {
            if (interval.equalsOnlyColor(ColoredTimeInterval.ZERO_INF_DYN_COLOR(Color.STAR_COLOR))) {
                arcElement.setAttribute("inscription", interval.getInterval(saveConstantNames));
                continue;
            }

            Element colorInterval = document.createElement("colorinterval");
            Element inscription = document.createElement("inscription");
            Element colorType = document.createElement("colortype");
            inscription.setAttribute("inscription", interval.getInterval(saveConstantNames));
            colorType.setAttribute("name", interval.getColor().getColorType().getName());
            if (interval.getColor().getTuple() != null) {
                for (Color color : interval.getColor().getTuple()) {
                    Element colorElement = document.createElement("color");
                    colorElement.setAttribute("value", color.getColorName());
                    colorType.appendChild(colorElement);
                }
            } else {
                Element colorElement = document.createElement("color");
                colorElement.setAttribute("value", interval.getColor().getColorName());
                colorType.appendChild(colorElement);
            }
            colorInterval.appendChild(inscription);
            colorInterval.appendChild(colorType);
            arcElement.appendChild(colorInterval);
        }
    }

    private String getInputArcTypeAsString(TimedInputArcComponent inputArc) {
        if (inputArc instanceof TimedTransportArcComponent) {
            return "transport";
        } else if (inputArc instanceof TimedInhibitorArcComponent) {
            return "tapnInhibitor";
        }
        return "timed";
    }

    private String getGuardAsString(TimedInputArcComponent inputArc) {
        if (inputArc instanceof TimedTransportArcComponent) {
            return inputArc.getGuardAsString() + ":" + ((TimedTransportArcComponent) inputArc).getGroupNr();
        }
        return inputArc.getGuardAsString();
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
