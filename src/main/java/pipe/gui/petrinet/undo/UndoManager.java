/*
 * UndoManager.java
 */
package pipe.gui.petrinet.undo;

import java.util.ArrayList;

import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.helpers.Reference.MutableReference;
import net.tapaal.helpers.Reference.Reference;
import net.tapaal.gui.GuiFrameActions;
import pipe.gui.Constants;
import pipe.gui.petrinet.PetriNetTab;

/**
 * Class to handle undo & redo functionality
 * 
 * @author pere
 */
public class UndoManager {

	private static final int UNDO_BUFFER_CAPACITY = Constants.DEFAULT_BUFFER_CAPACITY;
    private final PetriNetTab tab;

    private int indexOfNextAdd = 0;
	private int sizeOfBuffer = 0;
	private int startOfBuffer = 0;
	private int undoneEdits = 0;

	private final ArrayList<ArrayList<Command>> edits = new ArrayList<ArrayList<Command>>(UNDO_BUFFER_CAPACITY);

	private Reference<GuiFrameActions> app = new MutableReference<>();
	public void setApp(Reference<GuiFrameActions> app) {
		this.app = app;

		// Undo/Redo is enabled based on undo/redo manager
		setUndoRedoStatus();
	}

	/**
	 * Creates a new instance of UndoManager
	 */
	public UndoManager(PetriNetTab tab) {
        this.tab = tab;

        // Initialize the buffer
		for (int i = 0; i < UNDO_BUFFER_CAPACITY; i++) {
			edits.add(null);
		}
	}

	public void redo() {

		if (undoneEdits > 0) {

			// The currentEdit to redo
			for (Command command : edits.get(indexOfNextAdd)) {
				command.redo();
			}
			indexOfNextAdd = (indexOfNextAdd + 1) % UNDO_BUFFER_CAPACITY;
			sizeOfBuffer++;
			undoneEdits--;
			if (undoneEdits == 0) {
				app.ifPresent(a -> a.setRedoActionEnabled(false));
			}
			app.ifPresent(a -> a.setUndoActionEnabled(true));
		}
	}

	public void setUndoRedoStatus() {

		boolean canRedo = (undoneEdits != 0);
		app.ifPresent(a -> a.setRedoActionEnabled(canRedo));

		boolean canUndo = sizeOfBuffer != 0;
		app.ifPresent(a -> a.setUndoActionEnabled(canUndo));

	}

	public void undo() {

		if (sizeOfBuffer > 0) {

			if (--indexOfNextAdd < 0) {
				indexOfNextAdd += UNDO_BUFFER_CAPACITY;
			}
			sizeOfBuffer--;
			undoneEdits++;

			// The currentEdit to undo (reverse order)
			ArrayList<Command> currentEdit = edits.get(indexOfNextAdd);
    
			for (int i = currentEdit.size() - 1; i >= 0; i--) {
				currentEdit.get(i).undo();
			}

			if (sizeOfBuffer == 0) {
				app.ifPresent(a -> a.setUndoActionEnabled(false));
			}
			app.ifPresent(a -> a.setRedoActionEnabled(true));
		}
	}

	public void clear() {
		indexOfNextAdd = 0;
		sizeOfBuffer = 0;
		startOfBuffer = 0;
		undoneEdits = 0;
		app.ifPresent(a -> a.setUndoActionEnabled(false));
		app.ifPresent(a -> a.setRedoActionEnabled(false));
	}

	public void newEdit() {
		ArrayList<Command> lastEdit = edits.get(currentIndex());
		if ((lastEdit != null) && (lastEdit.isEmpty())) {
			return;
		}

		undoneEdits = 0;
		app.ifPresent(a -> a.setUndoActionEnabled(true));
		app.ifPresent(a -> a.setRedoActionEnabled(false));

        //XXX this is properly not the place to set net changed, can be null as also used in batch processor undo/redo
        if (tab != null) {
            tab.setNetChanged(true);
        }

		ArrayList<Command> compoundEdit = new ArrayList<Command>();
		edits.set(indexOfNextAdd, compoundEdit);
		indexOfNextAdd = (indexOfNextAdd + 1) % UNDO_BUFFER_CAPACITY;
		if (sizeOfBuffer < UNDO_BUFFER_CAPACITY) {
			sizeOfBuffer++;
		} else {
			startOfBuffer = (startOfBuffer + 1) % UNDO_BUFFER_CAPACITY;
		}
	}

	public void addEdit(Command undoableEdit) {
		ArrayList<Command> compoundEdit = edits.get(currentIndex());
		compoundEdit.add(undoableEdit);
		// debug();
	}

	public void addNewEdit(Command undoableEdit) {
		newEdit(); // mark for a new "transtaction""
		addEdit(undoableEdit);
	}


	private int currentIndex() {
		int lastAdd = indexOfNextAdd - 1;
		if (lastAdd < 0) {
			lastAdd += UNDO_BUFFER_CAPACITY;
		}
		return lastAdd;
	}

    public void removeCurrentEdit() {
        if (sizeOfBuffer > 0 && currentIndex() >= 0 && currentIndex() < edits.size()) {
            edits.set(currentIndex(), null);
            --sizeOfBuffer;
            --indexOfNextAdd;
        }

        setUndoRedoStatus();
    }

    public boolean currentEditIsEmpty() {
        return edits.get(currentIndex()).isEmpty();
    }
}
