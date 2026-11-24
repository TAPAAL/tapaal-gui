package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import pipe.gui.petrinet.PetriNetTab;
import javax.swing.JTextArea;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasAnnotationCreationTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void committedAnnotationTextSurvivesUndoAndRedo() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Annotation");
        gui.clickCanvas(tab, 195, 165);
        var editor = gui.awaitDialog("Edit Annotation");
        var text = gui.onEdt(() -> GuiHarness.find(editor, JTextArea.class, JTextArea::isEditable));
        gui.typeInto(text, "Waiting for input");
        gui.clickButton(editor, "OK");
        gui.awaitClosed(editor);
        assertEquals(List.of("Waiting for input"), annotations(tab));

        gui.menu("Edit", "Undo");
        assertEquals(List.of(), annotations(tab));
        gui.menu("Edit", "Redo");
        assertEquals(List.of("Waiting for input"), annotations(tab));
    }

    private List<String> annotations(PetriNetTab tab) throws Exception {
        return gui.onEdt(() -> Arrays.stream(tab.currentTemplate().guiModel().getLabels())
            .map(note -> note.getText()).toList());
    }
}
