package net.tapaal.gui;

import dk.aau.cs.debug.Logger;
import net.tapaal.gui.petrinet.dialog.BatchProcessingDialog;
import net.tapaal.gui.petrinet.smartdraw.SmartDrawDialog;
import net.tapaal.resourcemanager.ResourceManager;
import dk.aau.cs.model.tapn.simulation.ShortestDelayMode;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPN;
import net.tapaal.Preferences;
import net.tapaal.TAPAAL;
import net.tapaal.helpers.Reference.MutableReference;
import net.tapaal.versioncheck.VersionChecker;
import net.tapaal.gui.petrinet.animation.DelayEnabledTransitionControl;
import pipe.gui.GuiFrame;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.animation.SimulationControl;
import net.tapaal.gui.petrinet.verification.EngineDialogPanel;
import net.tapaal.gui.petrinet.dialog.NewTAPNPanel;
import net.tapaal.gui.petrinet.dialog.QueryDialog;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.swingcomponents.filebrowser.FileBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.lang.UnsupportedOperationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GuiFrameController implements GuiFrameControllerActions{

    final GuiFrame guiFrameDirectAccess; //XXX - while refactoring should only use guiFrameActions
    final GuiFrameActions guiFrame;
    private final ArrayList<PetriNetTab> tabs = new ArrayList<>();

    final MutableReference<TabActions> currentTab = new MutableReference<>();

    public GuiFrameController(GuiFrame appGui) {
        super();

        guiFrame = appGui;
        guiFrameDirectAccess = appGui;

        loadPreferences();
        appGui.registerController(this, currentTab);
    }

    //XXX should be private and should prop. live in controllers not GUI, tmp while refactoring //kyrke 2019-11-05
    boolean showComponents = true;
    boolean showSharedPT = true;
    boolean showConstants = true;
    boolean showColoredTokens = true;
    boolean showQueries = true;
    boolean showEnabledTransitions = true;
    boolean showDelayEnabledTransitions = true;
    private boolean showToolTips = true;
    private boolean showZeroToInfinityIntervals = true;
    private boolean showTokenAge = true;

    public List<PetriNetTab> getTabs() {
        return Collections.unmodifiableList(tabs);
    }

    private void loadPreferences() {
        Preferences prefs = Preferences.getInstance();

        QueryDialog.setAdvancedView(prefs.getAdvancedQueryView());
        PetriNetTab.setEditorModelRoot(prefs.getEditorModelRoot());
        PetriNetTab.setSimulatorModelRoot(prefs.getSimulatorModelRoot());

        showComponents = prefs.getShowComponents();
        guiFrame.setShowComponentsSelected(showComponents);

        showSharedPT = prefs.getShowSharedPT();
        guiFrame.setShowSharedPTSelected(showSharedPT);

        showQueries = prefs.getShowQueries();
        guiFrame.setShowQueriesSelected(showQueries);

        showConstants = prefs.getShowConstants();
        guiFrame.setShowConstantsSelected(showConstants);

        showColoredTokens = prefs.getShowColoredTokens();
        guiFrame.setShowColoredTokensSelected(showColoredTokens);

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

    public void setEnableSidepanel(PetriNetTab tab) {
        Preferences prefs = Preferences.getInstance();
        tab.showComponents(prefs.getShowComponents());
        tab.showSharedPT(prefs.getShowSharedPT());
        tab.showQueries(prefs.getShowQueries());
        tab.showConstantsPanel(prefs.getShowConstants());
        tab.showDelayEnabledTransitions(prefs.getShowDelayEnabledTransitions());
    }

    @Override
    public void openTab(PetriNetTab tab) {
        tabs.add(tab);
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
    public void closeTab(PetriNetTab tab) {
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
                tabs.remove(tab);
            }
        }

    }

    //TODO: 2018-05-07 //kyrke Create CloseTab function, used to close a tab
    //XXX: Temp solution to call getCurrentTab to get new new selected tab (should use index) --kyrke 2019-07-08
    @Override
    public void changeToTab(PetriNetTab tab) {

        //De-register old model
        currentTab.ifPresent(t -> t.setApp(null));

        //Set current tab
        currentTab.setReference(tab);

        guiFrame.changeToTab(tab);

        currentTab.ifPresent(t -> t.setApp(guiFrame));
        guiFrame.setTitle(currentTab.map(TabActions::getTabTitle).orElse(null));

    }

    @Override
    public void clearPreferences() {
        // Clear persistent storage
        Preferences.getInstance().clear();
        // Engines reset individually to remove preferences for already setup engines
        Verifyta.reset();
        VerifyTAPN.reset();
        VerifyDTAPN.reset();
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
        NewTAPNPanel.showNewTapnPanel(guiFrameDirectAccess);
    }

    @Override
    public void saveWorkspace() {
        Preferences prefs = Preferences.getInstance();

        prefs.setAdvancedQueryView(QueryDialog.getAdvancedView());
        prefs.setEditorModelRoot(PetriNetTab.getEditorModelRoot());
        prefs.setSimulatorModelRoot(PetriNetTab.getSimulatorModelRoot());
        prefs.setWindowSize(guiFrameDirectAccess.getSize());

        prefs.setShowComponents(showComponents);
        prefs.setShowSharedPT(showSharedPT);
        prefs.setShowQueries(showQueries);
        prefs.setShowConstants(showConstants);
        prefs.setShowColoredTokens(showColoredTokens);

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
    public void showAbout() {
        StringBuilder buffer = new StringBuilder("About " + TAPAAL.getProgramName());
        buffer.append("\n\n");
        buffer.append("TAPAAL is a tool for modelling, simulation and verification of Timed-Arc Petri nets developed at Department of Computer Science at Aalborg University.\n");
        buffer.append("The GUI is based on PIPE2: https://pipe2.sourceforge.net/\n");
        buffer.append("License information and more is available at: https://www.tapaal.net/about/\n\n");

        buffer.append("Credits\n\n");
        buffer.append("Current Core Team:\n");
        buffer.append("Tanguy Dubois, Peter G. Jensen, Kenneth Yrke Joergensen, Jiri Srba and Mikkel Tygesen\n\n");

        buffer.append("GUI and Translations:\n");
            buffer.append("Mathias Andersen, Sine Viesmose Birch, Jacob Hjort Bundgaard, Joakim Byg, Malo Dautry, \nTanguy Dubois, Jakob Dyhr, Louise Foshammer, Malte Neve-Graesboell, ");
            buffer.append("Lasse Jacobsen, \nMorten Jacobsen, Thomas Stig Jacobsen, Jacob Jon Jensen, Peter G. Jensen, ");
            buffer.append("Mads Johannsen,\nKenneth Yrke J\u00F8rgensen, Mikael H. Moeller, Christoffer Moesgaard, Kristian Morsing Pedersen,\nThomas Pedersen, Lena Said, Niels N. Samuelsen, Mathias G. Soerensen,\nJakob H. Taankvist, Peter H. Taankvist and Mikkel Tygesen\n");

            buffer.append("\n");

        buffer.append("TAPAAL Continuous Engine (verifytapn):\n");
            buffer.append("Alexandre David, Lasse Jacobsen, Morten Jacobsen and Jiri Srba\n");
            buffer.append("\n");

        buffer.append("TAPAAL Discrete Engine (verifydtapn):\n");
            buffer.append("Mathias Andersen, Tanguy Dubois, Peter G. Jensen, Heine G. Larsen, Jiri Srba,\n");
            buffer.append("Mathias G. Soerensen, Jakob H. Taankvist and Mikkel Tygesen\n");
            buffer.append("\n");

        buffer.append("TAPAAL Untimed Engine (verifypn):\n");
            buffer.append("Alexander Bilgram, Frederik Meyer Boenneland, Emil Normann Brandt, Malo Dautry, \n");
            buffer.append("Jesper Adriaan van Diepen, Jakob Dyhr, Peter Fogh, Emil Gybel Henriksen, \n");
            buffer.append("Jens Emil Fink Højriis, Jonas Finnemann Jensen, Lasse Steen Jensen, Peter Gjøl Jensen, \n");
            buffer.append("Nicolaj Østerby Jensen, Tobias Skovgaard Jepsen, Mads Johannsen, Kenneth Yrke Jørgensen, \n");
            buffer.append("Isabella Kaufmann, Alan Mozafar Khorsid, Andreas Hairing Klostergaard, Esben Nielsen, \n");
            buffer.append("Søren Moss Nielsen, Thomas Søndersø Nielsen, Samuel Pastva, Thomas Pedersen, \n");
            buffer.append("Theodor Risager, Jiri Srba, Adam Moloney Stück, Andreas Sebastian Sørensen, \n");
            buffer.append("Mathias Mehl Sørensen, Peter Haar Taankvist, Rasmus Grønkjær Tollund, Mikkel Tygesen, \n");
            buffer.append("Nikolaj Jensen Ulrik, Simon Mejlby Virenfeldt and Lars Kærlund Østergaard \n");
            buffer.append("\n");

        buffer.append("Other Contributors:\n");
        buffer.append("Gideon Blegmand, Thomas Frandsen, Michael Garde, Henrik N. Ginnerup, Mark Glavind,\n");
        buffer.append("Rasmus H. Hansen, Jacob Buchreitz Harbo, Arild Martin Moeller Haugstad, Daniel Heidemann,\n");
        buffer.append("Martin Chmelik, Martin Christensen, Niels Christensen, Sebastian Lassen, Martin Leginus,\n");
        buffer.append("Jose Antonio Mateo, Thomas Birch Mogensen, Nichlas Korgaard Moeller, Heidi Selmer Nielsen,\n");
        buffer.append("Daniel O. Nykjær, Marius Pallisgaard Olsen, Christian Hoegh Pedersen, Morten Turn Pedersen,\n");
        buffer.append("Long Huy Phan, Kenneth Sejersgaard-Jacobsen, Christian Stephansen, Mikkel Holm Soegaard\n");
        buffer.append("and Valdas Zemaitis\n\n");

        buffer.append("Acknowledgments:\n");
        buffer.append("The UPPAAL development team, especially Alexandre David and Arild Martin Moeller Haugstad,\n");
        buffer.append("for their help with UPPAAL; Krishna Prasad Gundam and Ye Tian for their comments and\n");
        buffer.append("suggestions for the first version of the translation algorithms; Michael J. A. Smith for\n");
        buffer.append("the idea and example code for the improved query designer; and the PIPE2 development teams\n");
        buffer.append("for creating and releasing PIPE2 under an open source license.\n");

        buffer.append("\n");
        JOptionPane.showMessageDialog(TAPAALGUI.getApp(), buffer.toString(), "About " + TAPAAL.getProgramName(),
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
    public void openFiles(List<File> files) {
        openTAPNFile(files.toArray(new File[0]));
    }

    @Override
    public void openTAPNFile() {
        final File[] files = FileBrowser.constructor(new String[]{"tapn", "xml", "pnml"}, FileBrowser.userPath).openFiles();
        //show loading cursor
        openTAPNFile(files);
    }

    @Override
    public void importPNMLFile() {
        final File[] files = FileBrowser.constructor("Import PNML", "pnml", FileBrowser.userPath).openFiles();

        openPNMLFile(files);
    }

    private void openTAPNFile(File[] files) {
        guiFrameDirectAccess.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Do loading
        SwingWorker<List<PetriNetTab>, Void> worker = new SwingWorker<List<PetriNetTab>, Void>() {
            @Override
            protected List<PetriNetTab> doInBackground() throws Exception {
                List<PetriNetTab> filesOpened = new ArrayList<>();
                for(File f : files){
                    if(f.exists() && f.isFile() && f.canRead()){
                        FileBrowser.userPath = f.getParent();

                        FileType fileType = inferFileType(f);
                        if (fileType == FileType.PNML) {
                            filesOpened.add(PetriNetTab.createNewTabFromPNMLFile(f));
                        } else if (fileType == FileType.TAPN) {
                            filesOpened.add(PetriNetTab.createNewTabFromFile(f));
                        } else {
                            throw new UnsupportedOperationException("The file " + f.getName() + " has an unsupported file extension. Please use a .tapn, .xml or .pnml file.");
                        }
                    }
                }

                return filesOpened;
            }
            @Override
            protected void done() {
                try {
                    List<PetriNetTab> tabs = get();
                    for (PetriNetTab tab : tabs) {
                        if (tab == null) continue;
                        openTab(tab);

                        //Don't auto-layout on empty net or net too big to draw, hotfix for issue #1960000, we assume only pnml file does not have layout, and they always only have one component
                        if (tab.network().paintNet() && !tab.currentTemplate().getHasPositionalInfo() && (tab.currentTemplate().guiModel().getPlaces().length + tab.currentTemplate().guiModel().getTransitions().length) > 0) {
                            int dialogResult = JOptionPane.showConfirmDialog (TAPAALGUI.getApp(), "The net does not have any layout information. Would you like to do automatic layout?","Automatic Layout?", JOptionPane.YES_NO_OPTION);
                            if(dialogResult == JOptionPane.YES_OPTION) {
                                SmartDrawDialog.showSmartDrawDialog();
                            }
                        }
                    }


                } catch (Exception e) {
                    String message = e.getMessage();

                    if (message.contains("Exception:")) {
                        message = message.split(":", 2)[1];
                    }
                    JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                        message,
                        "Error loading file",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }finally {
                    guiFrameDirectAccess.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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

    private enum FileType {
        TAPN, PNML, UNKNOWN
    }

    private FileType inferFileType(File file) {
        String fileName = file.getName().toLowerCase();
        try {
            String content = Files.readString(file.toPath());
            if (content.contains("<query") || content.contains("<feature") || !content.contains("<page")) {
                return FileType.TAPN;
            } else if (content.contains("<pnml")) {
                return FileType.PNML;
            }
        } catch (IOException ignore) {}

        if (fileName.endsWith(".tapn") || fileName.endsWith(".xml")) {
            return FileType.TAPN;
        } else if (fileName.endsWith(".pnml")) {
            return FileType.PNML;
        } else {
            return FileType.UNKNOWN;
        } 
    }

    private void openPNMLFile(File[] files) {
        //Show loading cursor
        guiFrameDirectAccess.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Do loading of net
        SwingWorker<List<PetriNetTab>, Void> worker = new SwingWorker<List<PetriNetTab>, Void>() {
            @Override
            protected List<PetriNetTab> doInBackground() throws Exception {
                List<PetriNetTab> fileOpened = new ArrayList<>();
                for(File f : files){
                    if(f.exists() && f.isFile() && f.canRead()){
                        FileBrowser.userPath = f.getParent();
                        fileOpened.add(PetriNetTab.createNewTabFromPNMLFile(f));
                    }
                }
                return fileOpened;
            }
            @Override
            protected void done() {
                try {
                    List<PetriNetTab> tabs = get();

                    for (PetriNetTab tab : tabs) {
                        openTab(tab);
                        //Don't autolayout on empty net or net too big to draw, hotfix for issue #1960000. Imported PNML will only have one template.
                        if(tab.network().paintNet() && !tab.currentTemplate().getHasPositionalInfo() && (tab.currentTemplate().guiModel().getPlaces().length + tab.currentTemplate().guiModel().getTransitions().length) > 0) {
                            int dialogResult = JOptionPane.showConfirmDialog (TAPAALGUI.getApp(), "The net does not have any layout information. Would you like to do automatic layout?","Automatic Layout?", JOptionPane.YES_NO_OPTION);
                            if(dialogResult == JOptionPane.YES_OPTION) {
                                SmartDrawDialog.showSmartDrawDialog();
                            }
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                        e.getMessage(),
                        "Error loading file",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }finally {
                    guiFrameDirectAccess.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
        if (versionChecker.checkForNewVersion(forcecheck)) {
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


            final JDialog dialog = optionPane.createDialog(TAPAALGUI.getApp(), "New Version of TAPAAL");
            laterButton.addActionListener(e -> {
                Preferences.getInstance().setLatestVersion(null);
                dialog.setVisible(false);
                dialog.dispose ();
            });
            updateButton.addActionListener(e -> {
                Preferences.getInstance().setLatestVersion(null);
                dialog.setVisible(false);
                dialog.dispose();
                GuiFrameController.showInBrowserDeprecatedDirectCall("https://www.tapaal.net/download/");
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
        //open the default browser on this page
        try {
            java.awt.Desktop.getDesktop().browse(url);
        } catch (IOException | UnsupportedOperationException e) {
            Logger.log("Cannot open the browser.");
            e.printStackTrace();
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "There was a problem opening the default web browser \n" +
                            "Please open the url in your browser by entering " + url.toString(),
                    "Error opening browser", JOptionPane.ERROR_MESSAGE);
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
            Logger.log("Error converting to URL");
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
            BatchProcessingDialog.showBatchProcessingDialog();
        }
    }

    private boolean save(TabActions tab) {
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

    private boolean saveAs(TabActions tab) {
        boolean result;
        // save as
        String suggestedName = tab.getTabTitle();
        String path = null;
        if (tab.getFile() != null) {
            path = tab.getFile().getParent();
            suggestedName = tab.getFile().getName();
        }

        String filename = FileBrowser.constructor("Timed-Arc Petri Net", "tapn", path).saveFile(suggestedName);
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
    private boolean showSavePendingChangesDialog(TabActions tab) {
        if(null == tab) return false;

        if (tab.getNetChanged()) {
            //XXX: this cast should not be done, its a quick fix while refactoring //kyrke 2019-12-31
            changeToTab((PetriNetTab) tab);

            int result = JOptionPane.showConfirmDialog(TAPAALGUI.getApp(),
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
        for (PetriNetTab tab : getTabs()) {
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
        getTabs().forEach(o->o.showQueries(showQueries));
    }

    @Override
    public void toggleConstants(){
        setConstants(!showConstants);
    }

    public void setConstants(boolean b){
        showConstants = b;

        guiFrame.setShowConstantsSelected(showConstants);
        //currentTab.ifPresent(o->o.showConstantsPanel(showConstants));
        getTabs().forEach(o->o.showConstantsPanel(showConstants));

    }

    @Override
    public void toggleTokenAge(){
        setTokenAge(!showTokenAge);
    }
    public void setTokenAge(boolean b){
        showTokenAge = b;

        Preferences.getInstance().setShowTokenAge(showTokenAge);

        guiFrame.setShowTokenAgeSelected(showTokenAge);
        currentTab.ifPresent(TabActions::repaintAll);

    }

    @Override
    public void toggleColorTokens(){
        setColorTokens(!showColoredTokens);
    }
    public void setColorTokens(boolean b){
        showColoredTokens = b;

        Preferences.getInstance().setShowColoredTokens(showColoredTokens);
        guiFrame.setShowColoredTokensSelected(showColoredTokens);
        currentTab.ifPresent(TabActions::repaintAll);
    }

    @Override
    public void toggleZeroToInfinityIntervals() {
        setZeroToInfinityIntervals(!showZeroToInfinityIntervals);
    }
    public void setZeroToInfinityIntervals(boolean b) {
        showZeroToInfinityIntervals = b;

        guiFrame.setShowZeroToInfinityIntervalsSelected(showZeroToInfinityIntervals);

        Preferences.getInstance().setShowZeroInfIntervals(showZeroToInfinityIntervals);
        currentTab.ifPresent(TabActions::repaintAll);
    }

    @Override
    public void toggleComponents(){
        setComponents(!showComponents);
    }

    public void setComponents(boolean b){
        showComponents = b;

        guiFrame.setShowComponentsSelected(showComponents);
        //currentTab.ifPresent(o->o.showComponents(showComponents));
        getTabs().forEach(o->o.showComponents(showComponents));
    }

    @Override
    public void toggleSharedPT(){
        setSharedPT(!showSharedPT);
    }

    public void setSharedPT(boolean b){
        showSharedPT = b;

        guiFrame.setShowSharedPTSelected(showSharedPT);
        getTabs().forEach(o->o.showSharedPT(showSharedPT));
    }

    @Override
    public void toggleEnabledTransitionsList(){
        setEnabledTransitionsList(!showEnabledTransitions);
    }
    
    @Override
    public void setEnabledTransitionsList(boolean b) {
        showEnabledTransitions = b;
        guiFrame.setShowEnabledTransitionsSelected(b);
        currentTab.ifPresent(o->o.showEnabledTransitionsList(b));
    }

    @Override
    public boolean isEnabledTransitionsListVisible() {
        return showEnabledTransitions;
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

        currentTab.ifPresent(TabActions::setResizeingDefault);

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
