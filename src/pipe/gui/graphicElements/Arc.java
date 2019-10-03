package pipe.gui.graphicElements;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.*;

import pipe.gui.CreateGui;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.handler.LabelHandler;
import dk.aau.cs.model.tapn.Weight;
import pipe.gui.handler.PlaceTransitionObjectHandler;

/**
   Implementation of Element for drawing an arc
 */
public abstract class Arc extends PetriNetObjectWithLabel {

	private static final long serialVersionUID = 6527845538091358791L;

	private Shape head = null;
	private boolean fillHead = true; //If true, fill the shape when drawing, if false, fill with bg color.

	/** References to the objects this arc connects */
	private PlaceTransitionObject source = null;
	private PlaceTransitionObject target = null;

	protected ArcPath myPath = new ArcPath(this);

	//Indicated wither the arc is being drawed (true), used to dispatch mouse events to parent
	//Set to true, when using constructor for creating new arc when drawing
	protected boolean isPrototype = false;

	public boolean isPrototype() {
		return isPrototype;
	}

	//Called to indicate arc is no longer a prototype
	public void sealArc() {
		isPrototype = false;
	}

	// Bounds of arc need to be grown in order to avoid clipping problems
	protected int zoomGrow = 10;

	private Arc(int nameOffsetX, int nameOffsetY) {
	    super(nameOffsetX, nameOffsetY);

        setHead();
    }

	/**
	 * Create Petri-Net Arc object
	 * 
	 */
	public Arc(
			PlaceTransitionObject sourceInput,
			PlaceTransitionObject targetInput, int weightInput, String idInput) {
		this(0,0);

		id = idInput;
		setSource(sourceInput);
		setTarget(targetInput);

	}


	/**
	 * Create Petri-Net Arc object
	 */
	public Arc(PlaceTransitionObject newSource) {
		this(0,0);
		isPrototype = true;

		setSource(newSource);

	}

	abstract public void setWeight(Weight weight);
	abstract public Weight getWeight();

	/**
	 * Set source
	 * 
	 * @param sourceInput
	 *            PlaceTransitionObject value for Arc source;
	 */
	public void setSource(PlaceTransitionObject sourceInput) {
		source = sourceInput;
	}

	/**
	 * Set target
	 * 
	 * @param targetInput
	 *            PlaceTransitionObject value for Arc target;
	 */
	public void setTarget(PlaceTransitionObject targetInput) {
		target = targetInput;
	}

	protected void setLabelPosition() {

		getNameLabel().setPosition(
				Grid.getModifiedX(myPath.midPoint.x + Zoomer.getZoomedValue(getNameOffsetX(), getZoom())),
				Grid.getModifiedY(myPath.midPoint.y + Zoomer.getZoomedValue(getNameOffsetY(), getZoom()))
		);

	}
	@Override
	public void updateLabelLocation() {
		setLabelPosition();
	}

	/**
	 * Get id
	 * 
	 * @return String value for Arc id;
	 */
	@Override
	public String getId() {
		if (id != null) {
			return id;
		} else {
			if (source != null && target != null) {
				return source.getId() + " to " + target.getId();
			}
		}
		return "";
	}

	@Override
	public String getName() {
		return getId();
	}

	/**
	 * Get source returns null if value not yet entered
	 * 
	 * @return String value for Arc source;
	 */
	public PlaceTransitionObject getSource() {
		return source;
	}

	/**
	 * Get target returns null if value not yet entered
	 * 
	 * @return String value for Arc target;
	 */
	public PlaceTransitionObject getTarget() {
		return target;
	}

	/**
	 * Updates the start position of the arc, resets the arrowhead and updates
	 * the bounds
	 */
	public void updateArcPosition() {
		if (source != null) {
			source.updateEndPoint(this);
		}
		if (target != null) {
			target.updateEndPoint(this);
		}
		myPath.createPath();
	}

	public void setEndPoint(double x, double y, boolean type) {
		myPath.setPointLocation(myPath.getEndIndex(), x, y);
		myPath.setPointType(myPath.getEndIndex(), type);
		updateArcPosition();
	}

	public void setTargetLocation(double x, double y) {
		myPath.setPointLocation(myPath.getEndIndex(), x, y);
		myPath.createPath();
		updateBounds();
		repaint();
	}

	public void setSourceLocation(double x, double y) {
		myPath.setPointLocation(0, x, y);
		myPath.createPath();
		updateBounds();
		repaint();
	}

	public void updateLabel(boolean displayConstantNames) {
		//No label to update for this type
	}

	/** Updates the bounding box of the arc component based on the arcs bounds */
	public void updateBounds() {
        Rectangle bounds = myPath.getBounds();
		bounds.grow(COMPONENT_DRAW_OFFSET + zoomGrow, COMPONENT_DRAW_OFFSET
				+ zoomGrow);
		setBounds(bounds);
	}

	public ArcPath getArcPath() {
		return myPath;
	}
	
	public void setArcPath(ArcPath newPath) {
		myPath = newPath;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.translate(COMPONENT_DRAW_OFFSET + zoomGrow
				- myPath.getBounds().getX(), COMPONENT_DRAW_OFFSET + zoomGrow
				- myPath.getBounds().getY());

		AffineTransform reset = g2.getTransform();

		//Draw Path
		if (selected) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
			//this.label.setForeground(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
			//this.label.setForeground(Pipe.ELEMENT_LINE_COLOUR);
		}

		g2.setStroke(new BasicStroke(0.01f * getZoom()));
		g2.draw(myPath);

		//Draw Arrow-head
		//Jump to arc end
		g2.translate(myPath.getPoint(myPath.getEndIndex()).getX(), myPath
				.getPoint(myPath.getEndIndex()).getY());

		//Rotate to match arrowhead to arc angle
		g2.rotate(myPath.getEndAngle() + Math.PI);
		g2.setColor(java.awt.Color.WHITE);

		g2.transform(Zoomer.getTransform(getZoom()));
		g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);

		if (selected) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
			//this.label.setForeground(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
			//this.label.setForeground(Pipe.ELEMENT_LINE_COLOUR);
		}

		g2.setStroke(new BasicStroke(0.8f));

		if (fillHead) {
			g2.fill(head);
		} else {
			Paint p = g2.getPaint();

			//Fill first to get thick edge
			g2.setColor(java.awt.Color.WHITE); // XXX: should be GB color of canvas / drawingsurface
			g2.fill(head);

			g2.setPaint(p);
			g2.draw(head);
		}

		g2.transform(reset);
	}

	@Override
	public boolean contains(int x, int y) {
		Point2D.Double point = new Point2D.Double(x + myPath.getBounds().getX()
				- COMPONENT_DRAW_OFFSET - zoomGrow, y
				+ myPath.getBounds().getY() - COMPONENT_DRAW_OFFSET - zoomGrow);
		if (!CreateGui.getCurrentTab().isInAnimationMode()) {
			if (myPath.proximityContains(point) || selected) {
				// show also if Arc itself selected
				myPath.showPoints();
			} else {
				myPath.hidePoints();
			}
		}
		return myPath.contains(point);
	}

	@Override
	public void addedToGui() {
		// called by GuiView / State viewer when adding component.
		setDeleted(false);

		myPath.addPointsToGui(getGuiModel());

		updateArcPosition();
		super.addedToGui();
	}

	@Override
	public void removedFromGui() {

		super.removedFromGui();

		//Remove arcpathpoints
		for (ArcPathPoint p : myPath.getArcPathPoints()){
			getGuiModel().removePetriNetObject(p);
		}

	}

	public void setPathToTransitionAngle(int angle) {
		myPath.setTransitionAngle(angle);
	}

	public Transition getTransition() {
		if (getTarget() instanceof Transition) {
			return (Transition) getTarget();
		} else {
			return (Transition) getSource();
		}
	}
	
	public void selectPath(){
		if (selectable) {
			myPath.selectAllPoints();
		}
	}

	@Override
	public int getLayerOffset() {
		return Pipe.ARC_LAYER_OFFSET;
	}

	public void translate(int x, int y) {
		// We don't translate an arc, we translate each selected arc point
	}

	public void zoomUpdate(int percent) {
		super.zoomUpdate(percent);

		this.updateArcPosition();
		this.updateOnMoveOrZoom();
	}

	/**
	 * Handles keyboard input when drawing arcs in the GUI. Keys are bound to action names,
	 * and action names are mapped to action objects. The key bindings are disabled when the
	 * arc object is deleted, or the arc is connected to a place/transition.
	 */
	public void enableDrawingKeyBindings() {
		InputMap iMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap aMap = this.getActionMap();

		// Bind keyboard keys to action names.
		iMap.put(KeyStroke.getKeyStroke("ESCAPE"), "deleteArc");
		iMap.put(KeyStroke.getKeyStroke("DELETE"), "deleteArc");

		// Associate action names with actions.
		aMap.put("deleteArc", new DeleteAction(this));
	}

	public void disableDrawingKeyBindings() {
		this.getInputMap().clear();
		this.getActionMap().clear();
	}

	private class DeleteAction extends AbstractAction {
		Arc arcBeingDraw;

		DeleteAction(Arc arc) {
			arcBeingDraw = arc;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DrawingSurfaceImpl aView = CreateGui.getDrawingSurface();
			if (aView.createArc == arcBeingDraw) {
				PlaceTransitionObjectHandler.cleanupArc(aView.createArc, aView);

				if ((CreateGui.getApp().getMode() == Pipe.ElementType.FAST_PLACE)
						|| (CreateGui.getApp().getMode() == Pipe.ElementType.FAST_TRANSITION)) {
					CreateGui.getApp().endFastMode();
				}
				aView.repaint();
			}
		}
	}

	protected final void setHead(Shape head, Boolean fillHead) {
		this.head = head;
		this.fillHead = fillHead;
	}

	/**
	 * This method should be overwritten in any class that wishes to set an other arc head.
	 */
	protected void setHead() {
		setHead(new Polygon(new int[] { 0, 5, 0, -5 }, new int[] {0, -10, -7, -10 }, 4), true) ;
	}
}
