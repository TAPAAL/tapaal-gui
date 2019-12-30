package pipe.gui;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.TabContentActions;
import dk.aau.cs.io.ResourceManager;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;
import net.tapaal.Preferences;
import net.tapaal.TAPAAL;
import net.tapaal.helpers.Reference.MutableReference;
import pipe.gui.widgets.EngineDialogPanel;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.NewTAPNPanel;
import pipe.gui.widgets.QueryDialog;
import pipe.gui.widgets.filebrowser.FileBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

class GuiFrameController implements GuiFrameControllerActions{

    GuiFrame guiFrameDirectAccess; //XXX - while refactoring shold only use guiFrameActions
    GuiFrameActions guiFrame;

    GuiFrameController(GuiFrame appGui) {
        super();

        guiFrame = appGui;
        guiFrameDirectAccess = appGui;

        appGui.registerController(this, currentTab);


    }

    final MutableReference<TabContentActions> currentTab = new MutableReference<>();

    @Override
    public void openTab(TabContent tab) {
        CreateGui.addTab(tab);
        tab.setSafeGuiFrameActions(guiFrameDirectAccess);

        guiFrame.attachTabToGuiFrame(tab);
        guiFrame.changeToTab(tab);

    }

    //TODO: 2018-05-07 //kyrke Create CloseTab function, used to close a tab
    //XXX: Temp solution to call getCurrentTab to get new new selected tab (should use index) --kyrke 2019-07-08
    @Override
    public void changeToTab(TabContent tab) {

        //De-register old model
        currentTab.ifPresent(t -> t.setApp(null));

        //Set current tab
        currentTab.setReference(tab);

        guiFrame.changeToTab(tab);

        currentTab.ifPresent(t -> t.setApp(guiFrame));
        guiFrameDirectAccess.setTitle(currentTab.map(TabContentActions::getTabTitle).orElse(null));
    }

    @Override
    public void clearPreferences() {
        // Clear persistent storage
        Preferences.getInstance().clear();
        // Engines reset individually to remove preferences for already setup engines
        Verifyta.reset();
        VerifyTAPN.reset();
        VerifyTAPNDiscreteVerification.reset();
    }

    @Override
    public void showEngineDialog() {
        new EngineDialogPanel().showDialog();
    }

    //XXX: should properly not have address as argument, make one function per page
    @Override
    public void openURL(String address) {
        showInBrowser(address);
    }

    @Override
    public void showNewPNDialog() {

        // Build interface
        EscapableDialog guiDialog = new EscapableDialog(guiFrameDirectAccess, "Create a New Petri Net", true);

        Container contentPane = guiDialog.getContentPane();

        // 1 Set layout
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

        // 2 Add Place editor
        contentPane.add(new NewTAPNPanel(guiDialog.getRootPane(), guiFrameDirectAccess));

        guiDialog.setResizable(false);

        // Make window fit contents' preferred size
        guiDialog.pack();

        // Move window to the middle of the screen
        guiDialog.setLocationRelativeTo(null);
        guiDialog.setVisible(true);


    }

    @Override
    public void saveWorkspace() {

        Preferences prefs = Preferences.getInstance();

        prefs.setAdvancedQueryView(QueryDialog.getAdvancedView());
        prefs.setEditorModelRoot(TabContent.getEditorModelRoot());
        prefs.setSimulatorModelRoot(TabContent.getSimulatorModelRoot());
        prefs.setWindowSize(guiFrameDirectAccess.getSize());

        prefs.setShowComponents(guiFrameDirectAccess.showComponents);
        prefs.setShowQueries(guiFrameDirectAccess.showQueries);
        prefs.setShowConstants(guiFrameDirectAccess.showConstants);

        prefs.setShowEnabledTrasitions(guiFrameDirectAccess.showEnabledTransitions);
        prefs.setShowDelayEnabledTransitions(guiFrameDirectAccess.showDelayEnabledTransitions);
        prefs.setShowTokenAge(guiFrameDirectAccess.showTokenAge());
        prefs.setDelayEnabledTransitionDelayMode(DelayEnabledTransitionControl.getDefaultDelayMode());
        prefs.setDelayEnabledTransitionGranularity(DelayEnabledTransitionControl.getDefaultGranularity());
        prefs.setDelayEnabledTransitionIsRandomTransition(DelayEnabledTransitionControl.isRandomTransition());

        JOptionPane.showMessageDialog(guiFrameDirectAccess,
                "The workspace has now been saved into your preferences.\n"
                        + "It will be used as the initial workspace next time you run the tool.",
                "Workspace Saved", JOptionPane.INFORMATION_MESSAGE);

    }

    @Override
    public void checkForUpdate() {
        checkForUpdate(true);
    }

    @Override
    public void showAbout() {
        StringBuilder buffer = new StringBuilder("About " + TAPAAL.getProgramName());
        buffer.append("\n\n");
        buffer.append("TAPAAL is a tool for editing, simulation and verification of P/T and timed-arc Petri nets.\n");
        buffer.append("The GUI is based on PIPE2: http://pipe2.sourceforge.net/\n\n");
        buffer.append("License information and more is availabe at: www.tapaal.net\n\n");

        buffer.append("Credits\n\n");
        buffer.append("TAPAAL GUI and Translations:\n");
            buffer.append("Mathias Andersen, Sine V. Birch, Jacob Hjort Bundgaard, Joakim Byg, Jakob Dyhr,\nLouise Foshammer, Malte Neve-Graesboell, ");
            buffer.append("Lasse Jacobsen, Morten Jacobsen,\nThomas S. Jacobsen, Jacob J. Jensen, Peter G. Jensen, ");
            buffer.append("Mads Johannsen,\nKenneth Y. Joergensen, Mikael H. Moeller, Christoffer Moesgaard, Niels N. Samuelsen,\nJiri Srba, Mathias G. Soerensen, Jakob H. Taankvist and Peter H. Taankvist\n");
            buffer.append("Aalborg University 2009-2019\n\n");

        buffer.append("TAPAAL Continuous Engine (verifytapn):\n");
            buffer.append("Alexandre David, Lasse Jacobsen, Morten Jacobsen and Jiri Srba\n");
            buffer.append("Aalborg University 2011-2019\n\n");

        buffer.append("TAPAAL Discrete Engine (verifydtapn):\n");
            buffer.append("Mathias Andersen, Peter G. Jensen, Heine G. Larsen, Jiri Srba,\n");
            buffer.append("Mathias G. Soerensen and Jakob H. Taankvist\n");
            buffer.append("Aalborg University 2012-2019\n\n");

        buffer.append("TAPAAL Untimed Engine (verifypn):\n");
            buffer.append("Frederik Meyer Boenneland, Jakob Dyhr, Peter Fogh, ");
            buffer.append("Jonas F. Jensen,\nLasse S. Jensen, Peter G. Jensen, ");
            buffer.append("Tobias S. Jepsen, Mads Johannsen,\nIsabella Kaufmann, ");
            buffer.append("Andreas H. Klostergaard, Soeren M. Nielsen,\nThomas S. Nielsen, Lars K. Oestergaard, ");
            buffer.append("Samuel Pastva and Jiri Srba\n");
            buffer.append("Aalborg University 2014-2019\n\n");


        buffer.append("\n");
        JOptionPane.showMessageDialog(null, buffer.toString(), "About " + TAPAAL.getProgramName(),
                JOptionPane.INFORMATION_MESSAGE, ResourceManager.appIcon());
    }

    @Override
    public void exit() {
        //XXX TODO: uses direct exit for now, untill safe is moved to controller, temp while refactoring //kyrke 2019-11-10
        guiFrameDirectAccess.exit();
    }

    @Override
    public void openTAPNFile() {
        final File[] files = FileBrowser.constructor("Timed-Arc Petri Net","tapn", "xml", FileBrowser.userPath).openFiles();
        //show loading cursor
        CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Do loading
        SwingWorker<java.util.List<TabContent>, Void> worker = new SwingWorker<java.util.List<TabContent>, Void>() {
            @Override
            protected java.util.List<TabContent> doInBackground() throws InterruptedException, Exception, FileNotFoundException {
                java.util.List<TabContent> filesOpened = new ArrayList<>();
                for(File f : files){
                    if(f.exists() && f.isFile() && f.canRead()){
                        FileBrowser.userPath = f.getParent();
                        filesOpened.add(TabContent.createNewTabFromFile(f));
                    }
                }
                return filesOpened;
            }
            @Override
            protected void done() {
                try {
                    List<TabContent> tabs = get();
                    openTab(tabs);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CreateGui.getApp(),
                            e.getMessage(),
                            "Error loading file",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }finally {
                    CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };
        worker.execute();

        //Sleep redrawing thread (EDT) until worker is done
        //This enables the EDT to schedule the many redraws called in createNewTabFromPNMLFile(f); much better
			    /*while(!worker.isDone()) {
			    	try {
			    		Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }*/
    }

    @Override
    public void importPNMLFile() {
        final File[] files = FileBrowser.constructor("Import PNML", "pnml", FileBrowser.userPath).openFiles();

        //Show loading cursor
        CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Do loading of net
        SwingWorker<List<TabContent>, Void> worker = new SwingWorker<List<TabContent>, Void>() {
            @Override
            protected List<TabContent> doInBackground() throws InterruptedException, Exception {
                List<TabContent> fileOpened = new ArrayList<>();
                for(File f : files){
                    if(f.exists() && f.isFile() && f.canRead()){
                        FileBrowser.userPath = f.getParent();
                        fileOpened.add(TabContent.createNewTabFromPNMLFile(f));
                    }
                }
                return fileOpened;
            }
            @Override
            protected void done() {
                try {
                    List<TabContent> tabs = get();
                    openTab(tabs);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CreateGui.getApp(),
                            e.getMessage(),
                            "Error loading file",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }finally {
                    CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };
        worker.execute();

        //Sleep redrawing thread (EDT) until worker is done
        //This enables the EDT to schedule the many redraws called in createNewTabFromPNMLFile(f); much better
			    /*while(!worker.isDone()) {
			    	try {
			    		Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }*/
    }

    //XXX 2018-05-23 kyrke, moved from CreateGui, static method
    //Needs further refactoring to seperate conserns
    public void checkForUpdate(boolean forcecheck) {
        final VersionChecker versionChecker = new VersionChecker();
        if (versionChecker.checkForNewVersion(forcecheck))  {
            StringBuilder message = new StringBuilder("There is a new version of TAPAAL available at www.tapaal.net.");
            message.append("\n\nCurrent version: ");
            message.append(TAPAAL.VERSION);
            message.append("\nNew version: ");
            message.append(versionChecker.getNewVersionNumber());
            String changelog = versionChecker.getChangelog();
            if (!changelog.equals("")){
                message.append('\n');
                message.append('\n');
                message.append("Changelog:");
                message.append('\n');
                message.append(changelog);
            }
            JOptionPane optionPane = new JOptionPane();
            optionPane.setMessage(message.toString());
            optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
            JButton updateButton, laterButton, ignoreButton;
            updateButton = new JButton("Update now");
            updateButton.setMnemonic(KeyEvent.VK_C);
            optionPane.add(updateButton);
            laterButton = new JButton("Update later");
            laterButton.setMnemonic(KeyEvent.VK_C);
            optionPane.add(laterButton);
            ignoreButton = new JButton("Ignore this update");
            laterButton.setMnemonic(KeyEvent.VK_C);
            optionPane.add(ignoreButton);

            optionPane.setOptions(new Object[] {updateButton, laterButton, ignoreButton});


            final JDialog dialog = optionPane.createDialog(null, "New Version of TAPAAL");
            laterButton.addActionListener(e -> {
                Preferences.getInstance().setLatestVersion(null);
                dialog.setVisible(false);
                dialog.dispose ();
            });
            updateButton.addActionListener(e -> {
                Preferences.getInstance().setLatestVersion(null);
                dialog.setVisible(false);
                dialog.dispose();
                GuiFrameController.showInBrowserDeprecatedDirectCall("http://www.tapaal.net/download");
            });
            ignoreButton.addActionListener(e -> {
                Preferences.getInstance().setLatestVersion(versionChecker.getNewVersionNumber());
                dialog.setVisible(false);
                dialog.dispose ();
            });

            updateButton.requestFocusInWindow();
            dialog.getRootPane().setDefaultButton(updateButton);
            dialog.setVisible(true);
        }
    }

    private static void openBrowser(URI url){
        //open the default bowser on this page
        try {
            java.awt.Desktop.getDesktop().browse(url);
        } catch (IOException e) {
            Logger.log("Cannot open the browser.");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There was a problem opening the default web browser \n" +
                            "Please open the url in your browser by entering " + url.toString(),
                    "Error opening browser", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    //XXX should be private, but for now used in action not yet moved to controller
    @Deprecated
    public static void showInBrowserDeprecatedDirectCall(String address) {
        showInBrowser(address);
    }
    private static void showInBrowser(String address) {
        try {
            URI url = new URI(address);
            openBrowser(url);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            Logger.log("Error convering to URL");
            e.printStackTrace();
        }
    }


}
