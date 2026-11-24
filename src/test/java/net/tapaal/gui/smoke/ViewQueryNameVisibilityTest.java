package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryNameVisibilityTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void hidingPlaceNamesLeavesTransitionNamesVisibleAndCanBeUndone() throws Exception {
        var tab = gui.open("view-query-fixtures/components.tapn");
        assertNames(tab, true, true);
        gui.menu("View", "Change visibility of transition/place names");
        var dialog = gui.awaitDialog("Change Name Visibility");
        gui.clickButton(dialog, "Hide");
        gui.clickButton(dialog, "Places");
        gui.clickButton(dialog, "Selected component");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        assertNames(tab, false, true);
        gui.menu("Edit", "Undo");
        assertNames(tab, true, true);
    }

    private void assertNames(PetriNetTab tab, boolean places, boolean transitions) throws Exception {
        gui.onEdt(() -> {
            assertEquals(places, GuiHarness.place(tab, "Start").getAttributesVisible());
            assertEquals(places, GuiHarness.place(tab, "Ready").getAttributesVisible());
            assertEquals(transitions, GuiHarness.transition(tab, "Go").getAttributesVisible());
            return null;
        });
    }
}
