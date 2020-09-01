package pipe.gui.handler;

import java.awt.event.MouseEvent;
import javax.swing.*;

import net.tapaal.TAPAAL;
import pipe.gui.action.SplitArcAction;
import pipe.gui.graphicElements.Arc;

/**
 * Class used to implement methods corresponding to mouse events on arcs.
 */
public class ArcHandler extends PetriNetObjectHandler {

	public ArcHandler(Arc obj) {
		super(obj);
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

        menuItem = new JMenuItem(new SplitArcAction((Arc) myObject, e.getPoint()));
        menuItem.setText("Insert Point");
        popup.insert(menuItem, popupIndex++);

        popup.insert(new JPopupMenu.Separator(), popupIndex);


        if ("DEV".equals(TAPAAL.VERSION)){
            JTextArea pane = new JTextArea();
            pane.setEditable(false);

            pane.setText(
                "(Debug) \n" +
                    "  Source: " + ((Arc) myObject).getSource().getId() +"\n"+
                    "  Target: " + ((Arc) myObject).getTarget().getId()
            );

            popup.insert(pane, 1);
        }

		return popup;
	}

    @Override
    public void mouseClicked(MouseEvent e) {
	    //Dispatch click on prototype arc to parent
        if (((Arc) myObject).isPrototype()) {
            dispatchToParentWithMouseLocationUpdated(e);
            return;
        }
	    super.mouseClicked(e);
    }

    public void mousePressed(MouseEvent e) {


		if (((Arc) myObject).isPrototype()) {
			dispatchToParentWithMouseLocationUpdated(e);
			return;
		}

        super.mousePressed(e);
	}

    @Override
    public void mouseDragged(MouseEvent e) {
        //Disable mouse drag for arcs, current drag implementation does not work when dragging arcs
    }
}
