/*
 * UndoManager.java
 */
package pipe.gui.undo;

import java.util.ArrayList;
import java.util.Iterator;

import pipe.dataLayer.AnnotationNote;
import pipe.dataLayer.Arc;
import pipe.dataLayer.ArcPathPoint;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedInhibitorArcComponent;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedOutputArcComponent;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.TransportArcComponent;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.GuiFrame;
import pipe.gui.Pipe;
import dk.aau.cs.gui.undo.Command;

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

	private ArrayList<ArrayList<Command>> edits = new ArrayList<ArrayList<Command>>(
			UNDO_BUFFER_CAPACITY);

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
			Iterator<Command> currentEdit = edits.get(indexOfNextAdd)
			.iterator();
			while (currentEdit.hasNext()) {
				currentEdit.next().redo();
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
		view.setNetChanged(true);

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

	public void deleteSelection(PetriNetObject pnObject) {
		if(pnObject instanceof PlaceTransitionObject){
			PlaceTransitionObject pto = (PlaceTransitionObject)pnObject;

			for(Arc arc : pto.getPreset()){
				deleteObject(arc);
			}

			for(Arc arc : pto.getPostset()){
				deleteObject(arc);
			}
		}

		deleteObject(pnObject);
	}

	public void deleteSelection(ArrayList<PetriNetObject> selection) {
		for (PetriNetObject pnObject : selection) {
			deleteSelection(pnObject);
		}
	}

	public void translateSelection(ArrayList<PetriNetObject> objects,
			int transX, int transY) {
		newEdit(); // new "transaction""
		Iterator<PetriNetObject> iterator = objects.iterator();
		while (iterator.hasNext()) {
			addEdit(new TranslatePetriNetObjectEdit(iterator.next(), transX,
					transY));
		}
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
		Arc arcBeingDrawn = view.createArc;
		if (arcBeingDrawn != null) {
			if (arcBeingDrawn.getParent() != null) {
				arcBeingDrawn.getParent().remove(arcBeingDrawn);
			}
			view.createArc = null;
		}
	}

	private void checkMode() {
		if ((app.getMode() == Pipe.FAST_PLACE)
				|| (app.getMode() == Pipe.FAST_TRANSITION)) {
			app.resetMode();
		}
	}

	private void deleteObject(PetriNetObject pnObject) {
		if (pnObject instanceof ArcPathPoint) {
			if (!((ArcPathPoint) pnObject).getArcPath().getArc().isSelected()) {
				addEdit(new DeleteArcPathPointEdit(((ArcPathPoint) pnObject)
						.getArcPath().getArc(), (ArcPathPoint) pnObject,
						((ArcPathPoint) pnObject).getIndex()));
			}
		}else{
			//		} else if (pnObject instanceof PlaceTransitionObject) {
			//			//
			//			Iterator<Arc> arcsTo = ((PlaceTransitionObject) pnObject)
			//			.getConnectToIterator();
			//			while (arcsTo.hasNext()) {
			//				Arc anArc = arcsTo.next();
			//				if (!anArc.isDeleted()) {
			//					addEdit(new DeletePetriNetObjectEdit(anArc, view, guiModel));
			//				}
			//			}
			//			//
			//			Iterator<Arc> arcsFrom = ((PlaceTransitionObject) pnObject)
			//			.getConnectFromIterator();
			//			while (arcsFrom.hasNext()) {
			//				Arc anArc = arcsFrom.next();
			//				if (!anArc.isDeleted()) {
			//					addEdit(new DeletePetriNetObjectEdit(anArc, view, guiModel));
			//				}
			//			}
			//		}

			if (!pnObject.isDeleted()) {
				if(pnObject instanceof TimedPlaceComponent){
					TimedPlaceComponent tp = (TimedPlaceComponent)pnObject;
					addEdit(new DeleteTimedPlaceCommand(tp, tp.underlyingPlace().model(), guiModel, view));
				}else if(pnObject instanceof TimedTransitionComponent){
					TimedTransitionComponent transition = (TimedTransitionComponent)pnObject;
					addEdit(new DeleteTimedTransitionCommand(transition, transition.underlyingTransition().model(), guiModel, view));
				}else if(pnObject instanceof TimedInputArcComponent){
					TimedInputArcComponent tia = (TimedInputArcComponent)pnObject;
					addEdit(new DeleteTimedInputArcCommand(tia, tia.underlyingTimedInputArc().model(), guiModel, view));
				}else if(pnObject instanceof TimedOutputArcComponent){
					TimedOutputArcComponent toa = (TimedOutputArcComponent)pnObject;
					addEdit(new DeleteTimedOutputArcCommand(toa, toa.underlyingArc().model(), guiModel, view));
				}else if(pnObject instanceof TransportArcComponent){
					TransportArcComponent transportArc = (TransportArcComponent)pnObject;
					addEdit(new DeleteTransportArcCommand(transportArc, transportArc.underlyingTransportArc(), transportArc.underlyingTransportArc().model(), guiModel, view));
				}else if(pnObject instanceof TimedInhibitorArcComponent){
					TimedInhibitorArcComponent tia = (TimedInhibitorArcComponent)pnObject;
					addEdit(new DeleteTimedInhibitorArcCommand(tia, tia.underlyingTimedInputArc().model(), guiModel, view));
				}else if(pnObject instanceof AnnotationNote){
					addEdit(new DeletePetriNetObjectEdit(pnObject, view, guiModel));
				}else{
					throw new RuntimeException("This should not be possible");
				}
				pnObject.delete();
			}
		}
	}
}
