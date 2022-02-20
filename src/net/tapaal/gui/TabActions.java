package net.tapaal.gui;

import pipe.gui.GuiFrame;
import pipe.gui.petrinet.graphicElements.PetriNetObject;
import pipe.gui.petrinet.PetriNetTab;

import java.io.File;
import java.util.Map;

public interface TabActions {

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

    boolean getNetChanged();

    void toggleAnimationMode();

    void setMode(PetriNetTab.DrawTool mode);

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

    void changeColorFeature(boolean isColor);

    void exportPNG();

    void exportPS();

    void exportTIKZ();

    void exportPNML();

    void exportQueryXML();

    Map<PetriNetObject, Boolean> showNames(boolean isVisible, boolean placeNames, boolean selectedComponent);

    void showChangeNameVisibility();

    void alignToGrid();
}
