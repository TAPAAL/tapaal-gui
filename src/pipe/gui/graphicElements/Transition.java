package pipe.gui.graphicElements;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

	private static final long serialVersionUID = 942116302162925121L;
	
	//Transition is of Rectangle2D.Double
	private GeneralPath transition;
	private Shape proximityTransition;

	// Transition Size
	public static final int TRANSITION_HEIGHT = Pipe.PLACE_TRANSITION_HEIGHT;
	public static final int TRANSITION_WIDTH = TRANSITION_HEIGHT / 3;
	
	protected int angle;
	
	// Animation Suff
	protected boolean enabled = false;
	public boolean delayEnabled = false;
	public boolean highlighted = false;
	private Timer blinkTimer;
	private int blinkCount;

	private static final double rootThreeOverTwo = 0.5 * Math.sqrt(3);

	private ArrayList<ArcAngleCompare> arcAngleList = new ArrayList<ArcAngleCompare>();

	/**
	 * Create Petri-Net Transition object
	 */
	public Transition(double positionXInput, double positionYInput,
			String idInput, String nameInput, double nameOffsetXInput,
			double nameOffsetYInput,
			boolean infServer, int angleInput, int priority) {
		super(positionXInput, positionYInput, idInput, nameInput,
				nameOffsetXInput, nameOffsetYInput);
		componentWidth = TRANSITION_HEIGHT; // sets width
		componentHeight = TRANSITION_HEIGHT;// sets height
		constructTransition();
		angle = 0;
		setCentre((int) positionX, (int) positionY);
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
	public Transition(double positionXInput, double positionYInput) {
		super(positionXInput, positionYInput);
		componentWidth = TRANSITION_HEIGHT; // sets width
		componentHeight = TRANSITION_HEIGHT;// sets height
		constructTransition();
		setCentre((int) positionX, (int) positionY);
		updateBounds();
		this.updateEndPoints();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (selected && !ignoreSelection) {
			g2.setColor(Pipe.SELECTION_FILL_COLOUR);
			pnName.setForeground(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
			pnName.setForeground(Pipe.ELEMENT_LINE_COLOUR);
		}

		if (highlighted) {
			g2.setPaint(Pipe.ENABLED_TRANSITION_COLOUR);
		} else if (delayEnabled && CreateGui.getApp().isShowingDelayEnabledTransitions() && !Animator.isUrgentTransitionEnabled()) {
			g2.setPaint(Pipe.YELLOW_TRANSITION_COLOR);
		} else if (selected && !ignoreSelection) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}

		g2.draw(transition);
		g2.fill(transition);
		
	}

	/**
	 * Rotates the Transition through the specified angle around the midpoint
	 */
	public Command rotate(int angleInc) {
		angle = (angle + angleInc) % 360;
		transition.transform(AffineTransform.getRotateInstance(Math
				.toRadians(angleInc), componentWidth / 2, componentHeight / 2));
		outlineTransition();

		Iterator<ArcAngleCompare> arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			(arcIterator.next()).calcAngle();
		}
		Collections.sort(arcAngleList);

		updateEndPoints();
		repaint();

		return new TransitionRotationEdit(this, angle);
	}

	private void outlineTransition() {
		proximityTransition = (new BasicStroke(
				Pipe.PLACE_TRANSITION_PROXIMITY_RADIUS))
				.createStrokedShape(transition);
	}

	/**
	 * Determines whether Transition is enabled
	 * 
	 * @param animationStatus
	 *            Anamation status
	 * @return True if enabled
	 */
	public boolean isEnabled(boolean animationStatus) {
		if (animationStatus) {
			if (isEnabled()) {
				highlighted = true;
				return true;
			} else {
				highlighted = false;
			}
		}
		return false;
	}

	/**
	 * Determines whether Transition is enabled
	 * 
	 * @return True if enabled
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public boolean isDelayEnabledTransition(boolean animationStatus){
		if(animationStatus){
			delayEnabled = isDelayEnabled();
			return delayEnabled;
		}
		return false;
	}
	
	//Dummy is overridden
	public boolean isDelayEnabled(){
		return delayEnabled;
	}
	
	//Dummy is overridden
	public TimeInterval getDInterval(){
		return null;
	}
	
	/* Called at the end of animation to reset Transitions to false */
	public void setDelayEnabledTransitionFalse(){
		delayEnabled = false;
	}
	
	/**
	 * Sets whether Transition is enabled
	 * 
	 * @return enabled if True
	 */
	@Override
	public void setEnabled(boolean status) {
		enabled = status;
	}

	/* Called at the end of animation to reset Transitions to false */
	public void setEnabledFalse() {
		enabled = false;
		highlighted = false;
	}

	public int getAngle() {
		return angle;
	}

	protected void constructTransition() {
		transition = new GeneralPath();
		transition.append(new Rectangle2D.Double(
				(componentWidth - TRANSITION_WIDTH) / 2, 0, TRANSITION_WIDTH,
				TRANSITION_HEIGHT), false);
		outlineTransition();
	}

	@Override
	public boolean contains(int x, int y) {
		int zoomPercentage = zoom;

		double unZoomedX = (x - COMPONENT_DRAW_OFFSET)
				/ (zoomPercentage / 100.0);
		double unZoomedY = (y - COMPONENT_DRAW_OFFSET)
				/ (zoomPercentage / 100.0);

		someArc = CreateGui.getDrawingSurface().createArc;
		if (someArc != null) { // Must be drawing a new Arc if non-NULL.
			if ((proximityTransition.contains((int) unZoomedX, (int) unZoomedY) || transition
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
					removeArcCompareObject(someArc);
					updateConnected();
				}
				return false;
			}
		} else {
			return transition.contains((int) unZoomedX, (int) unZoomedY);
		}
	}

	public void removeArcCompareObject(Arc a) {
		Iterator<ArcAngleCompare> arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			if ((arcIterator.next()).arc == a) {
				arcIterator.remove();
			}
		}
	}

	@Override
	public void updateEndPoint(Arc arc) {
		boolean match = false;

		Iterator<ArcAngleCompare> arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = arcIterator.next();
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

		transform.concatenate(Zoomer.getTransform(zoom));

		arcIterator = top.iterator();
		transform.transform(new Point2D.Double(1, 0.5 * TRANSITION_HEIGHT),
				transformed); // +1 due to rounding making it off by 1
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
		transform.transform(new Point2D.Double(0, -0.5 * TRANSITION_HEIGHT),
				transformed);
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
			transform.transform(new Point2D.Double(-0.5 * TRANSITION_WIDTH,
					current + 1), transformed); // +1 due to rounding making it off by 1
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
			transform.transform(new Point2D.Double(+0.5 * TRANSITION_WIDTH,
					current), transformed);
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

	@Override
	public void addedToGui() {
		super.addedToGui();
		update(true);
	}

	private String getText() {
		return "";
	}

	@Override
	public void setCentre(double x, double y) {
		super.setCentre(x, y);
		update(true);
	}

	@Override
	public void toggleAttributesVisible() {
		attributesVisible = !attributesVisible;
		update(true);
	}

	@Override
	public void showEditor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(boolean displayConstantNames) {
		pnName.setText(getText());
		pnName.zoomUpdate(zoom);
		super.update(displayConstantNames);
		this.repaint();
	}

	class ArcAngleCompare implements Comparable<ArcAngleCompare> {

		private static final boolean SOURCE = false;
		private static final boolean TARGET = true;
		private Arc arc;
		private Transition transition;
		private double angle;

		public ArcAngleCompare(Arc _arc, Transition _transition) {
			arc = _arc;
			transition = _transition;
			calcAngle();
		}

		public int compareTo(ArcAngleCompare arg0) {
			double angle2 = arg0.angle;

			return (angle < angle2 ? -1 : (angle == angle2 ? 0 : 1));
		}

		private void calcAngle() {
			int index = sourceOrTarget() ? arc.getArcPath().getEndIndex() - 1
					: 1;
			Point2D.Double p1 = new Point2D.Double(positionX
					+ centreOffsetLeft(), positionY + centreOffsetTop());
			Point2D.Double p2 = new Point2D.Double(arc.getArcPath().getPoint(
					index).x, arc.getArcPath().getPoint(index).y);

			if (p1.y <= p2.y) {
				angle = Math.atan((p1.x - p2.x) / (p2.y - p1.y));
			} else {
				angle = Math.atan((p1.x - p2.x) / (p2.y - p1.y)) + Math.PI;
			}

			// This makes sure the angle overlap lies at the intersection
			// between edges of a transition
			// Yes it is a nasty hack (a.k.a. ingeneous solution). But it works!
			if (angle < (Math.toRadians(30 + transition.getAngle()))) {
				angle += (2 * Math.PI);
			}

			// Needed to eliminate an exception on Windows
			if (p1.equals(p2)) {
				angle = 0;
			}

		}

		private boolean sourceOrTarget() {
			return (arc.getSource() == transition ? SOURCE : TARGET);
		}

	}

	 @Override
	 public Transition clone() {
		 Transition toReturn=null;
		 toReturn = (Transition)super.clone();

		 toReturn.positionX = positionX;

		 toReturn.componentWidth = TRANSITION_HEIGHT; //sets width
		 toReturn.componentHeight = TRANSITION_HEIGHT;//sets height
		 toReturn.constructTransition();
		 toReturn.angle = 0;
		 toReturn.setCentre((int)positionX, (int)positionY);
		 toReturn.rotate(getAngle());
		 toReturn.updateBounds();

		 return toReturn;
	 }
	 
	 public void blink(){
		 if(blinkTimer == null) { initBlinkTimer(); }
		 blinkCount = 0;
		 blinkTimer.start();
	 }
	 
	 private void initBlinkTimer(){
		 blinkTimer = new Timer(150, new ActionListener() {			 
		      public void actionPerformed(ActionEvent evt) {		    	  		    	  
		    	  if(blinkCount <= 2 ){
		    		  setVisible(!isVisible());
		    		  blinkCount++;		    	  
		    	  } else {
		    		  setVisible(true); // Ensures that transition is always visible after last blink
		    		  blinkTimer.stop();
		    	  }
		      }
		 });
	 }
}
