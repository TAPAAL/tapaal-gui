package dk.aau.cs.io;

import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.SMCUserDefinedDistribution;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Require;

/**
 * Writes the network-level model data in a native TAPAAL document.
 *
 * <p>Template elements are intentionally not handled here yet: their native
 * representation currently combines model values with diagram IDs and
 * geometry. Keeping this writer limited to network-level model data gives the
 * persistence refactor a seam that does not depend on a {@code DataLayer}.</p>
 */
final class TapnModelXmlWriter {
    private final TimedArcPetriNetNetwork network;
    private final Iterable<Constant> constants;
    private final writeTACPN writeTACPN;

    TapnModelXmlWriter(
        TimedArcPetriNetNetwork network,
        Iterable<Constant> constants,
        writeTACPN writeTACPN
    ) {
        this.network = network;
        this.constants = constants;
        this.writeTACPN = writeTACPN;
    }

    void appendNetworkData(
        Document document,
        Element root,
        boolean isColored,
        boolean isStochastic,
        boolean saveConstantNames
    ) {
        Require.that(document != null, "Error: document was null");
        Require.that(root != null, "Error: root was null");

        writeTACPN.appendDeclarations(document, root);

        if (isStochastic) {
            appendCustomDistributions(document, root);
        }

        appendSharedPlaces(document, root, isColored, saveConstantNames);
        appendSharedTransitions(document, root, saveConstantNames);
        appendConstants(document, root);
    }

    private void appendCustomDistributions(Document document, Element root) {
        for (SMCUserDefinedDistribution distribution : network.userDefinedDistributions()) {
            Element element = document.createElement("custom_distribution");
            element.setAttribute("name", distribution.getName());
            element.setAttribute("randomStart", String.valueOf(distribution.isRandomStart()));
            for (Double value : distribution.getValues()) {
                Element valueElement = document.createElement("value");
                valueElement.setTextContent(value.toString());
                element.appendChild(valueElement);
            }

            root.appendChild(element);
        }
    }

    private void appendSharedPlaces(
        Document document,
        Element root,
        boolean isColored,
        boolean saveConstantNames
    ) {
        for (SharedPlace place : network.sharedPlaces()) {
            Element element = document.createElement("shared-place");
            element.setAttribute("invariant", place.invariant().toString());
            element.setAttribute("name", place.name());
            element.setAttribute("initialMarking", String.valueOf(place.numberOfTokens()));
            writeInitialMarkingAges(place, element, isColored);
            createColoredInvariants(place, document, element, saveConstantNames);
            writeTACPN.appendColoredPlaceDependencies(place, document, element);

            root.appendChild(element);
        }
    }

    private void appendSharedTransitions(Document document, Element root, boolean saveConstantNames) {
        for (SharedTransition transition : network.sharedTransitions()) {
            Element element = document.createElement("shared-transition");
            element.setAttribute("name", transition.name());
            element.setAttribute("urgent", transition.isUrgent() ? "true" : "false");
            element.setAttribute("player", transition.isUncontrollable() ? "1" : "0");
            element.setAttribute("weight", transition.getWeight().nameForSaving(saveConstantNames));
            element.setAttribute("firingMode", transition.getFiringMode().toString());
            transition.getDistribution().writeToXml(element, saveConstantNames);
            root.appendChild(element);
        }
    }

    private void appendConstants(Document document, Element root) {
        for (Constant constant : constants) {
            Element element = createConstantElement(constant, document);
            root.appendChild(element);
        }

        for (var constant : network.realConstants()) {
            Element element = document.createElement("constant");
            element.setAttribute("name", constant.name());
            element.setAttribute("type", "real");
            element.setAttribute(
                "value",
                constant.values().stream().map(String::valueOf).collect(Collectors.joining(","))
            );
            root.appendChild(element);
        }
    }

    private Element createConstantElement(Constant constant, Document document) {
        Require.that(constant != null, "Error: constant was null");
        Require.that(document != null, "Error: document was null");

        Element constantElement = document.createElement("constant");
        constantElement.setAttribute("name", constant.name());
        if (constant.hasMultipleValues()) {
            String values = constant.values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            constantElement.setAttribute("value", values);
        } else {
            constantElement.setAttribute("value", String.valueOf(constant.value()));
        }

        return constantElement;
    }

    private void writeInitialMarkingAges(TimedPlace place, Element element, boolean isColored) {
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
            if (isColored) {
                tokenElement.setAttribute("color", token.color().toString());
            }

            tokenElement.setAttribute("age", token.age().toPlainString());
            markingAge.appendChild(tokenElement);
        }

        element.appendChild(markingAge);
    }

    private void createColoredInvariants(
        TimedPlace place,
        Document document,
        Element placeElement,
        boolean saveConstantNames
    ) {
        List<ColoredTimeInvariant> invariants = place.getCtiList();

        for (ColoredTimeInvariant coloredTimeInvariant : invariants) {
            Element invariant = document.createElement("colorinvariant");
            Element inscription = document.createElement("inscription");
            Element colorType = document.createElement("colortype");
            colorType.setAttribute("name", coloredTimeInvariant.getColor().getColorType().getName());
            if (coloredTimeInvariant.equalsOnlyColor(ColoredTimeInvariant.LESS_THAN_INFINITY_AND_STAR)) {
                placeElement.setAttribute("inscription", coloredTimeInvariant.getInvariantString(saveConstantNames));
            } else {
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
    }
}
