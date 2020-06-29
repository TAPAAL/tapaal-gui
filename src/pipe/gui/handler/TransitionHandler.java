package pipe.gui.handler;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pipe.gui.CreateGui;
import pipe.gui.Zoomer;
import pipe.gui.Pipe.ElementType;
import pipe.gui.action.ShowHideInfoAction;
import pipe.gui.graphicElements.Transition;

/**
 * Class used to implement methods corresponding to mouse events on transitions.
 */
public class TransitionHandler extends PlaceTransitionObjectHandler implements
		java.awt.event.MouseWheelListener {

	public TransitionHandler(Transition obj) {
		super(obj);
	}

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

		CreateGui.getCurrentTab().getUndoManager().addNewEdit(((Transition) myObject).rotate(rotation));

	}

	/**
	 * Creates the popup menu that the user will see when they right click on a
	 * component
	 */
	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		int index = 0;
		JPopupMenu popup = super.getPopup(e);

		JMenuItem menuItem = new JMenuItem("Edit Transition");
		menuItem.addActionListener(o -> ((Transition) myObject).showEditor());
		popup.insert(menuItem, index++);

		menuItem = new JMenuItem(new ShowHideInfoAction((Transition) myObject));
		if (((Transition) myObject).getAttributesVisible()) {
			menuItem.setText("Hide Transition Name");
		} else {
			menuItem.setText("Show Transition Name");
		}
		popup.insert(menuItem, index++);
		popup.insert(new JPopupMenu.Separator(), index);

		return popup;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {

		} else if (SwingUtilities.isRightMouseButton(e)) {
			if (CreateGui.getApp().isEditionAllowed() && CreateGui.getApp().getMode() == ElementType.SELECT) {
				JPopupMenu m = getPopup(e);
				if (m != null) {
					int x = Zoomer.getZoomedValue(((Transition) myObject).getNameOffsetX(), myObject.getZoom());
					int y = Zoomer.getZoomedValue(((Transition) myObject).getNameOffsetY(), myObject.getZoom());
					m.show(myObject, x, y);
				}
			}
		}
	}

}
