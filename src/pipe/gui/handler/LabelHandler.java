package pipe.gui.handler;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;
import pipe.dataLayer.NetType;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.NameLabel;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;

public class LabelHandler extends javax.swing.event.MouseInputAdapter implements
		java.awt.event.MouseWheelListener {

	private PetriNetObject obj;

	private NameLabel nl;

	protected Point dragInit = new Point();

	public LabelHandler(NameLabel _nl, PetriNetObject _obj) {
		obj = _obj;
		nl = _nl;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		obj.dispatchEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(obj instanceof Arc) {
			if (((Arc) obj).isPrototype()) {
				return;
			}
	
			if (CreateGui.getApp().isEditionAllowed()) {
				if (e.getClickCount() == 2) {
					Arc arc = (Arc) obj;
					if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
						((TimedOutputArcComponent) arc).showTimeIntervalEditor();
					}
				}
			}
		}
		dragInit = e.getPoint(); //
		// dragInit = e.getLocationOnScreen(); //causes exception in Windows!
		dragInit = javax.swing.SwingUtilities.convertPoint(nl, dragInit, obj);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// 
		if (!SwingUtilities.isLeftMouseButton(e) || CreateGui.getDrawingSurface().isInAnimationMode()) {
			return;
		}

		Point p = javax.swing.SwingUtilities
				.convertPoint(nl, e.getPoint(), obj);
		// obj.setNameOffsetX((e.getXOnScreen() - dragInit.x)); //causes
		// exception in Windows!
		// obj.setNameOffsetY((e.getYOnScreen() - dragInit.y)); //causes
		// exception in Windows!
		// dragInit = e.getLocationOnScreen(); //causes exception in Windows!
		obj.setNameOffsetX((p.x - dragInit.x));
		obj.setNameOffsetY((p.y - dragInit.y));
		dragInit = p;
		obj.updateOnMoveOrZoom();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		obj.dispatchEvent(e);
	}
}
