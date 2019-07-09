package dk.aau.cs.gui;

import pipe.gui.GuiFrameActions;

public interface TabContentActions{

    //public interface UndoRedo {
        void undo();
        void redo();
    //}

    void setApp(GuiFrameActions app);

}
