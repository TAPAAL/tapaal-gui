package pipe.gui.canvas;

import java.awt.Component;

import pipe.gui.petrinet.graphicElements.GraphicalElement;

public interface Canvas {


    void addNewPetriNetObject(GraphicalElement newObject);

    //XXX temp solution while refactorting, component removes children them self
    //migth not be best solution long term.
    void removePetriNetObject(GraphicalElement pno);

    void scrollToCenter(Component component);
}
