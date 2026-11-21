package net.tapaal.gui.smoke;

import dk.aau.cs.model.tapn.Constant;
import net.tapaal.gui.petrinet.editor.ConstantsPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HistoricEditorsGlobalConstantTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void createsAndSavesAnIntegerConstantInAnUncoloredNet() throws Exception {
        var tab = gui.open("historic-editors-constant/flow.tapn");
        var constants = gui.onEdt(() -> find(tab, ConstantsPane.class, pane -> true));
        var list = gui.onEdt(() -> find(constants, JList.class, component -> true));
        int previousSize = gui.onEdt(() -> list.getModel().getSize());

        // Launchpad #1932286
        gui.clickButton(constants, "New");
        var dialog = gui.awaitDialog("Edit Constant");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Name: ", JTextField.class)), "retryLimit");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Value: ", JSpinner.class)), "7");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        gui.onEdt(() -> {
            assertEquals(previousSize + 1, list.getModel().getSize());
            var constant = assertInstanceOf(Constant.class, list.getModel().getElementAt(previousSize));
            assertEquals("retryLimit", constant.name());
            assertEquals(7, constant.value());
            return null;
        });

        gui.menu("File", "Save");
        gui.assertSavedMatches("historic-editors-constant/expected.tapn");
    }
}
