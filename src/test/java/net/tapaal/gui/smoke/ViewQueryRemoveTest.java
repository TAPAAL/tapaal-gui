package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import pipe.gui.petrinet.PetriNetTab;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryRemoveTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void removeTargetsSelectedQueryAndUndoRestoresIt() throws Exception {
        var tab = gui.open("view-query-fixtures/queries.tapn");
        gui.menu("View", "Show simple workspace");
        var pane = gui.onEdt(tab::getQueryPane);
        var list = gui.onEdt(pane::getJList);
        assertEquals(List.of("Reach Start", "Avoid empty"), names(tab));

        gui.click(gui.centerOfRow(list, 1));
        gui.clickButton(pane, "Remove");
        assertEquals(List.of("Reach Start"), names(tab));
        gui.menu("Edit", "Undo");
        assertEquals(List.of("Reach Start", "Avoid empty"), names(tab));
        gui.menu("Edit", "Redo");
        assertEquals(List.of("Reach Start"), names(tab));
    }

    private List<String> names(PetriNetTab tab) throws Exception {
        return gui.onEdt(() -> {
            var names = new ArrayList<String>();
            for (TAPNQuery query : tab.queries()) {
                names.add(query.getName());
            }
            return names;
        });
    }
}
