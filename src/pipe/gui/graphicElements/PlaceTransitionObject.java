package pipe.gui.graphicElements;

import java.awt.*;
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
public abstract class PlaceTransitionObject extends PetriNetObjectWithLabel {

	private static final long serialVersionUID = -6629006415467929184L;

	protected double componentWidth;
	protected double componentHeight;

	private LinkedList<Arc> connectTo = new LinkedList<Arc>();
	private LinkedList<Arc> connectFrom = new LinkedList<Arc>();

	protected boolean attributesVisible = false;

	public PlaceTransitionObject(
			int positionXInput,
			int positionYInput,
			String idInput,
			int nameOffsetXInput,
			int nameOffsetYInput
	){
		super(nameOffsetXInput, nameOffsetYInput);

		setPositionX(positionXInput);
		setPositionY(positionYInput);

		id = idInput;

	}

	public PlaceTransitionObject(int positionXInput, int positionYInput) {
		this(positionXInput, positionYInput, null, Pipe.DEFAULT_OFFSET_X, Pipe.DEFAULT_OFFSET_Y);
	}

	@Deprecated
	public PlaceTransitionObject(
			double positionXInput,
			double positionYInput,
			String idInput,
			double nameOffsetXInput,
			double nameOffsetYInput
	) {
		this((int)positionXInput, (int) positionYInput, idInput, (int)nameOffsetXInput, (int)nameOffsetYInput);
	}

	@Deprecated
	public PlaceTransitionObject(double positionXInput, double positionYInput) {
		this(positionXInput, positionYInput, null, Pipe.DEFAULT_OFFSET_X, Pipe.DEFAULT_OFFSET_Y);
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
		getNameLabel().setName(nameInput);
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
		return (getNameLabel() != null) ? getNameLabel().getName() : "";
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
		g2.transform(Zoomer.getTransform(getZoom()));
	}

	/**
	 * Returns the distance between the outside of the component to the centre,
	 * in order to position the centre of the place where the mouse clicks on
	 * the screen
	 * 
	 * @return Top offset of Place
	 */
	public int centreOffsetTop() {
		return (int) (Zoomer.getZoomedValue(componentHeight / 2.0, getZoom()));
	}

	/**
	 * Returns the distance between the outside of the component to the centre,
	 * in order to position the centre of the place where the mouse clicks on
	 * the screen
	 * 
	 * @return Left offset of Place
	 */
	public int centreOffsetLeft() {
		return (int) (Zoomer.getZoomedValue(componentWidth / 2.0, getZoom()));
	}

	/** Calculates the BoundsOffsets used for setBounds() method */
	public void updateBounds() {
		double scaleFactor = Zoomer.getScaleFactor(getZoom());
		positionX = originalX * scaleFactor;
		positionY = originalY * scaleFactor;
		Rectangle bounds = new Rectangle();
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
		for (Arc someArc : connectFrom) {
			updateEndPoint(someArc);
			someArc.updateArcPosition();
		}

		for (Arc someArc : connectTo) {
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
		update(displayConstantNames, true);
	}

	public void update(boolean displayConstantNames, boolean alignToGrid) {
		updateOnMoveOrZoom(alignToGrid);
	}

	public Point2D.Double getCentre() {
		return new Point2D.Double(positionX + getWidth() / 2.0, positionY
				+ getHeight() / 2.0);
	}

	/** Handles selection for Place/Transitions */
	@Override
	public void select() {
		if (selectable && !selected) {
			selected = true;

			if (getNameLabel() != null) {
				getNameLabel().setForeground(Pipe.SELECTION_LINE_COLOUR);
			}

			// Select arcs that are connected from this object to another selected object.
			for (Arc arc : getPostset()) {
				if(arc.getTarget().isSelected()){
					arc.select();
				}
			}

			// Select arcs that are connected to this object from another selected object.
			for (Arc arc : getPreset()) {
				if(arc.getSource().isSelected()){
					arc.select();
				}
			}
			repaint();
		}
	}

	@Override
	public void addedToGui() {
		setDeleted(false);
		super.addedToGui();
		update(true);
		updateOnMoveOrZoom();
	}


    @Override
    public void removedFromGui() {}

    // TODO: Find a better name for this

	@Override
	public void updateOnMoveOrZoom() {
		updateOnMoveOrZoom(true);
	}
	
	// TODO: Find a better name for this
	public void updateOnMoveOrZoom(boolean alignToGrid) {
		updateBounds();
		updateConnected();
		updateLabelLocation(alignToGrid);
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


	public void toggleAttributesVisible() {
		attributesVisible = !attributesVisible;
		update(true);
	};

	public void zoomUpdate(int value) {
		super.zoomUpdate(value);
		update(true, false);
	}

}
