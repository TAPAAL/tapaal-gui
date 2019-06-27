package pipe.gui.graphicElements;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;

import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.handler.LabelHandler;

/**
 * Petri-Net Place or Transition SuperClass
 * 
 */
public abstract class PlaceTransitionObject extends PetriNetObject {

	private static final long serialVersionUID = -6629006415467929184L;

	protected double componentWidth;
	protected double componentHeight;

	private LinkedList<Arc> connectTo = new LinkedList<Arc>();
	private LinkedList<Arc> connectFrom = new LinkedList<Arc>();

	protected boolean attributesVisible = false;

	// The "real" x/y coordinate of this place or transition in the net.
	// i.e. the x position at 100% zoom.
	private double locationX;
	private double locationY;

	/**
	 * Create Petri-Net Object
	 * 
	 * @param positionXInput
	 *            X-axis Position
	 * @param positionYInput
	 *            Y-axis Position
	 * @param idInput
	 *            Place id
	 * @param nameOffsetXInput
	 *            Name X-axis Position
	 * @param nameOffsetYInput
	 *            Name Y-axis Position
	 */
	public PlaceTransitionObject(double positionXInput, double positionYInput,
			String idInput, double nameOffsetXInput,
			double nameOffsetYInput) {
		this(positionXInput, positionYInput);
		id = idInput;
		nameOffsetX = nameOffsetXInput;
		nameOffsetY = nameOffsetYInput;
		pnName.setPosition((int) nameOffsetX, (int) nameOffsetY);
		// setName(nameInput);
	}

	/**
	 * Create Petri-Net Object This constructor does all the work, the others
	 * just call it.
	 * 
	 * @param positionXInput
	 *            X-axis Position
	 * @param positionYInput
	 *            Y-axis Position
	 */
	public PlaceTransitionObject(double positionXInput, double positionYInput) {

		setPositionX(positionXInput);
		setPositionY(positionYInput);

		nameOffsetX = Pipe.DEFAULT_OFFSET_X;
		nameOffsetY = Pipe.DEFAULT_OFFSET_Y;

		// sets up Namelabel for each PN object
		pnName = new NameLabel(zoom);
		LabelHandler labelHandler = new LabelHandler(pnName, this);
		getNameLabel().addMouseListener(labelHandler);
		getNameLabel().addMouseMotionListener(labelHandler);
		getNameLabel().addMouseWheelListener(labelHandler);
	}

	/**
	 * Set X-axis position
	 * 
	 * @param positionXInput
	 *            Double value for X-axis position
	 */
	public void setPositionX(double positionXInput) {
		positionX = positionXInput;
		locationX = Zoomer.getUnzoomedValue(positionX, zoom);
	}

	/**
	 * Set Y-axis position
	 * 
	 * @param positionYInput
	 *            Double value for Y-axis position
	 */
	public void setPositionY(double positionYInput) {
		positionY = positionYInput;
		locationY = Zoomer.getUnzoomedValue(positionY, zoom);
	}

	/**
	 * Set name
	 * 
	 * @param nameInput
	 *            String value for Place name;
	 */
	@Override
	public void setName(String nameInput) {
		// sets the text within the label
		// System.out.println("setting name to: " + nameInput);
		pnName.setName(nameInput);
	}

	/**
	 * Get X-axis position
	 * 
	 * @return Double value for X-axis position
	 */
	public double getPositionX() {
		return positionX;
	}

	/**
	 * Get Y-axis position
	 * 
	 * @return Double value for Y-axis position
	 */
	public double getPositionY() {
		return positionY;
	}

	/**
	 * Set id
	 * 
	 * @param idInput
	 *            String value for Place id;
	 */
	@Override
	public void setId(String idInput) {
		id = idInput;
		setName(id);
	}

	/**
	 * Get id
	 * 
	 * @return String value for Place id;
	 */
	@Override
	public String getId() {
		return getName();
	}

	/**
	 * Get name
	 * 
	 * @return String value for Place name;
	 */
	@Override
	public String getName() {
		return (pnName != null) ? pnName.getName() : "";
	}

	public double getNameOffsetX() {
		return nameOffsetX;
	}

	public double getNameOffsetY() {
		return nameOffsetY;
	}

	/**
	 * Get X-axis position, returns null if value not yet entered
	 * 
	 * @return Double value for X-axis position
	 */
	public Double getPositionXObject() {
		return locationX;
		// return new Double(positionX);
	}

	/**
	 * Get Y-axis position, returns null if value not yet entered
	 * 
	 * @return Double value for Y-axis position
	 */
	public Double getPositionYObject() {
		return locationY;
		// return new Double(positionY);
	}

	/**
	 * Implemented in subclasses as involves some tailoring according to the
	 * shape
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.translate(COMPONENT_DRAW_OFFSET, COMPONENT_DRAW_OFFSET);
		g2.transform(Zoomer.getTransform(zoom));
	}

	public Point2D getIntersectOffset(Point2D start) {
		return new Point2D.Double();
	}

	/**
	 * Returns the distance between the outside of the component to the centre,
	 * in order to position the centre of the place where the mouse clicks on
	 * the screen
	 * 
	 * @return Top offset of Place
	 */
	public int centreOffsetTop() {
		return (int) (Zoomer.getZoomedValue(componentHeight / 2.0, zoom));
	}

	/**
	 * Returns the distance between the outside of the component to the centre,
	 * in order to position the centre of the place where the mouse clicks on
	 * the screen
	 * 
	 * @return Left offset of Place
	 */
	public int centreOffsetLeft() {
		return (int) (Zoomer.getZoomedValue(componentWidth / 2.0, zoom));
	}

	/** Calculates the BoundsOffsets used for setBounds() method */
	public void updateBounds() {
		double scaleFactor = Zoomer.getScaleFactor(zoom);
		positionX = locationX * scaleFactor;
		positionY = locationY * scaleFactor;
		bounds.setBounds((int) positionX, (int) positionY,
				(int) (componentWidth * scaleFactor),
				(int) (componentHeight * scaleFactor));
		bounds.grow(COMPONENT_DRAW_OFFSET, COMPONENT_DRAW_OFFSET);
		setBounds(bounds);
	}

	/** Adds outwards arc to place/transition */
	public void addConnectTo(Arc newArc) {
		connectTo.add(newArc);
	}

	/** Adds inwards arc to place/transition */
	public void addConnectFrom(Arc newArc) {
		connectFrom.add(newArc);
	}

	public void removeFromArc(Arc oldArc) {
		connectFrom.remove(oldArc);
	}

	public void removeToArc(Arc oldArc) {
		connectTo.remove(oldArc);
	}

	/** Updates location of any attached arcs */
	public void updateConnected() {
		Iterator<Arc> arcsFrom = connectFrom.iterator();

		Arc someArc;
		while (arcsFrom.hasNext()) {
			someArc = (arcsFrom.next());
			updateEndPoint(someArc);
			someArc.updateArcPosition();
		}

		for (Arc connectTo1 : connectTo) {
			someArc = (connectTo1);
			updateEndPoint(someArc);
			someArc.updateArcPosition();
		}
	}

	/** Translates the component by x,y */
	public void translate(int x, int y) {
		setPositionX(positionX + x);
		setPositionY(positionY + y);
		updateOnMoveOrZoom();
	}

	/** Sets the center of the component to position x, y */
	public void setCentre(double x, double y) {
		setPositionX(x - (getWidth() / 2.0));
		setPositionY(y - (getHeight() / 2.0));
		update(true);
	}

	public void update(boolean displayConstantNames) {
		updateOnMoveOrZoom();
	}

	public Point2D.Double getCentre() {
		return new Point2D.Double(positionX + getWidth() / 2.0, positionY
				+ getHeight() / 2.0);
	}

	@Override
	public void delete() {
		if (getParent() != null) {
			getParent().remove(pnName);
		}
		super.delete();
	}

	/** Handles selection for Place/Transitions */
	@Override
	public void select() {
		if (selectable && !selected) {
			selected = true;

			if (pnName != null) {
				pnName.setForeground(Pipe.SELECTION_LINE_COLOUR);
			}

			// Select arcs that are connected from this object to another selected object.
			for (Arc arc : getPostset()) {
				if(arc.getTarget().isSelected()){
					arc.select();
					arc.selectPath();
				}
			}

			// Select arcs that are connected to this object from another selected object.
			for (Arc arc : getPreset()) {
				if(arc.getSource().isSelected()){
					arc.select();
					arc.selectPath();
				}
			}
			repaint();
		}
	}

	@Override
	public void addedToGui() {
		deleted = false;
		addLabelToContainer();
		update(true);
		updateOnMoveOrZoom();
	}

    @Override
    public void removedFromGui() {
        removeLabelFromContainer();
    }

    // TODO: Find a better name for this
	@Override
	public void updateOnMoveOrZoom() {
		updateBounds();
		updateConnected();
		updateLabelLocation();
	}

	public boolean areNotSameType(PlaceTransitionObject o) {
		return (this.getClass() != o.getClass());
	}

	/* KYRKE - added functions for getting pre and post set af lists */
	public LinkedList<Arc> getPreset() {
		// XXX - possible type error
		return new LinkedList<Arc>(connectTo);
	}

	public LinkedList<Arc> getPostset() {
		// XXX - possible type error
		return new LinkedList<Arc>(connectFrom);
	}

	public Iterator<Arc> getConnectFromIterator() {
		return connectFrom.iterator();
	}

	public Iterator<Arc> getConnectToIterator() {
		return connectTo.iterator();
	}

	public abstract void updateEndPoint(Arc arc);

	public abstract void showEditor();

	public void setAttributesVisible(boolean flag) {
		attributesVisible = flag;
	}

	public boolean getAttributesVisible() {
		return attributesVisible;
	}

	@Override
	public int getLayerOffset() {
		return Pipe.PLACE_TRANSITION_LAYER_OFFSET;
	}

	public abstract void toggleAttributesVisible();

	public void zoomUpdate(int value) {
		zoom = value;
		update(true);
	}

}
