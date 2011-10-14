/*
 * TranslatePetriNetObjectEdit.java
 */

package pipe.gui.undo;

import pipe.gui.graphicElements.PetriNetObject;
import dk.aau.cs.gui.undo.Command;

/**
 * 
 * @author Pere Bonet
 */
public class TranslatePetriNetObjectEdit extends Command {

	PetriNetObject pnObject;
	Integer transX;
	Integer transY;

	/** Creates a new instance of */
	public TranslatePetriNetObjectEdit(PetriNetObject _pnObject,
			Integer _transX, Integer _transY) {
		pnObject = _pnObject;
		transX = _transX;
		transY = _transY;
	}

	/** */
	@Override
	public void undo() {
		pnObject.translate(-transX, -transY);
	}

	/** */
	@Override
	public void redo() {
		pnObject.translate(transX, transY);
	}

	@Override
	public String toString() {
		return super.toString() + " " + pnObject.getName() + " (" + transX
				+ "," + transY + ")";
	}

}
