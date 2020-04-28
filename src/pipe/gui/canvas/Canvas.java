package pipe.gui.canvas;

import pipe.gui.graphicElements.PetriNetObject;

public interface Canvas {


    void addNewPetriNetObject(PetriNetObject newObject);

    //XXX temp solution while refactorting, component removes children them self
    //migth not be best solution long term.
    void removePetriNetObject(PetriNetObject pno);
}
