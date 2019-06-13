package pipe.gui;

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

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;
import pipe.gui.GuiFrame.GUIMode;
import pipe.gui.Pipe.ElementType;
import pipe.gui.graphicElements.AnnotationNote;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.tapn.*;
import pipe.gui.undo.AddPetriNetObjectEdit;
import pipe.gui.undo.AddTimedPlaceCommand;
import pipe.gui.undo.AddTimedTransitionCommand;
import pipe.gui.undo.UndoManager;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

/**
 * The petrinet is drawn onto this frame.
 */
public class DrawingSurfaceImpl extends JLayeredPane implements Printable {
	private static final long serialVersionUID = 4434596266503933386L;
	private boolean netChanged = false;
	private boolean animationmode = false;

	public Arc createArc; // no longer static
	
	private static final int DRAWING_SURFACE_GROW = 100;

	private SelectionManager selection;
	private UndoManager undoManager;
	private ArrayList<PetriNetObject> petriNetObjects = new ArrayList<PetriNetObject>();
	private GuiFrame app = CreateGui.getApp();
	private Zoomer zoomControl;

	private DataLayer guiModel;
	private TimedArcPetriNet model;
	private MouseHandler mouseHandler;
	private NameGenerator nameGenerator = new NameGenerator();

	public DrawingSurfaceImpl(DataLayer dataLayer) {
		guiModel = dataLayer;
		setLayout(null);
		setOpaque(true);
		setDoubleBuffered(true);
		setAutoscrolls(true);
		setBackground(Pipe.ELEMENT_FILL_COLOUR);

		zoomControl = new Zoomer(100);

		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		mouseHandler = new MouseHandler(this, dataLayer);
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		addMouseWheelListener(mouseHandler);

		selection = new SelectionManager(this);
		undoManager = new UndoManager(this, guiModel, app);
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
		this.selection.disableSelection();

		nameGenerator.add(model);
		this.mouseHandler.setModel(guiModel, model);
		this.undoManager.setModel(guiModel);
		this.guiModel = guiModel;
		this.model = model;
		this.zoomControl = zoomer;
		app.updateZoomCombo();

		if (animationmode) {
			app.getAnimator().highlightEnabledTransitions();
			app.getAnimator().unhighlightDisabledTransitions();
			app.getAnimator().reportBlockingPlaces();
		}

		this.removeAll();
		setPreferredSize(new Dimension(0,0));
		for (PetriNetObject pnObject : guiModel.getPetriNetObjects()) {
			pnObject.zoomUpdate(zoomer.getPercent());
			addNewPetriNetObject(pnObject);
		}

		if(app.getMode() == ElementType.SELECT) {
			this.selection.enableSelection();
		}

	}

	public void addNewPetriNetObject(PetriNetObject newObject) {
		setLayer(newObject, DEFAULT_LAYER + newObject.getLayerOffset());
		newObject.zoomUpdate(zoomControl.getPercent());
		super.add(newObject);

		newObject.addedToGui();
		petriNetObjects.add(newObject);

		calculateNewBoundsForScrollPane(newObject.getBounds());
		if(newObject.getNameLabel() != null){
			calculateNewBoundsForScrollPane(newObject.getNameLabel().getBounds());
		}

		//Does not seem to be needed
		//validate();
		//repaint();
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

		selection.updateBounds();

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

	public void changeAnimationMode(boolean status) {
		animationmode = status;
		if(status){
			selection.disableSelection();
		}else{
			selection.enableSelection();
		}
	}

	public SelectionManager getSelectionObject() {
		return selection;
	}

	public UndoManager getUndoManager() {
		return undoManager;
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

	@Override
	public void removeAll() {
		petriNetObjects.clear();
		super.removeAll();
	}


	public boolean isInAnimationMode() {
		return animationmode;
	}

	public boolean getNetChanged() {
		return netChanged;
	}

	public void setNetChanged(boolean _netChanged) {
		netChanged = _netChanged;
	}

	public ArrayList<PetriNetObject> getPNObjects() {
		return petriNetObjects;
	}
	
	public ArrayList<PetriNetObject> getPlaceTransitionObjects(){
		ArrayList<PetriNetObject> result = new ArrayList<PetriNetObject>();
		for (PetriNetObject pnObject : petriNetObjects) {
			if((pnObject instanceof PlaceTransitionObject)){
				result.add(pnObject);
			}
		}
		return result;
	}

	@Override
	public void remove(Component comp) {
		petriNetObjects.remove(comp);

		super.remove(comp);
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

	void zoomToMidPoint() {

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

	class MouseHandler extends MouseInputAdapter {

		private PetriNetObject pnObject;

		private DrawingSurfaceImpl view;

		private DataLayer guiModel;

		private Point dragStart;

		private TimedArcPetriNet model;

		public void setModel(DataLayer newGuiModel, TimedArcPetriNet newModel) {
			this.guiModel = newGuiModel;
			this.model = newModel;
		}

		public MouseHandler(DrawingSurfaceImpl _view, DataLayer _model) {
			super();
			view = _view;
			guiModel = _model;
		}

		private Point adjustPoint(Point p, int zoom) {
			int offset = (int) (Zoomer.getScaleFactor(zoom)
					* Pipe.PLACE_TRANSITION_HEIGHT / 2);

			int x = Zoomer.getUnzoomedValue(p.x - offset, zoom);
			int y = Zoomer.getUnzoomedValue(p.y - offset, zoom);

			p.setLocation(x, y);
			return p;
		}

		private PlaceTransitionObject newTimedPlace(Point p) {
			p = adjustPoint(p, view.getZoom());
			dk.aau.cs.model.tapn.LocalTimedPlace tp = new dk.aau.cs.model.tapn.LocalTimedPlace(nameGenerator.getNewPlaceName(model));
			pnObject = new TimedPlaceComponent(Grid.getModifiedX(p.x), Grid
					.getModifiedY(p.y), tp);
			model.add(tp);
			guiModel.addPetriNetObject(pnObject);
			view.addNewPetriNetObject(pnObject);
			return (PlaceTransitionObject) pnObject;
		}

		private PlaceTransitionObject newTAPNTransition(Point p) {
			p = adjustPoint(p, view.getZoom());
			dk.aau.cs.model.tapn.TimedTransition transition = new dk.aau.cs.model.tapn.TimedTransition(
					nameGenerator.getNewTransitionName(model));

			pnObject = new TimedTransitionComponent(Grid.getModifiedX(p.x),
					Grid.getModifiedY(p.y), transition);
			
			model.add(transition);
			guiModel.addPetriNetObject(pnObject);
			view.addNewPetriNetObject(pnObject);
			return (PlaceTransitionObject) pnObject;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(app.getGUIMode().equals(GUIMode.animation)) return;

			// check for control down here enables it to attach the arc being drawn to an existing place/transition

			Point start = e.getPoint();
			Point p;
			if (SwingUtilities.isLeftMouseButton(e)) {

				Pipe.ElementType mode = app.getMode();
				PlaceTransitionObject pto;
				switch (mode) {

				case TAPNPLACE:
					// create place
					PlaceTransitionObject pto2 = newTimedPlace(e.getPoint());
					getUndoManager().addNewEdit(
							new AddTimedPlaceCommand(
									(TimedPlaceComponent) pto2, model,
									guiModel, view));
					if (e.isControlDown()) {
						// connect arc
						app.setMode(ElementType.TAPNARC);
						pto2.getMouseHandler().mousePressed(e);
						pto2.getMouseHandler().mouseReleased(e);
						app.setMode(ElementType.FAST_TRANSITION);
						// enter fast mode
						pnObject.dispatchEvent(e);
					}
					break;

				case TAPNTRANS:
					// create transition
					pto = newTAPNTransition(e.getPoint());
					getUndoManager().addNewEdit(
							new AddTimedTransitionCommand(
									(TimedTransitionComponent) pto, model,
									guiModel, view));
					if (e.isControlDown()) {
						// connect arc
						app.setMode(ElementType.TAPNARC);
						pto.getMouseHandler().mousePressed(e);
						pto.getMouseHandler().mouseReleased(e);
						// enter fast mode
						app.setMode(ElementType.FAST_PLACE);
						pnObject.dispatchEvent(e);
					}
					break;

				case ARC:
				case TAPNARC:
				case INHIBARC:
				case TRANSPORTARC:
				case TAPNINHIBITOR_ARC:
						// Add point to arc in creation
					if (createArc != null) {
						addPoint(createArc, e);
					}
					break;

				case ANNOTATION:
					p = adjustPoint(e.getPoint(), view.getZoom());

					pnObject = new AnnotationNote(p.x, p.y, true);
					guiModel.addPetriNetObject(pnObject);
					view.addNewPetriNetObject(pnObject);
					getUndoManager()
					.addNewEdit(
							new AddPetriNetObjectEdit(pnObject, view,
									guiModel));
					((AnnotationNote) pnObject).enableEditMode();
					break;
					case DRAG:
					dragStart = new Point(start);
					break;

				case FAST_TRANSITION:
					// create transition
					pto = newTAPNTransition(e.getPoint());
					getUndoManager().addNewEdit(new AddTimedTransitionCommand((TimedTransitionComponent) pto, model, guiModel, view));
					app.setMode(ElementType.TAPNARC);
					pto.getMouseHandler().mouseReleased(e);

					if (e.isControlDown()) {
						// connect arc
						pnObject.dispatchEvent(e);
						app.setMode(ElementType.TAPNARC);
						pto.getMouseHandler().mousePressed(e);
						pto.getMouseHandler().mouseReleased(e);
						// enter fast mode
						app.setMode(ElementType.FAST_PLACE);
					} else{
						app.endFastMode();
					}
					break;
				case FAST_PLACE:
					// create place
					PlaceTransitionObject pto3 = newTimedPlace(e.getPoint());
					getUndoManager().addNewEdit(new AddTimedPlaceCommand((TimedPlaceComponent) pto3, model, guiModel, view));
					app.setMode(ElementType.TAPNARC);
					pto3.getMouseHandler().mouseReleased(e);

					if (e.isControlDown()) {
						// connect arc
						pnObject.dispatchEvent(e);
						app.setMode(ElementType.TAPNARC);
						pto3.getMouseHandler().mousePressed(e);
						pto3.getMouseHandler().mouseReleased(e);
						// enter fast mode
						app.setMode(ElementType.FAST_TRANSITION);
					} else{
						app.endFastMode();
					}
					break;
				default:
					break;
				}
			} else {
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				dragStart = new Point(start);
			}
			updatePreferredSize();
		}

		private void addPoint(final Arc createArc, final MouseEvent e) {
			int x = Grid.getModifiedX(e.getX());
			int y = Grid.getModifiedY(e.getY());

			boolean shiftDown = e.isShiftDown();
			createArc.setEndPoint(x, y, shiftDown);
			createArc.getArcPath().addPoint(x, y, shiftDown);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			//setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (createArc != null) {
				createArc.setEndPoint(Grid.getModifiedX(e.getX()), Grid
						.getModifiedY(e.getY()), e.isShiftDown());
			}
		}

		/**
		 * @see javax.swing.event.MouseInputAdapter#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			// if (CreateGui.getApp().getMode() == Pipe.DRAG){
			view.drag(dragStart, e.getPoint());
			// }
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
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

	public boolean isCurrentGuiModel(DataLayer dataLayer) {
		return guiModel.equals(dataLayer);
	}

	public void repaintAll() {
		this.repaint();
		guiModel.repaintAll(!isInAnimationMode());
	}

	public void setupNameGeneratorsFromTemplates(Iterable<Template> templates) {
		nameGenerator.setupNameGeneratorFromTemplates(templates);
	}
}
