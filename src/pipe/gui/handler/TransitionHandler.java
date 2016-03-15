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
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Zoomer;
import pipe.gui.Pipe.ElementType;
import pipe.gui.action.ShowHideInfoAction;
import pipe.gui.graphicElements.Transition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

/**
 * Class used to implement methods corresponding to mouse events on transitions.
 */
public class TransitionHandler extends PlaceTransitionObjectHandler implements
		java.awt.event.MouseWheelListener {

	public TransitionHandler(Container contentpane, Transition obj) {
		super(contentpane, obj);
	}

	public TransitionHandler(DrawingSurfaceImpl drawingSurfaceImpl,
			Transition newObject, DataLayer guiModel, TimedArcPetriNet model) {
		super(drawingSurfaceImpl, newObject, guiModel, model);
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
		CreateGui.getView().getUndoManager().addNewEdit(
				((Transition) myObject).rotate(rotation));

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
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Transition) myObject).showEditor();
			}
		});
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
			if (CreateGui.getApp().isEditionAllowed()) {
				if (e.getClickCount() == 2
						&& (CreateGui.getApp().getMode() == ElementType.TIMEDTRANS
								|| CreateGui.getApp().getMode() == ElementType.IMMTRANS || CreateGui
								.getApp().getMode() == ElementType.SELECT)) {
					((Transition) myObject).showEditor();
				}
			}
		} else if (SwingUtilities.isRightMouseButton(e)) {
			if (CreateGui.getApp().isEditionAllowed() && enablePopup && CreateGui.getApp().getMode() == ElementType.SELECT) {
				JPopupMenu m = getPopup(e);
				if (m != null) {
					int x = Zoomer.getZoomedValue(((Transition) myObject)
							.getNameOffsetXObject().intValue(), myObject
							.getZoom());
					int y = Zoomer.getZoomedValue(((Transition) myObject)
							.getNameOffsetYObject().intValue(), myObject
							.getZoom());
					m.show(myObject, x, y);
				}
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
	@Override
	public void mouseEntered(MouseEvent e) {
		if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
			if ((myObject instanceof TimedTransitionComponent) && !isDragging) {// &&
				if (CreateGui.getView().isInAnimationMode()) {
					((TimedTransitionComponent) myObject).showDInterval(true);
				}
			}
		}

		if (isDragging) {
			((TimedTransitionComponent) myObject).showDInterval(false);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if ((myObject instanceof TimedTransitionComponent)) {// &&
			if (CreateGui.getView().isInAnimationMode()) {
				((TimedTransitionComponent) myObject).showDInterval(false);
			}
		}
	}
	
	public void mouseEngfstered(MouseEvent e) {
		if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
			if ((myObject instanceof pipe.gui.graphicElements.tapn.TimedTransitionComponent) && !isDragging) {// &&
				if (CreateGui.getView().isInAnimationMode()) {
					((TimedTransitionComponent) myObject).updateToolTip(true);
				} else {
					((TimedTransitionComponent) myObject).updateToolTip(false);
				}
			}
		}
	}
}
