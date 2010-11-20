package pipe.dataLayer;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import dk.aau.cs.gui.undo.Command;

import pipe.dataLayer.simulation.Marking;
import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.undo.PlaceCapacityEdit;
import pipe.gui.undo.PlaceMarkingEdit;


/**
 * <b>Place</b> - Petri-Net Place Class
 *
 * @see <p><a href="..\PNMLSchema\index.html">PNML  -  Petri-Net XMLSchema (stNet.xsd)</a>
 * @see </p><p><a href="..\..\..\UML\dataLayer.html">UML  -  PNML Package </a></p>
 * @version 1.0
 * @author James D Bloom
 *  
 * @author Edwin Chung corresponding states of matrixes has been set 
 * to change when markings are altered. Users will be prompted to save their
 * work when the markings of places are altered. (6th Feb 2007)
 * 
 * @author Edwin Chung 16 Mar 2007: modified the constructor and several
 * other functions so that DataLayer objects can be created outside the
 * GUI
 */
public class Place 
extends PlaceTransitionObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5155964364065651381L;
	public final static String type = "Place";
	/** Initial Marking */
	protected Integer initialMarking = 0;

	/** Current Marking */
	protected Integer currentMarking = 0;

	/** Initial Marking X-axis Offset */
	protected Double markingOffsetX = 0d;

	/** Initial Marking Y-axis Offset */
	protected Double markingOffsetY = 0d;

	/**  Value of the capacity restriction; 0 means no capacity restriction */
	protected Integer capacity = 0;
	/*
   private boolean strongCapacity = false;
	 */

	public static final int DIAMETER = Pipe.PLACE_TRANSITION_HEIGHT;

	/** Token Width */
	public static int tWidth = 4;

	/** Token Height */
	public static int tHeight = 4;

	/** Ellipse2D.Double place */
	protected static Ellipse2D.Double placeEllipse = 
		new Ellipse2D.Double(0, 0, DIAMETER, DIAMETER);
	protected static Shape proximityPlace =
		(new BasicStroke(Pipe.PLACE_TRANSITION_PROXIMITY_RADIUS)).createStrokedShape(placeEllipse);
	
	/**
	 * Create Petri-Net Place object
	 * @param positionXInput X-axis Position
	 * @param positionYInput Y-axis Position
	 * @param idInput Place id
	 * @param nameInput Name
	 * @param nameOffsetXInput Name X-axis Position
	 * @param nameOffsetYInput Name Y-axis Position
	 * @param initialMarkingInput Initial Marking
	 * @param markingOffsetXInput Marking X-axis Position
	 * @param markingOffsetYInput Marking Y-axis Position
	 * @param capacityInput Capacity 
	 */

	public Place(double positionXInput,  double positionYInput, 
			String idInput, 
			String nameInput, 
			Double nameOffsetXInput, Double nameOffsetYInput, 
			int initialMarkingInput, 
			double markingOffsetXInput,  double markingOffsetYInput,
			int capacityInput){
		super(positionXInput, positionYInput,
				idInput,
				nameInput,
				nameOffsetXInput, nameOffsetYInput);
		initialMarking = new Integer(initialMarkingInput);
		currentMarking = new Integer(initialMarkingInput);
		markingOffsetX = new Double(markingOffsetXInput);
		markingOffsetY = new Double(markingOffsetYInput);
		componentWidth = DIAMETER;
		componentHeight = DIAMETER;
		setCapacity(capacityInput);
		setCentre((int)positionX, (int)positionY);
		//updateBounds();
	}   


	/**
	 * Create Petri-Net Place object
	 * @param positionXInput X-axis Position
	 * @param positionYInput Y-axis Position
	 */
	public Place(double positionXInput, double positionYInput){
		super(positionXInput, positionYInput);
		componentWidth = DIAMETER;
		componentHeight = DIAMETER;
		setCentre((int)positionX, (int)positionY);
		//updateBounds();    
	}


	public Place(String idInput, String nameInput, int initialMarkingInput, int capacityInput) {
		this(0.0, 0.0, idInput, nameInput, 0.0,  0.0, 
				initialMarkingInput, 
				0.0,   0.0,
				capacityInput);
	}


	public Place paste(double x, double y, boolean fromAnotherView){
		this.incrementCopyNumber();
		Place copy = new Place (
				Grid.getModifiedX(x + this.getX() + Pipe.PLACE_TRANSITION_HEIGHT/2),
				Grid.getModifiedY(y + this.getY() + Pipe.PLACE_TRANSITION_HEIGHT/2));
		copy.pnName.setName(this.pnName.getName()  
				+ "(" + this.getCopyNumber() +")");
		this.newCopy(copy);
		copy.nameOffsetX = this.nameOffsetX;
		copy.nameOffsetY = this.nameOffsetY;
		copy.capacity = this.capacity;
		copy.attributesVisible = this.attributesVisible;
		copy.initialMarking = this.initialMarking;
		copy.currentMarking = this.currentMarking;
		copy.markingOffsetX = this.markingOffsetX;
		copy.markingOffsetY = this.markingOffsetY;
		copy.update();
		return copy;
	}


	public Place copy(){
		Place copy = new Place (Zoomer.getUnzoomedValue(this.getX(), zoom), 
				Zoomer.getUnzoomedValue(this.getY(), zoom));
		copy.pnName.setName(this.getName());
		copy.nameOffsetX = this.nameOffsetX;
		copy.nameOffsetY = this.nameOffsetY;
		copy.capacity = this.capacity;
		copy.attributesVisible = this.attributesVisible;
		copy.initialMarking = this.initialMarking;
		copy.currentMarking = this.currentMarking;
		copy.markingOffsetX = this.markingOffsetX;
		copy.markingOffsetY = this.markingOffsetY;
		copy.setOriginal(this);
		return copy;
	}   


	/**
	 * Paints the Place component taking into account the number of tokens from 
	 * the currentMarking
	 * @param g The Graphics object onto which the Place is drawn.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;

		Insets insets = getInsets();
		int x = insets.left;
		int y = insets.top;

		if (hasCapacity()){
			g2.setStroke(new BasicStroke(2.0f));
		} else {
			g2.setStroke(new BasicStroke(1.0f));
		}
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);

		if(selected && !ignoreSelection){
			g2.setColor(Pipe.SELECTION_FILL_COLOUR);
		} else{
			g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
		}
		g2.fill(placeEllipse);

		if (selected && !ignoreSelection){
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else{
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}
		g2.draw(placeEllipse);

		g2.setStroke(new BasicStroke(1.0f));
		int marking = getCurrentMarking();

		// structure sees how many markings there are and fills the place in with 
		// the appropriate number.
		switch(marking) {
		case 5: 
			g.drawOval(x + 6, y + 6, tWidth, tHeight);
			g.fillOval(x + 6, y + 6, tWidth, tHeight);
			/* falls through */
		case 4:
			g.drawOval(x + 18, y + 20, tWidth, tHeight);
			g.fillOval(x + 18, y + 20, tWidth, tHeight);
			/* falls through */
		case 3:
			g.drawOval(x + 6, y + 20, tWidth, tHeight);
			g.fillOval(x + 6, y + 20, tWidth, tHeight);
			/* falls through */
		case 2: 
			g.drawOval(x + 18, y + 6, tWidth, tHeight);
			g.fillOval(x + 18, y + 6, tWidth, tHeight);
			/* falls through */
		case 1:
			g.drawOval(x + 12, y + 13, tWidth, tHeight);
			g.fillOval(x + 12, y + 13, tWidth, tHeight);
			break;
		case 0:
			break;
		default:
			if (marking > 999){
				g.drawString("#"+String.valueOf(marking), x, y + 20);
			} else if (marking > 99){
				g.drawString("#"+String.valueOf(marking), x, y + 20);
			} else if (marking > 9){
				g.drawString("#"+String.valueOf(marking), x + 2, y + 20);
			} else {
				g.drawString("#"+String.valueOf(marking), x + 6, y + 20);
			}
			break;
		}
	}


	/**
	 * Set initial marking
	 * @param initialMarkingInput Integer value for initial marking
	 */
	public void setInitialMarking(int initialMarkingInput) {
		initialMarking = new Integer(initialMarkingInput);
	}


	/**
	 * Set current marking
	 * @param currentMarkingInput Integer value for current marking
	 */
	public Command setCurrentMarking(int currentMarkingInput) {
		int oldMarking = currentMarking;

		if (capacity == 0){
			currentMarking = currentMarkingInput;
		} else {
			if (currentMarkingInput > capacity) {
				currentMarking = capacity;
			} else{
				currentMarking = currentMarkingInput;
			}
		}
		repaint();
		return new PlaceMarkingEdit(this, oldMarking, currentMarking);      
	}


	/**
	 * Set capacity 
	 * This method doesn't check if marking fulfilles current capacity restriction
	 * @param newCapacity Integer value for capacity restriction
	 */
	public Command setCapacity(int newCapacity) {
		int oldCapacity = capacity;

		if (capacity != newCapacity) {
			capacity = newCapacity;
			update();  
		}
		return new PlaceCapacityEdit(this, oldCapacity, newCapacity);
	}   

	/**
	 * Get initial marking
	 * @return Integer value for initial marking
	 */
	public int getInitialMarking() {
		return ((initialMarking == null) ? 0 : initialMarking.intValue());
	}


	/**
	 * Get current marking
	 * @return Integer value for current marking
	 */
	public int getCurrentMarking() {
		return ((currentMarking == null) ? 0 : currentMarking.intValue());
	}


	/**
	 * Get current capacity
	 * @return Integer value for current capacity
	 */
	public int getCapacity() {
		return ((capacity == null) ? 0 : capacity.intValue());
	}   


	/**
	 * Get current marking
	 * @return Integer value for current marking
	 */
	public Integer getCurrentMarkingObject() {
		return currentMarking;
	}


	/**
	 * Get X-axis offset for initial marking
	 * @return Double value for X-axis offset of initial marking
	 */
	public Double getMarkingOffsetXObject() {
		return markingOffsetX;
	}


	/**
	 * Get Y-axis offset for initial marking
	 * @return Double value for X-axis offset of initial marking
	 */
	public Double getMarkingOffsetYObject() {
		return markingOffsetY;
	}


	/**
	 * Returns the diameter of this Place at the current zoom
	 */
	private int getDiameter() {
		return (Zoomer.getZoomedValue(DIAMETER, zoom));
	}   


	@Override
	public boolean contains(int x, int y) {
		double unZoomedX = 
			Zoomer.getUnzoomedValue(x - COMPONENT_DRAW_OFFSET, zoom);
		double unZoomedY = 
			Zoomer.getUnzoomedValue(y - COMPONENT_DRAW_OFFSET, zoom);

		someArc = CreateGui.getView().createArc;
		if (someArc != null){		// Must be drawing a new Arc if non-NULL.
			if ((proximityPlace.contains((int)unZoomedX, (int)unZoomedY)
					|| placeEllipse.contains((int)unZoomedX, (int)unZoomedY))
					&& areNotSameType(someArc.getSource())){
				// assume we are only snapping the target...
				if (someArc.getTarget() != this){
					someArc.setTarget(this);
				}
				someArc.updateArcPosition();
				return true;
			} else {
				if (someArc.getTarget() == this) {
					someArc.setTarget(null);
					updateConnected();
				}
				return false;
			}
		} else {
			return placeEllipse.contains((int)unZoomedX, (int)unZoomedY); 
		}
	}


	/* (non-Javadoc)
	 * @see pipe.dataLayer.PlaceTransitionObject#updateEndPoint(pipe.dataLayer.Arc)
	 */
	@Override
	public void updateEndPoint(Arc arc) {
		if (arc.getSource()==this) {
			// Make it calculate the angle from the centre of the place rather than
			// the current start point
			arc.setSourceLocation(positionX + (getDiameter() * 0.5),
					positionY + (getDiameter() * 0.5));
			double angle = arc.getArcPath().getStartAngle();
			arc.setSourceLocation(positionX + centreOffsetLeft() 
					- (0.5 * getDiameter() * (Math.sin(angle))),
					positionY + centreOffsetTop() 
					+ (0.5 * getDiameter() * (Math.cos(angle))));         
		} else {
			// Make it calculate the angle from the centre of the place rather than the current target point
			arc.setTargetLocation(positionX + (getDiameter() * 0.5),
					positionY + (getDiameter() * 0.5));
			double angle = arc.getArcPath().getEndAngle();
			arc.setTargetLocation(positionX + centreOffsetLeft() 
					- (0.5 * getDiameter() * (Math.sin(angle))),
					positionY + centreOffsetTop() 
					+ (0.5 * getDiameter() * (Math.cos(angle))));
		}
	}


	@Override
	public void toggleAttributesVisible(){
		attributesVisible = !attributesVisible;
		update();  
	}


	public boolean hasCapacity(){
		return capacity > 0;
	}


	@Override
	public void addedToGui(){
		super.addedToGui();
		update();
	}   


	@Override
	public void showEditor(){
		throw new UnsupportedOperationException();
	}


	@Override
	public void update() {
		if (attributesVisible == true){
			pnName.setText("\nk=" + (capacity > 0 ? capacity :"\u221E"));

		} else {
			pnName.setText("");
		}          
		pnName.zoomUpdate(zoom);
		super.update();
		repaint();
	}


	@Override
	public Place clone(){

		Place toReturn = (Place) super.clone();

		toReturn.initialMarking = this.initialMarking;
		toReturn.currentMarking = this.currentMarking;
		toReturn.markingOffsetX = this.markingOffsetX;
		toReturn.markingOffsetY = this.markingOffsetY;
		toReturn.componentWidth = componentWidth;
		toReturn.componentHeight = componentHeight;
		toReturn.setCapacity(this.getCapacity());
		toReturn.setCentre((int)positionX, (int)positionY);




		return toReturn;

	}


	public void showMarking(Marking marking) {
		currentMarking = marking.getTokensInPlace(this).size();
		repaint();
	}
}
