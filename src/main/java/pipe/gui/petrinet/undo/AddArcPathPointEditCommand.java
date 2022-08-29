/*
 * AddArcPathPointEdit.java
 */

package pipe.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.ArcPath;
import pipe.gui.petrinet.graphicElements.ArcPathPoint;
import net.tapaal.gui.petrinet.undo.Command;

/**
 * 
 * @author Pere Bonet
 */
public class AddArcPathPointEditCommand extends Command {

	final ArcPath arcPath;
	final ArcPathPoint point;
	private final DataLayer guiModel;
	final Integer index;

	/** Creates a new instance of AddArcPathPointEdit */
	public AddArcPathPointEditCommand(Arc _arc, ArcPathPoint _point, DataLayer guiModel) {
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
