package pipe.gui;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.BatchProcessingDialog;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.TabContentActions;
import dk.aau.cs.gui.smartDraw.SmartDrawDialog;
import net.tapaal.resourcemanager.ResourceManager;
import dk.aau.cs.model.tapn.simulation.ShortestDelayMode;
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
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class GuiFrameController implements GuiFrameControllerActions{

    final GuiFrame guiFrameDirectAccess; //XXX - while refactoring shold only use guiFrameActions
    final GuiFrameActions guiFrame;

    final MutableReference<TabContentActions> currentTab = new MutableReference<>();

    public GuiFrameController(GuiFrame appGui) {
        super();

        guiFrame = appGui;
        guiFrameDirectAccess = appGui;

        loadPrefrences();
        appGui.registerController(this, currentTab);


    }

    //XXX should be private and should prop. live in controllers not GUI, tmp while refactoring //kyrke 2019-11-05
    boolean showComponents = true;
    boolean showSharedPT = true;
    boolean showConstants = true;
    boolean showQueries = true;
    boolean showEnabledTransitions = true;
    boolean showDelayEnabledTransitions = true;
    private boolean showToolTips = true;
    private boolean showZeroToInfinityIntervals = true;
    private boolean showTokenAge = true;

    private void loadPrefrences() {
        Preferences prefs = Preferences.getInstance();

        QueryDialog.setAdvancedView(prefs.getAdvancedQueryView());
        TabContent.setEditorModelRoot(prefs.getEditorModelRoot());
        TabContent.setSimulatorModelRoot(prefs.getSimulatorModelRoot());

        showComponents = prefs.getShowComponents();
        guiFrame.setShowComponentsSelected(showComponents);

        showQueries = prefs.getShowQueries();
        guiFrame.setShowQueriesSelected(showQueries);

        showConstants = prefs.getShowConstants();
        guiFrame.setShowConstantsSelected(showConstants);

        showEnabledTransitions = prefs.getShowEnabledTransitions();
        guiFrame.setShowEnabledTransitionsSelected(showEnabledTransitions);

        showDelayEnabledTransitions = prefs.getShowDelayEnabledTransitions();
        guiFrame.setShowDelayEnabledTransitionsSelected(showDelayEnabledTransitions);

        DelayEnabledTransitionControl.setDefaultDelayMode(prefs.getDelayEnabledTransitionDelayMode());
        DelayEnabledTransitionControl.setDefaultGranularity(prefs.getDelayEnabledTransitionGranularity());
        SimulationControl.setDefaultIsRandomTransition(prefs.getDelayEnabledTransitionIsRandomTransition());

        showToolTips = prefs.getShowToolTips();
        setDisplayToolTips(showToolTips);
        guiFrame.setShowToolTipsSelected(showToolTips);

        showZeroToInfinityIntervals = prefs.getShowZeroInfIntervals();
        guiFrame.setShowZeroToInfinityIntervalsSelected(showZeroToInfinityIntervals);

        showTokenAge = prefs.getShowTokenAge();
        guiFrame.setShowTokenAgeSelected(showTokenAge);

        guiFrame.setWindowSize(prefs.getWindowSize());

    }



    @Override
    public void openTab(TabContent tab) {
        CreateGui.addTab(tab);
        tab.setSafeGuiFrameActions(guiFrameDirectAccess);
        tab.setGuiFrameControllerActions(this);

        guiFrame.attachTabToGuiFrame(tab);
        guiFrame.changeToTab(tab);
        //XXX fixes an issue where on first open of a net the time intervals are not shown
        tab.drawingSurface().repaintAll();
    }

    //If needed, add boolean forceClose, where net is not checkedForSave and just closed
    //XXX 2018-05-23 kyrke, implementation close to undoAddTab, needs refactoring
    @Override
    public void closeTab(TabContent tab) {
        if(tab != null) {
            boolean closeNet = true;
            if (tab.getNetChanged()) {
                closeNet = showSavePendingChangesDialog(tab);
            }

            if (closeNet) {
                tab.setSafeGuiFrameActions(null);
                tab.setGuiFrameControllerActions(null);
                //Close the gui part first, else we get an error bug #826578
                guiFrame.detachTabFromGuiFrame(tab);
                CreateGui.removeTab(tab);
            }
        }

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

        prefs.setShowComponents(showComponents);
        prefs.setShowQueries(showQueries);
        prefs.setShowConstants(showConstants);

        prefs.setShowEnabledTrasitions(showEnabledTransitions);
        prefs.setShowDelayEnabledTransitions(showDelayEnabledTransitions);
        prefs.setShowTokenAge(guiFrameDirectAccess.showTokenAge());
        prefs.setDelayEnabledTransitionDelayMode(DelayEnabledTransitionControl.getDefaultDelayMode());
        prefs.setDelayEnabledTransitionGranularity(DelayEnabledTransitionControl.getDefaultGranularity());
        prefs.setDelayEnabledTransitionIsRandomTransition(SimulationControl.isRandomTransition());

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
    public void     showAbout() {
        StringBuilder buffer = new StringBuilder("About " + TAPAAL.getProgramName());
        buffer.append("\n\n");
        buffer.append("TAPAAL is a tool for editing, simulation and verification of P/T and timed-arc Petri nets.\n");
        buffer.append("The GUI is based on PIPE2: http://pipe2.sourceforge.net/\n\n");
        buffer.append("License information and more is availabe at: www.tapaal.net\n\n");

        buffer.append("Credits\n\n");
        buffer.append("TAPAAL GUI and Translations:\n");
            buffer.append("Mathias Andersen, Sine V. Birch, Jacob Hjort Bundgaard, Joakim Byg, Jakob Dyhr,\nLouise Foshammer, Malte Neve-Graesboell, ");
            buffer.append("Lasse Jacobsen, Morten Jacobsen,\nThomas S. Jacobsen, Jacob J. Jensen, Peter G. Jensen, ");
            buffer.append("Mads Johannsen,\nKenneth Y. Joergensen, Mikael H. Moeller, Christoffer Moesgaard, Kristian Morsing Pedersen,\nThomas Pedersen, Lena Said, Niels N. Samuelsen, Jiri Srba, Mathias G. Soerensen,\nJakob H. Taankvist and Peter H. Taankvist\n");

            buffer.append("Aalborg University 2008-2021\n\n");

        buffer.append("TAPAAL Continuous Engine (verifytapn):\n");
            buffer.append("Alexandre David, Lasse Jacobsen, Morten Jacobsen and Jiri Srba\n");
            buffer.append("Aalborg University 2011-2021\n\n");

        buffer.append("TAPAAL Discrete Engine (verifydtapn):\n");
            buffer.append("Mathias Andersen, Peter G. Jensen, Heine G. Larsen, Jiri Srba,\n");
            buffer.append("Mathias G. Soerensen and Jakob H. Taankvist\n");
            buffer.append("Aalborg University 2012-2021\n\n");

        buffer.append("TAPAAL Untimed Engine (verifypn):\n");
            buffer.append("Alexander Bilgram, Frederik M. Boenneland, Jakob Dyhr, Peter Fogh, ");
            buffer.append("Jonas F. Jensen,\nLasse S. Jensen, Peter G. Jensen, ");
            buffer.append("Tobias S. Jepsen, Kenneth Y. Joergensen,\nMads Johannsen, Isabella Kaufmann, ");
            buffer.append("Andreas H. Klostergaard, Soeren M. Nielsen,\nThomas S. Nielsen, Lars K. Oestergaard, ");
            buffer.append("Samuel Pastva, Thomas Pedersen, Jiri Srba,\nPeter H. Taankvist, Nikolaj J. Ulrik and Simon M. Virenfeldt\n");
            buffer.append("Aalborg University 2014-2021\n\n");


        buffer.append("\n");
        JOptionPane.showMessageDialog(null, buffer.toString(), "About " + TAPAAL.getProgramName(),
                JOptionPane.INFORMATION_MESSAGE, ResourceManager.appIcon());
    }

    @Override
    public void exit() {
        if (showSavePendingChangesDialogForAllTabs()) {
            guiFrameDirectAccess.dispose();
            System.exit(0);
        }
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
                    String message = e.getMessage();

                    if (message.contains("Exception:")) {
                        message = message.split(":", 2)[1];
                    }
                    JOptionPane.showMessageDialog(CreateGui.getApp(),
                            message,
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
			    while(!worker.isDone()) {
			    	try {
			    		Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
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

                    if(files.length != 0 && !CreateGui.getCurrentTab().currentTemplate().getHasPositionalInfo()) {
                        int dialogResult = JOptionPane.showConfirmDialog (null, "The net does not have any layout information. Would you like to do automatic layout?","Automatic Layout?", JOptionPane.YES_NO_OPTION);
                        if(dialogResult == JOptionPane.YES_OPTION) {
                            SmartDrawDialog.showSmartDrawDialog();
                        }
                    }

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
			    while(!worker.isDone()) {
			    	try {
			    		Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
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

    @Override
    public void save() {
        save(currentTab.get());
    }
    @Override
    public void saveAs(){
        saveAs(currentTab.get());
    }

    @Override
    public void showBatchProcessingDialog() {
        if (showSavePendingChangesDialogForAllTabs()) {
            BatchProcessingDialog.showBatchProcessingDialog(new JList<>(new DefaultListModel<>()));
        }
    }

    private boolean save(TabContentActions tab) {
        File modelFile = tab.getFile();
        boolean result;
        if (modelFile != null ) { // ordinary save
            tab.saveNet(modelFile);
            result = true;
        } else {
            result = saveAs(tab);
        }
        return result;
    }

    private boolean saveAs(TabContentActions tab) {
        boolean result;
        // save as
        String path = tab.getTabTitle();

        String filename = FileBrowser.constructor("Timed-Arc Petri Net", "tapn", path).saveFile(path);
        if (filename != null) {
            File modelFile = new File(filename);
            tab.saveNet(modelFile);
            result = true;
        }else{
            result = false;
        }

        return result;
    }

    /**
     * If current net has modifications, asks if you want to save and does it if
     * you want.
     *
     * @return true if handled, false if cancelled
     */
    private boolean showSavePendingChangesDialog(TabContentActions tab) {
        if(null == tab) return false;

        if (tab.getNetChanged()) {
            //XXX: this cast should not be done, its a quick fix while refactoring //kyrke 2019-12-31
            changeToTab((TabContent) tab);

            int result = JOptionPane.showConfirmDialog(CreateGui.getApp(),
                    "The net has been modified. Save the current net?",
                    "Confirm Save Current File",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            switch (result) {
                case JOptionPane.YES_OPTION:
                    boolean saved = save(tab);
                    return saved;
                case JOptionPane.NO_OPTION:
                    return true;
                case JOptionPane.CLOSED_OPTION:
                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }
        return true;
    }

    /**
     * If current net has modifications, asks if you want to save and does it if
     * you want.
     *
     * @return true if handled, false if cancelled
     */
    private boolean showSavePendingChangesDialogForAllTabs() {
        // Loop through all tabs and check if they have been saved
        for (TabContent tab : CreateGui.getTabs()) {
            if (tab.getNetChanged()) {
                if (!(showSavePendingChangesDialog(tab))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void toggleQueries(){
        setQueries(!showQueries);
    }
    public void setQueries(boolean b){
        showQueries = b;

        guiFrame.setShowQueriesSelected(showQueries);
        //currentTab.ifPresent(o->o.showQueries(showQueries));
        CreateGui.getTabs().forEach(o->o.showQueries(showQueries));

    }

    @Override
    public void toggleConstants(){
        setConstants(!showConstants);
    }

    public void setConstants(boolean b){
        showConstants = b;

        guiFrame.setShowConstantsSelected(showConstants);
        //currentTab.ifPresent(o->o.showConstantsPanel(showConstants));
        CreateGui.getTabs().forEach(o->o.showConstantsPanel(showConstants));

    }

    @Override
    public void toggleTokenAge(){
        setTokenAge(!showTokenAge);
    }
    public void setTokenAge(boolean b){
        showTokenAge = b;

        Preferences.getInstance().setShowTokenAge(showTokenAge);

        guiFrame.setShowTokenAgeSelected(showTokenAge);
        currentTab.ifPresent(TabContentActions::repaintAll);

    }

    @Override
    public void toggleZeroToInfinityIntervals() {
        setZeroToInfinityIntervals(!showZeroToInfinityIntervals);
    }
    public void setZeroToInfinityIntervals(boolean b) {
        showZeroToInfinityIntervals = b;

        guiFrame.setShowZeroToInfinityIntervalsSelected(showZeroToInfinityIntervals);

        Preferences.getInstance().setShowZeroInfIntervals(showZeroToInfinityIntervals);
        currentTab.ifPresent(TabContentActions::repaintAll);
    }

    @Override
    public void toggleComponents(){
        setComponents(!showComponents);
    }

    public void setComponents(boolean b){
        showComponents = b;

        guiFrame.setShowComponentsSelected(showComponents);
        //currentTab.ifPresent(o->o.showComponents(showComponents));
        CreateGui.getTabs().forEach(o->o.showComponents(showComponents));
    }

    @Override
    public void toggleSharedPT(){
        setSharedPT(!showSharedPT);
    }

    public void setSharedPT(boolean b){
        showSharedPT = b;

        guiFrame.setShowSharedPTSelected(showSharedPT);
        CreateGui.getTabs().forEach(o->o.showSharedPT(showSharedPT));
    }

    @Override
    public void toggleEnabledTransitionsList(){
        setEnabledTransitionsList(!showEnabledTransitions);
    }
    private void setEnabledTransitionsList(boolean b){
        showEnabledTransitions = b;
        guiFrame.setShowEnabledTransitionsSelected(b);
        currentTab.ifPresent(o->o.showEnabledTransitionsList(b));
    }

    @Override
    public void toggleDelayEnabledTransitions(){
        setDelayEnabledTransitions(!showDelayEnabledTransitions);
    }

    private void setDelayEnabledTransitions(boolean b) {
        showDelayEnabledTransitions = b;
        guiFrame.setShowDelayEnabledTransitionsSelected(b);
        currentTab.ifPresent(o->o.showDelayEnabledTransitions(b));
    }

    @Override
    public void toggleDisplayToolTips() {
        showToolTips = !showToolTips;
        setDisplayToolTips(showToolTips);
    }

    private void setDisplayToolTips(boolean b) {
        guiFrame.setShowToolTipsSelected(b);

        Preferences.getInstance().setShowToolTips(b);

        ToolTipManager.sharedInstance().setEnabled(b);
        ToolTipManager.sharedInstance().setInitialDelay(400);
        ToolTipManager.sharedInstance().setReshowDelay(800);
        ToolTipManager.sharedInstance().setDismissDelay(60000);
    }

    @Override
    public void showAdvancedWorkspace() {
        showAdvancedWorkspace(true);
    }

    @Override
    public void showSimpleWorkspace() {
        showAdvancedWorkspace(false);
    }
    private void showAdvancedWorkspace(boolean advanced){
        QueryDialog.setAdvancedView(advanced);
        setComponents(advanced);
        setSharedPT(advanced);
        setConstants(advanced);

        //Queries and enabled transitions should always be shown
        setQueries(true);
        setEnabledTransitionsList(true);
        setDisplayToolTips(true);

        currentTab.ifPresent(TabContentActions::setResizeingDefault);

        if (advanced) {

            setZeroToInfinityIntervals(true);
            setTokenAge(true);

        } else {
            setZeroToInfinityIntervals(false);
            setTokenAge(false);
        }

        //Delay-enabled Transitions
        //showDelayEnabledTransitions(advanced);
        DelayEnabledTransitionControl.getInstance().setValue(new BigDecimal("0.1"));
        DelayEnabledTransitionControl.getInstance().setDelayMode(ShortestDelayMode.getInstance());
        SimulationControl.getInstance().setRandomTransitionMode(false);
    }

}
