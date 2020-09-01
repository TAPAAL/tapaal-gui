package pipe.gui.action;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import pipe.gui.CreateGui;
import pipe.gui.Zoomer;
import pipe.gui.graphicElements.Arc;

/**
 * This class is used to split an arc in two at the point the user clicks the
 * mouse button.
 * 
 * @author Pere Bonet
 */
public class SplitArcAction extends javax.swing.AbstractAction {

	private final Arc selected;
	private final Point2D.Double mouseposition;

	public SplitArcAction(Arc arc, Point mousepos) {
		selected = arc;

		// Mousepos is relative to selected component i.e. the arc
		// Need to convert this into actual coordinates
		Point2D.Double offset = new Point2D.Double(selected.getX(), selected.getY());
		mouseposition = new Point2D.Double(
            Zoomer.getUnzoomedValue(mousepos.x + offset.x, arc.getZoom()),
            Zoomer.getUnzoomedValue(mousepos.y + offset.y, arc.getZoom())
        );
	}

	public void actionPerformed(ActionEvent arg0) {
		CreateGui.getCurrentTab().getUndoManager().addNewEdit(
				selected.getArcPath().insertPoint(mouseposition, false)
        );
		selected.getArcPath().showPoints();
		// selected.split(mouseposition));
	}

}
