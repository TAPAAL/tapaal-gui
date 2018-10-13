package pipe.gui.graphicElements;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;

/**
  * Class for drawing a Place
 */
public abstract class Place extends PlaceTransitionObject {

	private static final long serialVersionUID = -5155964364065651381L;

	// Initial Marking X-axis Offset 
	protected Double markingOffsetX = 0d;

	// Initial Marking Y-axis Offset 
	protected Double markingOffsetY = 0d;

	// Value of the capacity restriction; 0 means no capacity restriction 
	protected Integer capacity = 0;

	public static final int DIAMETER = Pipe.PLACE_TRANSITION_HEIGHT;

	// Token Width and Height
	public static int tWidth = 5;
	public static int tHeight = 5;

	// Token dot position offsets
	public static int tLeftX = 7;
	public static int tRightX = 19;
	public static int tTopY = 7;
	public static int tBotY = 19;
	public static int tMiddleX = 13;
	public static int tMiddleY = 13;

	// Ellipse2D.Double place
	protected static Ellipse2D.Double placeEllipse = new Ellipse2D.Double(0, 0,	DIAMETER, DIAMETER);
	protected static Shape proximityPlace = (new BasicStroke(Pipe.PLACE_TRANSITION_PROXIMITY_RADIUS)).createStrokedShape(placeEllipse);

	public Place(double positionXInput, double positionYInput, String idInput,
			Double nameOffsetXInput, Double nameOffsetYInput,
			int initialMarkingInput, double markingOffsetXInput,
			double markingOffsetYInput, int capacityInput) {
		super(positionXInput, positionYInput, idInput,	nameOffsetXInput, nameOffsetYInput);
		markingOffsetX = new Double(markingOffsetXInput);
		markingOffsetY = new Double(markingOffsetYInput);
		componentWidth = DIAMETER;
		componentHeight = DIAMETER;
		setCentre((int) positionX, (int) positionY);
	}

	
	public Place(double positionXInput, double positionYInput) {
		super(positionXInput, positionYInput);
		componentWidth = DIAMETER;
		componentHeight = DIAMETER;
		setCentre((int) positionX, (int) positionY);
	}


//	public Place paste(double x, double y, boolean fromAnotherView) {
//		this.incrementCopyNumber();
//		Place copy = new Place(Grid.getModifiedX(x + this.getX()+ Pipe.PLACE_TRANSITION_HEIGHT / 2),
//				Grid.getModifiedY(y	+ this.getY() + Pipe.PLACE_TRANSITION_HEIGHT / 2));
//		copy.pnName.setName(pnName.getName() + "(" + this.getCopyNumber()	+ ")");
//		this.newCopy(copy);
//		copy.nameOffsetX = nameOffsetX;
//		copy.nameOffsetY = nameOffsetY;
//		copy.capacity = capacity;
//		copy.attributesVisible = attributesVisible;
//		copy.markingOffsetX = markingOffsetX;
//		copy.markingOffsetY = markingOffsetY;
//		copy.update(true);
//		return copy;
//	}
//
//	public Place copy() {
//		Place copy = new Place(Zoomer.getUnzoomedValue(this.getX(), zoom), Zoomer.getUnzoomedValue(this.getY(), zoom));
//		copy.pnName.setName(this.getName());
//		copy.nameOffsetX = nameOffsetX;
//		copy.nameOffsetY = nameOffsetY;
//		copy.capacity = capacity;
//		copy.attributesVisible = attributesVisible;
//		copy.markingOffsetX = markingOffsetX;
//		copy.markingOffsetY = markingOffsetY;
//		copy.setOriginal(this);
//		return copy;
//	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setStroke(new BasicStroke(1.0f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (selected && !ignoreSelection) {
			g2.setColor(Pipe.SELECTION_FILL_COLOUR);
			pnName.setForeground(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
			pnName.setForeground(Pipe.ELEMENT_LINE_COLOUR);
		}
		g2.fill(placeEllipse);

		if (selected && !ignoreSelection) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}
		g2.draw(placeEllipse);

		g2.setStroke(new BasicStroke(1.0f));
	}

	/**
	 * Get X-axis offset for initial marking
	 * 
	 * @return Double value for X-axis offset of initial marking
	 */
	public Double getMarkingOffsetXObject() {
		return markingOffsetX;
	}

	/**
	 * Get Y-axis offset for initial marking
	 * 
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
		double unZoomedX = Zoomer.getUnzoomedValue(x - COMPONENT_DRAW_OFFSET, zoom);
		double unZoomedY = Zoomer.getUnzoomedValue(y - COMPONENT_DRAW_OFFSET, zoom);

		Arc someArc = CreateGui.getDrawingSurface().createArc;
		if (someArc != null) { // Must be drawing a new Arc if non-NULL.
			if ((proximityPlace.contains((int) unZoomedX, (int) unZoomedY) || placeEllipse
					.contains((int) unZoomedX, (int) unZoomedY))
					&& areNotSameType(someArc.getSource())) {
				// assume we are only snapping the target...
				if (someArc.getTarget() != this) {
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
			return placeEllipse.contains((int) unZoomedX, (int) unZoomedY);
		}
	}

	@Override
	public void updateEndPoint(Arc arc) {
		if (arc.getSource() == this) {
			// Make it calculate the angle from the centre of the place rather
			// than the current start point
			arc.setSourceLocation(positionX + (getDiameter() * 0.5), positionY
					+ (getDiameter() * 0.5));
			double angle = arc.getArcPath().getStartAngle();
			arc.setSourceLocation(positionX + centreOffsetLeft()
					- (0.5 * getDiameter() * (Math.sin(angle))), positionY
					+ centreOffsetTop()
					+ (0.5 * getDiameter() * (Math.cos(angle))));
		} else {
			// Make it calculate the angle from the centre of the place rather
			// than the current target point
			arc.setTargetLocation(positionX + (getDiameter() * 0.5), positionY
					+ (getDiameter() * 0.5));
			double angle = arc.getArcPath().getEndAngle();
			arc.setTargetLocation(positionX + centreOffsetLeft()
					- (0.5 * getDiameter() * (Math.sin(angle))), positionY
					+ centreOffsetTop()
					+ (0.5 * getDiameter() * (Math.cos(angle))));
		}
	}

	@Override
	public void toggleAttributesVisible() {
		attributesVisible = !attributesVisible;
		update(true);
	}

	@Override
	public void addedToGui() {
		super.addedToGui();
		update(true);
	}

	@Override
	public void showEditor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(boolean displayConstantNames) {
		if (attributesVisible) {
			pnName.setText("\nk=" + (capacity > 0 ? capacity : "\u221E"));
		} else {
			pnName.setText("");
		}
		pnName.zoomUpdate(zoom);
		super.update(displayConstantNames);
		repaint();
	}

	@Override
	public Place clone() {

		Place toReturn = (Place) super.clone();

		toReturn.markingOffsetX = markingOffsetX;
		toReturn.markingOffsetY = markingOffsetY;
		toReturn.componentWidth = componentWidth;
		toReturn.componentHeight = componentHeight;
		toReturn.setCentre((int) positionX, (int) positionY);

		return toReturn;

	}
}
