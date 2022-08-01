/*
 * TranslatePetriNetObjectEdit.java
 */

package pipe.gui.petrinet.undo;

import pipe.gui.canvas.Zoomer;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.petrinet.graphicElements.PetriNetObject;
import net.tapaal.gui.petrinet.undo.Command;

/**
 * 
 * @author Pere Bonet
 */
public class TranslatePetriNetObjectEditCommand extends Command {

	final PetriNetObject pnObject;
	final Integer transX;
	final Integer transY;
	final DrawingSurfaceImpl ds;

	/** Creates a new instance of */
	public TranslatePetriNetObjectEditCommand(PetriNetObject _pnObject, Integer _transX, Integer _transY, DrawingSurfaceImpl drawingSurface) {
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
