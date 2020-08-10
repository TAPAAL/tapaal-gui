package pipe.gui.canvas;

import pipe.gui.graphicElements.GraphicalElement;

/**
 * ProtypeCanvas is used by controllers to draw objects that are not (yet) committed to the medel
 * eg. arcs being drawn. When changing controllers the state can removed by calling clearAllPrototypes.
 */
public interface PrototypeCanvas {

    void addPrototype(GraphicalElement pno);
    void removePrototype(GraphicalElement pno);

    void clearAllPrototype();

}
