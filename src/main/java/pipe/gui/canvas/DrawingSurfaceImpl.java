package pipe.gui.canvas;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.DrawingSurfaceManager.AbstractDrawingSurfaceManager;
import net.tapaal.helpers.Reference.Reference;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.*;
import pipe.gui.petrinet.graphicElements.*;
import pipe.gui.petrinet.undo.TranslatePetriNetObjectEditCommand;

/**
 * The petrinet is drawn onto this frame.
 */
public class DrawingSurfaceImpl extends JLayeredPane implements Printable, Canvas, PrototypeCanvas {

    private static final int DRAWING_SURFACE_GROW = 100;

	private final SelectionManager selection;

    private Zoomer zoomControl;

	private DataLayer guiModel;
	private final PetriNetTab tabContent;
	private final Reference<AbstractDrawingSurfaceManager> managerRef;

	private static final boolean showDebugBounds = false;

	public DrawingSurfaceImpl(DataLayer dataLayer, PetriNetTab tabContent, Reference<AbstractDrawingSurfaceManager> managerRef) {
		this.guiModel = dataLayer;
		this.tabContent = tabContent;
		this.managerRef = managerRef;
        this.zoomControl = new Zoomer(100);

        selection = new SelectionManager(this);

		setLayout(null);
		setOpaque(true);
		setDoubleBuffered(true);
		setAutoscrolls(true);
		setBackground(Constants.ELEMENT_FILL_COLOUR);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        MouseHandler mouseHandler = new MouseHandler();
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		addMouseWheelListener(mouseHandler);
	}

	public DataLayer getGuiModel() {
		return guiModel;
	}

	public void setModel(DataLayer guiModel, Zoomer zoomer) {
		this.guiModel.removedFromView(); //Remove the old model from view
		guiModel.addedToView(this); //Add the new model to view

		this.guiModel = guiModel;
		this.zoomControl = zoomer;

		this.removeAll();
		setPreferredSize(new Dimension(0,0));
		for (PetriNetObject pnObject : guiModel.getPetriNetObjectsWithArcPathPoint()) {
			addNewPetriNetObject(pnObject);
		}

		validate();
		repaint();
	}

	@Override
	public void addNewPetriNetObject(GraphicalElement newObject) {
		setLayer(newObject, DEFAULT_LAYER + newObject.getLayerOffset());
		newObject.zoomUpdate(zoomControl.getPercent());
		newObject.setManagerRef(managerRef);

		super.add(newObject);
		newObject.addedToGui(); //Must be called after added to component, as children might use referenceto Drawingsurface

		calculateNewBoundsForScrollPane(newObject.getBounds());
		//XXX: templ solution while refactoring, kyrke 2019-09-17
		if(newObject instanceof PetriNetObjectWithLabel && ((PetriNetObjectWithLabel)newObject).getNameLabel() != null){
			calculateNewBoundsForScrollPane(((PetriNetObjectWithLabel)newObject).getNameLabel().getBounds());
		}

		validate();
		repaint();
	}

	//XXX temp solution while refactorting, component removes children them self migth not be best solution long term.
	@Override
	public void removePetriNetObject(GraphicalElement pno) {
		pno.removedFromGui();
		pno.setManagerRef(null);
		super.remove(pno); //Must be called after removeFromGui as children might use the references to Drawingsurface

        updatePreferredSize();
        validate();
		repaint();
	}

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (pageIndex > 0) {
			return Printable.NO_SUCH_PAGE;
		}
		Graphics2D g2D = (Graphics2D) g;
		// Move origin to page printing area corner
		g2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		g2D.scale(0.5, 0.5);
		print(g2D); // Draw the net

		return Printable.PAGE_EXISTS;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (Grid.isEnabled()) {
			Grid.updateSize(this);
			Grid.drawGrid(g);
		}

		if (showDebugBounds) {
			Graphics2D g2 = (Graphics2D) g;
			for (Component c : getComponents()) {
				Paint p = new Color(255,0,0, 80);

				g2.setPaint(p);
				g2.fill(c.getBounds());
				g2.setPaint(Color.black);
				g2.draw(c.getBounds());
			}
		}
	}

	public void updatePreferredSize() {
		Component[] components = getComponents();
		Dimension d = new Dimension(0, 0);
		for (Component component : components) {
			if (component.getClass() == SelectionManager.class) {
				continue; // SelectionObject not included
			}
			Rectangle r = component.getBounds();
			int x = r.x + r.width + DRAWING_SURFACE_GROW;
			int y = r.y + r.height + DRAWING_SURFACE_GROW;
			if (x > d.width) {
				d.width = x;
			}
			if (y > d.height) {
				d.height = y;
			}
		}
		setPreferredSize(d);
		validate();
		repaint();
		Container parent = getParent();
		if (parent != null) {
			parent.validate();
		}
	}

	public Rectangle calculateBoundingRectangle() {
		Rectangle rect = new Rectangle(0, 0, -1, -1);

		Component[] components = getComponents();
		for (Component component : components) {
			if (component.getClass() == SelectionManager.class) {
				continue; // SelectionObject not included
			}
			rect.add(component.getBounds());
		}

		return rect;
	}

	public SelectionManager getSelectionObject() {
		return selection;
	}

	public Zoomer getZoomController() {
		return zoomControl;
	}

	private void calculateNewBoundsForScrollPane(Rectangle rect) {
		boolean changed = false;
		Dimension current = getPreferredSize();

		int this_width = (rect.x + rect.width + 2);
		int this_height = (rect.y + rect.height+ 2);

		if (this_width > current.width) {
			current.width = this_width; changed=true;
		}
		if (this_height > current.height) {
			current.height = this_height; changed=true;
		}

		if (changed) {
			//Update client's preferred size because the area taken up by the graphics has changed
			setPreferredSize(current);

			//Let the scroll pane know to update itself and its scrollbars.
			revalidate();
		}
	}

	public void drag(Point dragStart, Point dragEnd) {
		if (dragStart == null) {
			return;
		}
		JViewport viewer = (JViewport) getParent();
		Point offScreen = viewer.getViewPosition();
		if (dragStart.x > dragEnd.x) {
			offScreen.translate(viewer.getWidth(), 0);
		}
		if (dragStart.y > dragEnd.y) {
			offScreen.translate(0, viewer.getHeight());
		}
		offScreen.translate(dragStart.x - dragEnd.x, dragStart.y - dragEnd.y);
		Rectangle r = new Rectangle(offScreen.x, offScreen.y, 1, 1);
		scrollRectToVisible(r);
	}

	private Point midpoint(int zoom) {
		JViewport viewport = (JViewport) getParent();
		double midpointX = Zoomer.getUnzoomedValue(viewport.getViewPosition().x + (viewport.getWidth() * 0.5), zoom);
		double midpointY = Zoomer.getUnzoomedValue(viewport.getViewPosition().y + (viewport.getHeight() * 0.5), zoom);
		return (new java.awt.Point((int) midpointX, (int) midpointY));
	}

	public void zoomToMidPoint() {
		Point midpoint = midpoint(zoomControl.getPercent());

        int zoomPercent = getZoom();

        JViewport viewport = (JViewport) getParent();

        Component[] children = getComponents();

        //Update elements in the view to zoom, i.e resize graphical elements and reposition them, all done in zoomUpdate.
        for (Component child : children) {
            if (child instanceof Zoomable) {
                ((Zoomable) child).zoomUpdate(zoomPercent);
            }
        }

        // Calculate new position of the Drawing Surface.
        double newZoomedX = Zoomer.getZoomedValue(midpoint.x, zoomPercent);
        double newZoomedY = Zoomer.getZoomedValue(midpoint.y, zoomPercent);

        int newViewX = (int) (newZoomedX - (viewport.getWidth() * 0.5));
        if (newViewX < 0) {
            newViewX = 0;
        }

        int newViewY = (int) (newZoomedY - (viewport.getHeight() * 0.5));
        if (newViewY < 0) {
            newViewY = 0;
        }

        viewport.setViewPosition(new Point(newViewX, newViewY));

        updatePreferredSize();
    }

    // This function should always be called after a change in zoom.
    public int getZoom() {
		return zoomControl.getPercent();
	}

    @Override
    public void addPrototype(GraphicalElement pno) {
		pno.zoomUpdate(getZoom());
        pno.setManagerRef(managerRef);

        add(pno);
        setLayer(pno, Constants.PROTOTYPE_LAYER_OFFSET);
        validate();
        repaint();
    }

    @Override
    public void removePrototype(GraphicalElement pno) {
        remove(pno);
        pno.setManagerRef(null);
        if (pno instanceof Arc) {
            var a = (Arc) pno;
            //Clear the arcs target/source. Transitions caches transitions to calculate the connection points.
            // This cache will only be removed if the arc target is removed.
            a.setSource(null);
            a.setTarget(null);
        }
        validate();
        repaint();
    }

    @Override
    public void clearAllPrototype() {
        for (Component c : getComponentsInLayer(Constants.PROTOTYPE_LAYER_OFFSET)) {
        	remove(c);
        }
        validate();
        repaint();
    }

    public Point adjustPointToZoom(Point p, int zoom) {
        //Converts center coord to upperleft coord
        int offset = (int) (Zoomer.getScaleFactor(zoom) * Constants.PLACE_TRANSITION_HEIGHT / 2);

        int x = Zoomer.getUnzoomedValue(p.x - offset, zoom);
        int y = Zoomer.getUnzoomedValue(p.y - offset, zoom);

        p.setLocation(x, y);
        return p;
    }

    public Point adjustPointToGrid(Point p) {
        int x = Grid.align(p.x, getZoom());
        int y = Grid.align(p.y, getZoom());

        return new Point(x, y);
    }

    public Point adjustPointToGridAndZoom(Point p, int zoom) {
        Point newP = adjustPointToZoom(p, zoom);
        newP = adjustPointToGrid(newP);

        return newP;
    }

    final class MouseHandler extends MouseInputAdapter {

        public MouseHandler() {
			super();
        }

		@Override
		public void mouseClicked(MouseEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMouseClicked(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMousePressed(e);
			}
		}

        @Override
		public void mouseReleased(MouseEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMouseReleased(e);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMouseMoved(e);
			}
        }


		@Override
		public void mouseDragged(MouseEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMouseDragged(e);
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMouseWheelMoved(e);
			}
			if (e.isControlDown()) {
				if (e.getWheelRotation() > 0) {
					tabContent.zoomIn();
				} else {
					tabContent.zoomOut();
				}
			} else {
				//Dispatch Event to scroll pane to allow scrolling up/down. -- kyrke
				getParent().dispatchEvent(e);
			}
		}
	}

	public void repaintAll() {
		this.repaint();
		guiModel.repaintAll(!tabContent.isInAnimationMode());
	}

	public void translateSelection(int transX, int transY) {
        var objects = getSelectionObject().getSelection();
		if (transX != 0 || transY != 0) {
            tabContent.getUndoManager().newEdit(); // new "transaction""
            for (PetriNetObject pnobject : objects) {
                tabContent.getUndoManager().addEdit(new TranslatePetriNetObjectEditCommand(pnobject, transX, transY, this));
            }
        }
	}

    public PetriNetTab getTabContent() {
        return tabContent;
    }
}
