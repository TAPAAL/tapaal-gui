/*
 * ArcPathPointTypeEdit.java
 */

package pipe.gui.undo;

import pipe.gui.graphicElements.ArcPathPoint;
import dk.aau.cs.gui.undo.Command;

/**
 * 
 * @author corveau
 */
public class ArcPathPointTypeEdit extends Command {

	ArcPathPoint arcPathPoint;

	/** Creates a new instance of placeWeightEdit */
	public ArcPathPointTypeEdit(ArcPathPoint _arcPathPoint) {
		arcPathPoint = _arcPathPoint;
	}

	/** */
	@Override
	public void undo() {
		arcPathPoint.togglePointType();
	}

	/** */
	@Override
	public void redo() {
		arcPathPoint.togglePointType();
	}

	@Override
	public String toString() {
		return super.toString() + " " + arcPathPoint.getName();
	}

}
