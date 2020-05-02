/*
 * TranslatePetriNetObjectEdit.java
 */

package pipe.gui.undo;

import pipe.gui.canvas.DrawingSurfaceImpl;
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
	DrawingSurfaceImpl ds;

	/** Creates a new instance of */
	public TranslatePetriNetObjectEdit(PetriNetObject _pnObject,
                                       Integer _transX, Integer _transY, DrawingSurfaceImpl drawingSurface) {
		pnObject = _pnObject;
		transX = _transX;
		transY = _transY;
		ds = drawingSurface;
	}

	/** */
	@Override
	public void undo() {
		pnObject.translate(-transX, -transY);
        ds.updatePreferredSize();
	}

	/** */
	@Override
	public void redo() {
		pnObject.translate(transX, transY);
        ds.updatePreferredSize();
	}

	@Override
	public String toString() {
		return super.toString() + " " + pnObject.getName() + " (" + transX
				+ "," + transY + ")";
	}

}
