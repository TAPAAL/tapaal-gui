/*
 * UndoManager.java
 */
package pipe.gui.undo;

import java.util.ArrayList;
import java.util.Iterator;

import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.GuiFrame;
import pipe.gui.Pipe;
import pipe.gui.graphicElements.*;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import dk.aau.cs.gui.undo.Command;
import pipe.gui.handler.PetriNetObjectHandler;
import pipe.gui.handler.PlaceTransitionObjectHandler;

/**
 * Class to handle undo & redo functionality
 * 
 * @author pere
 */
public class UndoManager {

	private static int UNDO_BUFFER_CAPACITY = Pipe.DEFAULT_BUFFER_CAPACITY;

	private int indexOfNextAdd = 0;
	private int sizeOfBuffer = 0;
	private int startOfBuffer = 0;
	private int undoneEdits = 0;

	private ArrayList<ArrayList<Command>> edits = new ArrayList<ArrayList<Command>>(UNDO_BUFFER_CAPACITY);

	private DrawingSurfaceImpl view;
	private DataLayer guiModel;
	private GuiFrame app;

	public void setModel(DataLayer guiModel) {
		this.guiModel = guiModel;
	}

	/**
	 * Creates a new instance of UndoManager
	 */
	public UndoManager(DrawingSurfaceImpl _view, DataLayer _model, GuiFrame _app) {
		view = _view;
		guiModel = _model;
		app = _app;
		app.setUndoActionEnabled(false);
		app.setRedoActionEnabled(false);
		for (int i = 0; i < UNDO_BUFFER_CAPACITY; i++) {
			edits.add(null);
		}
	}

	public void redo() {

		if (undoneEdits > 0) {
			checkArcBeingDrawn();
			checkMode();

			// The currentEdit to redo
			for (Command command : edits.get(indexOfNextAdd)) {
				command.redo();
			}
			indexOfNextAdd = (indexOfNextAdd + 1) % UNDO_BUFFER_CAPACITY;
			sizeOfBuffer++;
			undoneEdits--;
			if (undoneEdits == 0) {
				app.setRedoActionEnabled(false);
			}
			app.setUndoActionEnabled(true);
		}
	}

	public void setUndoRedoStatus() {

		boolean canRedo = (undoneEdits != 0);
		app.setRedoActionEnabled(canRedo);

		boolean canUndo = sizeOfBuffer != 0;
		app.setUndoActionEnabled(canUndo);

	}

	public void undo() {

		if (sizeOfBuffer > 0) {
			checkArcBeingDrawn();
			checkMode();

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
				app.setUndoActionEnabled(false);
			}
			app.setRedoActionEnabled(true);
		}
	}

	public void clear() {
		indexOfNextAdd = 0;
		sizeOfBuffer = 0;
		startOfBuffer = 0;
		undoneEdits = 0;
		app.setUndoActionEnabled(false);
		app.setRedoActionEnabled(false);
	}

	public void newEdit() {
		ArrayList<Command> lastEdit = edits.get(currentIndex());
		if ((lastEdit != null) && (lastEdit.isEmpty())) {
			return;
		}

		undoneEdits = 0;
		app.setUndoActionEnabled(true);
		app.setRedoActionEnabled(false);
		CreateGui.getCurrentTab().setNetChanged(true);

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

	// removes the arc currently being drawn if any
	private void checkArcBeingDrawn() {

		if (CreateGui.getDrawingSurface().createArc != null) {
			PlaceTransitionObjectHandler.cleanupArc(CreateGui.getDrawingSurface().createArc, CreateGui.getDrawingSurface());
		}
	}

	private void checkMode() {
		if ((app.getMode() == Pipe.ElementType.FAST_PLACE)
				|| (app.getMode() == Pipe.ElementType.FAST_TRANSITION)) {
			app.endFastMode();
		}
	}


}
