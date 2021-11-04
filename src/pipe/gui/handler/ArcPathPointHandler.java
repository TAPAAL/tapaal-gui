/*
 * Created on 28-Feb-2004
 * Author is Michael Camacho
 *
 */
package pipe.gui.handler;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.gui.CreateGui;
import pipe.gui.action.SplitArcPointAction;
import pipe.gui.action.ToggleArcPointAction;
import pipe.gui.graphicElements.ArcPathPoint;

public class ArcPathPointHandler extends PetriNetObjectHandler {

	public ArcPathPointHandler(ArcPathPoint obj) {
		super(obj);
	}

	/**
	 * Creates the popup menu that the user will see when they right click on a
	 * component
	 */
	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		JPopupMenu popup = super.getPopup(e);

		if (!((ArcPathPoint) myObject).isDeleteable()) {
			popup.getComponent(0).setEnabled(false);
		}

		popup.insert(new JPopupMenu.Separator(), 0);

		if (((ArcPathPoint) myObject).getIndex() == 0) {
			return popup;
		} else {
			JMenuItem menuItem = new JMenuItem(new ToggleArcPointAction((ArcPathPoint) myObject));

			if (((ArcPathPoint) myObject).getPointType() == ArcPathPoint.STRAIGHT) {
				menuItem.setText("Change to Curved");
			} else {
				menuItem.setText("Change to Straight");
			}
			popup.insert(menuItem, 0);

			menuItem = new JMenuItem(new SplitArcPointAction((ArcPathPoint) myObject));
			menuItem.setText("Split Point");
			popup.add(menuItem, 1);

		}
		return popup;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (myObject.isEnabled()) {
			((ArcPathPoint) e.getComponent()).setVisibilityLock(true);
			super.mousePressed(e);
		}
	}

    @Override
    public void mouseDragged(MouseEvent e) {
	    //Can't drag endpoint at its broken
        if (!((ArcPathPoint) myObject).isEndPoint()) {
            super.mouseDragged(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        ((ArcPathPoint) myObject).getArcPath().showPoints();
    }

    @Override
	public void mouseReleased(MouseEvent e) {
		((ArcPathPoint) e.getComponent()).setVisibilityLock(false);
		super.mouseReleased(e);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isShiftDown()) {
		    CreateGui.getCurrentTab().getUndoManager().addNewEdit(
			    ((ArcPathPoint) myObject).togglePointType()
            );
		}
	}

}
