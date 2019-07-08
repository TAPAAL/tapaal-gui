package pipe.gui;

/**
 * Used to delegate control of the state of AppGUI to tabs
 * control accessible functions -- kyrke - 2019-07-08
 */
public interface GuiFrameActions {

    void updateZoomCombo();

    void setRedoActionEnabled(boolean b);

    void setUndoActionEnabled(boolean b);
}
