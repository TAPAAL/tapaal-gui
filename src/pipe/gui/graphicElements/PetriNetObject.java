package pipe.gui.graphicElements;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.*;

import dk.aau.cs.gui.TabContent;
import net.tapaal.TAPAAL;
import pipe.dataLayer.DataLayer;
import pipe.gui.TAPAALGUI;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.Constants;
import pipe.gui.Zoomer;

import javax.swing.*;

/**
 * Petri-Net Object Class 
 * Implements things in common between all types of objects
 */
public abstract class PetriNetObject extends GraphicalElement implements Drawable {

	protected static final int COMPONENT_DRAW_OFFSET= 5;
	/** x/y position position on screen (zoomed) */
	protected int positionX;
	protected int positionY;
	protected TabContent.TAPNLens lens = TabContent.TAPNLens.Default;

	// The x/y coordinate of object at 100% zoom.
	//XXX: pushed down from PlaceTransitionObject and consolidated from note, need further refactoring and rename, //kyrke 2019-08-23
	protected int originalX;
	protected int originalY;

	protected String id = null;


	protected boolean selected = false; // True if part of the current selection.
	protected boolean selectable = true; // True if object can be selected.
	protected boolean draggable = true; // True if object can be dragged.

	private boolean deleted = false;

	// Integer value which represents a zoom percentage
	private int zoom = Constants.ZOOM_DEFAULT;

	private DataLayer guiModel;

	PetriNetObject(String idInput, int positionXInput, int positionYInput) {
		super();

        id = idInput;
        setOriginalX(positionXInput);
        setOriginalY(positionYInput);

	}

    public JPopupMenu getPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(TAPAALGUI.getApp().deleteAction);
        menuItem.setText("Delete");
        popup.add(menuItem);

        if (TAPAAL.IS_DEV){
            JTextArea pane = new JTextArea();
            pane.setEditable(false);

            pane.setText(
                "(Debug) \n" +
                    "  org X:" + this.getOriginalX() + " Y:" + this.getOriginalY() +"\n" +
                    "  pos X:" + this.getPositionX() + " Y:" + this.getPositionY() +""
            );

            popup.insert(pane, 1);
        }

        return popup;
    }

	public void setGuiModel(DataLayer guiModel) {
		this.guiModel = guiModel;
	}
	//XXX: not sure if datalayer should be accessable, but needed for refactorings away from "public" view.
	public DataLayer getGuiModel() { return this.guiModel;}

	public void setId(String idInput) {
		id = idInput;
		setName(idInput);
	}

	public void updateOnMoveOrZoom() {}

	/**
	 * Get id returns null if value not yet entered
	 * 
	 * @return String value for id;
	 */
	public String getId() {
		return getName();
	}

	public boolean isSelected() {
		return selected;
	}

	public void select() {
		select(true);
	}

	public void select(boolean shouldRepaint) {
		if (selectable && !selected) {
			selected = true;

			if (shouldRepaint) {
				repaint();
			}
		}

	}

	public void deselect() {
		if (selected) {
			selected = false;

			repaint();
		}
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(boolean allow) {
		selectable = allow;
	}

	public boolean isDraggable() {
		return draggable;
	}

	public void setDraggable(boolean allow) {
		draggable = allow;
	}

	public abstract void addedToGui();
	public abstract void removedFromGui();

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public boolean isDeleted() {
		return deleted;
	}

	public void select(Rectangle selectionRectangle) {
		if (selectionRectangle.intersects(this.getBounds())) {
			select();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	public abstract int getLayerOffset();

	public int getZoom() {
		return zoom;
	}

	public DrawingSurfaceImpl getParent() {
		return (DrawingSurfaceImpl) super.getParent();
	}


    public int getOriginalX() {
		return originalX;
	}

	public int getOriginalY() {
		return originalY;
	}

	@Override
	public void zoomUpdate(int zoom) {
		this.zoom = zoom;
		updateOnMoveOrZoom();
	}

	@Override
	public void translate(int x, int y) {}

	//XXX: pushed down from Placetransition object, might be dublicated //kyrke 2019-09-20
	/**
	 * Set X-axis position
	 *
	 * @param positionXInput
	 *            Double value for X-axis position
	 */
    public void setOriginalX(int positionXInput) {
        originalX = positionXInput;
        positionX = Zoomer.getZoomedValue(positionXInput, getZoom());
    }
    public void setPositionX(int positionXInput) {
        positionX = positionXInput;
        originalX = Zoomer.getUnzoomedValue(positionX, getZoom());
    }

	//XXX: pushed down from Placetransition object, might be dublicated //kyrke 2019-09-20
	/**
	 * Set Y-axis position
	 *
	 * @param positionYInput
	 *            Double value for Y-axis position
	 */
	public void setOriginalY(int positionYInput) {
        originalY = positionYInput;
        positionY = Zoomer.getZoomedValue(positionYInput, getZoom());
	}
    public void setPositionY(int positionYInput) {
        positionY = positionYInput;
        originalY = Zoomer.getUnzoomedValue(positionY, getZoom());
    }

	//XXX: pushed down from Placetransition object, might be dublicated //kyrke 2019-09-20
	/**
	 * Get X-axis position
	 *
	 * @return Double value for X-axis position
	 */
	public int getPositionX() {
		return positionX;
	}

	//XXX: pushed down from Placetransition object, might be dublicated //kyrke 2019-09-20
	/**
	 * Get Y-axis position
	 *
	 * @return Double value for Y-axis position
	 */
	public int getPositionY() {
		return positionY;
	}

	public boolean isTimed(){
	    return lens.isTimed();
    }

    public void setLens(TabContent.TAPNLens lens){
	    this.lens = lens;
    }

    public boolean isColored(){
	    return lens.isColored();
    }

    @Override
    public GraphicalElement getGraphicalElement() {
        return this;
    }

}
