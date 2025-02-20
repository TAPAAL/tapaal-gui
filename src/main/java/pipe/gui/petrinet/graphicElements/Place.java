package pipe.gui.petrinet.graphicElements;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import pipe.gui.TAPAALGUI;
import pipe.gui.Constants;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.action.ShowHideInfoAction;

import javax.swing.*;

/**
  * Class for drawing a Place
 */
public abstract class Place extends PlaceTransitionObject {

	protected static final int DIAMETER = Constants.PLACE_TRANSITION_HEIGHT;

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
	protected static final Shape proximityPlace = (new BasicStroke(Constants.PLACE_TRANSITION_PROXIMITY_RADIUS)).createStrokedShape(placeEllipse);


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
    public JPopupMenu getPopup(MouseEvent e) {
        int index = 0;
        JPopupMenu popup = super.getPopup(e);

        JMenuItem menuItem = new JMenuItem("Edit Place");
        menuItem.addActionListener(o -> this.showEditor());
        popup.insert(menuItem, index++);

        menuItem = new JMenuItem(new ShowHideInfoAction(this));
        if (this.getAttributesVisible()) {
            menuItem.setText("Hide Place Name");
        } else {
            menuItem.setText("Show Place Name");
        }
        popup.insert(menuItem, index++);
        popup.insert(new JPopupMenu.Separator(), index);

        return popup;
    }

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setStroke(new BasicStroke(1.0f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (selected) {
			g2.setColor(Constants.SELECTION_FILL_COLOUR);
			//pnName.setForeground(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setColor(Constants.ELEMENT_FILL_COLOUR);
			//pnName.setForeground(Pipe.ELEMENT_LINE_COLOUR);
		}
		g2.fill(placeEllipse);

		if (selected) {
			g2.setPaint(Constants.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Constants.ELEMENT_LINE_COLOUR);
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

		if (TAPAALGUI.useExtendedBounds) { // Must be drawing a new Arc if non-NULL.
			return (proximityPlace.contains((int) unZoomedX, (int) unZoomedY) ||
					placeEllipse.contains((int) unZoomedX, (int) unZoomedY));
		} else {
			return placeEllipse.contains((int) unZoomedX, (int) unZoomedY);
		}
	}

    @Override
    public void updateEndPoint(Arc arc) {
        double centerX = positionX + centreOffsetLeft();
        double centerY = positionY + centreOffsetTop();
        double radius = getDiameter() * 0.5;
        
        boolean isSource = arc.getSource() == this;
        double angle = isSource ? arc.getArcPath().getStartAngle() : arc.getArcPath().getEndAngle();
        
        // Calculate intersection point with place circumference
        double intersectX = centerX - (radius * Math.cos(angle));
        double intersectY = centerY + (radius * Math.sin(angle));
        
        if (isSource) {
            arc.setSourceLocation(centerX, centerY);
            arc.setSourceLocation(intersectX, intersectY);
        } else {
            arc.setTargetLocation(centerX, centerY);
            arc.setTargetLocation(intersectX, intersectY);
        }
    }
    
	@Override
	public void update(boolean displayConstantNames) {
        getNameLabel().setText("");
        super.update(displayConstantNames);
		repaint();
	}
}
