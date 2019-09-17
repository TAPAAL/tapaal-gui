package pipe.gui.graphicElements;

import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;

public abstract class PetriNetObjectWithLabel extends PetriNetObject {


    /* Name Label for displaying name */
    private NameLabel pnName = new NameLabel(Pipe.ZOOM_DEFAULT);
    /** X/Y-axis Position on screen */
    private double nameOffsetX;
    private double nameOffsetY;

    PetriNetObjectWithLabel(int nameOffsetX, int nameOffsetY) {
        super();

        this.nameOffsetX = nameOffsetX;
        this.nameOffsetY = nameOffsetY;

        pnName.setPosition(nameOffsetX, nameOffsetY);

    }

    protected void updateLabelLocation() {
        this.getNameLabel().setPosition(
                Grid.getModifiedX((int) (positionX + Zoomer.getZoomedValue(nameOffsetX, getZoom()))),
                Grid.getModifiedY((int) (positionY + Zoomer.getZoomedValue(nameOffsetY, getZoom())))
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
        nameOffsetX += Zoomer.getUnzoomedValue(nameOffsetXInput, getZoom());
    }

    /**
     * Set Y-axis offset for name position
     *
     * @param nameOffsetYInput
     *            Double value for name Y-axis offset
     */
    public void setNameOffsetY(double nameOffsetYInput) {
        nameOffsetY += Zoomer.getUnzoomedValue(nameOffsetYInput, getZoom());
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

    //XXX pushed up from PlaceTransitionObject while refactorings, dublicates getNameOffsetXObject? //kyrke 2019-09-17
    public double getNameOffsetX() {
        return nameOffsetX;
    }
    public double getNameOffsetY() {
        return nameOffsetY;
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
    public void removeLabelFromContainer() {
        if (getParent() != null && pnName != null) {
            getParent().remove(pnName);
        }
    }

    public void select(boolean shouldRepaint) {
        if (selectable && !selected) {
            selected = true;

            if (pnName != null) {
                pnName.setForeground(Pipe.SELECTION_LINE_COLOUR);
            }

            if (shouldRepaint) {
                repaint();
            }
        }

    }

    public void deselect() {
        if (selected) {
            selected = false;

            if (pnName != null) {
                pnName.setForeground(Pipe.ELEMENT_LINE_COLOUR);
            }

            repaint();
        }
    }


    @Override
    public void zoomUpdate(int zoom) {
        super.zoomUpdate(zoom);
        if (pnName != null) {
            pnName.zoomUpdate(zoom);
        }
    }
}
