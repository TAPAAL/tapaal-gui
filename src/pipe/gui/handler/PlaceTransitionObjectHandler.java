package pipe.gui.handler;

import java.awt.event.MouseEvent;
import javax.swing.*;

import net.tapaal.TAPAAL;
import pipe.gui.CreateGui;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.GuiFrame;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import pipe.gui.undo.UndoManager;

/**
 * Class used to implement methods corresponding to mouse events on places.
 * 
 * @author Pere Bonet - changed the mousePressed method to only allow the
 *         creation of an arc by left-clicking
 * @author Matthew Worthington - modified the handler which was causing the null
 *         pointer exceptions and incorrect petri nets xml representation.
 * @author Kenneth Yrke Joergensen - cleanup removed unused stuff, and refactored 
 * 	       bad design.
 */

public class PlaceTransitionObjectHandler extends PetriNetObjectHandler {

    // constructor passing in all required objects
	public PlaceTransitionObjectHandler(PlaceTransitionObject obj) {
		super(obj);
		enablePopup = true;
	}

    @Override
    public JPopupMenu getPopup(MouseEvent e) {
	    JPopupMenu popup = super.getPopup(e);
        if ("DEV".equals(TAPAAL.VERSION)){
            JTextArea pane = new JTextArea();
            pane.setEditable(false);

            String preset = "";
            for (Arc arc : ((PlaceTransitionObject) myObject).getPreset()) {
                preset += "    "  + arc.getId() + "\n";
            }
            String postset = "";
            for (Arc arc : ((PlaceTransitionObject) myObject).getPostset()) {
                postset += "    "  + arc.getId() + "\n";
            }

            pane.setText(
                "(Debug) \n" +
                    "  preset: " +"\n" +
                    preset +
                "  postset: " +"\n"+
                    postset
            );

            popup.insert(pane, 1);
        }
        return popup;
    }

    // Disable key bindings that are only available when drawing arcs.
	private static void sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(Arc newArc){
		newArc.setSelectable(true);
		newArc.sealArc();
		newArc.disableDrawingKeyBindings();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		// Prevent creating arcs with a right-click or a middle-click
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}
	}


    public static void cleanupArc(Arc arc, DrawingSurfaceImpl view) {
		//XXX this is problematic if we are creating a transport arc and it part2, then part1 is never cleanup!

		//Called delete, only removes is from view since it finished, should be same af view.remove()
		//arc.delete();

		if (arc instanceof TimedTransportArcComponent) {
			TimedTransportArcComponent partner = ((TimedTransportArcComponent)arc).getConnectedTo();
			if (partner != null) {
				partner.disableDrawingKeyBindings();
				view.remove(partner);
			}
		}

		sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(arc);
		
		view.remove(arc);
		view.repaint();
	}
}
