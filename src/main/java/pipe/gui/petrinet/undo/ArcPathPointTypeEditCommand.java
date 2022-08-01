/*
 * ArcPathPointTypeEdit.java
 */

package pipe.gui.petrinet.undo;

import pipe.gui.petrinet.graphicElements.ArcPathPoint;
import net.tapaal.gui.petrinet.undo.Command;

/**
 * 
 * @author corveau
 */
public class ArcPathPointTypeEditCommand extends Command {

	final ArcPathPoint arcPathPoint;

	/** Creates a new instance of placeWeightEdit */
	public ArcPathPointTypeEditCommand(ArcPathPoint _arcPathPoint) {
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
