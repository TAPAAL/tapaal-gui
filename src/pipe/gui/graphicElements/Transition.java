package pipe.gui.graphicElements;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.Timer;

import pipe.gui.Animator;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.undo.TransitionRotationEdit;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;

/**
 * Petri-Net Transition Class for Drawing Transitions
 */
public abstract class Transition extends PlaceTransitionObject {
	
	//Transition is of Rectangle2D.Double
	private GeneralPath transition;
	private Shape proximityTransition;

	// Transition Size
	protected static final int TRANSITION_HEIGHT = Pipe.PLACE_TRANSITION_HEIGHT;
	protected static final int TRANSITION_WIDTH = TRANSITION_HEIGHT / 3;
	
	protected int angle;
	
	// Animation Suff
	protected boolean highlightedDelayEnabled = false;
	protected boolean highlightedEnabled = false;
	private Timer blinkTimer;
	private int blinkCount;

	private static final double rootThreeOverTwo = 0.5 * Math.sqrt(3);

	private final ArrayList<ArcAngleCompare> arcAngleList = new ArrayList<ArcAngleCompare>();

	/**
	 * Create Petri-Net Transition object
	 */
	public Transition(
			int positionXInput,
			int positionYInput,
			String idInput,
			int nameOffsetXInput,
			int nameOffsetYInput,
			int angleInput
	){
		super(TRANSITION_HEIGHT, TRANSITION_HEIGHT, positionXInput, positionYInput, idInput, nameOffsetXInput, nameOffsetYInput);

		constructTransition();
		angle = 0;
		rotate(angleInput);
		updateBounds();
	}

	/**
	 * Create Petri-Net Transition object
	 * 
	 * @param positionXInput
	 *            X-axis Position
	 * @param positionYInput
	 *            Y-axis Position
	 */
	public Transition(int positionXInput, int positionYInput) {
		this(positionXInput, positionYInput,null, 0,0, 0);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(new BasicStroke(2.0f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (selected) {
			g2.setColor(Pipe.SELECTION_FILL_COLOUR);
			//pnName.setForeground(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
			//pnName.setForeground(Pipe.ELEMENT_LINE_COLOUR);
		}
        g2.fill(transition);

		if (highlightedEnabled) {
			g2.setPaint(Pipe.ENABLED_TRANSITION_COLOUR);
		} else if (highlightedDelayEnabled && CreateGui.getApp().isShowingDelayEnabledTransitions() && !Animator.isUrgentTransitionEnabled()) {
			g2.setPaint(Pipe.YELLOW_TRANSITION_COLOR);
		} else if (selected) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}

		g2.draw(transition);
	}

	/**
	 * Rotates the Transition through the specified angle around the midpoint
	 */
	public Command rotate(int angleInc) {
		angle = (angle + angleInc) % 360;
		transition.transform(AffineTransform.getRotateInstance(Math
				.toRadians(angleInc), componentWidth / 2, componentHeight / 2));
		outlineTransition();

		for (ArcAngleCompare arcAngleCompare : arcAngleList) {
			arcAngleCompare.calcAngle();
		}
		Collections.sort(arcAngleList);

		updateEndPoints();
		repaint();

		return new TransitionRotationEdit(this, angle);
	}

	private void outlineTransition() {
		proximityTransition = (new BasicStroke(Pipe.PLACE_TRANSITION_PROXIMITY_RADIUS)).createStrokedShape(transition);
	}

	/**
	 * Hightlights a transtion as enabled
	 */
	public void markTransitionEnabled(boolean b) {
        highlightedEnabled = b;
    }

	public void markTransitionDelayEnabled(boolean b){
        highlightedDelayEnabled = b;
    }

	//Named with prefix Transition due to JComponent have isEnabled
	public abstract boolean isTransitionEnabled();
	public abstract boolean isDelayEnabled();
	public abstract TimeInterval getDInterval();

    /* Called at the end of animation to reset Transitions to false */
	public void disableHightlight() {
		highlightedEnabled = false;
        highlightedDelayEnabled = false;
	}

	public int getAngle() {
		return angle;
	}

	protected void constructTransition() {
		transition = new GeneralPath();
		transition.append(
		    new Rectangle2D.Double(
		        (componentWidth - TRANSITION_WIDTH) / 2,
                0,
                TRANSITION_WIDTH,
                TRANSITION_HEIGHT
            ),
            false
        );
		outlineTransition();
	}

	@Override
	public boolean contains(int x, int y) {
		int zoomPercentage = getZoom();

		double unZoomedX = (x - COMPONENT_DRAW_OFFSET) / (zoomPercentage / 100.0);
		double unZoomedY = (y - COMPONENT_DRAW_OFFSET) / (zoomPercentage / 100.0);

		if (CreateGui.useExtendedBounds) { // Must be drawing a new Arc if non-NULL.
			return (proximityTransition.contains((int) unZoomedX, (int) unZoomedY) ||
					transition.contains((int) unZoomedX, (int) unZoomedY));
		} else {
			return transition.contains((int) unZoomedX, (int) unZoomedY);
		}
	}

	public void removeArcCompareObject(Arc a) {
        arcAngleList.removeIf(arcAngleCompare -> (arcAngleCompare).arc == a);
	}

	@Override
	public void updateEndPoint(Arc arc) {
		boolean match = false;

		Iterator<ArcAngleCompare> arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = arcIterator.next();
			if (thisArc.arc.getTarget()!=this) {
				arcIterator.remove();
				continue;
			}

			if (thisArc.arc == arc) {
				thisArc.calcAngle();
				match = true;
				break;
			}
		}

		if (!match) {
			arcAngleList.add(new ArcAngleCompare(arc, this));
		}

		Collections.sort(arcAngleList);
		updateEndPoints();
	}

	public void updateEndPoints() {
		ArrayList<ArcAngleCompare> top = new ArrayList<ArcAngleCompare>();
		ArrayList<ArcAngleCompare> bottom = new ArrayList<ArcAngleCompare>();
		ArrayList<ArcAngleCompare> left = new ArrayList<ArcAngleCompare>();
		ArrayList<ArcAngleCompare> right = new ArrayList<ArcAngleCompare>();

		Iterator<ArcAngleCompare> arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = arcIterator.next();
			double thisAngle = thisArc.angle - Math.toRadians(angle);
			if (Math.cos(thisAngle) > (rootThreeOverTwo)) {
				top.add(thisArc);
				thisArc.arc.setPathToTransitionAngle(angle + 90);
			} else if (Math.cos(thisAngle) < -rootThreeOverTwo) {
				bottom.add(thisArc);
				thisArc.arc.setPathToTransitionAngle(angle + 270);
			} else if (Math.sin(thisAngle) > 0) {
				left.add(thisArc);
				thisArc.arc.setPathToTransitionAngle(angle + 180);
			} else {
				right.add(thisArc);
				thisArc.arc.setPathToTransitionAngle(angle);
			}
		}

		AffineTransform transform = AffineTransform.getRotateInstance(Math
				.toRadians(angle + Math.PI));
		Point2D.Double transformed = new Point2D.Double();

		transform.concatenate(Zoomer.getTransform(getZoom()));

		arcIterator = top.iterator();
		transform.transform(new Point2D.Double(1, 0.5 * TRANSITION_HEIGHT), transformed); // +1 due to rounding making it off by 1
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = arcIterator.next();

			if (thisArc.sourceOrTarget()) {
				thisArc.arc.setTargetLocation(positionX + centreOffsetLeft()
						+ transformed.x, positionY + centreOffsetTop()
						+ transformed.y);
			} else {
				thisArc.arc.setSourceLocation(positionX + centreOffsetLeft()
						+ transformed.x, positionY + centreOffsetTop()
						+ transformed.y);
			}
		}

		arcIterator = bottom.iterator();
		transform.transform(new Point2D.Double(0, -0.5 * TRANSITION_HEIGHT), transformed);
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = arcIterator.next();

			if (thisArc.sourceOrTarget()) {
				thisArc.arc.setTargetLocation(positionX + centreOffsetLeft()
						+ transformed.x, positionY + centreOffsetTop()
						+ transformed.y);
			} else {
				thisArc.arc.setSourceLocation(positionX + centreOffsetLeft()
						+ transformed.x, positionY + centreOffsetTop()
						+ transformed.y);
			}
		}

		arcIterator = left.iterator();
		double inc = TRANSITION_HEIGHT / (left.size() + 1);
		double current = TRANSITION_HEIGHT / 2d - inc;
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = arcIterator.next();
			transform.transform(new Point2D.Double(-0.5 * TRANSITION_WIDTH, current + 1), transformed); // +1 due to rounding making it off by 1

            if (thisArc.sourceOrTarget()) {
				thisArc.arc.setTargetLocation(positionX + centreOffsetLeft()
						+ transformed.x, positionY + centreOffsetTop()
						+ transformed.y);
			} else {
				thisArc.arc.setSourceLocation(positionX + centreOffsetLeft()
						+ transformed.x, positionY + centreOffsetTop()
						+ transformed.y);
			}
			current -= inc;
		}

		inc = TRANSITION_HEIGHT / (right.size() + 1);
		current = -TRANSITION_HEIGHT / 2 + inc;
		arcIterator = right.iterator();
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = arcIterator.next();
			transform.transform(new Point2D.Double(+0.5 * TRANSITION_WIDTH, current), transformed);

			if (thisArc.sourceOrTarget()) {
				thisArc.arc.setTargetLocation(positionX + centreOffsetLeft()
						+ transformed.x, positionY + centreOffsetTop()
						+ transformed.y);
			} else {
				thisArc.arc.setSourceLocation(positionX + centreOffsetLeft()
						+ transformed.x, positionY + centreOffsetTop()
						+ transformed.y);
			}
			current += inc;
		}
	}

	private String getText() {
		return "";
	}

	@Override
	public void update(boolean displayConstantNames) {
		getNameLabel().setText(getText());

		super.update(displayConstantNames);
		this.repaint();
	}

	class ArcAngleCompare implements Comparable<ArcAngleCompare> {

		private static final boolean SOURCE = false;
		private static final boolean TARGET = true;
		private final Arc arc;
		private final Transition transition;
		private double angle;

		public ArcAngleCompare(Arc _arc, Transition _transition) {
			arc = _arc;
			transition = _transition;
			calcAngle();
		}

		public int compareTo(ArcAngleCompare arg0) {
			return (Double.compare(this.angle, arg0.angle));
		}

		private void calcAngle() {
			int index = sourceOrTarget() ? arc.getArcPath().getEndIndex() - 1 : 1;
			Point2D.Double p1 = new Point2D.Double(positionX
					+ centreOffsetLeft(), positionY + centreOffsetTop());
			Point2D.Double p2 = new Point2D.Double(arc.getArcPath().getPoint(index).x, arc.getArcPath().getPoint(index).y);

            angle = Math.atan2((p1.x - p2.x) , (p2.y - p1.y));

			// This makes sure the angle overlap lies at the intersection
			// between edges of a transition
			// Yes it is a nasty hack (a.k.a. ingeneous solution). But it works!
			if (angle < (Math.toRadians(30 + transition.getAngle()))) {
				angle += (2 * Math.PI);
			}

		}

		private boolean sourceOrTarget() {
			return (arc.getSource() == transition ? SOURCE : TARGET);
		}

	}
	 
	 public void blink(){
		 if(blinkTimer == null) { initBlinkTimer(); }
		 blinkCount = 0;
		 blinkTimer.start();
	 }
	 
	 private void initBlinkTimer(){
		 blinkTimer = new Timer(150, evt -> {
			 if(blinkCount <= 2 ){
				 setVisible(!isVisible());
				 blinkCount++;
			 } else {
				 setVisible(true); // Ensures that transition is always visible after last blink
				 blinkTimer.stop();
			 }
		 });
	 }
	 public void fillTransition(Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
         g2.fill(transition);
     }
}
