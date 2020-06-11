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
import pipe.gui.undo.UndoManager;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.TimedPlaceMarkingEdit;

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
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.getClickCount() == 2 &&
                CreateGui.getApp().isEditionAllowed() &&
                (CreateGui.getApp().getMode() == ElementType.PLACE || CreateGui.getApp().getMode() == ElementType.SELECT)
            ) {
				((TimedPlaceComponent) myObject).showAgeOfTokens(false);
				((Place) myObject).showEditor();
			}
		} else if (SwingUtilities.isRightMouseButton(e)) {
			if (CreateGui.getApp().isEditionAllowed() && enablePopup && CreateGui.getApp().getMode() == ElementType.SELECT) {
				JPopupMenu m = getPopup(e);
				if (m != null) {

					if (myObject instanceof PetriNetObjectWithLabel) {
						int x = Zoomer.getZoomedValue(((PetriNetObjectWithLabel)myObject).getNameOffsetX(), myObject.getZoom());
						int y = Zoomer.getZoomedValue(((PetriNetObjectWithLabel)myObject).getNameOffsetY(), myObject.getZoom());
						m.show(myObject, x, y);
					}
				}
			}
		}/*
		 * else if (SwingUtilities.isMiddleMouseButton(e)){ // TODO -
		 * middelclick draw a arrow }
		 */
	}



	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!(CreateGui.getApp().isEditionAllowed()) || e.isControlDown() || !(myObject.isSelected()) || !(myObject instanceof TimedPlaceComponent)) {
			return;
		}

		if (myObject instanceof TimedPlaceComponent) {
            TimedPlaceComponent p = (TimedPlaceComponent) myObject;
		    if (e.getWheelRotation() < 0) {
                p.addTokens(1);
            } else {
                p.removeTokens(1);
            }
        }
	}
}
