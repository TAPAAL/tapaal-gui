package pipe.gui.graphicElements;

import net.tapaal.gui.DrawingSurfaceManager.AbstractDrawingSurfaceManager;
import net.tapaal.helpers.Reference.Reference;
import pipe.gui.Translatable;
import pipe.gui.Zoomable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class GraphicalElement extends JComponent implements Zoomable, Translatable {


    protected Reference<AbstractDrawingSurfaceManager> managerRef = null;

    /*private*/
    public GraphicalElement() {
        addMouseListener(new MouseListener() {
            private boolean rightMouseDown = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                if (getManagerRef() != null && getManagerRef().get() != null) {
                    switch (e.getClickCount()) {
                        case 1:
                            getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                                GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.clicked
                            ));
                            break;
                        case 2:
                            getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                                GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.doubleClicked
                            ));
                            break;
                    }

                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

                if (getManagerRef() != null && getManagerRef().get() != null) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                            GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.pressed
                        ));

                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        rightMouseDown = true;
                    }
                }

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (getManagerRef() != null && getManagerRef().get() != null) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                            GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.released
                        ));
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        if (rightMouseDown) {
                            getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                                GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.rightClicked
                            ));
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (getManagerRef() != null && getManagerRef().get() != null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.entered
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rightMouseDown = false;

                if (getManagerRef() != null && getManagerRef().get() != null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.exited
                    ));
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (getManagerRef() != null && getManagerRef().get() != null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.wheel
                    ));
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (getManagerRef() != null && getManagerRef().get() != null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.dragged
                    ));
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (getManagerRef() != null && getManagerRef().get() != null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.moved
                    ));
                }
            }
        });

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
    }

    public abstract int getLayerOffset();

    public Reference<AbstractDrawingSurfaceManager> getManagerRef() {
        return managerRef;
    }

    public void setManagerRef(Reference<AbstractDrawingSurfaceManager> manager) {
        this.managerRef = manager;
    }

    public abstract void addedToGui();

    public abstract void removedFromGui();

    /*public abstract static class GraphicalNode extends GraphicalElement implements Translatable{
        protected GraphicalNode() {
            super();

        }
    }

    public abstract static class GraphicalVertex extends GraphicalElement {
        protected GraphicalVertex() {
            super();

        }
    }*/

}
