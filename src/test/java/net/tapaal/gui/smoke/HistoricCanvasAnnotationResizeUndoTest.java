package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.graphicElements.AnnotationNote.ResizePoint;

import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Comparator;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoricCanvasAnnotationResizeUndoTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void undoRestoresTheOriginalAnnotationSizeWithoutRemovingIt() throws Exception {
        // Launchpad 1887759: the first Undo after dragging a resize handle must undo the drag.
        var tab = gui.open("canvas-workbench/before.tapn");
        String caption = "Waiting for input";

        gui.selectTool(tab, "Annotation");
        gui.clickCanvas(tab, 195, 165);
        var editor = gui.awaitDialog("Edit Annotation");
        var field = gui.onEdt(() -> find(editor, JTextArea.class, JTextArea::isEditable));
        gui.typeInto(field, caption);
        gui.clickButton(editor, "OK");
        gui.awaitClosed(editor);

        var before = gui.onEdt(() -> annotationBounds(tab, caption));
        gui.selectTool(tab, "Select");
        var note = gui.onEdt(() -> tab.currentTemplate().guiModel().getLabels()[0]);
        gui.click(gui.centerOf(() -> note));
        assertTrue(gui.onEdt(note::isSelected), "Selecting the note should show its resize handles");
        var bottomRight = gui.centerOf(() -> Arrays.stream(note.getComponents())
            .filter(ResizePoint.class::isInstance)
            .map(ResizePoint.class::cast)
            .max(Comparator.comparingInt(Component::getX).thenComparingInt(Component::getY))
            .orElseThrow());
        gui.drag(bottomRight, 90, 60);

        var resized = gui.onEdt(() -> annotationBounds(tab, caption));
        assertTrue(resized.width > before.width && resized.height > before.height,
            () -> "Dragging the bottom-right handle should enlarge the note: " + before + " -> " + resized);
        assertEquals(before.getLocation(), resized.getLocation());

        gui.keys("ctrl Z");
        assertEquals(before, gui.onEdt(() -> annotationBounds(tab, caption)));
        gui.keys("ctrl Y");
        assertEquals(resized, gui.onEdt(() -> annotationBounds(tab, caption)));
    }

    private static Rectangle annotationBounds(PetriNetTab tab, String caption) {
        var notes = tab.currentTemplate().guiModel().getLabels();
        assertEquals(1, notes.length, "The annotation must remain on the canvas");
        assertTrue(notes[0].isShowing());
        assertEquals(caption, notes[0].getText());
        return notes[0].getBounds();
    }
}
