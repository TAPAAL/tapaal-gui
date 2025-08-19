package pipe.gui.petrinet.handler;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;

import net.tapaal.gui.petrinet.undo.UpdateNameLabelOffsetCommand;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.NameLabel;
import pipe.gui.petrinet.graphicElements.PetriNetObjectWithLabel;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;

public class LabelHandler extends javax.swing.event.MouseInputAdapter implements
		java.awt.event.MouseWheelListener {

	private final PetriNetObjectWithLabel obj;

	private final NameLabel nl;

	protected Point dragInit = new Point();
	private int originalOffsetX, originalOffsetY;

	public LabelHandler(NameLabel _nl, PetriNetObjectWithLabel _obj) {
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
	
			if (TAPAALGUI.getAppGui().isEditionAllowed()) {
				if (e.getClickCount() == 2) {
					Arc arc = (Arc) obj;
					((TimedOutputArcComponent) arc).showTimeIntervalEditor();
				}
			}
		}
		dragInit = e.getPoint(); //

		dragInit = javax.swing.SwingUtilities.convertPoint(nl, dragInit, obj);
		originalOffsetX = obj.getNameOffsetX();
		originalOffsetY = obj.getNameOffsetY();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// 
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}

		Point p = javax.swing.SwingUtilities.convertPoint(nl, e.getPoint(), obj);
		
		obj.translateNameOffsetX((p.x - dragInit.x));
		obj.translateNameOffsetY((p.y - dragInit.y));
		dragInit = p;
		obj.updateOnMoveOrZoom();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {

	    if (originalOffsetX!=obj.getNameOffsetX() && originalOffsetY != obj.getNameOffsetY()) {
            TAPAALGUI.getCurrentTab().getUndoManager().addNewEdit(
                new UpdateNameLabelOffsetCommand(obj.getNameOffsetX(), obj.getNameOffsetY(), originalOffsetX, originalOffsetY, obj)
            );
        }
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		obj.dispatchEvent(e);
	}
}
