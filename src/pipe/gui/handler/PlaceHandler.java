package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Zoomer;
import pipe.gui.Pipe.elementType;
import pipe.gui.action.ShowHideInfoAction;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.undo.UndoManager;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.TimedPlaceMarkingEdit;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;

/**
 * Class used to implement methods corresponding to mouse events on places.
 */
public class PlaceHandler extends PlaceTransitionObjectHandler {

	public PlaceHandler(Container contentpane, Place obj) {
		super(contentpane, obj);
	}

	public PlaceHandler(DrawingSurfaceImpl drawingSurfaceImpl, Place newObject,
			DataLayer guiModel, TimedArcPetriNet model) {
		super(drawingSurfaceImpl, newObject, guiModel, model);
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
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Place) myObject).showEditor();
			}
		});
		popup.insert(menuItem, index++);

		menuItem = new JMenuItem(new ShowHideInfoAction((Place) myObject));
		if (((Place) myObject).getAttributesVisible()) {
			menuItem.setText("Hide Attributes");
		} else {
			menuItem.setText("Show Attributes");
		}
		popup.insert(menuItem, index++);
		popup.insert(new JPopupMenu.Separator(), index);

		return popup;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (e.getClickCount() == 2
					&& CreateGui.getApp().isEditionAllowed()
					&& (CreateGui.getApp().getMode() == elementType.PLACE || CreateGui.getApp().getMode() == elementType.SELECT)) {
				((TimedPlaceComponent) myObject).showAgeOfTokens(false);
				((Place) myObject).showEditor();
			} else {
				UndoManager undoManager = CreateGui.getView().getUndoManager();

				switch (CreateGui.getApp().getMode()) {
				case ADDTOKEN:
					if (myObject instanceof TimedPlaceComponent) {
						Command command = new TimedPlaceMarkingEdit((TimedPlaceComponent) myObject, 1);
						command.redo();
						undoManager.addNewEdit(command);
					}
					break;
				case DELTOKEN:
					if (myObject instanceof TimedPlaceComponent) {
						Command command = new TimedPlaceMarkingEdit((TimedPlaceComponent) myObject, -1);
						command.redo();
						undoManager.addNewEdit(command);
					} 

					break;
				default:
					break;
				}
			}
		} else if (SwingUtilities.isRightMouseButton(e)) {
			if (CreateGui.getApp().isEditionAllowed() && enablePopup) {
				JPopupMenu m = getPopup(e);
				if (m != null) {
					int x = Zoomer.getZoomedValue(((Place) myObject).getNameOffsetXObject().intValue(), myObject.getZoom());
					int y = Zoomer.getZoomedValue(((Place) myObject).getNameOffsetYObject().intValue(), myObject.getZoom());
					m.show(myObject, x, y);
				}
			}
		}/*
		 * else if (SwingUtilities.isMiddleMouseButton(e)){ // TODO -
		 * middelclick draw a arrow }
		 */
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!(CreateGui.getApp().isEditionAllowed()) || e.isControlDown()) {
			return;
		}

		UndoManager undoManager = CreateGui.getView().getUndoManager();

		if (myObject instanceof TimedPlaceComponent) {
			int clicks = -e.getWheelRotation();
			TimedPlace place = ((TimedPlaceComponent)myObject).underlyingPlace();
			if(clicks > 0) {
				place.addToken(new TimedToken(place));
			}else{
				if(place.numberOfTokens() == 0)
					return;
				
				place.removeToken();
			}
			Command command = new TimedPlaceMarkingEdit((TimedPlaceComponent) myObject, clicks);
			undoManager.addNewEdit(command);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
			if ((myObject instanceof TimedPlaceComponent) && !isDragging) {// &&
				if (CreateGui.getView().isInAnimationMode()) {
					((TimedPlaceComponent) myObject).showAgeOfTokens(true);
				}
			}
		}

		if (isDragging) {
			((TimedPlaceComponent) myObject).showAgeOfTokens(false);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if ((myObject instanceof TimedPlaceComponent)) {// &&
			if (CreateGui.getView().isInAnimationMode()) {
				((TimedPlaceComponent) myObject).showAgeOfTokens(false);
			}
		}
	}

	// Override
	@Override
	public void mousePressed(MouseEvent e) {
		if (CreateGui.getApp().isEditionAllowed()) {
			super.mousePressed(e);
		}

	}
}
