package pipe.gui.petrinet.undo;

import net.tapaal.gui.petrinet.undo.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void emptyTransactionsDoNotCountAsAppliedEdits() {
        var undoManager = new UndoManager(null);

        undoManager.newEdit();

        assertTrue(undoManager.currentEditIsEmpty());
        assertFalse(undoManager.hasAppliedNormalEdits());

        undoManager.removeCurrentEdit();

        assertFalse(undoManager.hasAppliedNormalEdits());
    }

    @Test
    void undoDoesNotConsumeAnOlderEditWhenATransactionIsEmpty() {
        var undoManager = new UndoManager(null);
        undoManager.addNewEdit(NO_OP);
        undoManager.undo();
        undoManager.newEdit();

        undoManager.undo();

        assertFalse(undoManager.hasAppliedNormalEdits());
        undoManager.redo();
        assertTrue(undoManager.hasAppliedNormalEdits());
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

    @Test
    void wrappedHistoryCanBeUndoneAndRedoneWithoutOverflowing() {
        var undoManager = new UndoManager(null);
        int[] value = {0};

        for (int i = 0; i <= pipe.gui.Constants.DEFAULT_BUFFER_CAPACITY; i++) {
            Command edit = new Command() {
                @Override
                public void undo() {
                    value[0]--;
                }

                @Override
                public void redo() {
                    value[0]++;
                }
            };
            edit.redo();
            undoManager.addNewEdit(edit);
        }

        undoManager.undoAll();
        assertEquals(1, value[0]);

        for (int i = 0; i < pipe.gui.Constants.DEFAULT_BUFFER_CAPACITY; i++) {
            undoManager.redo();
        }

        assertEquals(pipe.gui.Constants.DEFAULT_BUFFER_CAPACITY + 1, value[0]);
    }
}
