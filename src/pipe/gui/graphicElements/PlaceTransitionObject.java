package pipe.gui.graphicElements;

import net.tapaal.TAPAAL;
import pipe.gui.Constants;
import pipe.gui.canvas.Zoomer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * Petri-Net Place or Transition SuperClass
 * 
 */
public abstract class PlaceTransitionObject extends PetriNetObjectWithLabel {

	protected final double componentWidth;
	protected final double componentHeight;

	private final LinkedList<Arc> connectTo = new LinkedList<Arc>();
	private final LinkedList<Arc> connectFrom = new LinkedList<Arc>();

	protected boolean attributesVisible = true;

    public PlaceTransitionObject(
	        double componentWidth,
			double componentHeight,
			int positionXInput,
			int positionYInput,
			String idInput,
			int nameOffsetXInput,
			int nameOffsetYInput
	){
		super(idInput, positionXInput, positionYInput, nameOffsetXInput, nameOffsetYInput);
		this.componentWidth = componentWidth;
		this.componentHeight = componentHeight;
	}

    @Override
    public JPopupMenu getPopup(MouseEvent e) {
        JPopupMenu popup = super.getPopup(e);
        if (TAPAAL.IS_DEV){
            JTextArea pane = new JTextArea();
            pane.setEditable(false);

            String preset = "";
            for (Arc arc : ((PlaceTransitionObject) this).getPreset()) {
                preset += "    "  + arc.getId() + "\n";
            }
            String postset = "";
            for (Arc arc : ((PlaceTransitionObject) this).getPostset()) {
                postset += "    "  + arc.getId() + "\n";
            }

            pane.setText(
                "(Debug) \n" +
                    "  preset: " +"\n" +
                    preset +
                    "  postset: " +"\n"+
                    postset
            );

            popup.insert(pane, 1);
        }
        return popup;
    }

	/**
	 * Set name
	 * 
	 * @param nameInput
	 *            String value for Place name;
	 */
	@Override
	public void setName(String nameInput) {
		getNameLabel().setName(nameInput);
	}

	/**
	 * Set id
	 * 
	 * @param idInput
	 *            String value for Place id;
	 */
	@Override
	public void setId(String idInput) {
		id = idInput;
		setName(id);
	}

	/**
	 * Get id
	 * 
	 * @return String value for Place id;
	 */
	@Override
	public String getId() {
		return getName();
	}

	/**
	 * Get name
	 * 
	 * @return String value for Place name;
	 */
	@Override
	public String getName() {
		return (getNameLabel() != null) ? getNameLabel().getName() : "";
	}

	/**
	 * Implemented in subclasses as involves some tailoring according to the
	 * shape
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.translate(COMPONENT_DRAW_OFFSET, COMPONENT_DRAW_OFFSET);
		g2.transform(Zoomer.getTransform(getZoom()));
	}

	/**
	 * Returns the distance between the outside of the component to the centre,
	 * in order to position the centre of the place where the mouse clicks on
	 * the screen
	 * 
	 * @return Top offset of Place
	 */
	public int centreOffsetTop() {
		return Zoomer.getZoomedValue(componentHeight / 2.0, getZoom());
	}

	/**
	 * Returns the distance between the outside of the component to the centre,
	 * in order to position the centre of the place where the mouse clicks on
	 * the screen
	 * 
	 * @return Left offset of Place
	 */
	public int centreOffsetLeft() {
		return Zoomer.getZoomedValue(componentWidth / 2.0, getZoom());
	}

	/** Calculates the BoundsOffsets used for setBounds() method */
	public void updateBounds() {
		double scaleFactor = Zoomer.getScaleFactor(getZoom());
		positionX = (int)Math.round(originalX * scaleFactor);
		positionY = (int)Math.round(originalY * scaleFactor);

		int width = (int) Math.round(componentWidth * scaleFactor);
		int height = (int) Math.round(componentHeight * scaleFactor);

		Rectangle bounds = new Rectangle(positionX, positionY, width, height);
		bounds.grow(COMPONENT_DRAW_OFFSET, COMPONENT_DRAW_OFFSET);
		setBounds(bounds);
	}

	/** Adds outwards arc to place/transition */
	public void addConnectTo(Arc newArc) {
		connectTo.add(newArc);
	}

	/** Adds inwards arc to place/transition */
	public void addConnectFrom(Arc newArc) {
		connectFrom.add(newArc);
	}

	public void removeFromArc(Arc oldArc) {
		connectFrom.remove(oldArc);
	}

	public void removeToArc(Arc oldArc) {
		connectTo.remove(oldArc);
	}

	/** Updates location of any attached arcs */
	public void updateConnected() {
		for (Arc someArc : connectFrom) {
			updateEndPoint(someArc);
			someArc.updateArcPosition();
		}

		for (Arc someArc : connectTo) {
			updateEndPoint(someArc);
			someArc.updateArcPosition();
		}
	}

	/** Translates the component by x,y */
	public void translate(int x, int y) {
		setPositionX(positionX + x);
		setPositionY(positionY + y);
		updateOnMoveOrZoom();
	}

	/** Sets the center of the component to position x, y */
	public void setCentre(int x, int y) {
		setPositionX((int) Math.round(x - (getWidth() / 2.0)));
		setPositionY((int) Math.round(y - (getHeight() / 2.0)));
		update(true);
	}

    public Point2D.Double getCentre() {
        return new Point2D.Double(positionX + getWidth() / 2.0, positionY + getHeight() / 2.0);
    }

	public void update(boolean displayConstantNames) {
		update(displayConstantNames, true);
	}

	public void update(boolean displayConstantNames, boolean alignToGrid) {
		updateOnMoveOrZoom(alignToGrid);
	}

	/** Handles selection for Place/Transitions */
	@Override
	public void select() {
		if (selectable && !selected) {
			selected = true;

			if (getNameLabel() != null) {
				getNameLabel().setForeground(Constants.SELECTION_LINE_COLOUR);
			}

			// Select arcs that are connected from this object to another selected object.
			for (Arc arc : getPostset()) {
				if(arc.getTarget().isSelected()){
					arc.select();
				}
			}

			// Select arcs that are connected to this object from another selected object.
			for (Arc arc : getPreset()) {
				if(arc.getSource().isSelected()){
					arc.select();
				}
			}
			repaint();
		}
	}

	@Override
	public void addedToGui() {
		setDeleted(false);
		super.addedToGui();
		update(true);
		updateOnMoveOrZoom();
	}

    // TODO: Find a better name for this

	@Override
	public void updateOnMoveOrZoom() {
		updateOnMoveOrZoom(true);
	}
	
	// TODO: Find a better name for this
	public void updateOnMoveOrZoom(boolean alignToGrid) {
		updateBounds();
		updateConnected();
		updateLabelLocation(alignToGrid);
	}

	public boolean areNotSameType(PlaceTransitionObject o) {
		return (this.getClass() != o.getClass());
	}

	/* KYRKE - added functions for getting pre and post set af lists */
	public Iterable<Arc> getPreset() {
		return connectTo;
	}

	public Iterable<Arc> getPostset() {
		return connectFrom;
	}

    public abstract void updateEndPoint(Arc arc);

	public abstract void showEditor();

	public void setAttributesVisible(boolean flag) {
		attributesVisible = flag;
	}

	public boolean getAttributesVisible() {
		return attributesVisible;
	}

	@Override
	public int getLayerOffset() {
		return Constants.PLACE_TRANSITION_LAYER_OFFSET;
	}


	public void toggleAttributesVisible() {
		attributesVisible = !attributesVisible;
		update(true);
	}

    public void zoomUpdate(int value) {
		super.zoomUpdate(value);
		update(true, false);
	}

}
