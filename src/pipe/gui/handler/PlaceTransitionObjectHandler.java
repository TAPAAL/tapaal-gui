package pipe.gui.handler;

import java.awt.event.MouseEvent;
import javax.swing.*;

import net.tapaal.TAPAAL;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.PlaceTransitionObject;

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

}
