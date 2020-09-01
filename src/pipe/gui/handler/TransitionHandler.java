package pipe.gui.handler;

import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.gui.action.ShowHideInfoAction;
import pipe.gui.graphicElements.Transition;

/**
 * Class used to implement methods corresponding to mouse events on transitions.
 */
public class TransitionHandler extends PlaceTransitionObjectHandler implements java.awt.event.MouseWheelListener {

	public TransitionHandler(Transition obj) {
		super(obj);
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
}
