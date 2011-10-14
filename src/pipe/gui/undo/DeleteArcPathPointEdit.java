/*
 * DeleteArcPathPointEdit.java
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
public class DeleteArcPathPointEdit extends Command {

	ArcPath arcPath;
	ArcPathPoint point;
	Integer index;

	/** Creates a new instance of placeWeightEdit */
	public DeleteArcPathPointEdit(Arc _arc, ArcPathPoint _point, Integer _index) {
		arcPath = _arc.getArcPath();
		point = _point;
		index = _index;
	}

	/** */
	@Override
	public void undo() {
		arcPath.insertPoint(index, point);
		arcPath.updateArc();
	}

	/** */
	@Override
	public void redo() {
		point.delete();
	}

}
