/*
 * TranslatePetriNetObjectEdit.java
 */

package pipe.gui.undo;

import pipe.gui.Zoomer;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.graphicElements.PetriNetObject;
import dk.aau.cs.gui.undo.Command;

/**
 * 
 * @author Pere Bonet
 */
public class TranslatePetriNetObjectEdit extends Command {

	final PetriNetObject pnObject;
	final Integer transX;
	final Integer transY;
	final DrawingSurfaceImpl ds;

	/** Creates a new instance of */
	public TranslatePetriNetObjectEdit(PetriNetObject _pnObject, Integer _transX, Integer _transY, DrawingSurfaceImpl drawingSurface) {
		pnObject = _pnObject;
		transX = Zoomer.getUnzoomedValue(_transX, pnObject.getZoom());
		transY = Zoomer.getUnzoomedValue(_transY, pnObject.getZoom());
		ds = drawingSurface;
	}

	/** */
	@Override
	public void undo() {
		pnObject.translate(Zoomer.getZoomedValue(-transX, pnObject.getZoom()), Zoomer.getZoomedValue(-transY, pnObject.getZoom()));
        ds.updatePreferredSize();
	}

	/** */
	@Override
	public void redo() {
        pnObject.translate(Zoomer.getZoomedValue(transX, pnObject.getZoom()), Zoomer.getZoomedValue(transY, pnObject.getZoom()));
        ds.updatePreferredSize();
	}

	@Override
	public String toString() {
		return super.toString() + " " + pnObject.getName() + " (" + transX + "," + transY + ")";
	}

}
