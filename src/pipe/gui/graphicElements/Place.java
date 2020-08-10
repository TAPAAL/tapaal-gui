package pipe.gui.graphicElements;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;

/**
  * Class for drawing a Place
 */
public abstract class Place extends PlaceTransitionObject {

	protected static final int DIAMETER = Pipe.PLACE_TRANSITION_HEIGHT;

	// Token Width and Height
	protected static final int tWidth = 5;
	protected static final int tHeight = 5;

	// Token dot position offsets
	protected static final int tLeftX = 7;
	protected static final int tRightX = 19;
	protected static final int tTopY = 7;
	protected static final int tBotY = 19;
	protected static final int tMiddleX = 13;
	protected static final int tMiddleY = 13;

	// Ellipse2D.Double place
	protected static final Ellipse2D.Double placeEllipse = new Ellipse2D.Double(0, 0,	DIAMETER, DIAMETER);
	protected static final Shape proximityPlace = (new BasicStroke(Pipe.PLACE_TRANSITION_PROXIMITY_RADIUS)).createStrokedShape(placeEllipse);


	public Place(
			int positionXInput,
			int positionYInput,
			String idInput,
			int nameOffsetXInput,
			int nameOffsetYInput
	){
		super(DIAMETER, DIAMETER, positionXInput, positionYInput, idInput,	nameOffsetXInput, nameOffsetYInput);
	}

	public Place(int positionXInput, int positionYInput) {
		this(positionXInput, positionYInput, null, 0,0);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setStroke(new BasicStroke(1.0f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (selected) {
			g2.setColor(Pipe.SELECTION_FILL_COLOUR);
			//pnName.setForeground(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
			//pnName.setForeground(Pipe.ELEMENT_LINE_COLOUR);
		}
		g2.fill(placeEllipse);

		if (selected) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}
		g2.draw(placeEllipse);

		g2.setStroke(new BasicStroke(1.0f));
	}

	/**
	 * Returns the diameter of this Place at the current zoom
	 */
	private int getDiameter() {
		return (Zoomer.getZoomedValue(DIAMETER, getZoom()));
	}

	@Override
	public boolean contains(int x, int y) {
		double unZoomedX = Zoomer.getUnzoomedValue(x - COMPONENT_DRAW_OFFSET, getZoom());
		double unZoomedY = Zoomer.getUnzoomedValue(y - COMPONENT_DRAW_OFFSET, getZoom());

		if (CreateGui.useExtendedBounds) { // Must be drawing a new Arc if non-NULL.
			return (proximityPlace.contains((int) unZoomedX, (int) unZoomedY) ||
					placeEllipse.contains((int) unZoomedX, (int) unZoomedY));
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
	public void update(boolean displayConstantNames) {
		if (attributesVisible) {
			getNameLabel().setText("");
		} else {
			getNameLabel().setText("");
		}
		super.update(displayConstantNames);
		repaint();
	}


}
