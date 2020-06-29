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
import pipe.gui.graphicElements.PetriNetObjectWithLabel;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;

/**
 * Class used to implement methods corresponding to mouse events on places.
 */
public class PlaceHandler extends PlaceTransitionObjectHandler {

	public PlaceHandler(Place obj) {
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

		JMenuItem menuItem = new JMenuItem("Edit Place");
		menuItem.addActionListener(o -> ((Place) myObject).showEditor());
		popup.insert(menuItem, index++);

		menuItem = new JMenuItem(new ShowHideInfoAction((Place) myObject));
		if (((Place) myObject).getAttributesVisible()) {
			menuItem.setText("Hide Place Name");
		} else {
			menuItem.setText("Show Place Name");
		}
		popup.insert(menuItem, index++);
		popup.insert(new JPopupMenu.Separator(), index);

		return popup;
	}

	@Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!(CreateGui.getApp().isEditionAllowed()) || e.isControlDown() || !(myObject.isSelected()) || !(myObject instanceof TimedPlaceComponent)) {
            return;
        }

        TimedPlaceComponent p = (TimedPlaceComponent) myObject;
        if (e.getWheelRotation() < 0) {
            p.underlyingPlace().addTokens(1);
        } else {
            p.underlyingPlace().removeTokens(1);
        }
    }
}
