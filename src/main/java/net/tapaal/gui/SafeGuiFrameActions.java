package net.tapaal.gui;

import pipe.gui.petrinet.PetriNetTab;

/*
Defines GuiFrameActions that are safe to do, even if the current tab does not have focus.
 */
public interface SafeGuiFrameActions {

    void updatedTabName(PetriNetTab tab);

}
