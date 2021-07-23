package dk.aau.cs.gui;

import pipe.gui.GuiFrame;
import pipe.gui.GuiFrameActions;
import pipe.gui.Pipe;
import pipe.gui.SafeGuiFrameActions;
import pipe.gui.graphicElements.PetriNetObject;

import java.io.File;
import java.util.Map;

public interface TabContentActions {

    //public interface UndoRedo {
    void undo();

    void redo();
    //}

    void setApp(GuiFrameActions app);

    void setSafeGuiFrameActions(SafeGuiFrameActions ref);

    void zoomOut();

    void zoomIn();

    void selectAll();

    void deleteSelection();

    //public interface Animation {}
    void stepBackwards();

    void stepForward();

    void timeDelay();

    void delayAndFire();

    boolean getNetChanged();

    void toggleAnimationMode();

    void setMode(Pipe.ElementType mode);

    void showStatistics();

    void importSUMOQueries();

    void importXMLQueries();

    void workflowAnalyse();

    void mergeNetComponents();

    File getFile();

    void verifySelectedQuery();

    void previousComponent();

    void nextComponent();

    void exportTrace();

    void importTrace();

    void zoomTo(int newZoomLevel);

    String getTabTitle();

    void saveNet(File outFile);

    void increaseSpacing();

    void decreaseSpacing();

    void showQueries(boolean showDelayEnabledTransitions);

    void repaintAll();

    void showConstantsPanel(boolean showDelayEnabledTransitions);

    void showComponents(boolean showDelayEnabledTransitions);

    void showSharedPT(boolean showSharedPT);

    void showEnabledTransitionsList(boolean showDelayEnabledTransitions);

    void showDelayEnabledTransitions(boolean showDelayEnabledTransitions);

    void setResizeingDefault();

    void updateEnabledActions(GuiFrame.GUIMode mode);

    void changeTimeFeature(boolean isTime);

    void changeGameFeature(boolean isGame);

    Map<PetriNetObject, Boolean> showNames(boolean isVisible, boolean placeNames, boolean selectedComponent);

    void showChangeNameVisibility();


}
