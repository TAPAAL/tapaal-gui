package pipe.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import pipe.dataLayer.AnnotationNote;
import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Note;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TransportArcComponent;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.gui.handler.AnimationHandler;
import pipe.gui.handler.AnnotationNoteHandler;
import pipe.gui.handler.ArcHandler;
import pipe.gui.handler.LabelHandler;
import pipe.gui.handler.PlaceHandler;
import pipe.gui.handler.TAPNTransitionHandler;
import pipe.gui.handler.TimedArcHandler;
import pipe.gui.handler.TransitionHandler;
import pipe.gui.handler.TransportArcHandler;
import pipe.gui.undo.AddPetriNetObjectEdit;
import pipe.gui.undo.AddTimedPlaceCommand;
import pipe.gui.undo.AddTimedTransitionCommand;
import pipe.gui.undo.UndoManager;
import dk.aau.cs.gui.DrawingSurface;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;


/**
 * The petrinet is drawn onto this frame.
 */
public class DrawingSurfaceImpl 
extends JLayeredPane 
implements Observer, Printable, DrawingSurface {
	private static final long serialVersionUID = 4434596266503933386L;

	private boolean netChanged = false;

	private boolean animationmode = false;

	public Arc createArc;  //no longer static
	public TransportArcComponent transportArcPart1;  //used when creating transport arcs

	public PlaceTransitionObject createPTO;

	private AnimationHandler animationHandler = new AnimationHandler();

	// When i'm using GNU/Linux, isMetaDown() doesn't return true when I press 
	// "Windows key". I don't know if a problem of my configuration or what.
	// metaDown is used in this case
	boolean metaDown = false; 

	private SelectionManager selection;
	private UndoManager undoManager;
	private ArrayList <PetriNetObject> petriNetObjects = new ArrayList<PetriNetObject>();
	private GuiFrame app = CreateGui.getApp();
	private Zoomer zoomControl;

	// flag used in fast mode to know if a new PetriNetObject has been created
	public boolean newPNO = false;

	// flag used in paintComponents() to know if a call to zoom() has been done
	private boolean doSetViewPosition = true;

	// position where the viewport must be set
	private Point viewPosition = new Point(0,0);

	private DataLayer guiModel;
	private TimedArcPetriNet model;
	private TabContent parent;
	private MouseHandler mouseHandler;
	private NameGenerator<TimedArcPetriNet> nameGenerator = new NameGenerator<TimedArcPetriNet>();

	public DrawingSurfaceImpl(DataLayer dataLayer, TabContent parent) {
		guiModel = dataLayer;
		this.parent = parent;
		setLayout(null);
		setOpaque(true);
		setDoubleBuffered(true);
		setAutoscrolls(true);
		setBackground(Pipe.ELEMENT_FILL_COLOUR);

		zoomControl = new Zoomer(100, app);



		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		mouseHandler = new MouseHandler(this, guiModel);
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		addMouseWheelListener(mouseHandler);

		selection = new SelectionManager(this);
		undoManager = new UndoManager(this, guiModel, app);
	}

	public void setModel(DataLayer guiModel, TimedArcPetriNet model){
		nameGenerator.add(model);
		this.mouseHandler.setModel(guiModel, model);
		this.undoManager.setModel(guiModel);
		this.guiModel = guiModel;
		this.model = model;

		this.removeAll();
		for(PetriNetObject pnObject : guiModel.getPetriNetObjects()){
			add(pnObject);
		}
		repaint();
	}

	public void addNewPetriNetObject(PetriNetObject newObject) {
		if (newObject != null) {
			if (newObject.getMouseListeners().length == 0) {
				if (newObject instanceof Place) {
					// XXX - kyrke
					if (newObject instanceof TimedPlaceComponent) {

						LabelHandler labelHandler =
							new LabelHandler(((Place)newObject).getNameLabel(),
									(Place)newObject);
						((Place)newObject).getNameLabel().addMouseListener(labelHandler);
						((Place)newObject).getNameLabel().addMouseMotionListener(labelHandler);
						((Place)newObject).getNameLabel().addMouseWheelListener(labelHandler);

						PlaceHandler placeHandler =
							new PlaceHandler(this, (Place)newObject, this.guiModel, this.model);
						newObject.addMouseListener(placeHandler);
						newObject.addMouseWheelListener(placeHandler);
						newObject.addMouseMotionListener(placeHandler);
						add(newObject);

					}else{

						LabelHandler labelHandler =
							new LabelHandler(((Place)newObject).getNameLabel(),
									(Place)newObject);
						((Place)newObject).getNameLabel().addMouseListener(labelHandler);
						((Place)newObject).getNameLabel().addMouseMotionListener(labelHandler);
						((Place)newObject).getNameLabel().addMouseWheelListener(labelHandler);

						PlaceHandler placeHandler =
							new PlaceHandler(this, (Place)newObject);
						newObject.addMouseListener(placeHandler);
						newObject.addMouseWheelListener(placeHandler);
						newObject.addMouseMotionListener(placeHandler);
						add(newObject);

					}
				} else if (newObject instanceof Transition) {
					TransitionHandler transitionHandler;
					if (newObject instanceof TimedTransitionComponent){
						transitionHandler =
							new TAPNTransitionHandler(this, (Transition)newObject, guiModel, model);
					}else {
						transitionHandler =
							new TransitionHandler(this, (Transition)newObject);	
					}

					LabelHandler labelHandler =
						new LabelHandler(((Transition)newObject).getNameLabel(),
								(Transition)newObject);
					((Transition)newObject).getNameLabel().addMouseListener(labelHandler);
					((Transition)newObject).getNameLabel().addMouseMotionListener(labelHandler);
					((Transition)newObject).getNameLabel().addMouseWheelListener(labelHandler);

					newObject.addMouseListener(transitionHandler);
					newObject.addMouseMotionListener(transitionHandler);
					newObject.addMouseWheelListener(transitionHandler);


					newObject.addMouseListener(animationHandler);

					add(newObject);
				} else if (newObject instanceof Arc) {
					add(newObject);
					/* CB - Joakim Byg add timed arcs*/
					if (newObject instanceof TimedInputArcComponent){
						if (newObject instanceof TransportArcComponent){ 
							TransportArcHandler transportArcHandler = new TransportArcHandler(this, (Arc)newObject);
							newObject.addMouseListener(transportArcHandler);
							newObject.addMouseWheelListener(transportArcHandler);
							newObject.addMouseMotionListener(transportArcHandler);
						}else {
							TimedArcHandler timedArcHandler = new TimedArcHandler(this, (Arc)newObject);
							newObject.addMouseListener(timedArcHandler);
							newObject.addMouseWheelListener(timedArcHandler);
							newObject.addMouseMotionListener(timedArcHandler);
						}
					}else {
						/*EOC*/            	
						ArcHandler arcHandler = new ArcHandler(this, (Arc)newObject);
						newObject.addMouseListener(arcHandler);
						newObject.addMouseWheelListener(arcHandler);
						newObject.addMouseMotionListener(arcHandler);
					}
				} else if (newObject instanceof AnnotationNote) {
					add(newObject);
					AnnotationNoteHandler noteHandler =
						new AnnotationNoteHandler(this, (AnnotationNote)newObject);
					newObject.addMouseListener(noteHandler);
					newObject.addMouseMotionListener(noteHandler);
					((Note)newObject).getNote().addMouseListener(noteHandler);
					((Note)newObject).getNote().addMouseMotionListener(noteHandler);
				} 

				if (newObject instanceof Zoomable) {
					newObject.zoomUpdate(getZoom());
				}
			}
			newObject.setGuiModel(guiModel);
		}
		validate();
		repaint();
	}


	public void update(Observable o, Object diffObj) {
		if ((diffObj instanceof PetriNetObject) && (diffObj != null)) {
			if (CreateGui.appGui.getMode() == Pipe.CREATING) {

				addNewPetriNetObject((PetriNetObject)diffObj);
			}
			repaint();
		}
	}


	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
	throws PrinterException {
		if (pageIndex > 0) {
			return Printable.NO_SUCH_PAGE;
		}
		Graphics2D g2D = (Graphics2D) g;
		//Move origin to page printing area corner
		g2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		g2D.scale(0.5,0.5);
		print(g2D); // Draw the net

		return Printable.PAGE_EXISTS;
	}


	/**
	 * This method is called whenever the frame is moved, resized etc.
	 * It iterates over the existing petrinet objects and repaints them.
	 * TODO: write a better description than this since it is now totally 
	 * not happening.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (Grid.isEnabled()) {
			Grid.updateSize(this);
			Grid.drawGrid(g);
		}

		selection.updateBounds();

		if (doSetViewPosition) {
			((JViewport)getParent()).setViewPosition(viewPosition);         
			app.validate();
			doSetViewPosition = false;
		}      
	}


	public void updatePreferredSize() {
		// iterate over net objects
		// setPreferredSize() accordingly

		Component[] components = getComponents();
		Dimension d = new Dimension(0,0);
		for (int i = 0; i < components.length; i++) {
			if (components[i].getClass() == SelectionManager.class) {
				continue; // SelectionObject not included
			}
			Rectangle r = components[i].getBounds();
			int x = r.x + r.width + 20;
			int y = r.y + r.height + 20;
			if (x > d.width) {
				d.width = x;
			}
			if (y > d.height) {
				d.height = y;
			}
		}
		setPreferredSize(d);
		Container parent = getParent();
		if (parent != null) {
			parent.validate();
		}
	}


	public void changeAnimationMode(boolean status) {
		animationmode = status;
	}


	public void setCursorType(String type) {
		if (type.equals("arrow")) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if (type.equals("crosshair")) {
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		} else if (type.equals("move")) {
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
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


	public void zoom() {
		Component[] children = getComponents();

		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof Zoomable) {
				((Zoomable)children[i]).zoomUpdate(zoomControl.getPercent());
			}
		}
		doSetViewPosition = true;
	}


	public void add(PetriNetObject pnObject) {
		setLayer(pnObject, DEFAULT_LAYER.intValue() + pnObject.getLayerOffset());
		super.add(pnObject);
		pnObject.addedToGui();
		petriNetObjects.add(pnObject);
	}


	//
	public void setMetaDown(boolean down) {
		metaDown = down;
	}


	public Point getPointer() {
		return getMousePosition();
	}

	/*Cb Joakim Byg - Animation not needed at the moment   
   public AnimationHandler getAnimationHandler() {
      return animationHandler;
   }
EOC*/   

	public boolean isInAnimationMode(){
		return animationmode;
	}


	public boolean getNetChanged() {
		return netChanged;
	}


	public void setNetChanged(boolean _netChanged) {
		netChanged = _netChanged;
	}


	public ArrayList <PetriNetObject> getPNObjects() {
		return petriNetObjects;
	}


	@Override
	public void remove(Component comp) {
		petriNetObjects.remove(comp);
		//if (result) {
		//   System.out.println("DEBUG: remove PNO from view");
		///}
		super.remove(comp);
	}


	public void drag(Point dragStart, Point dragEnd) {
		if (dragStart == null) {
			return;
		}
		JViewport viewer = (JViewport)getParent();
		Point offScreen = viewer.getViewPosition();
		if (dragStart.x > dragEnd.x){
			offScreen.translate(viewer.getWidth(), 0);
		}
		if (dragStart.y > dragEnd.y){
			offScreen.translate(0, viewer.getHeight());
		}
		offScreen.translate(dragStart.x - dragEnd.x, dragStart.y - dragEnd.y);
		Rectangle r = new Rectangle(offScreen.x, offScreen.y, 1, 1);
		scrollRectToVisible(r);
	}


	private Point midpoint(int zoom){
		JViewport viewport = (JViewport)getParent();
		double midpointX = Zoomer.getUnzoomedValue(
				viewport.getViewPosition().x + (viewport.getWidth() * 0.5), zoom);
		double midpointY = Zoomer.getUnzoomedValue(
				viewport.getViewPosition().y + (viewport.getHeight() * 0.5), zoom);
		return (new java.awt.Point((int)midpointX, (int)midpointY));
	}


	public void zoomIn(){
		int zoom = zoomControl.getPercent();
		if (zoomControl.zoomIn()) {
			zoomTo(midpoint(zoom));
		}
	}


	public void zoomOut(){
		int zoom = zoomControl.getPercent();
		if (zoomControl.zoomOut()) {
			zoomTo(midpoint(zoom));
		}
	}


	public void zoomTo(Point point){
		// The zoom is not as smooth as it should be. As far I know, this behavior
		// is caused when the method setSize() is called in NameLabel's updateSize()
		// In order to disguise it, the view is hidden and a white layer is shown.
		// I know it's not a smart solution...
		// I think zoom function should be redone from scratch so that BlankLayer
		// class and doSetViewPosition could be removed


		int zoom = zoomControl.getPercent();

		JViewport viewport = (JViewport)getParent();

		Zoomer.getUnzoomedValue(
				viewport.getViewPosition().x + (viewport.getWidth() * 0.5), zoom);
		double newZoomedX = Zoomer.getZoomedValue(point.x, zoom);
		double newZoomedY = Zoomer.getZoomedValue(point.y, zoom);

		int newViewX = (int)(newZoomedX - (viewport.getWidth() * 0.5));
		if (newViewX < 0) {
			newViewX = 0;
		}

		int newViewY = (int)(newZoomedY - (viewport.getHeight() * 0.5));
		if (newViewY < 0) {
			newViewY = 0;
		}

		//if (doSetViewPosition) {
		viewPosition.setLocation(newViewX, newViewY);
		viewport.setViewPosition(viewPosition);
		//}

		zoom();

		app.hideNet(true); // hide current view :-(

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

		public void setModel(DataLayer newGuiModel, TimedArcPetriNet newModel){
			this.guiModel = newGuiModel;
			this.model = newModel;
		}


		public MouseHandler(DrawingSurfaceImpl _view, DataLayer _model){
			super();
			view = _view;
			guiModel = _model;
		}


		private Point adjustPoint(Point p, int zoom) {
			int offset = (int)(Zoomer.getScaleFactor(zoom) *
					Pipe.PLACE_TRANSITION_HEIGHT/2);

			int x = Zoomer.getUnzoomedValue(p.x - offset, zoom);
			int y = Zoomer.getUnzoomedValue(p.y - offset, zoom);

			p.setLocation(x, y);
			return p;
		}


		private PlaceTransitionObject newPlace(Point p){
			p = adjustPoint(p, view.getZoom());

			pnObject = new Place(Grid.getModifiedX(p.x), Grid.getModifiedY(p.y));
			guiModel.addPetriNetObject(pnObject);
			view.addNewPetriNetObject(pnObject);
			return (PlaceTransitionObject)pnObject;
		}

		private PlaceTransitionObject newTimedPlace(Point p){
			p = adjustPoint(p, view.getZoom());
			dk.aau.cs.model.tapn.TimedPlace tp = new dk.aau.cs.model.tapn.TimedPlace(nameGenerator.getNewPlaceName(model));
			pnObject = CreateGui.getModel().isUsingColors() ? new ColoredTimedPlace(Grid.getModifiedX(p.x), Grid.getModifiedY(p.y)) 
			: new TimedPlaceComponent(Grid.getModifiedX(p.x), Grid.getModifiedY(p.y), tp);
			model.add(tp);
			guiModel.addPetriNetObject(pnObject);
			view.addNewPetriNetObject(pnObject);
			return (PlaceTransitionObject)pnObject;
		}


		private PlaceTransitionObject newTransition(Point p, boolean timed){
			p = adjustPoint(p, view.getZoom());

			pnObject = new Transition(Grid.getModifiedX(p.x),
					Grid.getModifiedY(p.y));
			((Transition)pnObject).setTimed(timed);
			guiModel.addPetriNetObject(pnObject);
			view.addNewPetriNetObject(pnObject);
			return (PlaceTransitionObject)pnObject;
		}

		private PlaceTransitionObject newTAPNTransition(Point p, boolean timed){
			p = adjustPoint(p, view.getZoom());
			dk.aau.cs.model.tapn.TimedTransition transition = new dk.aau.cs.model.tapn.TimedTransition(nameGenerator.getNewTransitionName(model));

			pnObject = new TimedTransitionComponent(Grid.getModifiedX(p.x),
					Grid.getModifiedY(p.y), transition);
			((Transition)pnObject).setTimed(timed);
			model.add(transition);
			guiModel.addPetriNetObject(pnObject);
			view.addNewPetriNetObject(pnObject);
			return (PlaceTransitionObject)pnObject;
		}

		private PlaceTransitionObject newTAPNTransition(Point p){
			return newTAPNTransition(p, false);
		}

		@Override
		public void mousePressed(MouseEvent e){
			Point start = e.getPoint();
			Point p;
			if (SwingUtilities.isLeftMouseButton(e)) {
				int mode = app.getMode();
				switch (mode){
				case Pipe.PLACE:
					PlaceTransitionObject pto = newPlace(e.getPoint());
					getUndoManager().addNewEdit(
							new AddPetriNetObjectEdit(pto, view, guiModel));
					if (e.isControlDown()) {
						app.setFastMode(Pipe.FAST_TRANSITION);
						pnObject.dispatchEvent(e);
					}
					break;

				case Pipe.TAPNPLACE:
					PlaceTransitionObject pto2 = newTimedPlace(e.getPoint());
					getUndoManager().addNewEdit(
							new AddTimedPlaceCommand((TimedPlaceComponent)pto2, model, guiModel, view));
					if (e.isControlDown()) {
						app.setFastMode(Pipe.FAST_TRANSITION);
						pnObject.dispatchEvent(e);
					}
					break;

				case Pipe.IMMTRANS:
				case Pipe.TIMEDTRANS:
					boolean timed = (mode == Pipe.TIMEDTRANS ? true : false);
					pto = newTransition(e.getPoint(), timed);
					getUndoManager().addNewEdit(
							new AddPetriNetObjectEdit(pto, view, guiModel));
					if (e.isControlDown()) {
						app.setFastMode(Pipe.FAST_PLACE);
						pnObject.dispatchEvent(e);
					}
					break;
				case Pipe.TAPNTRANS:
					pto = newTAPNTransition(e.getPoint());
					getUndoManager().addNewEdit(
							new AddTimedTransitionCommand((TimedTransitionComponent)pto, model, guiModel, view));
					if (e.isControlDown()) {
						app.setFastMode(Pipe.FAST_PLACE);
						pnObject.dispatchEvent(e);
					}
					break;

				case Pipe.ARC:
					/*CB Joakim Byg - handle TimedArc*/            	   
				case Pipe.TAPNARC: 
					/*EOC*/            	   
				case Pipe.INHIBARC:
					// Add point to arc in creation
					if (createArc != null) {
						addPoint(createArc, e);
					}
					break;

				case Pipe.ANNOTATION:
					p = adjustPoint(e.getPoint(), view.getZoom());

					pnObject = new AnnotationNote(p.x, p.y);
					guiModel.addPetriNetObject(pnObject);
					view.addNewPetriNetObject(pnObject);
					getUndoManager().addNewEdit(
							new AddPetriNetObjectEdit(pnObject, view, guiModel));
					((AnnotationNote)pnObject).enableEditMode();
					break;
					//				case Pipe.FAST_PLACE:
					//					if (e.isMetaDown() || metaDown) { // provisional
					//						if (createArc != null) {
					//							addPoint(createArc, e);
					//						}
					//					} else {
					//						if (createArc == null) {
					//							break;
					//						}                     
					//						// user has not clicked on an old PetriNetObject, so
					//						// a new PNO must be created
					//						view.newPNO = true;
					//
					//						createPTO = newPlace(e.getPoint());
					//						getUndoManager().addNewEdit(
					//								new AddPetriNetObjectEdit(createPTO, view, guiModel));
					//						pnObject.getMouseListeners()[0].mouseReleased(e);
					//						if (e.isControlDown()){
					//							// keep "fast mode"
					//							app.setMode(Pipe.FAST_TRANSITION);
					//							pnObject.getMouseListeners()[0].mousePressed(e);
					//						} else {
					//							//exit "fast mode"
					//							app.resetMode();
					//						}
					//					}
					//					break;
					//
					//				case Pipe.FAST_TRANSITION:
					//					if (e.isMetaDown() || metaDown) { // provisional
					//						if (createArc != null) {
					//							addPoint(createArc, e);
					//						}
					//					} else {
					//						if ( createArc == null) {
					//							break;
					//						}
					//						// user has not clicked on an old PetriNetObject, so
					//						// a new PNO must be created
					//						view.newPNO = true;
					//
					//						createPTO = newTransition(e.getPoint(), e.isAltDown());
					//						getUndoManager().addNewEdit(
					//								new AddPetriNetObjectEdit(createPTO, view, guiModel));
					//						pnObject.getMouseListeners()[0].mouseReleased(e);
					//						if (e.isControlDown()){
					//							// keep "fast mode"
					//							app.setMode(Pipe.FAST_PLACE);
					//							pnObject.getMouseListeners()[0].mousePressed(e);
					//						} else {
					//							// exit "fast mode"
					//							app.resetMode();
					//						}
					//					}
					//					break;
					//
					//				case Pipe.FAST_TAPNPLACE:
					//
					//					//kyrke working 
					//					if (e.isMetaDown() || metaDown) { // provisional
					//						if (createArc != null) {
					//							addPoint(createArc, e);
					//						}
					//					} else {
					//						if (createArc == null) {
					//							break;
					//						}                     
					//						// user has not clicked on an old PetriNetObject, so
					//						// a new PNO must be created
					//						view.newPNO = true;
					//
					//						createPTO = newTimedPlace(e.getPoint());
					//						getUndoManager().addNewEdit(
					//								new AddTimedPlaceCommand((TimedPlaceComponent)createPTO, model, guiModel, view));
					//						pnObject.getMouseListeners()[0].mouseReleased(e);
					//						if (e.isControlDown()){
					//
					//							// keep "fast mode"
					//							app.setMode(Pipe.FAST_TAPNTRANSITION);
					//							pnObject.getMouseListeners()[0].mousePressed(e);
					//						} else {
					//							//exit "fast mode"
					//							app.resetMode();
					//						}
					//					}
					//					break;
					//
					//				case Pipe.FAST_TAPNTRANSITION:
					//					//kyrke working 
					//
					//					if (e.isMetaDown() || metaDown) { // provisional
					//						if (createArc != null) {
					//							addPoint(createArc, e);
					//						}
					//					} else {
					//						if ( createArc == null) {
					//							break;
					//						}
					//						// user has not clicked on an old PetriNetObject, so
					//						// a new PNO must be created
					//						view.newPNO = true;
					//
					//						createPTO = newTAPNTransition(e.getPoint(), e.isAltDown());
					//						getUndoManager().addNewEdit(
					//								new AddTimedTransitionCommand((TimedTransitionComponent)createPTO, model, guiModel, view));
					//						pnObject.getMouseListeners()[0].mouseReleased(e);
					//						if (e.isControlDown()){
					//							// keep "fast mode"
					//
					//							app.setMode(Pipe.FAST_TAPNPLACE);
					//							pnObject.getMouseListeners()[0].mousePressed(e);
					//							pnObject.getMouseListeners()[0].mousePressed(e);
					//						} else {
					//							// exit "fast mode"
					//							app.resetMode();
					//						}
					//					}
					//					break;
				case Pipe.TRANSPORTARC: 
					if (createArc != null) {
						addPoint(createArc, e);
					}
					break;
				case Pipe.TAPNINHIBITOR_ARC:
					// Add point to arc in creation
					if (createArc != null) {
						addPoint(createArc, e);
					}
					break;
				case Pipe.DRAG:
					dragStart = new Point(start);
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
		public void mouseReleased(MouseEvent e){
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}


		@Override
		public void mouseMoved(MouseEvent e) {
			if (createArc != null) {
				createArc.setEndPoint(Grid.getModifiedX(e.getX()),
						Grid.getModifiedY(e.getY()),
						e.isShiftDown());
			}
		}

		/**
		 * @see javax.swing.event.MouseInputAdapter#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			//if (CreateGui.getApp().getMode() == Pipe.DRAG){
			view.drag(dragStart, e.getPoint());
			// }
		}


		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (!e.isControlDown()) {
				return;
			} else {
				if (e.getWheelRotation()> 0) {
					view.zoomIn();
				} else {
					view.zoomOut();
				}
			}
		}
	}



	public boolean isCurrentGuiModel(DataLayer dataLayer) {
		return guiModel.equals(dataLayer);
	}
}
