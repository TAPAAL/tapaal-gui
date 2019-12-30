package pipe.gui;

import dk.aau.cs.gui.TabContent;

public interface GuiFrameControllerActions {
    void openTab(TabContent tab);
    default void openTab(Iterable<TabContent> tabs) {
        tabs.forEach(this::openTab);
    }

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
}
