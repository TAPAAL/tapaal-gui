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
	private final DataLayer guiModel;
	final Integer index;

	/** Creates a new instance of AddArcPathPointEdit */
	public AddArcPathPointEdit(Arc _arc, ArcPathPoint _point, DataLayer guiModel) {
		arcPath = _arc.getArcPath();
		point = _point;
		this.guiModel = guiModel;
		index = point.getIndex();
	}

	/**
    *
    */
	@Override
	public void undo() {
		arcPath.deletePoint(point);
		arcPath.updateArc();

		guiModel.removePetriNetObject(point);
	}

	/** */
	@Override
	public void redo() {
		//guiModel.addPetriNetObject(point);

		arcPath.insertPoint(index, point);
		arcPath.updateArc();
	}

}
