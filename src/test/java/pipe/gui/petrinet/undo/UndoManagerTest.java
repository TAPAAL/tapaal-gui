package pipe.gui.petrinet.undo;

import net.tapaal.gui.petrinet.undo.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoManagerTest {

    private static final Command NO_OP = new Command() {
        @Override
        public void undo() {
        }

        @Override
        public void redo() {
        }
    };

    @Test
    void appliedNormalEditsFollowUndoAndRedo() {
        var undoManager = new UndoManager(null);

        assertFalse(undoManager.hasAppliedNormalEdits());

        undoManager.addNewEdit(NO_OP);
        assertTrue(undoManager.hasAppliedNormalEdits());

        undoManager.undo();
        assertFalse(undoManager.hasAppliedNormalEdits());

        undoManager.redo();
        assertTrue(undoManager.hasAppliedNormalEdits());
    }

    @Test
    void clearingTheHistoryResetsAppliedNormalEdits() {
        var undoManager = new UndoManager(null);
        undoManager.addNewEdit(NO_OP);

        undoManager.clear();

        assertFalse(undoManager.hasAppliedNormalEdits());
    }

    @Test
    void truncatedHistoryRemainsChangedAfterAllAvailableEditsAreUndone() {
        var undoManager = new UndoManager(null);

        for (int i = 0; i <= pipe.gui.Constants.DEFAULT_BUFFER_CAPACITY; i++) {
            undoManager.addNewEdit(NO_OP);
        }
        for (int i = 0; i < pipe.gui.Constants.DEFAULT_BUFFER_CAPACITY; i++) {
            undoManager.undo();
        }

        assertTrue(undoManager.hasAppliedNormalEdits());
    }
}
