package pipe.gui.canvas;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import pipe.gui.Constants;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.PetriNetObject;

import javax.swing.*;


/**
 * @author Peter Kyme, Michael Camacho Class to handle selection rectangle
 *         functionality
 */
public class SelectionManager extends JComponent {

	private Point startPoint;

	private final Point upperLeftCorner = new Point(0,0);
	private final Rectangle selectionRectangle = new Rectangle(-1, -1);
	private boolean isSelecting;
	private static final Color selectionColor = new Color(0, 0, 255, 30);
	private static final Color selectionColorOutline = new Color(0, 0, 100);
	private final DrawingSurfaceImpl drawingSurface;

	public SelectionManager(DrawingSurfaceImpl _view) {
		addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && !(e.isControlDown())) {
                    isSelecting = true;
                    drawingSurface.add(SelectionManager.this);
                    drawingSurface.setLayer(SelectionManager.this, Constants.SELECTION_LAYER_OFFSET);
                    startPoint = e.getPoint();
                    selectionRectangle.setSize(0, 0);
                    upperLeftCorner.setLocation(e.getPoint());
                    // Select anything that intersects with the rectangle.
                    processSelection(e);
                    updateBounds();
                    repaint();
                } else {
                    startPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isSelecting) {
                    // Select anything that intersects with the rectangle.
                    processSelection(e);
                    isSelecting = false;
                    drawingSurface.setLayer(SelectionManager.this, Constants.LOWEST_LAYER_OFFSET);
                    selectionRectangle.setSize(0, 0);
                    upperLeftCorner.setLocation(0,0);
                    updateBounds();
                    drawingSurface.remove(SelectionManager.this);
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}


        });
		addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isSelecting) {
                    selectionRectangle.setSize(
                        (int) Math.abs(e.getX() - startPoint.getX()),
                        (int) Math.abs(e.getY() - startPoint.getY())
                    );
                    upperLeftCorner.setLocation(
                        (int) Math.min(startPoint.getX(), e.getX()),
                        (int) Math.min(startPoint.getY(), e.getY())
                    );
                    // Select anything that intersects with the rectangle.
                    processSelection(e);
                    updateBounds();
                    repaint();
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {}
        });
		drawingSurface = _view;
	}

	private void updateBounds() {
		setBounds(
				upperLeftCorner.x,
				upperLeftCorner.y,
				selectionRectangle.width+1,
				selectionRectangle.height+1
		);
	}

    private void processSelection(MouseEvent e) {
		if (!e.isShiftDown()) {
			clearSelection();
		}

		Rectangle intersectionRectangle = new Rectangle(
				upperLeftCorner.x,
				upperLeftCorner.y,
				selectionRectangle.width + 1,
				selectionRectangle.height +1
		);

		// Get all the place and transition objects in the current window
		for (PetriNetObject pnObject : drawingSurface.getGuiModel().getPlaceTransitionObjects()) {
			pnObject.select(intersectionRectangle);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(selectionColor);
		g2d.fill(selectionRectangle);
		g2d.setPaint(selectionColorOutline);
		g2d.draw(selectionRectangle);
	}

	public void clearSelection() {
		// Get all the objects in the current window
		for (PetriNetObject pnObject : drawingSurface.getGuiModel().getPNObjects()) {
			if (pnObject.isSelectable()) {
				pnObject.deselect();
			}
		}
	}

	public void translateSelection(int transX, int transY) {

		if (transX == 0 && transY == 0) {
			return;
		}

		//Find selection
        List<PetriNetObject> selection = getSelection();

        Point topleft = getTopLeftMostPoint(selection);

        //Adjust the translate distance to avoid exceeding 0,0
		if (topleft != null) {
			topleft.translate(transX, transY);
			if (topleft.x < 0) {
				transX -= topleft.x;
			}
			if (topleft.y < 0) {
				transY -= topleft.y;
			}
			if (transX == 0 && transY == 0) {
				return;
			}
		}

		for (PetriNetObject pnObject : selection) {
		    pnObject.translate(transX, transY);
		}
		drawingSurface.updatePreferredSize();
	}

    @Nullable
    private Point getTopLeftMostPoint(List<PetriNetObject> selection) {
        // First see if translation will put anything at a negative location
        Point topleft = null;

        // Get all the objects in the current window, ignoring Arcs
        for (PetriNetObject pnObject : selection) {
            if (pnObject.isSelected() && !(pnObject instanceof Arc)) {
                Point point = pnObject.getLocation();
                if (topleft == null) {
                    topleft = point;
                } else {
                    if (point.x < topleft.x) {
                        topleft.x = point.x;
                    }
                    if (point.y < topleft.y) {
                        topleft.y = point.y;
                    }
                }
            }
        }
        return topleft;
    }

    public ArrayList<PetriNetObject> getSelection() {
	    /*
	       Alternative stream implementation
	       drawingSurface.getGuiModel().getPNObjects().stream()
            .filter(PetriNetObject::isSelected)
            .collect(Collectors.toList());
	     */

		ArrayList<PetriNetObject> selection = new ArrayList<PetriNetObject>();

		// Get all the objects in the current window
		for (PetriNetObject pnObject : drawingSurface.getGuiModel().getPNObjects()) {
			if (pnObject.isSelected()) {
				selection.add(pnObject);
			}
		}
		return selection;
	}



	public void selectAll() {
		for (PetriNetObject pnObject : drawingSurface.getGuiModel().getPNObjects()) {
			pnObject.select(false);
		}
		drawingSurface.repaint();
	}

}
