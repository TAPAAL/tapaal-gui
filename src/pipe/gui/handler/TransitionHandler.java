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
import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.Pipe.elementType;
import pipe.gui.action.ShowHideInfoAction;
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

		if (CreateGui.getApp().isEditionAllowed() == false || e.isControlDown()) {
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
		if (((Transition) myObject).getAttributesVisible() == true) {
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
			if (CreateGui.getApp().isEditionAllowed()) {
				if (e.getClickCount() == 2
						&& (CreateGui.getApp().getMode() == elementType.TIMEDTRANS
								|| CreateGui.getApp().getMode() == elementType.IMMTRANS || CreateGui
								.getApp().getMode() == elementType.SELECT)) {
					((Transition) myObject).showEditor();
				}
			} else {
				// do nothing except the things that one do in the simulator
				// (handled somewhere else)
			}
		} else if (SwingUtilities.isRightMouseButton(e)) {
			if (CreateGui.getApp().isEditionAllowed() && enablePopup) {
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
		} else {
			// do nothing except the things that one do in the simulator
			// (handled somewhere else).
		}
	}
}
