package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JPopupMenu;

import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.Transition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class TAPNTransitionHandler extends TransitionHandler {

	public TAPNTransitionHandler(Transition obj) {
		super(obj);
	}


	// overwrite to remove shift behaviour
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		if (!(CreateGui.getApp().isEditionAllowed()) || e.isControlDown() || !(myObject.isSelected())) {
			return;
		}

		int rotation = 0;
		if (e.getWheelRotation() < 0) {
			rotation = -e.getWheelRotation() * 135;
		} else {
			rotation = e.getWheelRotation() * 45;
		}
		CreateGui.getDrawingSurface().getUndoManager().addNewEdit(
				((Transition) myObject).rotate(rotation));
	}

	/**
	 * Creates the popup menu that the user will see when they right click on a
	 * component
	 */
	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		JPopupMenu popup = super.getPopup(e);

		return popup;
	}

}
