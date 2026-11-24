package net.tapaal.gui.smoke;

import dk.aau.cs.model.CPN.ColorType;
import net.tapaal.gui.TapnXmlComparison;
import net.tapaal.gui.petrinet.editor.ConstantsPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JList;
import java.util.List;

import static net.tapaal.gui.smoke.GuiHarness.dialogText;
import static net.tapaal.gui.smoke.GuiHarness.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoricEditorsColorTypeDependencyTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void rejectsRemovingAColorTypeReferencedByAVariable() throws Exception {
        var tab = gui.open("historic-editors-color-type-dependency/declarations.tapn");
        var declarations = gui.onEdt(() -> find(tab, ConstantsPane.class, pane -> true));
        var types = gui.onEdt(() -> find(declarations, JList.class, list -> true));
        gui.click(gui.centerOfRow(types, 1));
        var type = gui.onEdt(() -> (ColorType) types.getSelectedValue());
        assertEquals("A", gui.onEdt(type::getName));
        var before = gui.snapshot(tab);

        // Launchpad #1964931
        gui.clickButton(declarations, "Remove");
        var warning = gui.awaitDialog("Could not remove color type");
        var message = gui.onEdt(() -> dialogText(warning));
        assertTrue(message.contains("Color type A is used in variable x"), message);
        gui.clickButton(warning, "OK");
        gui.awaitClosed(warning);

        gui.onEdt(() -> {
            assertSame(type, types.getSelectedValue());
            assertEquals(List.of("dot", "A"), tab.network().colorTypes().stream().map(ColorType::getName).toList());
            var variables = tab.network().variables();
            assertEquals(1, variables.size());
            var variable = variables.get(0);
            assertEquals("x", variable.getName());
            assertSame(type, variable.getColorType());
            return null;
        });

        var differences = TapnXmlComparison.differences(before, gui.snapshot(tab));
        assertTrue(differences.isEmpty(), () -> String.join("\n", differences));
    }
}
