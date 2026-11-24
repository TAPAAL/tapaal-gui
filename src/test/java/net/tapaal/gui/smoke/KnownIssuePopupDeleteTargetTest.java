package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Known issue: the Delete item of a place's popup menu deletes the current selection, not the place that was
 * right-clicked.
 *
 * <p>A right click does not change the selection ({@code GraphicalElement} sends only left presses to the
 * selection logic), and the popup's Delete item is the global delete action. So with P0 selected, right-clicking
 * P1 and choosing Delete removes P0 and keeps P1.
 */
@EnabledIfEnvironmentVariable(named = "TAPAAL_GUI_KNOWN_ISSUES", matches = "true",
    disabledReason = "Known-issue reproducer; see tools/validation/gui-known-issues.md")
class KnownIssuePopupDeleteTargetTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void popupDeleteRemovesTheRightClickedPlace() throws Exception {
        var tab = gui.open("known-issue-workbench/two-places.tapn");
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));

        gui.clickObject(tab, "P0");
        assertTrue(gui.onEdt(() -> place(tab, "P0").isSelected()), "The click should select P0");

        gui.rightClickObject(tab, "P1");
        boolean p0Selected = gui.onEdt(() -> place(tab, "P0").isSelected());
        boolean p1Selected = gui.onEdt(() -> place(tab, "P1").isSelected());
        System.out.println("Popup opened on P1; selection: P0=" + p0Selected + ", P1=" + p1Selected);
        gui.clickPopupItem("Delete");

        var marking = gui.marking(tab);
        System.out.println("Marking after popup Delete on P1: " + marking);
        assertEquals(Map.of("P0", 1), marking,
            "Delete in P1's popup menu should remove P1 and keep P0 (selection before the right click: P0)");
    }
}
