package pipe.gui;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.BatchProcessingDialog;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.TabContentActions;
import dk.aau.cs.gui.smartDraw.SmartDrawDialog;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.ModelLoader;
import dk.aau.cs.io.PNMLoader;
import dk.aau.cs.util.JavaUtil;
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
import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class GuiFrameController implements GuiFrameControllerActions{

    public static final String WARNING_OLD_JAVAVERSION = "You are using an older version of Java than 11. Some of the functionalities may not work or display correctly.";

    final GuiFrame guiFrameDirectAccess; //XXX - while refactoring shold only use guiFrameActions
    final GuiFrameActions guiFrame;

    final MutableReference<TabContentActions> activeTab = new MutableReference<>();

    public GuiFrameController(GuiFrame appGui) {
        super();

        guiFrame = appGui;
        guiFrameDirectAccess = appGui;

        appGui.registerController(this, activeTab);

        loadPrefrences();
        loadAndRegisterExampleNets();
        checkJavaVersion();

    }


    private void checkJavaVersion() {
        int version = JavaUtil.getJREMajorVersion();

        if (version < TAPAAL.MINIMUM_SUPPORTED_JAVAVERSION) {
            JOptionPane.showMessageDialog(CreateGui.getRootFrame(), WARNING_OLD_JAVAVERSION);
            System.out.println(WARNING_OLD_JAVAVERSION);
        }
    }

    private void loadAndRegisterExampleNets() {
        var testNets = loadTestNets();
        guiFrame.registerExampleNets(Arrays.asList(testNets));
    }
    /**
     * The function loads the example nets as InputStream from the resources
     * Notice the check for if we are inside a jar file, as files inside a jar cant
     * be listed in the normal way.
     *
     * @author Kenneth Yrke Joergensen <kenneth@yrke.dk>, 2011-06-27
     */
    private String[] loadTestNets() {

        String[] nets = null;

        try {
            URL dirURL = Thread.currentThread().getContextClassLoader().getResource("resources/Example nets/");
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                /* A file path: easy enough */
                nets = new File(dirURL.toURI()).list();
            }

            if (dirURL == null) {
                /*
                 * In case of a jar file, we can't actually find a directory. Have to assume the
                 * same jar as clazz.
                 */
                String me = "TAPAAL.class";
                dirURL = Thread.currentThread().getContextClassLoader().getResource(me);
            }

            if (dirURL.getProtocol().equals("jar")) {
                /* A JAR path */
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf('!')); // strip out only the JAR
                // file
                JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));
                Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
                Set<String> result = new HashSet<String>(); // avoid duplicates in case it is a subdirectory
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith("resources/Example nets/")) { // filter according to the path
                        String entry = name.substring("resources/Example nets/".length());
                        int checkSubdir = entry.indexOf('/');
                        if (checkSubdir >= 0) {
                            // if it is a subdirectory, we just return the directory name
                            entry = entry.substring(0, checkSubdir);
                        }
                        result.add(entry);
                    }
                }
                nets = result.toArray(new String[result.size()]);
                jar.close();
            }

            Arrays.sort(nets, (one, two) -> {

                int toReturn = one.compareTo(two);
                // Special hack to get intro-example first and game-example last
                if (one.equals("intro-example.tapn")) {
                    toReturn = -1;
                } else if (one.equals("game-harddisk.tapn")) {
                    toReturn = 1;
                }
                if (two.equals("intro-example.tapn")) {
                    toReturn = 1;
                } else if (two.equals("game-harddisk.tapn")) {
                    toReturn = -1;
                }
                return toReturn;
            });
        } catch (Exception e) {
            Logger.log("Error getting example files:" + e);
            e.printStackTrace();
        }
        return nets;
    }

    //XXX should be private //kyrke 2019-11-05
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
        activeTab.ifPresent(t -> t.setApp(null));

        //Set current tab
        activeTab.setReference(tab);

        guiFrame.changeToTab(tab);

        activeTab.ifPresent(t -> t.setApp(guiFrame));
        guiFrameDirectAccess.setTitle(activeTab.map(TabContentActions::getTabTitle).orElse(null));

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
            buffer.append("Mads Johannsen,\nKenneth Y. Joergensen, Mikael H. Moeller, Christoffer Moesgaard, Thomas Pedersen,\nLena Said, Niels N. Samuelsen, Jiri Srba, Mathias G. Soerensen, Jakob H. Taankvist\nand Peter H. Taankvist\n");

            buffer.append("Aalborg University 2008-2020\n\n");

        buffer.append("TAPAAL Continuous Engine (verifytapn):\n");
            buffer.append("Alexandre David, Lasse Jacobsen, Morten Jacobsen and Jiri Srba\n");
            buffer.append("Aalborg University 2011-2020\n\n");

        buffer.append("TAPAAL Discrete Engine (verifydtapn):\n");
            buffer.append("Mathias Andersen, Peter G. Jensen, Heine G. Larsen, Jiri Srba,\n");
            buffer.append("Mathias G. Soerensen and Jakob H. Taankvist\n");
            buffer.append("Aalborg University 2012-2020\n\n");

        buffer.append("TAPAAL Untimed Engine (verifypn):\n");
            buffer.append("Frederik Meyer Boenneland, Jakob Dyhr, Peter Fogh, ");
            buffer.append("Jonas F. Jensen,\nLasse S. Jensen, Peter G. Jensen, ");
            buffer.append("Tobias S. Jepsen, Mads Johannsen,\nIsabella Kaufmann, ");
            buffer.append("Andreas H. Klostergaard, Soeren M. Nielsen,\nThomas S. Nielsen, Lars K. Oestergaard, ");
            buffer.append("Samuel Pastva and Jiri Srba\n");
            buffer.append("Aalborg University 2014-2020\n\n");


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
                        filesOpened.add(createNewTabFromFile(f));
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
                    JOptionPane.showMessageDialog(CreateGui.getRootFrame(),
                            e.getMessage(),
                            "Error loading file",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
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
                        fileOpened.add(createNewTabFromPNMLFile(f));
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
                    JOptionPane.showMessageDialog(CreateGui.getRootFrame(),
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
        save(activeTab.get());
    }
    @Override
    public void saveAs(){
        saveAs(activeTab.get());
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

            int result = JOptionPane.showConfirmDialog(CreateGui.getRootFrame(),
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
        activeTab.ifPresent(TabContentActions::repaintAll);

    }

    @Override
    public void toggleZeroToInfinityIntervals() {
        setZeroToInfinityIntervals(!showZeroToInfinityIntervals);
    }
    public void setZeroToInfinityIntervals(boolean b) {
        showZeroToInfinityIntervals = b;

        guiFrame.setShowZeroToInfinityIntervalsSelected(showZeroToInfinityIntervals);

        Preferences.getInstance().setShowZeroInfIntervals(showZeroToInfinityIntervals);
        activeTab.ifPresent(TabContentActions::repaintAll);
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
        activeTab.ifPresent(o->o.showEnabledTransitionsList(b));
    }

    @Override
    public void toggleDelayEnabledTransitions(){
        setDelayEnabledTransitions(!showDelayEnabledTransitions);
    }

    private void setDelayEnabledTransitions(boolean b) {
        showDelayEnabledTransitions = b;
        guiFrame.setShowDelayEnabledTransitionsSelected(b);
        activeTab.ifPresent(o->o.showDelayEnabledTransitions(b));
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

        activeTab.ifPresent(TabContentActions::setResizeingDefault);

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


    /**
     * Creates a new tab with the selected file, or a new file if filename==null
     */
    @Override
    public TabContent createNewTabFromInputStream(InputStream file, String name) {

        try {
            ModelLoader loader = new ModelLoader();
            LoadedModel loadedModel = loader.load(file);

            if (loadedModel.getMessages().size() != 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        String message = "While loading the net we found one or more warnings: \n\n";
                        for (String s : loadedModel.getMessages()) {
                            message += s + "\n\n";
                        }

                        new MessengerImpl().displayInfoMessage(message, "Warning");
                    }
                }).start();
            }

            TabContent tab = new TabContent(loadedModel.network(), loadedModel.templates(), loadedModel.queries(), loadedModel.getLens());

            tab.setInitialName(name);

            tab.selectFirstElements();

            tab.setFile(null);

            return tab;
        } catch (Exception e) {
            Logger.log("TAPAAL encountered an error while loading the file: " + name + "\n\nPossible explanations:\n  - " + e.toString());
            System.err.println("TAPAAL encountered an error while loading the file: " + name + "\n\nPossible explanations:\n  - " + e.toString());
            //throw new Exception("TAPAAL encountered an error while loading the file: " + name + "\n\nPossible explanations:\n  - " + e.toString());
        }
        return null;

    }

    /**
     * Creates a new tab with the selected file, or a new file if filename==null
     */

    public TabContent createNewTabFromPNMLFile(File file) throws Exception {

        if (file != null) {
            try {

                LoadedModel loadedModel;

                PNMLoader loader = new PNMLoader();
                loadedModel = loader.load(file);

                TabContent tab = new TabContent(loadedModel.network(), loadedModel.templates(), loadedModel.queries(),  new TabContent.TAPNLens(true, false));

                String name = null;

                if (file != null) {
                    name = file.getName().replaceAll(".pnml", ".tapn");
                }
                tab.setInitialName(name);

                tab.selectFirstElements();

                tab.setMode(Pipe.ElementType.SELECT);

                //appView.updatePreferredSize(); //XXX 2018-05-23 kyrke seems not to be needed
                name = name.replace(".pnml",".tapn"); // rename .pnml input file to .tapn
                return tab;

            } catch (Exception e) {
                throw new Exception("TAPAAL encountered an error while loading the file: " + file.getName() + "\n\nPossible explanations:\n  - " + e.toString());
            }
        }
        return null;
    }

    /**
     * Creates a new tab with the selected file, or a new file if filename==null
     */
    //XXX should properly be in controller?
    public TabContent createNewTabFromFile(File file) throws Exception {
        try {
            String name = file.getName();
            boolean showFileEndingChangedMessage = false;

            if(name.toLowerCase().endsWith(".xml")){
                name = name.substring(0, name.lastIndexOf('.')) + ".tapn";
                showFileEndingChangedMessage = true;
            }

            InputStream stream = new FileInputStream(file);
            TabContent tab = createNewTabFromInputStream(stream, name);
            if (tab != null && !showFileEndingChangedMessage) tab.setFile(file);

            if(showFileEndingChangedMessage) {
                //We thread this so it does not block the EDT
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        new MessengerImpl().displayInfoMessage(FILE_FORMAT_CHANGED_MESSAGE, "FILE CHANGED");
                    }
                }).start();
            }

            return tab;
        }catch (FileNotFoundException e) {
            throw new FileNotFoundException("TAPAAL encountered an error while loading the file: " + file.getName() + "\n\nFile not found:\n  - " + e.toString());
        }
    }

    public static final String FILE_FORMAT_CHANGED_MESSAGE = "We have changed the ending of TAPAAL files from .xml to .tapn and the opened file was automatically renamed to end with .tapn.\n"
        + "Once you save the .tapn model, we recommend that you manually delete the .xml file.";

}
