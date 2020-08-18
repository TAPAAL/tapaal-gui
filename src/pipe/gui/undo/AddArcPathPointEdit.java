/*
 * AddArcPathPointEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPath;
import pipe.gui.graphicElements.ArcPathPoint;
import dk.aau.cs.gui.undo.Command;

/**
 * 
 * @author Pere Bonet
 */
public class AddArcPathPointEdit extends Command {

	final ArcPath arcPath;
	final ArcPathPoint point;
	final Integer index;

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
		arcPath.deletePoint(point);
		arcPath.updateArc();
	}

	/** */
	@Override
	public void redo() {
		arcPath.insertPoint(index, point);
		arcPath.updateArc();
	}

}
