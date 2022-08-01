package net.tapaal.gui;

import net.tapaal.helpers.Reference.Reference;
import pipe.gui.GuiFrame;
import pipe.gui.petrinet.action.GuiAction;
import pipe.gui.petrinet.PetriNetTab;

import java.awt.*;
import java.util.List;

/**
 * Used to delegate control of the state of AppGUI to tabs
 * control accessible functions -- kyrke - 2019-07-08
 */
public interface GuiFrameActions {

    void updateZoomCombo(int zoom);

    void setRedoActionEnabled(boolean b);

    void setUndoActionEnabled(boolean b);

    void setWindowSize(Dimension dimension);

    void attachTabToGuiFrame(PetriNetTab tab);

    void detachTabFromGuiFrame(PetriNetTab tab);

    void setGUIMode(GuiFrame.GUIMode animation);

    void registerDrawingActions(List<GuiAction> drawActions);

    void registerAnimationActions(List<GuiAction> animationActions);

    void registerViewActions(List<GuiAction> viewActions);

    void registerToolsActions(List<GuiAction> toolsActions);

    void setStatusBarText(String s);

    void registerController(GuiFrameControllerActions guiFrameController, Reference<TabActions> currentTab);

    void changeToTab(PetriNetTab tab);

    //Actions for controller
    void setShowComponentsSelected(boolean b);
    void setShowSharedPTSelected(boolean b);
    void setShowConstantsSelected(boolean b);
    void setShowColoredTokensSelected(boolean b);
    void setShowQueriesSelected(boolean b);
    void setShowEnabledTransitionsSelected(boolean b);
    void setShowDelayEnabledTransitionsSelected(boolean b);
    void setShowToolTipsSelected(boolean b);

    void setShowZeroToInfinityIntervalsSelected(boolean b);
    void setShowTokenAgeSelected(boolean b);

    void setFeatureInfoText(boolean[] features);

    void setTitle(String title);
}
