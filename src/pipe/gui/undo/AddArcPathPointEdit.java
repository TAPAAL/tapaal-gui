/*
 * AddArcPathPointEdit.java
 */

package pipe.gui.undo;

import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPath;
import pipe.gui.graphicElements.ArcPathPoint;
import dk.aau.cs.gui.undo.Command;

/**
 * 
 * @author Pere Bonet
 */
public class AddArcPathPointEdit extends Command {

	ArcPath arcPath;
	ArcPathPoint point;
	Integer index;

	/** Creates a new instance of AddArcPathPointEdit */
	public AddArcPathPointEdit(Arc _arc, ArcPathPoint _point) {
		arcPath = _arc.getArcPath();
		point = _point;
		index = point.getIndex();
	}

	/**
    *
    */
	@Override
	public void undo() {
		point.delete();
	}

	/** */
	@Override
	public void redo() {
		arcPath.insertPoint(index, point);
		arcPath.updateArc();
	}

}
