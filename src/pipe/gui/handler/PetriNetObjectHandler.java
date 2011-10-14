package pipe.gui.handler;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pipe.dataLayer.PetriNetObject;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Grid;
import pipe.gui.Pipe.elementType;
import pipe.gui.GuiFrame.GUIMode;
import pipe.gui.action.DeletePetriNetObjectAction;

/**
 * Class used to implement methods corresponding to mouse events on all
 * PetriNetObjects.
 * 
 * @author unknown
 */
public class PetriNetObjectHandler extends javax.swing.event.MouseInputAdapter
		implements java.awt.event.MouseWheelListener {

	protected Container contentPane;
	protected PetriNetObject myObject = null;

	// justSelected: set to true on press, and false on release;
	protected static boolean justSelected = false;

	protected boolean isDragging = false;
	protected boolean enablePopup = false;
	protected Point dragInit = new Point();

	private int totalX = 0;
	private int totalY = 0;

	// constructor passing in all required objects
	public PetriNetObjectHandler(Container contentpane, PetriNetObject obj) {
		contentPane = contentpane;
		myObject = obj;
	}

	/**
	 * Creates the popup menu that the user will see when they right click on a
	 * component
	 */
	public JPopupMenu getPopup(MouseEvent e) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem(new DeletePetriNetObjectAction(
				myObject));
		menuItem.setText("Delete");
		popup.add(menuItem);
		return popup;
	}

	/**
	 * Displays the popup menu
	 */
	private void checkForPopup(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			JPopupMenu m = getPopup(e);
			if (m != null) {
				m.show(myObject, e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(CreateGui.getApp().getGUIMode().equals(GUIMode.animation)) return;
		
		if (CreateGui.getApp().isEditionAllowed() && enablePopup) {
			checkForPopup(e);
		}

		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}

		if (CreateGui.getApp().getMode() == elementType.SELECT) {
			if (!myObject.isSelected()) {
				if (!e.isShiftDown()) {
					((DrawingSurfaceImpl) contentPane).getSelectionObject()
							.clearSelection();
				}
				myObject.select();
				justSelected = true;
			}
			dragInit = e.getPoint();
		}
	}

	/**
	 * Event handler for when the user releases the mouse, used in conjunction
	 * with mouseDragged and mouseReleased to implement the moving action
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if(CreateGui.getApp().getGUIMode().equals(GUIMode.animation)) return;
		// Have to check for popup here as well as on pressed for
		// crossplatform!!
		if (CreateGui.getApp().isEditionAllowed() && enablePopup) {
			checkForPopup(e);
		}

		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}

		if (CreateGui.getApp().getMode() == elementType.SELECT) {
			if (isDragging) {
				isDragging = false;
				CreateGui.getView().getUndoManager().translateSelection(
						((DrawingSurfaceImpl) contentPane).getSelectionObject()
								.getSelection(), totalX, totalY);
				totalX = 0;
				totalY = 0;
			} else {
				if (!justSelected) {
					if (e.isShiftDown()) {
						myObject.deselect();
					} else {
						((DrawingSurfaceImpl) contentPane).getSelectionObject()
								.clearSelection();
						myObject.select();
					}
				}
			}
		}
		justSelected = false;
	}

	/**
	 * Handler for dragging PlaceTransitionObjects around
	 */
	@Override
	public void mouseDragged(MouseEvent e) {

		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}

		if (CreateGui.getApp().getMode() == elementType.SELECT) {
			if (myObject.isDraggable()) {
				if (!isDragging) {
					isDragging = true;
				}
			}

			// Calculate translation in mouse
			int transX = Grid.getModifiedX(e.getX() - dragInit.x);
			int transY = Grid.getModifiedY(e.getY() - dragInit.y);
			totalX += transX;
			totalY += transY;
			((DrawingSurfaceImpl) contentPane).getSelectionObject()
					.translateSelection(transX, transY);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}

}
