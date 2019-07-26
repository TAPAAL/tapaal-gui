package pipe.gui.handler;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.dataLayer.NetType;
import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.action.SplitArcAction;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

/**
 * Class used to implement methods corresponding to mouse events on arcs.
 */
public class ArcHandler extends PetriNetObjectHandler {

	public ArcHandler(Arc obj) {
		super(obj);
		enablePopup = true;
	}

	/**
	 * Creates the popup menu that the user will see when they right click on a
	 * component
	 */
	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		int popupIndex = 0;
		JMenuItem menuItem;
		JPopupMenu popup = super.getPopup(e);

		if (myObject instanceof TimedOutputArcComponent
				&& !(myObject instanceof TimedInputArcComponent)
				&& !(myObject instanceof TimedTransportArcComponent)) {
			
			menuItem = new JMenuItem(new SplitArcAction((Arc) myObject, e
					.getPoint()));
			menuItem.setText("Insert Point");
			popup.insert(menuItem, popupIndex++);

			popup.insert(new JPopupMenu.Separator(), popupIndex);
		}
		return popup;
	}
	
	public void mousePressed(MouseEvent e) {

		if (((Arc) myObject).isPrototype()) {
			dispatchToParentWithMouseLocationUpdated(e);
			return;
		}

		if (CreateGui.getApp().isEditionAllowed()) {
			if (e.getClickCount() == 2) {
				Arc arc = (Arc) myObject;
				if (e.isControlDown()) {
					CreateGui.getCurrentTab().getUndoManager().addNewEdit(
							arc.getArcPath().insertPoint(
									new Point2D.Float(arc.getX() + e.getX(),
											arc.getY() + e.getY()),
									e.isAltDown()));
				} else if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
					((TimedOutputArcComponent) myObject).showTimeIntervalEditor();
				}
			} else {
				getPopup(e);
				super.mousePressed(e);
			}
		}
	}

}
