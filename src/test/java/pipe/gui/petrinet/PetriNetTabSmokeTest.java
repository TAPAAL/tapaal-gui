package pipe.gui.petrinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import net.tapaal.gui.petrinet.Template;
import net.tapaal.gui.GuiFrameController;
import net.tapaal.gui.TabInteraction;
import pipe.gui.GuiFrame;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;

@Tag("gui")
class PetriNetTabSmokeTest {

    @Test
    void createsAnEditorAndKeepsPlaceModelAndViewInSyncAcrossUndoRedo() throws Exception {
        AtomicReference<PetriNetTab> tabRef = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            PetriNetTab tab = PetriNetTab.createNewEmptyTab("smoke.tapn", true, false, false, false);
            tabRef.set(tab);

            Template template = tab.currentTemplate();
            assertNotNull(template);
            assertEquals(0, template.model().places().size());

            TimedPlaceComponent place = tab.guiModelManager
                .addNewTimedPlace(template.guiModel(), new Point(100, 100)).result;
            assertEquals(1, template.model().places().size());
            assertEquals(1, template.guiModel().getPlaces().length);

            tab.getUndoManager().undo();
            assertEquals(0, template.model().places().size());
            assertEquals(0, template.guiModel().getPlaces().length);

            tab.getUndoManager().redo();
            assertEquals(1, template.model().places().size());
            assertEquals(1, template.guiModel().getPlaces().length);

            tab.getUndoManager().newEdit();
            tab.guiModelManager.deleteSelection(new ArrayList<>(List.of(place)));
            assertEquals(0, template.model().places().size());
            assertEquals(0, template.guiModel().getPlaces().length);

            // Undo must restore the model before the view repaints and reads
            // the place marking.
            tab.getUndoManager().undo();
            assertEquals(1, template.model().places().size());
            assertEquals(1, template.guiModel().getPlaces().length);
        });
    }

    @Test
    void controllerOwnsCurrentTabSelectionAndTabModelsKnowTheirOwner() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GuiFrame frame = new GuiFrame("test");
            GuiFrameController controller = new GuiFrameController(frame);
            TabInteraction interaction = controller;
            PetriNetTab tab = PetriNetTab.createNewEmptyTab("owner.tapn", true, false, false, false);
            PetriNetTab secondTab = PetriNetTab.createNewEmptyTab("second.tapn", true, false, false, false);

            assertTrue(interaction.getCurrentTab().isEmpty());
            controller.openTab(tab);

            assertEquals(tab, interaction.getCurrentTab().orElseThrow());
            assertEquals(tab, tab.getModel().getOwnerTab());
            assertEquals(tab, tab.currentTemplate().guiModel().getOwnerTab());

            controller.openTab(secondTab);
            assertEquals(secondTab, interaction.getCurrentTab().orElseThrow());
            controller.changeToTab(tab);
            assertEquals(tab, interaction.getCurrentTab().orElseThrow());
            controller.changeToTab(secondTab);
            assertEquals(secondTab, interaction.getCurrentTab().orElseThrow());

            controller.closeTab(secondTab);
            assertEquals(tab, interaction.getCurrentTab().orElseThrow());
            controller.closeTab(tab);
            assertTrue(interaction.getCurrentTab().isEmpty());
            frame.dispose();
        });
    }
}
