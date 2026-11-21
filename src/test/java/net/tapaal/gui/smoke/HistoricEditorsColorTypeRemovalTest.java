package net.tapaal.gui.smoke;

import dk.aau.cs.model.CPN.ColorType;
import net.tapaal.gui.petrinet.editor.ConstantsPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JButton;
import javax.swing.JList;
import java.util.List;
import java.util.stream.IntStream;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoricEditorsColorTypeRemovalTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void removingTheLastColorTypeDisablesStaleMoveControls() throws Exception {
        var tab = gui.open("historic-editors-color-type-removal/declarations.tapn");
        var declarations = gui.onEdt(() -> find(tab, ConstantsPane.class, pane -> true));
        var types = gui.onEdt(() -> find(declarations, JList.class, list -> true));
        var moveUp = gui.onEdt(() -> find(declarations, JButton.class,
            button -> "Move the selected color type up".equals(button.getToolTipText())));
        var moveDown = gui.onEdt(() -> find(declarations, JButton.class,
            button -> "Move down the selected color type".equals(button.getToolTipText())));

        assertEquals(List.of("dot", "A", "B"), gui.onEdt(() -> typeNames(types)));
        gui.click(gui.centerOfRow(types, 2));
        gui.onEdt(() -> {
            assertEquals("B", ((ColorType) types.getSelectedValue()).getName());
            assertTrue(moveUp.isEnabled(), "B can move up before removal");
            assertFalse(moveDown.isEnabled(), "B is already the last type");
            return null;
        });

        // Launchpad #1932271
        gui.clickButton(declarations, "Remove");
        assertTypeRemoved(types, moveUp, moveDown);

        // Redo removes the type through the same command, bypassing the Remove button's refresh.
        gui.menu("Edit", "Undo");
        assertEquals(List.of("dot", "A", "B"), gui.onEdt(() -> typeNames(types)));
        gui.click(gui.centerOfRow(types, 1));
        gui.click(gui.centerOfRow(types, 2));
        assertTrue(gui.onEdt(moveUp::isEnabled), "B can move up after undo");
        gui.menu("Edit", "Redo");
        assertTypeRemoved(types, moveUp, moveDown);
    }

    private void assertTypeRemoved(JList<?> types, JButton moveUp, JButton moveDown) throws Exception {
        gui.onEdt(() -> {
            assertEquals(List.of("dot", "A"), typeNames(types));
            assertTrue(types.getSelectedValuesList().isEmpty(), "No remaining type should be selected");
            assertFalse(moveUp.isEnabled(), "An enabled up arrow would try to move the deleted type");
            assertFalse(moveDown.isEnabled(), "There is no selected type to move down");
            return null;
        });
    }

    private List<String> typeNames(JList<?> types) {
        var model = types.getModel();
        return IntStream.range(0, model.getSize())
            .mapToObj(index -> ((ColorType) model.getElementAt(index)).getName())
            .toList();
    }
}
