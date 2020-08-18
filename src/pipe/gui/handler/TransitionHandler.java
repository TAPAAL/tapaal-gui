package pipe.gui.handler;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import dk.aau.cs.model.tapn.TimedTransition;
import pipe.gui.action.ShowHideInfoAction;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;

/**
 * Class used to implement methods corresponding to mouse events on transitions.
 */
public class TransitionHandler extends PlaceTransitionObjectHandler implements java.awt.event.MouseWheelListener {
    private KeyListener keyListener = keyListener();
    private long when;

	public TransitionHandler(Transition obj) {
		super(obj);
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
		menuItem.addActionListener(o -> ((Transition) myObject).showEditor());
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
	    if (myObject.isSelected()) {
	        myObject.addKeyListener(keyListener);
            myObject.setFocusable(true);
            myObject.requestFocus();
        } else {
            myObject.removeKeyListener(keyListener);
        }
    }

    private KeyListener keyListener() {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (myObject instanceof TimedTransitionComponent && e.getWhen() != when) {
                    TimedTransition transition = ((TimedTransitionComponent) myObject).underlyingTransition();
                    if (e.getKeyChar() == 'E' || e.getKeyChar() == 'e') {
                        transition.setUncontrollable(!transition.isUncontrollable());
                        myObject.repaint();
                    } else if (e.getKeyChar() == 'U' || e.getKeyChar() == 'u') {
                        transition.setUrgent(!transition.isUrgent());
                        myObject.repaint();
                    }
                    when = e.getWhen();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) { }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        };
    }
}
