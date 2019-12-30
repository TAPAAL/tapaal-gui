package pipe.gui;

import dk.aau.cs.gui.TabContent;

/*
Defines GuiFrameActions that are safe to do, even if the current tab does not have focus.
 */
public interface SafeGuiFrameActions {

    void updatedTabName(TabContent tab);

}
