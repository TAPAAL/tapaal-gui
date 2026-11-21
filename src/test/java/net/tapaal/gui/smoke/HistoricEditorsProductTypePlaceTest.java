package net.tapaal.gui.smoke;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;
import net.tapaal.gui.petrinet.editor.ConstantsPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Point;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static net.tapaal.gui.smoke.GuiHarness.find;
import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoricEditorsProductTypePlaceTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void acceptsTheTupleMarkingAfterExtendingItsProcessColorType() throws Exception {
        gui.startEmpty();
        gui.menu("File", "Example nets", "Colored P/T nets", "token-ring");
        var tab = gui.currentTab();
        var marking = gui.marking(tab);
        assertEquals(6, marking.get("state"));
        var tuples = gui.onEdt(() -> stateTuples(tab));

        var declarations = gui.onEdt(() -> find(tab, ConstantsPane.class, pane -> true));
        var types = gui.onEdt(() -> find(declarations, JList.class, list -> true));
        gui.click(gui.onEdt(() -> {
            // Process has a long label. Click within the portion of its row inside the viewport.
            var visibleRow = types.getCellBounds(1, 1).intersection(types.getVisibleRect());
            assertFalse(visibleRow.isEmpty(), "The Process row must be visible");
            var point = new Point(visibleRow.x + visibleRow.width / 2, visibleRow.y + visibleRow.height / 2);
            SwingUtilities.convertPointToScreen(point, types);
            return point;
        }));
        assertEquals("Process", gui.onEdt(() -> ((ColorType) types.getSelectedValue()).getName()));
        gui.clickButton(declarations, "Edit");
        var colorEditor = gui.awaitDialog("Edit color type");
        gui.typeInto(gui.onEdt(() -> labelled(colorEditor, "Element name: ", JTextField.class)), "process6");
        gui.clickButton(colorEditor, "Add");
        gui.clickButton(colorEditor, "OK");
        gui.awaitClosed(colorEditor);

        gui.onEdt(() -> {
            var process = tab.network().getColorTypeByName("Process");
            assertNotNull(process.getColorByName("process6"), "The new color must be committed");
            return null;
        });

        // Launchpad #1988734
        gui.rightClickObject(tab, "state");
        gui.clickPopupItem("Edit Place");
        var placeEditor = gui.awaitDialog("Edit Place");
        gui.clickButton(placeEditor, "OK");
        gui.awaitClosed(placeEditor);

        assertEquals(marking, gui.marking(tab));
        assertEquals(tuples, gui.onEdt(() -> stateTuples(tab)));
    }

    private Map<List<String>, Long> stateTuples(PetriNetTab tab) {
        var state = place(tab, "state").underlyingPlace();
        var couple = assertInstanceOf(ProductType.class, state.getColorType());
        assertEquals("Couple", couple.getName());
        assertEquals(List.of("Process", "Process"), couple.getColorTypes().stream().map(ColorType::getName).toList());

        return state.tokens().stream().map(token -> {
            var color = token.getColor();
            assertEquals(couple, color.getColorType());
            assertNotNull(color.getTuple(), "Every token in state must have a tuple color");
            assertEquals(2, color.getTuple().size());
            return color.getTuple().stream().map(Color::getColorName).toList();
        }).collect(groupingBy(tuple -> tuple, counting()));
    }
}
