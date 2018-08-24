package pipe.gui.graphicElements;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;

import javax.swing.JComponent;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Translatable;
import pipe.gui.Zoomable;
import pipe.gui.Zoomer;

/**
 * Petri-Net Object Class 
 * Implements things in common between all types of objects
 */
public abstract class PetriNetObject extends JComponent implements Zoomable,
		Cloneable, Translatable {

	private static final long serialVersionUID = 2693171860021066729L;

	protected static final int COMPONENT_DRAW_OFFSET= 5;
	/** X-axis Position on screen */
	protected double positionX;
	/** Y-axis Position on screen */
	protected double positionY;

	/** X-axis Position on screen */
	protected double nameOffsetX;
	/** Y-axis Position on screen */
	protected double nameOffsetY;
	
	
	protected String id = null;

	/* Name Label for displaying name */
	protected NameLabel pnName;
	protected Color objectColour = Pipe.ELEMENT_LINE_COLOUR;
	protected Color selectionBorderColour = Pipe.SELECTION_LINE_COLOUR;
	protected boolean selected = false; // True if part of the current selection.
	protected boolean selectable = true; // True if object can be selected.
	protected boolean draggable = true; // True if object can be dragged.
	protected boolean copyPasteable = true; // True if object can be cloned.
	protected static boolean ignoreSelection = false;
	protected Rectangle bounds = new Rectangle();

	protected boolean deleted = false;
	protected boolean markedAsDeleted = false;

	// Integer value which represents a zoom percentage
	protected int zoom = Pipe.ZOOM_DEFAULT;
	private DataLayer guiModel;

	public void setGuiModel(DataLayer guiModel) {
		this.guiModel = guiModel;
	}

	public DataLayer getGuiModel() {
		return guiModel;
	}

	public void setId(String idInput) {
		id = idInput;
		setName(idInput);
	}
	
	protected void updateLabelLocation() {
		this.getNameLabel().setPosition(
				Grid.getModifiedX((int) (positionX + Zoomer.getZoomedValue(nameOffsetX, zoom))), 
				Grid.getModifiedY((int) (positionY + Zoomer.getZoomedValue(nameOffsetY, zoom)))
		);
	}
	
	public void updateOnMoveOrZoom() {
		updateLabelLocation();
	}
	/**
	 * Set X-axis offset for name position
	 * 
	 * @param nameOffsetXInput
	 *            Double value for name X-axis offset
	 */
	public void setNameOffsetX(double nameOffsetXInput) {
		nameOffsetX += Zoomer.getUnzoomedValue(nameOffsetXInput, zoom);
	}

	/**
	 * Set Y-axis offset for name position
	 * 
	 * @param nameOffsetYInput
	 *            Double value for name Y-axis offset
	 */
	public void setNameOffsetY(double nameOffsetYInput) {
		nameOffsetY += Zoomer.getUnzoomedValue(nameOffsetYInput, zoom);
	}
	/**
	 * Get X-axis offset for ...
	 * 
	 * @return Double value for X-axis offset of ...
	 */
	public Double getNameOffsetXObject() {
		return nameOffsetX;
	}

	/**
	 * Moved to PetriNetObject Get Y-axis offset for ...
	 * 
	 * @return Double value for Y-axis offset of ...
	 */
	public Double getNameOffsetYObject() {
		return nameOffsetY;
	}

	/**
	 * Get id returns null if value not yet entered
	 * 
	 * @return String value for id;
	 */
	public String getId() {
		return getName();
	}

	/**
	 * Returns Name Label - is used by GuiView
	 * 
	 * @return PetriNetObject's Name Label (Model View Controller Design
	 *         Pattern)
	 */
	public NameLabel getNameLabel() {
		return pnName;
	}

	public void addLabelToContainer() {
		if (getParent() != null && pnName.getParent() == null) {
			getParent().add(pnName);
		}
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

	public static void ignoreSelection(boolean ignore) {
		ignoreSelection = ignore;
	}

	public boolean isDraggable() {
		return draggable;
	}

	public void setDraggable(boolean allow) {
		draggable = allow;
	}

	public void setObjectColour(Color c) {
		objectColour = c;
	}

	public void setSelectionBorderColour(Color c) {
		selectionBorderColour = c;
	}

	public abstract void addedToGui();

	public void delete() {
		deleted = true;
		if(guiModel != null) {
			guiModel.removePetriNetObject(this);
		}
		removeFromContainer();
		removeAll();
	}

	public void undelete(DrawingSurfaceImpl view) {
		guiModel.addPetriNetObject(this);
		if (view.isCurrentGuiModel(guiModel)) {
			view.add(this);
		}
	}

	protected void removeFromContainer() {
		Container c = getParent();

		if (c != null) {
			c.remove(this);
		}
	}


	public boolean isDeleted() {
		return deleted || markedAsDeleted;
	}

	public void markAsDeleted() {
		markedAsDeleted = true;
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

	@Override
	public PetriNetObject clone() {
		try {
			PetriNetObject pnObjectCopy = (PetriNetObject) super.clone();

			// Remove all mouse listeners on the new object
			EventListener[] mouseListeners = pnObjectCopy
					.getListeners(MouseListener.class);
			for (int i = 0; i < mouseListeners.length; i++) {
				pnObjectCopy.removeMouseListener((MouseListener) mouseListeners[i]);
			}

			mouseListeners = pnObjectCopy.getListeners(MouseMotionListener.class);

			for (int i = 0; i < mouseListeners.length; i++) {
				pnObjectCopy.removeMouseMotionListener((MouseMotionListener) mouseListeners[i]);
			}

			return pnObjectCopy;
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

}
