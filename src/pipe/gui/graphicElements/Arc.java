package pipe.gui.graphicElements;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.*;

import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.handler.LabelHandler;
import pipe.gui.undo.AddArcPathPointEdit;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Weight;

/**
   Implementation of Element for drawing an arc
 */
public abstract class Arc extends PetriNetObject {

	private static final long serialVersionUID = 6527845538091358791L;

	protected Shape head = new Polygon(new int[] { 0, 5, 0, -5 }, new int[] {
			0, -10, -7, -10 }, 4);
	protected boolean fillHead = true; //If true, fill the shape when drawing, if false, fill with bg color.

	//protected NameLabel label;

	private static Point2D.Double point;

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

	/**
	 * Create Petri-Net Arc object
	 * 
	 */
	public Arc(double startPositionXInput, double startPositionYInput,
			double endPositionXInput, double endPositionYInput,
			PlaceTransitionObject sourceInput,
			PlaceTransitionObject targetInput, int weightInput, String idInput) {
		pnName = new NameLabel(zoom);
		myPath.addPoint((float) startPositionXInput,
				(float) startPositionYInput, ArcPathPoint.STRAIGHT);
		myPath.addPoint((float) endPositionXInput, (float) endPositionYInput,
				ArcPathPoint.STRAIGHT);
		myPath.createPath();
		updateBounds();
		id = idInput;
		setSource(sourceInput);
		setTarget(targetInput);

		//XXX see comment in function
		setLableHandler();
	}


	/**
	 * Create Petri-Net Arc object
	 */
	public Arc(PlaceTransitionObject newSource) {
		isPrototype = true;
		pnName = new NameLabel(zoom);
		source = newSource;
		myPath.addPoint();
		myPath.addPoint();
		myPath.createPath();

		//XXX see comment in function
		setLableHandler();
	}

	public Arc() {
		super();

		pnName = new NameLabel(zoom);
		//XXX see comment in function
		setLableHandler();
	}


	private void setLableHandler() {

		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		LabelHandler labelHandler = new LabelHandler(this.getNameLabel(), this);

		getNameLabel().addMouseListener(labelHandler);
		getNameLabel().addMouseMotionListener(labelHandler);
		getNameLabel().addMouseWheelListener(labelHandler);

	}

	abstract public void setWeight(Weight weight);
	abstract public Weight getWeight();
	
	public double getNameOffsetX() {
		return nameOffsetX;
	}
	public double getNameOffsetY() {
		return nameOffsetY;
	}

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

	public void setLabelPosition() {

		pnName.setPosition(Grid.getModifiedX((double) (myPath.midPoint.x + Zoomer.getZoomedValue(nameOffsetX, zoom))),
						  Grid.getModifiedY((double) (myPath.midPoint.y + Zoomer.getZoomedValue(nameOffsetY, zoom))));
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
	
	@Override 
	public NameLabel getNameLabel() {
		return pnName;
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
	 * Get X-axis value of start position
	 * 
	 * @return Double value for X-axis of start position
	 */
	public double getStartPositionX() {
		return myPath.getPoint(0).getX();
	}

	/**
	 * Get Y-axis value of start position
	 * 
	 * @return Double value for Y-axis of start position
	 */
	public double getStartPositionY() {
		return myPath.getPoint(0).getY();
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
		bounds = myPath.getBounds();
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

		g2.setStroke(new BasicStroke(0.01f * zoom));
		g2.draw(myPath);

		//Draw Arrow-head
		//Jump to arc end
		g2.translate(myPath.getPoint(myPath.getEndIndex()).getX(), myPath
				.getPoint(myPath.getEndIndex()).getY());

		//Rotate to match arrowhead to arc angle
		g2.rotate(myPath.getEndAngle() + Math.PI);
		g2.setColor(java.awt.Color.WHITE);

		g2.transform(Zoomer.getTransform(zoom));
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
		point = new Point2D.Double(x + myPath.getBounds().getX()
				- COMPONENT_DRAW_OFFSET - zoomGrow, y
				+ myPath.getBounds().getY() - COMPONENT_DRAW_OFFSET - zoomGrow);
		if (!CreateGui.getDrawingSurface().isInAnimationMode()) {
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
		deleted = false;

		myPath.addPointsToGui((DrawingSurfaceImpl) getParent());

		updateArcPosition();
		if (getParent() != null && pnName.getParent() == null) {
			getParent().setLayer(pnName, JLayeredPane.DEFAULT_LAYER + pnName.getLayerOffset());
			getParent().add(pnName);
		}
	}

	@Override
	public void delete() {
		if (!deleted) {
			if (getParent() != null) {
				getParent().remove(pnName);
			}
			if(source != null) source.removeFromArc(this);
			if(target != null) target.removeToArc(this);
			myPath.forceHidePoints();
			super.delete();
			deleted = true;
		}
	}

	public void setPathToTransitionAngle(int angle) {
		myPath.setTransitionAngle(angle);
	}

	public Command split(Point2D.Float mouseposition) {
		ArcPathPoint newPoint = myPath.splitSegment(mouseposition);
		return new AddArcPathPointEdit(this, newPoint);
	}

	public Transition getTransition() {
		if (getTarget() instanceof Transition) {
			return (Transition) getTarget();
		} else {
			return (Transition) getSource();
		}
	}

	public void removeFromView() {
		if (getParent() != null) {
			getParent().remove(pnName);
		}
		myPath.forceHidePoints();
		removeFromContainer();
	}

	public boolean getsSelected(Rectangle selectionRectangle) {
		if (selectable) {
			ArcPath arcPath = getArcPath();
			if (arcPath.proximityIntersects(selectionRectangle)) {
				arcPath.showPoints();
			} else {
				arcPath.hidePoints();
			}
			if (arcPath.intersects(selectionRectangle)) {
				select();
				return true;
			}
		}
		return false;
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
		zoom = percent;
		this.updateArcPosition();
		this.updateOnMoveOrZoom();
		pnName.zoomUpdate(percent);
		pnName.updateSize();
	}

	public void setZoom(int percent) {
		zoom = percent;
	}

	@Override
	public void undelete(DrawingSurfaceImpl view) {
		if (this.isDeleted()) {
			super.undelete(view);
			getSource().addConnectFrom(this);
			getTarget().addConnectTo(this);
			deleted = false;
		}
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
		iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "deleteArc");
		iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "deleteArc");
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
				aView.createArc = null;
				delete();

				if ((CreateGui.getApp().getMode() == Pipe.ElementType.FAST_PLACE)
						|| (CreateGui.getApp().getMode() == Pipe.ElementType.FAST_TRANSITION)) {
					CreateGui.getApp().endFastMode();
				}
				aView.repaint();
			}
		}
	}
}
