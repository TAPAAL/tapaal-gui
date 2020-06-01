package pipe.gui;

import dk.aau.cs.gui.TabContent;

public interface GuiFrameControllerActions {
    void openTab(TabContent tab);
    default void openTab(Iterable<TabContent> tabs) {
        tabs.forEach(this::openTab);
    }

    //If needed, add boolean forceClose, where net is not checkedForSave and just closed
    //XXX 2018-05-23 kyrke, implementation close to undoAddTab, needs refactoring
    void closeTab(TabContent tab);

    //TODO: 2018-05-07 //kyrke Create CloseTab function, used to close a tab
    //XXX: Temp solution to call getCurrentTab to get new new selected tab (should use index) --kyrke 2019-07-08
    void changeToTab(TabContent tab);

    void clearPreferences();

    void showEngineDialog();

    void openURL(String s);

    void showNewPNDialog();

    void saveWorkspace();

    void checkForUpdate();

    void showAbout();

    void exit();

    void openTAPNFile();

    void importPNMLFile();

    void save();

    void saveAs();

    void showBatchProcessingDialog();
    void toggleQueries();
    void toggleConstants();
    void toggleTokenAge();
    void toggleZeroToInfinityIntervals();
    void toggleComponents();
    void toggleSharedPT();
    void toggleEnabledTransitionsList();
    void toggleDelayEnabledTransitions();
    void toggleDisplayToolTips();

    void showAdvancedWorkspace();

    void showSimpleWorkspace();
}
