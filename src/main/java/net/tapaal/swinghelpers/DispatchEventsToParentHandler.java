<<<<<<< HEAD
package net.tapaal.swinghelpers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public final class DispatchEventsToParentHandler implements MouseListener, MouseWheelListener, MouseMotionListener {

    private static void dispatchEventToParent(MouseEvent e) {
        if(e.getSource() instanceof Component) {
            Component c = ((Component) e.getSource());
            if (c.getParent() != null) {
                SwingUtilities.convertPoint(c, e.getPoint(), c.getParent());
                c.getParent().dispatchEvent(e);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        dispatchEventToParent(e);
    }
}
=======
package net.tapaal.swinghelpers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public final class DispatchEventsToParentHandler implements MouseListener, MouseWheelListener, MouseMotionListener {

    private static void dispatchEventToParent(MouseEvent e) {
        if(e.getSource() instanceof Component) {
            Component c = ((Component) e.getSource());
            if (c.getParent() != null) {
                SwingUtilities.convertPoint(c, e.getPoint(), c.getParent());
                c.getParent().dispatchEvent(e);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        dispatchEventToParent(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        dispatchEventToParent(e);
    }
}
>>>>>>> origin/cpn
