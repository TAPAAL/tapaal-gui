package pipe.dataLayer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;

import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;

/**
 * <b>PlaceTransitionObject</b> - Petri-Net PLace or Transition SuperClass<b> - <i>Abstract</i></b>
 *
 * @see <p><a href="..\PNMLSchema\index.html">PNML  -  Petri-Net XMLSchema (stNet.xsd)</a>
 * @see </p><p><a href="..\..\..\UML\dataLayer.html">UML  -  PNML Package </a></p>
 * @version 1.0
 * @author James D Bloom
 *
 * @author Edwin Chung 16 Mar 2007: modified the constructor and several
 * other functions so that DataLayer objects can be created outside the
 * GUI
 */
public abstract class PlaceTransitionObject 
extends PetriNetObject 
implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6629006415467929184L;
	/** X-axis Position on screen*/
	protected double positionX;
	/** Y-axis Position on screen*/
	protected double positionY;

	/** X-axis Position on screen*/
	protected double nameOffsetX;
	/** Y-axis Position on screen*/
	protected double nameOffsetY;

	protected double componentWidth;
	protected double componentHeight;

	private LinkedList<Arc> connectTo = new LinkedList<Arc>();
	private LinkedList<Arc> connectFrom = new LinkedList<Arc>();
	protected static Arc someArc;


	private PlaceTransitionObject lastCopy = null;
	private PlaceTransitionObject original = null;
	private int copyNumber = 0;

	protected boolean attributesVisible = false;

	// The "real" x coordinate of this place or transition in the net.
	// i.e. the x position at 100% zoom.
	private double locationX;

	// The "real" y coordinate of this place or transition in the net.
	// i.e. the y position at 100% zoom.
	private double locationY;


	/**
	 * Create Petri-Net Object
	 * @param positionXInput X-axis Position
	 * @param positionYInput Y-axis Position
	 * @param idInput Place id
	 * @param nameInput Name
	 * @param nameOffsetXInput Name X-axis Position
	 * @param nameOffsetYInput Name Y-axis Position
	 */
	public PlaceTransitionObject(double positionXInput, 
			double positionYInput,
			String idInput, 
			String nameInput, 
			double nameOffsetXInput,
			double nameOffsetYInput) {
		this(positionXInput, positionYInput, idInput);
		nameOffsetX = nameOffsetXInput;
		nameOffsetY = nameOffsetYInput;
		pnName.setPosition((int)nameOffsetX, (int)nameOffsetY);
		setName(nameInput);
	}


	/**
	 * Create Petri-Net Object
	 * @param positionXInput X-axis Position
	 * @param positionYInput Y-axis Position
	 * @param idInput Place id
	 */
	public PlaceTransitionObject(double positionXInput, 
			double positionYInput,
			String idInput) {
		this(positionXInput, positionYInput);
		id = idInput;
	}


	/**
	 * Create Petri-Net Object
	 * This constructor does all the work, the others just call it.
	 * @param positionXInput X-axis Position
	 * @param positionYInput Y-axis Position
	 */
	public PlaceTransitionObject(double positionXInput, double positionYInput) {

		setPositionX(positionXInput);
		setPositionY(positionYInput);      

		nameOffsetX = Pipe.DEFAULT_OFFSET_X;
		nameOffsetY = Pipe.DEFAULT_OFFSET_Y;

		// sets up Namelabel for each PN object
		pnName = new NameLabel(zoom);
	}


	/**
	 * Set X-axis position
	 * @param positionXInput Double value for X-axis position
	 */
	public void setPositionX(double positionXInput) {
		positionX = positionXInput;     
		locationX = Zoomer.getUnzoomedValue(positionX, zoom);
	}


	/**
	 * Set Y-axis position
	 * @param positionYInput Double value for Y-axis position
	 */
	public void setPositionY(double positionYInput) {
		positionY = positionYInput;
		locationY = Zoomer.getUnzoomedValue(positionY, zoom);
	}


	/**
	 * Set name
	 * @param nameInput String value for Place name;
	 */
	@Override
	public void setName(String nameInput) {
		//sets the text within the label
		//System.out.println("setting name to: " + nameInput);
		pnName.setName(nameInput);
	}


	/**
	 * Set X-axis offset for name position
	 * @param nameOffsetXInput Double value for name X-axis offset
	 */
	public void setNameOffsetX(double nameOffsetXInput) {
		nameOffsetX += Zoomer.getUnzoomedValue(nameOffsetXInput, zoom);
	}


	/**
	 * Set Y-axis offset for name position
	 * @param nameOffsetYInput Double value for name Y-axis offset
	 */
	public void setNameOffsetY(double nameOffsetYInput) {
		nameOffsetY += Zoomer.getUnzoomedValue(nameOffsetYInput, zoom);      
	}


	/**
	 * Get X-axis position
	 * @return Double value for X-axis position
	 */
	public double getPositionX() {
		return positionX;
	}


	/**
	 * Get Y-axis position
	 * @return Double value for Y-axis position
	 */
	public double getPositionY() {
		return positionY;
	}


	/**
	 * Set id
	 * @param idInput String value for Place id;
	 */
	@Override
	public void setId(String idInput) {
		id = idInput;
		setName(id);
		//      System.out.println("setting id to: " + idInput);
	}


	/**
	 * Get id
	 * @return String value for Place id;
	 */
	@Override
	public String getId() {
		return (id!=null) ? id : pnName.getName();
	}


	/**
	 * Get name
	 * @return String value for Place name;
	 */
	@Override
	public String getName() {
		return (pnName!=null) ? pnName.getName() : id;
	}


	public double getNameOffsetX() {
		return nameOffsetX;
	}   


	public double getNameOffsetY() {
		return nameOffsetY;
	}      


	/** 
	 * Get X-axis offset for ...
	 * @return Double value for X-axis offset of ...
	 */
	public Double getNameOffsetXObject() {
		return this.nameOffsetX;
	}


	/** Moved to PlaceTransitionObject
	 * Get Y-axis offset for ...
	 * @return Double value for Y-axis offset of ...
	 */
	public Double getNameOffsetYObject() {
		return this.nameOffsetY;
	}   


	/**
	 * Get X-axis position, returns null if value not yet entered
	 * @return Double value for X-axis position
	 */
	public Double getPositionXObject() {
		return new Double(locationX);
		//return new Double(positionX);
	}


	/**
	 * Get Y-axis position, returns null if value not yet entered
	 * @return Double value for Y-axis position
	 */
	public Double getPositionYObject() {
		return new Double(locationY);
		//return new Double(positionY);
	}


	/** Implemented in subclasses as involves some tailoring according to the shape
	 * @param e Mouse Event
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g;
		g2.translate(COMPONENT_DRAW_OFFSET, COMPONENT_DRAW_OFFSET);
		g2.transform(Zoomer.getTransform(zoom));
	}


	public Point2D getIntersectOffset(Point2D start) {
		return new Point2D.Double();
	}


	/** Returns the distance between the outside of the component to the centre, 
	 * in order to position the centre of the place where the mouse clicks on the
	 * screen
	 * @return Top offset of Place
	 */
	public int centreOffsetTop() {
		return (int)(Zoomer.getZoomedValue(componentHeight/2.0, zoom));
	}


	/** Returns the distance between the outside of the component to the centre, 
	 * in order to position the centre of the place where the mouse clicks on the
	 * screen
	 * @return Left offset of Place
	 */
	public int centreOffsetLeft() {
		return (int)(Zoomer.getZoomedValue(componentWidth/2.0, zoom));
	}


	/** Calculates the BoundsOffsets used for setBounds() method */
	public void updateBounds() {
		double scaleFactor = Zoomer.getScaleFactor(zoom);
		positionX = locationX * scaleFactor;
		positionY = locationY * scaleFactor;
		bounds.setBounds((int)positionX,
				(int)positionY,
				(int)(componentWidth * scaleFactor),
				(int)(componentHeight * scaleFactor));
		bounds.grow(COMPONENT_DRAW_OFFSET, COMPONENT_DRAW_OFFSET);
		setBounds(bounds);
	}


	/** Adds outwards arc to place/transition */
	public void addConnectTo(Arc newArc) {
		//      System.out.println("DEBUG: added arc (to)!:" + newArc + " de " + this);
		connectTo.add(newArc);
	}


	/** Adds inwards arc to place/transition */
	public void addConnectFrom(Arc newArc) {
		//      System.out.println("DEBUG: added arc (from)!:" + newArc + " de " + this);
		connectFrom.add(newArc);
	}


	public void removeFromArc(Arc oldArc) {
		if (connectFrom.remove(oldArc)) {
			//         System.out.println("DEBUG: removeFromArc_ok");
		} else {
			//         System.out.println("DEBUG: removeFromArc_ko");
		}
	}


	public void removeToArc(Arc oldArc) {
		if (connectTo.remove(oldArc)) {
			//         System.out.println("DEBUG: removeToArc_ok");
		} else {
			//         System.out.println("DEBUG: removeToArc_ko");
		}
	}


	/** Updates location of any attached arcs */
	public void updateConnected() {
		Iterator<Arc> arcsFrom = connectFrom.iterator();

		while (arcsFrom.hasNext()) {
			someArc = (arcsFrom.next());
			updateEndPoint(someArc);
			someArc.updateArcPosition();
		}

		Iterator<Arc> arcsTo = connectTo.iterator();
		while (arcsTo.hasNext()) {
			someArc = (arcsTo.next());
			updateEndPoint(someArc);
			someArc.updateArcPosition();
		}
	}


	/** Translates the component by x,y */
	public void translate(int x, int y) {
		setPositionX(positionX + x);
		setPositionY(positionY + y);  
		update();
	}


	/** Sets the center of the component to position x, y */
	public void setCentre(double x,double y) {
		setPositionX(x - (getWidth()/2.0));
		setPositionY(y - (getHeight()/2.0));
		update();
	}


	public void update() {
		updateBounds();
		updateLabelLocation();
		updateConnected();
	}


	public Point2D.Double getCentre() {
		return new Point2D.Double(positionX + getWidth()/2.0, 
				positionY + getHeight()/2.0); 
	}


	protected void updateLabelLocation() {
		pnName.setPosition(Grid.getModifiedX((int)(positionX + 
				Zoomer.getZoomedValue(nameOffsetX, zoom))),
				Grid.getModifiedY((int)(positionY + 
						Zoomer.getZoomedValue(nameOffsetY, zoom))));          
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

			Iterator<Arc> arcsFrom = connectFrom.iterator();
			while(arcsFrom.hasNext()) {
				arcsFrom.next().select();
			}

			Iterator<Arc> arcsTo = connectTo.iterator();
			while(arcsTo.hasNext()) {
				arcsTo.next().select();
			}
			repaint();
		}
	}


	@Override
	public void addedToGui() {
		deleted = false;
		markedAsDeleted = false;
		addLabelToContainer();
		update();
	}


	public boolean areNotSameType(PlaceTransitionObject o) {
		return (this.getClass() != o.getClass());
	}   


	/* KYRKE - added functions for getting pre and post set af lists */ 
	public LinkedList<Arc> getPreset(){
		//XXX - possible type error
		return new LinkedList<Arc>(connectTo);
	}

	public LinkedList<Arc> getPostset(){
		//XXX - possible type error
		return new LinkedList<Arc>(connectFrom);
	}

	public Iterator<Arc> getConnectFromIterator() {
		return connectFrom.iterator();
	}


	public Iterator<Arc> getConnectToIterator() {
		return connectTo.iterator();
	}


	public abstract void updateEndPoint(Arc arc);


	public int getCopyNumber() {
		if (original != null) {
			return original.copyNumber;
		} else {
			return 0;
		}
	}
	public void incrementCopyNumber(){
		if (original != null){
			original.copyNumber++;
		}
	}


	public void newCopy(PlaceTransitionObject ptObject) {
		if (original != null) {
			original.lastCopy = ptObject;
		}
	}


	public PlaceTransitionObject getLastCopy() {
		return lastCopy;  
	}


	public void resetLastCopy() {
		lastCopy = null;
	}


	public void setOriginal(PlaceTransitionObject ptObject) {   
		original = ptObject;
	}


	public PlaceTransitionObject getOriginal() {
		return original;
	}


	public abstract void showEditor();


	public void setAttributesVisible(boolean flag){
		attributesVisible = flag;   
	}


	public boolean getAttributesVisible(){
		return attributesVisible;
	}   


	@Override
	public int getLayerOffset() {
		return Pipe.PLACE_TRANSITION_LAYER_OFFSET;
	}   


	public abstract void toggleAttributesVisible();


	public void zoomUpdate(int value) {
		zoom = value;
		update();
	}


	// XXX - kyrke now also clones arcs
	// Clone object and deep copy the pnNames
	@Override
	public PlaceTransitionObject clone() {
		PlaceTransitionObject toReturn = (PlaceTransitionObject)super.clone();
		toReturn.pnName = (NameLabel) pnName.clone();

		toReturn.connectFrom = this.connectFrom;
		toReturn.connectTo = this.connectTo;

		return toReturn;
	}
}
