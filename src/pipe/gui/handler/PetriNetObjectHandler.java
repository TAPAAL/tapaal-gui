package pipe.gui.handler;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Grid;
import pipe.gui.Pipe.ElementType;
import pipe.gui.GuiFrame.GUIMode;

import pipe.gui.graphicElements.PetriNetObject;

/**
 * Class used to implement methods corresponding to mouse events on all
 * PetriNetObjects.
 * 
 * @author unknown
 */
public class PetriNetObjectHandler extends javax.swing.event.MouseInputAdapter
		implements java.awt.event.MouseWheelListener {

	protected PetriNetObject myObject = null;

	// justSelected: set to true on press, and false on release;
	protected static boolean justSelected = false;

	protected boolean isDragging = false;
	protected boolean enablePopup = false;
	protected Point dragInit = new Point();

	private int totalX = 0;
	private int totalY = 0;

	// constructor passing in all required objects
	public PetriNetObjectHandler(PetriNetObject obj) {
		myObject = obj;
	}

	/**
	 * Creates the popup menu that the user will see when they right click on a
	 * component
	 */
	public JPopupMenu getPopup(MouseEvent e) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem(CreateGui.getApp().deleteAction);
		menuItem.setText("Delete");
		popup.add(menuItem);
		return popup;
	}

	/**
	 * Displays the popup menu
	 */
	private void checkForPopup(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e) && CreateGui.getApp().getMode() == ElementType.SELECT) {
			JPopupMenu m = getPopup(e);
			if (m != null) {
				m.show(myObject, e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(CreateGui.getApp().getGUIMode().equals(GUIMode.animation)) return;
		
		if (CreateGui.getApp().getMode() == ElementType.SELECT) {
			if (!myObject.isSelected()) {
				if (!e.isShiftDown()) {
					myObject.getParent().getSelectionObject().clearSelection();
				}
				myObject.select();
				justSelected = true;
			}
			dragInit = e.getPoint();
		}
		
		if (CreateGui.getApp().isEditionAllowed() && enablePopup) {
			checkForPopup(e);
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

		if (CreateGui.getApp().getMode() == ElementType.SELECT) {
			if (isDragging) {
				isDragging = false;
				CreateGui.getDrawingSurface().getUndoManager().translateSelection(
						myObject.getParent().getSelectionObject().getSelection(), totalX, totalY);
				totalX = 0;
				totalY = 0;
			} else {
				if (!justSelected) {
					if (e.isShiftDown()) {
						myObject.deselect();
					} else {
						myObject.getParent().getSelectionObject().clearSelection();
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
		int previousX = myObject.getX();
		int previousY = myObject.getY();
		
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}

		if (CreateGui.getApp().getMode() == ElementType.SELECT) {
			if (myObject.isDraggable()) {
				if (!isDragging) {
					isDragging = true;
				}
			}

			// Calculate translation in mouse
			int transX = Grid.getModifiedX(e.getX() - dragInit.x);
			int transY = Grid.getModifiedY(e.getY() - dragInit.y);
			myObject.getParent().getSelectionObject().translateSelection(transX, transY);
			
			//Only register the actual distance and direction moved (in case of dragging past edge)
			totalX += myObject.getX() - previousX;
			totalY += myObject.getY() - previousY;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}

	//Changes dispatches an event to the parent component, with the mouse location updated to the parent
	//MouseLocation is relative to the component
	public void dispatchToParentWithMouseLocationUpdated(MouseEvent e) {
		e.translatePoint(myObject.getX(), myObject.getY());
		myObject.getParent().dispatchEvent(e);
	}

}
