package pipe.gui.canvas;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import net.tapaal.gui.DrawingSurfaceManager.AbstractDrawingSurfaceManager;
import net.tapaal.helpers.Reference.Reference;
import pipe.dataLayer.DataLayer;
import pipe.gui.*;
import pipe.gui.Pipe.ElementType;
import pipe.gui.graphicElements.*;
import pipe.gui.graphicElements.tapn.*;
import pipe.gui.undo.*;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

/**
 * The petrinet is drawn onto this frame.
 */
public class DrawingSurfaceImpl extends JLayeredPane implements Printable, Canvas, PrototypeCanvas {


	public Arc createArc; // no longer static

	private static final int DRAWING_SURFACE_GROW = 100;

	private final SelectionManager selection;

	private final GuiFrame app = CreateGui.getApp();
	private Zoomer zoomControl;

	private DataLayer guiModel;
	private final TabContent tabContent;
	private final Reference<AbstractDrawingSurfaceManager> managerRef;
	private TimedArcPetriNet model;
    private final NameGenerator nameGenerator = new NameGenerator();
	private static final boolean showDebugBounds = false;

	public DrawingSurfaceImpl(DataLayer dataLayer, TabContent tabContent, Reference<AbstractDrawingSurfaceManager> managerRef) {
		guiModel = dataLayer;
		this.tabContent = tabContent;
		this.managerRef = managerRef;
		setLayout(null);
		setOpaque(true);
		setDoubleBuffered(true);
		setAutoscrolls(true);
		setBackground(Pipe.ELEMENT_FILL_COLOUR);

		zoomControl = new Zoomer(100);

		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        MouseHandler mouseHandler = new MouseHandler(this);
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		addMouseWheelListener(mouseHandler);

		selection = new SelectionManager(this);

	}

	public NameGenerator getNameGenerator() {
		return nameGenerator;
	}

	public DataLayer getGuiModel() {
		return guiModel;
	}

	public TimedArcPetriNet getModel() {
		return model;
	}

	public void setModel(DataLayer guiModel, TimedArcPetriNet model, Zoomer zoomer) {
		//Remove the old model from view
		this.guiModel.removedFromView();
		//Add the new model to view
		guiModel.addedToView(this);

		nameGenerator.add(model);

		this.guiModel = guiModel;
		this.model = model;
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

	//XXX temp solution while refactorting, component removes children them self
	//migth not be best solution long term.
	@Override
	public void removePetriNetObject(GraphicalElement pno) {
		pno.removedFromGui();
		pno.setManagerRef(null);
		super.remove(pno); //Must be called after removeFromGui as children might use the references to Drawingsurface

		validate();
		repaint();
	}

	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
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

		//selection.updateBounds();

	}

	public void updatePreferredSize() {
		// iterate over net objects
		// setPreferredSize() accordingly

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
			//Update client's preferred size because
			//the area taken up by the graphics has
			// changed
			setPreferredSize(current);

			//Let the scroll pane know to update itself
			//and its scrollbars.
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
		double midpointX = Zoomer.getUnzoomedValue(viewport.getViewPosition().x
				+ (viewport.getWidth() * 0.5), zoom);
		double midpointY = Zoomer.getUnzoomedValue(viewport.getViewPosition().y
				+ (viewport.getHeight() * 0.5), zoom);
		return (new java.awt.Point((int) midpointX, (int) midpointY));
	}

	public void zoomToMidPoint() {

		Point midpoint = midpoint(zoomControl.getPercent());
		zoomTo(midpoint);

	}

	public void zoomIn() {
		int zoom = zoomControl.getPercent();
		if (zoomControl.zoomIn()) {
			zoomTo(midpoint(zoom));
		}
	}

	public void zoomOut() {
		int zoom = zoomControl.getPercent();
		if (zoomControl.zoomOut()) {
			zoomTo(midpoint(zoom));
		}
	}

	// This function should always be called after a change in zoom.
	public void zoomTo(Point point) {

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
		double newZoomedX = Zoomer.getZoomedValue(point.x, zoomPercent);
		double newZoomedY = Zoomer.getZoomedValue(point.y, zoomPercent);

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

	public int getZoom() {
		return zoomControl.getPercent();
	}

    @Override
    public void addPrototype(GraphicalElement pno) {
		pno.zoomUpdate(getZoom());

        add(pno);
        setLayer(pno, Pipe.PROTOTYPE_LAYER_OFFSET);
        validate();
        repaint();
    }

    @Override
    public void removePrototype(GraphicalElement pno) {
        remove(pno);
    }

    @Override
    public void clearAllPrototype() {
        for (Component c : getComponentsInLayer(Pipe.PROTOTYPE_LAYER_OFFSET)) {
        	remove(c);
        }
        validate();
        repaint();
    }

    public Point adjustPointToZoom(Point p, int zoom) {
        //Converts center coord to upperleft coord
        int offset = (int) (Zoomer.getScaleFactor(zoom)
            * Pipe.PLACE_TRANSITION_HEIGHT / 2);

        int x = Zoomer.getUnzoomedValue(p.x - offset, zoom);
        int y = Zoomer.getUnzoomedValue(p.y - offset, zoom);

        p.setLocation(x, y);
        return p;
    }
    public Point adjustPointToGrid(Point p) {
        int x = Grid.getModifiedX(p.x);
        int y = Grid.getModifiedY(p.y);

        return new Point(x, y);
    }

    public Point adjustPointToGridAndZoom(Point p, int zoom) {
        Point newP = adjustPointToZoom(p, zoom);
        newP = adjustPointToGrid(newP);

        return newP;
    }

    class MouseHandler extends MouseInputAdapter {

		private DrawingSurfaceImpl view;

        private Point dragStart;

		public MouseHandler(DrawingSurfaceImpl _view) {
			super();
			view = _view;
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
			if(app.getCurrentTab().isInAnimationMode()) return;

			// check for control down here enables it to attach the arc being drawn to an existing place/transition


			Point clickPoint = e.getPoint();

			if (SwingUtilities.isLeftMouseButton(e)) {

				Pipe.ElementType mode = app.getMode();
				PlaceTransitionObject newpto; //declared here as switch is one big scope

				switch (mode) {

					case ARC:
					case TAPNARC:
					case INHIBARC:
					case TRANSPORTARC:
					case TAPNINHIBITOR_ARC: // Add point to arc in creation
						if (createArc != null) {
							addArcPathPoint(createArc, e);
						}
						break;

					case DRAG:
						dragStart = new Point(clickPoint);
						break;

					case SELECT:
						getSelectionObject().dispatchEvent(e);
						break;

					default:
						break;
				}
			} else {
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				dragStart = new Point(clickPoint);
			}
			updatePreferredSize();
		}

        private void addArcPathPoint(final Arc createArc, final MouseEvent e) {
			int x = Grid.getModifiedX(e.getX());
			int y = Grid.getModifiedY(e.getY());

			boolean shiftDown = e.isShiftDown();
			createArc.setEndPoint(x, y, shiftDown);
			createArc.getArcPath().addPoint(x, y, shiftDown);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMouseReleased(e);
			}
			//setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			if (dragStart != null) {
				dragStart = null;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			if (app.getMode() == ElementType.SELECT) {
				getSelectionObject().dispatchEvent(e);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMouseMoved(e);
			}
			if (createArc != null) {
				createArc.setEndPoint(Grid.getModifiedX(e.getX()), Grid
						.getModifiedY(e.getY()), e.isShiftDown());
			}
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMouseDragged(e);
			}
			if (dragStart != null) {
				view.drag(dragStart, e.getPoint());
			} else if (app.getMode() == ElementType.SELECT) {
				getSelectionObject().dispatchEvent(e);
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (managerRef!=null && managerRef.get() != null) {
				managerRef.get().drawingSurfaceMouseWheelMoved(e);
			}
			if (e.isControlDown()) {
				if (e.getWheelRotation() > 0) {
					view.zoomIn();
				} else {
					view.zoomOut();
				}
				CreateGui.getAppGui().updateZoomCombo();
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

	//XXX: function moved from undoManager --kyrke - 2019-07-06
	private void deleteObject(PetriNetObject pnObject) {
		if (pnObject instanceof ArcPathPoint) {

			ArcPathPoint arcPathPoint = (ArcPathPoint)pnObject;

			//If the arc is marked for deletion, skip deleting individual arcpathpoint
			if (!(arcPathPoint.getArcPath().getArc().isSelected())) {

				//Don't delete the two last arc path points
				if (arcPathPoint.isDeleteable()) {
					Command cmd = new DeleteArcPathPointEdit(
							arcPathPoint.getArcPath().getArc(),
							arcPathPoint,
							arcPathPoint.getIndex(),
							guiModel
					);
					cmd.redo();
					tabContent.getUndoManager().addEdit(cmd);
				}
			}
		}else{
			//The list of selected objects is not updated when a element is deleted
			//We might delete the same object twice, which will give an error
			//Eg. a place with output arc is deleted (deleted also arc) while arc is also selected.
			//There is properly a better way to track this (check model?) but while refactoring we will keeps it close
			//to the orginal code -- kyrke 2019-06-27
			if (!pnObject.isDeleted()) {
				Command cmd = null;
				if(pnObject instanceof TimedPlaceComponent){
					TimedPlaceComponent tp = (TimedPlaceComponent)pnObject;
					cmd = new DeleteTimedPlaceCommand(tp, this.getModel(), guiModel);
				}else if(pnObject instanceof TimedTransitionComponent){
					TimedTransitionComponent transition = (TimedTransitionComponent)pnObject;
					cmd = new DeleteTimedTransitionCommand(transition, transition.underlyingTransition().model(), guiModel);
				}else if(pnObject instanceof TimedTransportArcComponent){
					TimedTransportArcComponent transportArc = (TimedTransportArcComponent)pnObject;
					cmd = new DeleteTransportArcCommand(transportArc, transportArc.underlyingTransportArc(), transportArc.underlyingTransportArc().model(), guiModel);
				}else if(pnObject instanceof TimedInhibitorArcComponent){
					TimedInhibitorArcComponent tia = (TimedInhibitorArcComponent)pnObject;
					cmd = new DeleteTimedInhibitorArcCommand(tia, tia.underlyingTimedInhibitorArc().model(), guiModel);
				}else if(pnObject instanceof TimedInputArcComponent){
					TimedInputArcComponent tia = (TimedInputArcComponent)pnObject;
					cmd = new DeleteTimedInputArcCommand(tia, tia.underlyingTimedInputArc().model(), guiModel);
				}else if(pnObject instanceof TimedOutputArcComponent){
					TimedOutputArcComponent toa = (TimedOutputArcComponent)pnObject;
					cmd = new DeleteTimedOutputArcCommand(toa, toa.underlyingArc().model(), guiModel);
				}else if(pnObject instanceof AnnotationNote){
					cmd = new DeleteAnnotationNoteCommand((AnnotationNote)pnObject, guiModel);
				}else{
					throw new RuntimeException("This should not be possible");
				}
				cmd.redo();
				tabContent.getUndoManager().addEdit(cmd);
			}
		}
	}


	private void deleteSelection(PetriNetObject pnObject) {
		if(pnObject instanceof PlaceTransitionObject){
			PlaceTransitionObject pto = (PlaceTransitionObject)pnObject;

			for(Arc arc : pto.getPreset()){
				deleteObject(arc);
			}

			for(Arc arc : pto.getPostset()){
				deleteObject(arc);
			}
		}

		deleteObject(pnObject);
	}

	public void deleteSelection(ArrayList<PetriNetObject> selection) {
		for (PetriNetObject pnObject : selection) {
			deleteSelection(pnObject);
		}
	}

	public void translateSelection(ArrayList<PetriNetObject> objects, int transX, int transY) {
		tabContent.getUndoManager().newEdit(); // new "transaction""
		for (PetriNetObject pnobject : objects) {
			tabContent.getUndoManager().addEdit(new TranslatePetriNetObjectEdit(pnobject, transX, transY, this));
		}
	}
}
