package net.tapaal.gui.petrinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Point;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import pipe.gui.petrinet.PetriNetTab;

@Tag("gui")
class TabTransformerTest {
    @Test
    void removingTimingConvertsBothTransportEndsOnce() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PetriNetTab tab = PetriNetTab.createNewEmptyTab("transport.tapn", true, false, false, false);
            Template template = tab.currentTemplate();
            var source = tab.guiModelManager.addNewTimedPlace(template.guiModel(), new Point(100, 100)).result;
            var destination = tab.guiModelManager.addNewTimedPlace(template.guiModel(), new Point(300, 100)).result;
            var transition = tab.guiModelManager.addNewTimedTransitions(
                template.guiModel(), new Point(200, 100), false, false).result;
            assertNotNull(tab.guiModelManager.addTimedTransportArc(
                template.guiModel(), source, transition, destination, null, null).result);

            TabTransformer.removeTimingInformation(tab);

            assertFalse(template.model().transportArcs().iterator().hasNext());
            assertEquals(2, template.guiModel().getArcs().length);
            assertNotNull(template.model().getInputArcFromPlaceToTransition(
                source.underlyingPlace(), transition.underlyingTransition()));
            assertNotNull(template.model().getOutputArcFromTransitionAndPlace(
                transition.underlyingTransition(), destination.underlyingPlace()));
        });
    }
}
