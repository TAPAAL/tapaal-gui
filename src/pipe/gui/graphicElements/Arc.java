package pipe.gui.graphicElements;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.*;

import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import dk.aau.cs.model.tapn.Weight;

/**
   Implementation of Element for drawing an arc
 */
public abstract class Arc extends PetriNetObjectWithLabel {

	private Shape head = null;
	private boolean fillHead = true; //If true, fill the shape when drawing, if false, fill with bg color.

	/** References to the objects this arc connects */
	private PlaceTransitionObject source = null;
	private PlaceTransitionObject target = null;

	protected ArcPath myPath = new ArcPath(this);

	//Indicated wither the arc is being drawed (true), used to dispatch mouse events to parent
	//Set to true, when using constructor for creating new arc when drawing
	protected boolean isPrototype = false;
	private static final boolean debugShowArcPathPoints = false;

	public boolean isPrototype() {
		return isPrototype;
	}

	//Called to indicate arc is no longer a prototype
	public void sealArc() {
		isPrototype = false;
	}

	// Bounds of arc need to be grown in order to avoid clipping problems
	protected int zoomGrow = 10;

	private Arc(String idInput) {
	    super(idInput, 0, 0, 0, 0);

        setHead();
    }

	/**
	 * Create Petri-Net Arc object
	 * 
	 */
	public Arc(
	    PlaceTransitionObject sourceInput,
        PlaceTransitionObject targetInput,
        int weightInput,
        String idInput
    ) {
		this(idInput);

		setSource(sourceInput);
		setTarget(targetInput);

	}


	/**
	 * Create Petri-Net Arc object
	 */
	public Arc(PlaceTransitionObject newSource) {
		this("");
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
	public void updateLabelLocation(boolean alignToGrid) {
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

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

		if (debugShowArcPathPoints) {
			Color c = g2.getColor();
			for (ArcPathPoint p : getArcPath().getArcPathPoints()) {
				drawCenteredCircle(g2, p.getPoint().x, p.getPoint().y, new Color(255, 0, 0, 80));
				drawCenteredCircle(g2, p.getControl1().x, p.getControl1().y, new Color(0, 255, 0, 80));
				drawCenteredCircle(g2, p.getControl2().x, p.getControl2().y, new Color(0, 0, 255, 80));
			}
			g2.setColor(c);
		}

		//Draw Arrow-head
		//Jump to arc end
		g2.translate(myPath.getPoint(myPath.getEndIndex()).getX(), myPath.getPoint(myPath.getEndIndex()).getY());

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

	private void drawCenteredCircle(Graphics2D g, double x, double y, Color c) {
		int r = 10;
		x = x-(r/2);
		y = y-(r/2);
		g.setColor(c);
		g.fillOval((int)x,(int)y,r,r);
	}

	@Override
	public boolean contains(int x, int y) {
		Point2D.Double point = new Point2D.Double(
		    x + myPath.getBounds().getX() - COMPONENT_DRAW_OFFSET - zoomGrow,
            y + myPath.getBounds().getY() - COMPONENT_DRAW_OFFSET - zoomGrow
        );

		//if (!CreateGui.getCurrentTab().isInAnimationMode()) {
		//	if (myPath.proximityContains(point) || selected) {
		//		// show also if Arc itself selected
		//		myPath.showPoints();
		//	} else {
		//		myPath.hidePoints();
		//	}
		//}
		return myPath.contains(point);
	}

	@Override
	public void addedToGui() {
		// called by GuiView / State viewer when adding component.
		setDeleted(false);

		myPath.addPointsToGui(getGuiModel());

		updateArcPosition();
		updateLabel(true);


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
	
	private void selectPath(){
		if (selectable) {
			myPath.selectAllPoints();
		}
	}

    public void select() {
	    super.select();
	    selectPath();
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
	}

	/**
	 * Handles keyboard input when drawing arcs in the GUI. Keys are bound to action names,
	 * and action names are mapped to action objects. The key bindings are disabled when the
	 * arc object is deleted, or the arc is connected to a place/transition.
     */
	public void enableDrawingKeyBindings(Runnable action) {
		InputMap iMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap aMap = this.getActionMap();

		// Bind keyboard keys to action names.
		iMap.put(KeyStroke.getKeyStroke("ESCAPE"), "deleteArc");
		iMap.put(KeyStroke.getKeyStroke("DELETE"), "deleteArc");
		iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "deleteArc");
		iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "deleteArc");
		aMap.put("deleteArc", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
	}

	public void disableDrawingKeyBindings() {
		this.getInputMap().clear();
		this.getActionMap().clear();
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

    @Override
    public void select(boolean shouldRepaint) {
        super.select(shouldRepaint);
        myPath.showPoints();
    }

    @Override
    public void deselect() {
        super.deselect();
        myPath.hidePoints();
    }
}
