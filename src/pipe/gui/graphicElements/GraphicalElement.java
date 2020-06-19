package pipe.gui.graphicElements;

import net.tapaal.gui.DrawingSurfaceManager.AbstractDrawingSurfaceManager;
import net.tapaal.helpers.Reference.Reference;
import pipe.gui.Translatable;
import pipe.gui.Zoomable;

import javax.swing.*;
import java.awt.*;

public abstract class GraphicalElement extends JComponent implements Zoomable, Translatable {


    protected Reference<AbstractDrawingSurfaceManager> managerRef = null;

    /*private*/ public GraphicalElement(){

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
    }

    public abstract int getLayerOffset();

    public Reference<AbstractDrawingSurfaceManager> getManagerRef() {
        return managerRef;
    }

    public void setManagerRef(Reference<AbstractDrawingSurfaceManager> manager) {
        this.managerRef = manager;
    }

    public abstract void addedToGui();

    public abstract void removedFromGui();

    /*public abstract static class GraphicalNode extends GraphicalElement implements Translatable{
        protected GraphicalNode() {
            super();

        }
    }

    public abstract static class GraphicalVertex extends GraphicalElement {
        protected GraphicalVertex() {
            super();

        }
    }*/

}
