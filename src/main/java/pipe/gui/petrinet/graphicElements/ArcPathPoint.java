/*
 * Created on 28-Feb-2004
 * @author Michael Camacho (and whoever wrote the first bit!)
 * @author Edwin Chung 16 Mar 2007: modified the constructor and several other
 * functions so that DataLayer objects can be created outside the GUI
 */
package pipe.gui.petrinet.graphicElements;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import pipe.gui.Constants;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.action.SplitArcPointAction;
import pipe.gui.petrinet.action.ToggleArcPointAction;
import pipe.gui.petrinet.undo.AddArcPathPointEditCommand;
import pipe.gui.petrinet.undo.ArcPathPointTypeEditCommand;
import net.tapaal.gui.petrinet.undo.Command;

import javax.swing.*;

public class ArcPathPoint extends PetriNetObject {

	public static final boolean STRAIGHT = false;
	public static final boolean CURVED = true;
	private int SIZE = 3;
	private final static int SIZE_OFFSET = 1;

	// The offset in x for the new point resulting from splitting a point
	private static final int DELTA = 10;

    private ArcPath myArcPath;

	private final Point2D.Double control1 = new Point2D.Double();
	private final Point2D.Double control2 = new Point2D.Double();

	private boolean pointType; // STRAIGHT or CURVED

	private ArcPathPoint() {
	    super("", 0, 0);
	}

	public ArcPathPoint(ArcPath a) {
		this();
		myArcPath = a;
		setPointLocation(0, 0);
	}

	public ArcPathPoint(double x, double y, boolean _pointType, ArcPath a) {
		this();
		myArcPath = a;
		setOriginalX((int)x);
		setOriginalY((int)y);
		pointType = _pointType;
	}

    @Override
    public JPopupMenu getPopup(MouseEvent e) {
        JPopupMenu popup = super.getPopup(e);

        if (!this.isDeleteable()) {
            popup.getComponent(0).setEnabled(false);
        }

        popup.insert(new JPopupMenu.Separator(), 0);

        if (this.getIndex() == 0) {
            return popup;
        } else {
            JMenuItem menuItem = new JMenuItem(new ToggleArcPointAction(this));

            if (this.getPointType() == ArcPathPoint.STRAIGHT) {
                menuItem.setText("Change to Curved");
            } else {
                menuItem.setText("Change to Straight");
            }
            popup.insert(menuItem, 0);

            menuItem = new JMenuItem(new SplitArcPointAction(this));
            menuItem.setText("Split Point");
            popup.add(menuItem, 1);

        }
        return popup;
    }



	/**
	 * @author Nadeem
	 */
	public ArcPathPoint(Point2D.Double point, boolean _pointType, ArcPath a) {
		this(point.x, point.y, _pointType, a);
	}

	public Point2D.Double getPoint() {
		return new Point2D.Double(getPositionX(), getPositionY());
	}

    @Override
    public void setOriginalX(int positionXInput) {
        if (myArcPath != null && !isEndPoint()) {
            // Snap to grid
            super.setOriginalX(positionXInput);
        } else {
            originalX = positionXInput;
            positionX = Zoomer.getZoomedValue(originalX, getZoom());
        }
    }

    @Override
    public void setOriginalY(int positionYInput) {
        if (myArcPath != null && !isEndPoint()) {
            // Snap to grid
            super.setOriginalY(positionYInput);
        } else {
            originalY = positionYInput;
            positionY = Zoomer.getZoomedValue(originalY, getZoom());
        }
    }

    public void setPointLocation(int x, int y) {
		setPositionX(x);
		setPositionY(y);
		updateOnMoveOrZoom();
	}

	public boolean getPointType() {
		return pointType;
	}

	public void setPointType(boolean type) {
		if (pointType != type) {
			pointType = type;
			myArcPath.createPath();
			myArcPath.getArc().updateArcPosition();
		}
	}

	public Command togglePointType() {
		pointType = !pointType;
		myArcPath.createPath();
		myArcPath.getArc().updateArcPosition();
		return new ArcPathPointTypeEditCommand(this);
	}

	public void setVisibilityLock(boolean lock) {
		myArcPath.setPointVisibilityLock(lock);
	}

	public double getAngle(Point2D.Double p2) {
		double angle;

        angle = Math.atan2( (p2.y - getPoint().y), (getPoint().x - p2.x) );

		return angle;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);

        RectangularShape shape;
        if (pointType == CURVED) {
			shape = new Ellipse2D.Double(0, 0, 2 * SIZE, 2 * SIZE);
		} else {
			shape = new Rectangle2D.Double(0, 0, 2 * SIZE, 2 * SIZE);
		}

		if (selected) {
			g2.setPaint(Constants.SELECTION_FILL_COLOUR);
			g2.fill(shape);
			g2.setPaint(Constants.SELECTION_LINE_COLOUR);
			g2.draw(shape);
		} else {
			g2.setPaint(Constants.ELEMENT_FILL_COLOUR);
			g2.fill(shape);
			g2.setPaint(Constants.ELEMENT_LINE_COLOUR);
			g2.draw(shape);
		}

	}

	public int getIndex() {
		for (int i = 0; i < myArcPath.getNumPoints(); i++) {
			if (myArcPath.getPathPoint(i) == this) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * splitPoint() This method is called when the user selects the popup menu
	 * option Split Point on an Arc Point. The method determines the index of
	 * the selected point in the listarray of ArcPathPoints that an arcpath has.
	 * Then then a new point is created BEFORE this one in the list and offset
	 * by a small delta in the x direction.
	 */
	public Command splitPoint() {
		int i = getIndex(); // Get the index of this point

		ArcPathPoint newPoint = new ArcPathPoint(getPositionX() + DELTA, getPositionY(), pointType, myArcPath);
		myArcPath.insertPoint(i + 1, newPoint);
		myArcPath.getArc().updateArcPosition();
		return new AddArcPathPointEditCommand(myArcPath.getArc(), newPoint, myArcPath.getArc().getGuiModel());
	}

	public Point2D.Double getMidPoint(ArcPathPoint target) {
		return new Point2D.Double(
		    (target.getPositionX() + getPositionX()) / 2,
            (target.getPositionY() + getPositionY()) / 2
        );
	}

	public boolean isDeleteable() {
		int i = getIndex();
		return (i > 0 && i != myArcPath.getNumPoints() - 1);
	}

	public Point2D.Double getControl1() {
		return control1;
	}

	public Point2D.Double getControl2() {
		return control2;
	}

	public void setControl1(double _x, double _y) {
		control1.x = _x;
		control1.y = _y;
	}

	public void setControl2(double _x, double _y) {
		control2.x = _x;
		control2.y = _y;
	}

	public void setControl1(Point2D.Double p) {
		control1.x = p.x;
		control1.y = p.y;
	}

	public void setControl2(Point2D.Double p) {
		control2.x = p.x;
		control2.y = p.y;
	}

	public ArcPath getArcPath() {
		return myArcPath;
	}

	@Override
	public void addedToGui() {}
	@Override
	public void removedFromGui() {}

	@Override
	public int getLayerOffset() {
		return Constants.ARC_POINT_LAYER_OFFSET;
	}

	public void translate(int x, int y) {
	    //We should ingnore move of the endpoints (linked to source/target)
	    if (!isEndPoint()) {

	        setPositionX(positionX + x);
            setPositionY(positionY + y);

            updateOnMoveOrZoom();

            myArcPath.updateArc();
        }
	}

	@Override
	public String getName() {
		return this.getArcPath().getArc().getName() + " - Point " + this.getIndex();
	}

	public void zoomUpdate(int zoom) {

		// change ArcPathPoint's size a little bit when it's zoomed in or zoomed out
		if (zoom > 213) {
			SIZE = 5;
		} else if (zoom > 126) {
			SIZE = 4;
		} else {
			SIZE = 3;
		}

        super.zoomUpdate(zoom);

	}

    @Override
    public void updateOnMoveOrZoom() {
        int x = Zoomer.getZoomedValue(getOriginalX(), getZoom());
        int y = Zoomer.getZoomedValue(getOriginalY(), getZoom());
        positionX = x;
        positionY = y;
        setBounds(x - SIZE, y - SIZE, 2 * SIZE + SIZE_OFFSET, 2 * SIZE + SIZE_OFFSET);
    }

    public Point2D.Double getRealPoint() {
		return new Point2D.Double(getOriginalX(), getOriginalY());
	}

	public boolean isEndPoint() {
        return this.getIndex() == 0 || this.getIndex() == myArcPath.getEndIndex();
	}


    @Override
    public void select(boolean shouldRepaint) {
        setVisible(true);
        super.select(shouldRepaint);
    }

    @Override
    public void deselect() {
        setVisible(false);
        super.deselect();
    }
}
