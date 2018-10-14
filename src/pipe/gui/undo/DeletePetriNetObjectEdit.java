/*
 * DeletePetriNetObjectEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.PetriNetObject;
import dk.aau.cs.gui.undo.Command;

/**
 * 
 * @author Pere Bonet
 */
public class DeletePetriNetObjectEdit extends Command {

	PetriNetObject pnObject;
	DataLayer model;
	DrawingSurfaceImpl view;
	Object[] objects;

	/** Creates a new instance of placeWeightEdit */
	public DeletePetriNetObjectEdit(PetriNetObject _pnObject,
			DrawingSurfaceImpl _view, DataLayer _model) {
		pnObject = _pnObject;
		view = _view;
		model = _model;
	}

	/** */
	@Override
	public void redo() {
		pnObject.delete();
		view.repaint();
	}

	/** */
	@Override
	public void undo() {
		pnObject.undelete(view);
		view.repaint();
	}

	@Override
	public String toString() {
		return super.toString() + " " + pnObject.getClass().getSimpleName()
				+ " [" + pnObject.getId() + "]";
	}

}
