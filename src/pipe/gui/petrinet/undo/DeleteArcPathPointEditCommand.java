/*
 * DeleteArcPathPointEdit.java
 */

package pipe.gui.petrinet.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.ArcPath;
import pipe.gui.petrinet.graphicElements.ArcPathPoint;
import net.tapaal.gui.petrinet.undo.Command;

/**
 * 
 * @author Pere Bonet
 */
public class DeleteArcPathPointEditCommand extends Command {

	ArcPath arcPath;
	ArcPathPoint point;
	Integer index;
	private final DataLayer guiModel;

	/** Creates a new instance of placeWeightEdit */
	public DeleteArcPathPointEditCommand(Arc _arc, ArcPathPoint _point, Integer _index, DataLayer guiModel) {
		arcPath = _arc.getArcPath();
		point = _point;
		index = _index;
		this.guiModel = guiModel;
	}

	/** */
	@Override
	public void undo() {
		//guiModel.addPetriNetObject(point);
	    point.deselect();
		arcPath.insertPoint(index, point);
		arcPath.updateArc();
	}

	/** */
	@Override
	public void redo() {
		guiModel.removePetriNetObject(point);
		arcPath.deletePoint(point);
		arcPath.updateArc();
	}

}
