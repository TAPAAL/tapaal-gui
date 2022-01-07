package pipe.gui;

/*
Defines GuiFrameActions that are safe to do, even if the current tab does not have focus.
 */
public interface SafeGuiFrameActions {

    void updatedTabName(PetriNetTab tab);

}
