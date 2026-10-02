package net.tapaal.gui.petrinet.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.aau.cs.io.writeTACPN;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;

class PetriNetModelEditorTest {

    @Test
    void createsAndMutatesDomainObjectsWithoutAView() {
        PetriNetModelEditor editor = new PetriNetModelEditor();
        TimedArcPetriNet model = new TimedArcPetriNet("Template");
        LocalTimedPlace source = editor.createPlace("Source");
        LocalTimedPlace target = editor.createPlace("Target");
        TimedTransition transition = editor.createTransition("Transition", true, false);

        editor.addPlace(model, source);
        editor.addPlace(model, target);
        editor.addTransition(model, transition);
        TimedInputArc input = editor.createInputArc(source, transition);
        TimedOutputArc output = editor.createOutputArc(transition, target);
        editor.addInputArc(model, input);
        editor.addOutputArc(model, output);

        assertEquals(2, model.places().size());
        assertEquals(1, model.transitions().size());
        assertEquals(1, count(model.inputArcs()));
        assertEquals(1, count(model.outputArcs()));
        assertTrue(transition.isUrgent());

        editor.removeOutputArc(output);
        editor.removeInputArc(input);
        assertEquals(0, count(model.inputArcs()));
        assertEquals(0, count(model.outputArcs()));
    }

    @Test
    void productColorTransportArcsSerializeTuplesForBothEndpoints() {
        ColorType colors = new ColorType("Colors");
        colors.addColor("red");
        colors.addColor("blue");
        ColorType sizes = new ColorType("Sizes");
        sizes.addColor("small");
        sizes.addColor("large");
        ProductType sourceColors = new ProductType("SourceColors");
        sourceColors.addType(colors);
        sourceColors.addType(sizes);
        ProductType destinationColors = new ProductType("DestinationColors");
        destinationColors.addType(sizes);
        destinationColors.addType(colors);

        TransportArc arc = new PetriNetModelEditor().createTransportArc(
            new LocalTimedPlace("Source", sourceColors),
            new TimedTransition("Transition"),
            new LocalTimedPlace("Destination", destinationColors)
        );

        assertAll(
            () -> assertSerializedTuple(arc.getInputExpression(), "red", "small"),
            () -> assertSerializedTuple(arc.getOutputExpression(), "small", "red")
        );
    }

    private static void assertSerializedTuple(ArcExpression expression, String firstColor, String secondColor) throws Exception {
        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element inscription = new writeTACPN(new TimedArcPetriNetNetwork())
            .createArcExpressionElement(document, expression);

        assertEquals(1, inscription.getElementsByTagName("tuple").getLength());
        assertEquals("1", ((Element) inscription.getElementsByTagName("numberconstant").item(0)).getAttribute("value"));
        NodeList colors = inscription.getElementsByTagName("useroperator");
        assertEquals(2, colors.getLength());
        assertEquals(firstColor, ((Element) colors.item(0)).getAttribute("declaration"));
        assertEquals(secondColor, ((Element) colors.item(1)).getAttribute("declaration"));
        assertEquals(0, inscription.getElementsByTagName("finiteintrangeconstant").getLength());
    }

    private static int count(Iterable<?> values) {
        int count = 0;
        for (Object ignored : values) count++;
        return count;
    }
}
