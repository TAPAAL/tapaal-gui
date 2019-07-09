package dk.aau.cs.gui;

import pipe.gui.GuiFrameActions;

public interface TabContentActions{

    //public interface UndoRedo {
        void undo();
        void redo();
    //}

    void setApp(GuiFrameActions app);

    void zoomOut();
    void zoomIn();

    void selectAll();

    void deleteSelection();
}

