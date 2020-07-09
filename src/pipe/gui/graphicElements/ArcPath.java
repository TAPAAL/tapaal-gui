package pipe.gui.graphicElements;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import pipe.dataLayer.DataLayer;
import pipe.gui.Pipe;
import pipe.gui.undo.AddArcPathPointEdit;
import dk.aau.cs.gui.undo.Command;

/**
 * @author Peter Kyme, Tom Barnwell and Michael Camacho
 * 
 * @author Edwin Chung 16 Mar 2007: modified the constructor and several other
 *         functions so that DataLayer objects can be created outside the GUI
 * 
 * @author Pere Bonet: modified the weight label placement
 */
public class ArcPath implements Shape {

	private GeneralPath path = new GeneralPath();
	private final List<ArcPathPoint> pathPoints = new ArrayList<ArcPathPoint>();
	private final Arc myArc;
	private boolean pointLock = false;
	private static final Stroke proximityStroke = new BasicStroke(Pipe.ARC_PATH_PROXIMITY_WIDTH);
	private static final Stroke stroke = new BasicStroke(Pipe.ARC_PATH_SELECTION_WIDTH);
	private Shape shape, proximityShape;
	private int transitionAngle;
	private final static boolean showDebugCurvedControlPoints = false;

	public Point2D.Double midPoint = new Point2D.Double();

	private ArcPath(Arc a, int transitionAngle) {
		super();

		this.myArc = a;
		this.transitionAngle = transitionAngle;
	}

	public ArcPath(Arc a) {
		this(a, 0);

		//Add the start/end point
		addPoint();
		addPoint();
	}

	public ArcPath(Arc a, ArcPath path) {
		this(a, 0);

		for (int i = 0; i <= path.getEndIndex(); i++) {
			addPoint(path.getRealPoint(i).getX(), path.getRealPoint(i).getY(), path.getPointType(i));
		}
	}

	public List<ArcPathPoint> getArcPathPoints(){
		return pathPoints;
	}
	
	public ArcPathPoint getArcPathPoint(int i) {
		return (pathPoints.get(i));
	}

	public void createPath() {
		ArcPathPoint currentPoint;
		setControlPoints();

		path = new GeneralPath();
		currentPoint = pathPoints.get(0);
		path.moveTo(currentPoint.getPoint().x, currentPoint.getPoint().y);

		currentPoint.setPointType(ArcPathPoint.STRAIGHT);

		double length = 0;
		for (int c = 1; c <= getEndIndex(); c++) {
			ArcPathPoint previousPoint = currentPoint;
			currentPoint = pathPoints.get(c);

			if (currentPoint.getPointType() == ArcPathPoint.STRAIGHT) {
				path.lineTo(currentPoint.getPoint().x, currentPoint.getPoint().y);
			} else if (currentPoint.getPointType() == ArcPathPoint.CURVED) {
				if (showDebugCurvedControlPoints) {
					// draw control lines for illustrative purposes
					path.lineTo(currentPoint.getControl1().x, currentPoint.getControl1().y);
					path.lineTo(currentPoint.getControl2().x, currentPoint.getControl2().y);
					path.lineTo(currentPoint.getPoint().x, currentPoint.getPoint().y);
					path.moveTo(previousPoint.getPoint().x, previousPoint.getPoint().y);
				}
				path.curveTo(
						currentPoint.getControl1().x,
						currentPoint.getControl1().y,
						currentPoint.getControl2().x,
						currentPoint.getControl2().y,
						currentPoint.getPoint().x,
						currentPoint.getPoint().y
				);
			}
			length += getMod(currentPoint.getPoint(), previousPoint.getPoint());
		}

		length /= 2;
		int c = 0;
		currentPoint = pathPoints.get(c++);

		//Calculate the arc mid-point for lable placement
		if (getEndIndex() < 2) {
			midPoint.x = (((pathPoints.get(0)).getPoint().x + (pathPoints.get(1)).getPoint().x) * 0.5);
			midPoint.y = (((pathPoints.get(0)).getPoint().y + (pathPoints.get(1)).getPoint().y) * 0.5);
		} else {
			double acc = 0;
			double percent = 0;
			for (c = 1; c <= getEndIndex(); c++) {
				ArcPathPoint previousPoint = currentPoint;
				currentPoint = pathPoints.get(c);

				double inc = getMod(currentPoint.getPoint(), previousPoint.getPoint());
				if ((acc + inc > length)) {
					percent = (length - acc) / inc;
					break;
				}
				acc += inc;
			}

			ArcPathPoint previousPoint = pathPoints.get(c - 1);
			midPoint.x = previousPoint.getPoint().x + ((currentPoint.getPoint().x - previousPoint.getPoint().x) * percent);
			midPoint.y = previousPoint.getPoint().y + ((currentPoint.getPoint().y - previousPoint.getPoint().y) * percent);
		}

		shape = stroke.createStrokedShape(this);
		getArc().setLabelPosition();
		proximityShape = proximityStroke.createStrokedShape(this);
	}

	private void setControlPoints() {
		setCurveControlPoints(); // must be in this order
		setStraightControlPoints();
		setEndControlPoints();
	}

	/* returns a control point for curve CD with incoming vector AB */
	private Point2D.Double getControlPoint(
	    Point2D.Double A,
        Point2D.Double B,
        Point2D.Double C,
        Point2D.Double D
    ) {
		Point2D.Double p = new Point2D.Double(0, 0);

		double modAB = getMod(A, B);
		double modCD = getMod(C, D);

		double ABx = (B.x - A.x) / modAB;
		double ABy = (B.y - A.y) / modAB;

		if (modAB < 7) {
			// hack, stops division by zero, modAB can only be this low if the
			// points are virtually superimposed anyway
			p = (Point2D.Double) C.clone();
		} else {
			p.x = C.x + (ABx * modCD / Pipe.ARC_CONTROL_POINT_CONSTANT);
			p.y = C.y + (ABy * modCD / Pipe.ARC_CONTROL_POINT_CONSTANT);
		}
		return p;
	}

	private double getMod(Point2D.Double A, Point2D.Double B) {
		double ABx = A.x - B.x;
		double ABy = A.y - B.y;

		return Math.sqrt(ABx * ABx + ABy * ABy);
	}

	/* function sets control points for any curved sections of the path */
	private void setCurveControlPoints() {
		if (pathPoints.size() < 1) {
			return;
		}
		ArcPathPoint myCurrentPoint = pathPoints.get(0);
		myCurrentPoint.setPointType(ArcPathPoint.STRAIGHT);

		Cubic[] X, Y;

		int endIndex = getEndIndex();

		for (int c = 1; c <= endIndex;) {
			int curveStartIndex = 0;
			int curveEndIndex = 0;
			myCurrentPoint = pathPoints.get(c);

			if (myCurrentPoint.getPointType()) {
				curveStartIndex = c - 1;

				for (; c <= endIndex && myCurrentPoint.getPointType(); c++) {
					myCurrentPoint = pathPoints.get(c);
					curveEndIndex = c;
				}
				/* calculate a cubic for each section of the curve */
				int lengthOfCurve = curveEndIndex - curveStartIndex;
				int k1;
				int[] x = new int[lengthOfCurve + 2];
				int[] y = new int[lengthOfCurve + 2];
				X = new Cubic[lengthOfCurve + 2];
				Y = new Cubic[lengthOfCurve + 2];

				for (k1 = 0; k1 <= (curveEndIndex - curveStartIndex); k1++) {
					x[k1] = (int) (pathPoints.get(curveStartIndex + k1)).getPoint().x;
					y[k1] = (int) (pathPoints.get(curveStartIndex + k1)).getPoint().y;
				}
				x[k1] = x[k1 - 1];
				y[k1] = y[k1 - 1];

				X = calcNaturalCubic(k1, x);
				Y = calcNaturalCubic(k1, y);

				for (int k2 = 1; k2 <= lengthOfCurve; k2++) {
					myCurrentPoint = pathPoints.get(k2 + curveStartIndex);
					myCurrentPoint.setControl1(X[k2 - 1].getX1(), Y[k2 - 1].getX1());
					myCurrentPoint.setControl2(X[k2 - 1].getX2(), Y[k2 - 1].getX2());
				}
			} else {
				c++;
			}
		}
	}

	/*
	 * fuction sets the control points for any straight sections and for smooth
	 * intersection between straight and curved sections
	 */
	private void setStraightControlPoints() {

		ArcPathPoint myCurrentPoint = pathPoints.get(0);
		ArcPathPoint myPreviousButOnePoint = null;
		ArcPathPoint myNextPoint = null;
		ArcPathPoint myPreviousPoint = null;

		for (int c = 1; c <= getEndIndex(); c++) {
			myPreviousPoint = pathPoints.get(c - 1);
			myCurrentPoint = pathPoints.get(c);

			if (!(myCurrentPoint.getPointType())) {
				myCurrentPoint.setControl1(
						getControlPoint(
								myPreviousPoint.getPoint(),
								myCurrentPoint.getPoint(),
								myPreviousPoint.getPoint(),
								myCurrentPoint.getPoint()
						)
				);
				myCurrentPoint.setControl2(
						getControlPoint(
								myCurrentPoint.getPoint(),
								myPreviousPoint.getPoint(),
								myCurrentPoint.getPoint(),
								myPreviousPoint.getPoint()
						)
				);
			} else {
				if (c > 1 && !(myPreviousPoint.getPointType())) {
					myPreviousButOnePoint = pathPoints.get(c - 2);
					myCurrentPoint.setControl1(
							getControlPoint(
									myPreviousButOnePoint.getPoint(),
									myPreviousPoint.getPoint(),
									myPreviousPoint.getPoint(),
									myCurrentPoint.getPoint()
							)
					);
				}
				if (c < getEndIndex()) {
					myNextPoint = pathPoints.get(c + 1);
					if (!(myNextPoint.getPointType())) {
						myCurrentPoint.setControl2(
								getControlPoint(
										myNextPoint.getPoint(),
										myCurrentPoint.getPoint(),
										myCurrentPoint.getPoint(),
										myPreviousPoint.getPoint()
								)
						);
					}
				}
			}
		}
	}

	private void setEndControlPoints() {
		PlaceTransitionObject source = getArc().getSource();
		PlaceTransitionObject target = getArc().getTarget();
		double anAngle = Math.toRadians(transitionAngle);

		if (getEndIndex() <= 0) {
			return;
		} else if (source instanceof Transition && (pathPoints.get(1)).getPointType()) {
			ArcPathPoint myPoint = pathPoints.get(1);
			ArcPathPoint myLastPoint = pathPoints.get(0);
			double distance = getMod(myPoint.getPoint(), myLastPoint.getPoint()) / Pipe.ARC_CONTROL_POINT_CONSTANT;
			myPoint.setControl1(
			    (myLastPoint.getPoint().x + Math.cos(anAngle) * distance),
                (myLastPoint.getPoint().y + Math.sin(anAngle) * distance)
            );

			myPoint = pathPoints.get(getEndIndex());
			myPoint.setControl2(getControlPoint(myPoint.getPoint(), myPoint.getControl1(), myPoint.getPoint(), myPoint.getControl1()));
		} else if (target != null && source instanceof Place && (pathPoints.get(getEndIndex())).getPointType()) {
			ArcPathPoint myPoint = pathPoints.get(getEndIndex());
			ArcPathPoint myLastPoint = pathPoints.get(getEndIndex() - 1);
            double distance = getMod(myPoint.getPoint(), myLastPoint.getPoint()) / Pipe.ARC_CONTROL_POINT_CONSTANT;
			myPoint.setControl2(
			    (myPoint.getPoint().x + Math.cos(anAngle) * distance),
                (myPoint.getPoint().y + Math.sin(anAngle) * distance)
            );

			myPoint = pathPoints.get(1);
			myPoint.setControl1(getControlPoint(
			    (pathPoints.get(0)).getPoint(),myPoint.getControl2(),
                (pathPoints.get(0)).getPoint(), myPoint.getControl2())
            );
		}
	}

	public void addPoint(int index, double x, double y, boolean type) {
	    var arcpath = new ArcPathPoint(x, y, type, this);

        if (myArc!=null) {
            arcpath.zoomUpdate(myArc.getZoom());
        }
		pathPoints.add(index, arcpath);
	}

	public void addPoint(double x, double y, boolean type) {
	    var arcpath = new ArcPathPoint(x, y, type, this);

	    //XXX: we set zoom here, it might be a prototype
        if (myArc!=null) {
            arcpath.zoomUpdate(myArc.getZoom());
        }
		pathPoints.add(arcpath);
	}


	public void addPoint() {
		pathPoints.add(new ArcPathPoint(this));
	}

	public void deletePoint(ArcPathPoint a) {
		pathPoints.remove(a);
	}

	public void updateArc() {
		myArc.updateArcPosition();
	}

    @Override
	public boolean contains(double arg0, double arg1) {
		return false;
	}

	public int getEndIndex() {
		return pathPoints.size() - 1;
	}

	public void setPointLocation(int index, double x, double y) {
		if (index < pathPoints.size() && index >= 0) {
			(pathPoints.get(index)).setPointLocation((int)x,(int) y);
		}
	}

	public void setPointType(int index, boolean type) {
		(pathPoints.get(index)).setPointType(type);
	}

	public boolean getPointType(int index) {
		return (pathPoints.get(index)).getPointType();
	}

	public void selectPoint(int index) {
		(pathPoints.get(index)).select();
	}
	
	public void selectAllPoints(){
		for(int i = 0; i < pathPoints.size(); i++){
			selectPoint(i);
		}
	}

	public int getNumPoints() {
		return pathPoints.size();
	}

	public Point2D.Double getPoint(int index) {
		return (pathPoints.get(index)).getPoint();
	}
    public Point2D.Double getRealPoint(int index) {
        return (pathPoints.get(index)).getRealPoint();
    }

	public ArcPathPoint getPathPoint(int index) {
		return (pathPoints.get(index));
	}

	public Arc getArc() {
		return myArc;
	}

	public void showPoints() {
		if (!pointLock) {
			for (ArcPathPoint pathPoint : pathPoints) {
				pathPoint.setVisible(true);
			}
		}
	}

	public void hidePoints() {
		if (!pointLock) {
			for (ArcPathPoint pathPoint : pathPoints) {
				if (!pathPoint.isSelected()) {
					pathPoint.setVisible(false);
				}
			}
		}
	}

	public void setPointVisibilityLock(boolean lock) {
		pointLock = lock;
	}

	/* modified to use control points, ensures a curve hits a place tangetially */
	public double getEndAngle() {
		if (getEndIndex() > 0) {
			if (getArc().getTarget() instanceof Transition) {
				return (pathPoints.get(getEndIndex())).getAngle(((pathPoints.get(getEndIndex()))).getControl2());
			} else {
				return (pathPoints.get(getEndIndex())).getAngle(((pathPoints.get(getEndIndex()))).getControl1());
			}
		}
		return 0;
	}

	public double getStartAngle() {
		if (getEndIndex() > 0) {
			return (pathPoints.get(0)).getAngle(((pathPoints.get(1))).getControl2());
		}
		return 0;
	}

	@Override
	public boolean contains(double arg0, double arg1, double arg2, double arg3) {
		return false;
	}

    @Override
	public boolean intersects(double arg0, double arg1, double arg2, double arg3) {
		return false;
	}

    @Override
	public Rectangle getBounds() {
		return path.getBounds();
	}

    @Override
	public boolean contains(Point2D p) {
		return shape.contains(p);
	}

	public boolean proximityContains(Point2D p) {
		return proximityShape.contains(p);
	}

    @Override
	public Rectangle2D getBounds2D() {
		return null;
	}

    @Override
	public boolean contains(Rectangle2D arg0) {
		return false;
	}

    @Override
	public boolean intersects(Rectangle2D r) {
		return shape.intersects(r);
	}

	public boolean proximityIntersects(Rectangle2D r) {
		return proximityShape.intersects(r);
	}

    @Override
	public PathIterator getPathIterator(AffineTransform arg0) {
		return path.getPathIterator(arg0);
	}

    @Override
	public PathIterator getPathIterator(AffineTransform arg0, double arg1) {
		return path.getPathIterator(arg0, arg1);
	}

	private Cubic[] calcNaturalCubic(int n, int[] x) {
        double[] gamma = new double[n + 1];
        double[] delta = new double[n + 1];
        double[] D = new double[n + 1];

		/*
		 * We solve the equation [2 1 ] [D[0]] [3(x[1] - x[0]) ] |1 4 1 | |D[1]|
		 * |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | | ..... | | . | | . | | 1 4
		 * 1| | . | |3(x[n] - x[n-2])| [ 1 2] [D[n]] [3(x[n] - x[n-1])]
		 * 
		 * by using row operations to convert the matrix to upper triangular and
		 * then back sustitution. The D[i] are the derivatives at the knots.
		 */

		gamma[0] = 1.0f / 2.0f;
		for (int i = 1; i < n; i++) {
			gamma[i] = 1 / (4 - gamma[i - 1]);
		}
		gamma[n] = 1 / (2 - gamma[n - 1]);

		delta[0] = 3 * (x[1] - x[0]) * gamma[0];
		for (int i = 1; i < n; i++) {
			delta[i] = (3 * (x[i + 1] - x[i - 1]) - delta[i - 1]) * gamma[i];
		}
		delta[n] = (3 * (x[n] - x[n - 1]) - delta[n - 1]) * gamma[n];

		D[n] = delta[n];
		for (int i = n - 1; i >= 0; i--) {
			D[i] = delta[i] - gamma[i] * D[i + 1];
		}

		/* now compute the coefficients of the cubics */
		Cubic[] C = new Cubic[n];
		for (int i = 0; i < n; i++) {
			C[i] = new Cubic(x[i], D[i], 3 * (x[i + 1] - x[i]) - 2 * D[i]
					- D[i + 1], 2 * (x[i] - x[i + 1]) + D[i] + D[i + 1]);
		}
		return C;
	}

	public void addPointsToGui(DataLayer model) {

		(pathPoints.get(0)).setDraggable(false);
		(pathPoints.get(pathPoints.size() - 1)).setDraggable(false);

		for (ArcPathPoint pathPoint : pathPoints) {
			pathPoint.setVisible(false);

			// Check whether the point has already been added to the gui
			// as addPointsToGui() may have been called after the user
			// split an existing point. If this is the case, we don't want
			// to add all the points again along with new action listeners,
			// we just want to add the new point.
			// Nadeem 21/06/2005
			if (!model.getPNObjects().contains(pathPoint)) {
				model.addPetriNetObject(pathPoint);
			}
		}
	}

	public String[][] getArcPathDetails() {
		int length = getEndIndex() + 1;
		String[][] details = new String[length][3];

        int x, y;
		for (int c = 0; c < length; c++) {
			x = (pathPoints.get(c)).getOriginalX();
			details[c][0] = String.valueOf(x);
			y = (pathPoints.get(c)).getOriginalY();
			details[c][1] = String.valueOf(y);
			details[c][2] = String.valueOf((pathPoints.get(c)).getPointType());
		}
		return details;
	}

	public void purgePathPoints() {
		// Dangerous! Only called from DataLayer when loading ArcPaths
		pathPoints.clear();
	}

	public void setTransitionAngle(int angle) {
		transitionAngle = angle;
		transitionAngle %= 360;
	}

	/**
	 * Inserts a new point into the Array List of path points at
	 * the specified index and shifts all the following points along
	 */
	public void insertPoint(int index, ArcPathPoint newpoint) {
		pathPoints.add(index, newpoint);

		addPointsToGui(myArc.getGuiModel());

	}

	public Command insertPoint(Point2D.Double mouseposition, boolean flag) {
		int wantedpoint = findPoint(mouseposition);

		// wantedpoint is now the index of the first point in the pair of arc
		// points marking the segment to be split. So we have all we need to
		// split the arc.
		ArcPathPoint newPoint = new ArcPathPoint(mouseposition, flag, this);
		insertPoint(wantedpoint + 1, newPoint);
		createPath();
		myArc.updateArcPosition();
        showPoints();

		return new AddArcPathPointEdit(this.getArc(), newPoint, getArc().getGuiModel());
	}

	private int findPoint(final Point2D.Double mouseposition) {
		// An array to store all the distances from the midpoints
		double[] distances = new double[pathPoints.size() - 1];

		// Calculate the midpoints and distances to them
		for (int index = 0; index < (pathPoints.size() - 1); index++) {
			ArcPathPoint first = pathPoints.get(index);
			ArcPathPoint second = pathPoints.get(index + 1);
			Point2D.Double midpoint = first.getMidPoint(second);
			distances[index] = midpoint.distance(mouseposition);
		}

		// Now determine the shortest midpoint
		double shortest = distances[0];
		int wantedpoint = 0;
		for (int index = 0; index < pathPoints.size() - 1; index++) {
			if (distances[index] < shortest) {
				shortest = distances[index];
				wantedpoint = index;
			}
		}
		return wantedpoint;
	}
}

class Cubic {

	double a, b, c, d; /* a + b*u + c*u^2 + d*u^3 */

	public Cubic(double _a, double _b, double _c, double _d) {
		a = _a;
		b = _b;
		c = _c;
		d = _d;
	}

	// Return first control point coordinate (calculated from coefficients)
	public double getX1() {
		return ((b + 3 * a) / 3);
	}

	// Return second control point coordinate (calculated from coefficients)
	public double getX2() {
		return ((c + 2 * b + 3 * a) / 3);
	}

	/** evaluate cubic */
	public double eval(double u) {
		return (((d * u) + c) * u + b) * u + a;
	}

}
