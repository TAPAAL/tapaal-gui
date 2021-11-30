package pipe.gui.graphicElements;

import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.handler.LabelHandler;
import javax.swing.*;

public abstract class PetriNetObjectWithLabel extends PetriNetObject {


    /* Name Label for displaying name */
    protected NameLabel pnName = new NameLabel();
    /** X/Y-axis Position on screen */
    private int nameOffsetX;
    private int nameOffsetY;

    PetriNetObjectWithLabel(String idInput, int positionXInput, int positionYInput, int nameOffsetX, int nameOffsetY) {
        super(idInput, positionXInput, positionYInput);

        this.nameOffsetX = nameOffsetX;
        this.nameOffsetY = nameOffsetY;

        pnName.setPosition(nameOffsetX, nameOffsetY);
        pnName.setForeground(Pipe.ELEMENT_LINE_COLOUR);

        //See note in function
        setLabelHandler();
    }

    protected void setLabelHandler() {

        //XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
        //XXX: handler is called. Make static constructor and add handler from there, to make it safe.
        LabelHandler labelHandler = new LabelHandler(this.getNameLabel(), this);

        getNameLabel().addMouseListener(labelHandler);
        getNameLabel().addMouseMotionListener(labelHandler);
        getNameLabel().addMouseWheelListener(labelHandler);

    }

    protected void updateLabelLocation(boolean alignToGrid) {
        if(alignToGrid) {
            this.getNameLabel().setPosition(
                    Grid.getModifiedX(positionX + Zoomer.getZoomedValue(nameOffsetX, getZoom())),
                    Grid.getModifiedY(positionY + Zoomer.getZoomedValue(nameOffsetY, getZoom()))
            );
        } else {
            this.getNameLabel().setPosition(
                positionX + Zoomer.getZoomedValue(nameOffsetX, getZoom()),
                positionY + Zoomer.getZoomedValue(nameOffsetY, getZoom())
            );
        }
    }

    public void updateOnMoveOrZoom() {
        updateLabelLocation(true);
    }

    @Override
    public void addedToGui() {
        addLabelToContainer();
    }

    @Override
    public void removedFromGui() {
        removeLabelFromContainer();
    }

    public void setNameOffsetX(int nameOffsetXInput) {
        nameOffsetX = nameOffsetXInput;
    }
    public void setNameOffsetY(int nameOffsetYInput) {
        nameOffsetY = nameOffsetYInput;
    }

    public void translateNameOffsetX(int nameOffsetXInput) {
        nameOffsetX += Zoomer.getUnzoomedValue(nameOffsetXInput, getZoom());

    }

    public void translateNameOffsetY(int nameOffsetYInput) {
        nameOffsetY += Zoomer.getUnzoomedValue(nameOffsetYInput, getZoom());
    }

    //XXX pushed up from PlaceTransitionObject while refactorings, dublicates getNameOffsetXObject? //kyrke 2019-09-17
    public int getNameOffsetX() {
        return nameOffsetX;
    }
    public int getNameOffsetY() {
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
            getParent().setLayer(pnName, JLayeredPane.DEFAULT_LAYER + pnName.getLayerOffset());
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

    @Override
    public void translate(int x, int y) {
        super.translate(x, y);
    }
}
