package pipe.gui.handler;

import net.tapaal.TAPAAL;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.PetriNetObject;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Class used to implement methods corresponding to mouse events on all
 * PetriNetObjects.
 * 
 * @author unknown
 */
public class PetriNetObjectHandler {

	protected final PetriNetObject myObject;

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

        if ("DEV".equals(TAPAAL.VERSION)){
            JTextArea pane = new JTextArea();
            pane.setEditable(false);

            pane.setText(
                "(Debug) \n" +
                "  org X:" + myObject.getOriginalX() + " Y:" + myObject.getOriginalY() +"\n" +
                "  pos X:" + myObject.getPositionX() + " Y:" + myObject.getPositionY() +""
            );

		    popup.insert(pane, 1);
        }

		return popup;
	}

}
