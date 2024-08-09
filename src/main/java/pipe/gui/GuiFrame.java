package pipe.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.*;

import com.sun.jna.Platform;
import net.tapaal.gui.*;
import net.tapaal.gui.debug.DEBUG;
import dk.aau.cs.util.JavaUtil;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import net.tapaal.Preferences;
import net.tapaal.TAPAAL;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.helpers.Reference.MutableReference;
import net.tapaal.helpers.Reference.Reference;
import net.tapaal.swinghelpers.ExtendedJTabbedPane;
import net.tapaal.swinghelpers.SwingHelper;
import net.tapaal.swinghelpers.ToggleButtonWithoutText;
import org.jetbrains.annotations.NotNull;
import pipe.gui.petrinet.action.GuiAction;
import pipe.gui.canvas.Grid;
import net.tapaal.gui.petrinet.dialog.ExportBatchDialog;
import net.tapaal.gui.petrinet.dialog.UnfoldDialog;
import dk.aau.cs.debug.Logger;
import net.tapaal.gui.petrinet.smartdraw.SmartDrawDialog;
import net.tapaal.resourcemanager.ResourceManager;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPN;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.animation.SimulatorFocusTraversalPolicy;
import pipe.gui.petrinet.editor.EditorFocusTraversalPolicy;


public class GuiFrame extends JFrame implements GuiFrameActions, SafeGuiFrameActions {

    // for zoom combobox and dropdown
    private final int[] zoomLevels = {40, 60, 80, 100, 120, 140, 160, 180, 200, 300};

    private final String frameTitle;

    final MutableReference<GuiFrameControllerActions> guiFrameController = new MutableReference<>();

    private final ExtendedJTabbedPane<PetriNetTab> appTab;

    private final StatusBar statusBar;
    private JMenuBar menuBar;
    private JMenu drawMenu;
    private JMenu animateMenu;
    private JMenu viewMenu;
    private JMenu toolsMenu;
    private JToolBar drawingToolBar;
    private final JLabel featureInfoText = new JLabel();

    private final JComboBox<String> timeFeatureOptions = new JComboBox<>(new String[]{"No", "Yes"});
    private final JComboBox<String> gameFeatureOptions = new JComboBox<>(new String[]{"No", "Yes"});
    private final JComboBox<String> colorFeatureOptions = new JComboBox<>(new String[]{"No", "Yes"});
    private final JComboBox<String> stochasticFeatureOptions = new JComboBox<>(new String[]{"No", "Yes"});

    private JComboBox<String> zoomComboBox;

    private static final int shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private final GuiAction createAction = new GuiAction("New", "Create a new Petri net", KeyStroke.getKeyStroke('N', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(GuiFrameControllerActions::showNewPNDialog);
        }
    };
    private final GuiAction openAction = new GuiAction("Open", "Open", KeyStroke.getKeyStroke('O', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(GuiFrameControllerActions::openTAPNFile);
        }
    };
    private final GuiAction closeAction = new GuiAction("Close", "Close the current tab", KeyStroke.getKeyStroke('W', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            PetriNetTab index = (PetriNetTab) appTab.getSelectedComponent();
            guiFrameController.ifPresent(o -> o.closeTab(index));
        }
    };
    private final GuiAction saveAction = new GuiAction("Save", "Save", KeyStroke.getKeyStroke('S', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
                guiFrameController.ifPresent(GuiFrameControllerActions::save);
        }
    };
    private final GuiAction saveAsAction = new GuiAction("Save as", "Save as...", KeyStroke.getKeyStroke('S', (shortcutkey + InputEvent.SHIFT_MASK))) {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(GuiFrameControllerActions::saveAs);
        }
    };
    private final GuiAction exitAction = new GuiAction("Exit", "Close the program", KeyStroke.getKeyStroke('Q', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(GuiFrameControllerActions::exit);
        }
    };
    private final GuiAction printAction = new GuiAction("Print", "Print", KeyStroke.getKeyStroke('P', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::print);
        }
    };
    private final GuiAction importPNMLAction = new GuiAction("PNML untimed net", "Import an untimed net in the PNML format", KeyStroke.getKeyStroke('X', InputEvent.SHIFT_MASK)) {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(GuiFrameControllerActions::importPNMLFile);
        }
    };
    private final GuiAction importSUMOAction = new GuiAction("SUMO queries (.txt)", "Import SUMO queries in a plain text format") {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::importSUMOQueries);
        }
    };
    private final GuiAction importXMLAction = new GuiAction("XML queries (.xml)", "Import MCC queries in XML format", KeyStroke.getKeyStroke('R', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::importXMLQueries);
        }
    };
    private final GuiAction exportPNGAction = new GuiAction("PNG", "Export the net to PNG format", KeyStroke.getKeyStroke('G', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::exportPNG);
        }
    };
    private final GuiAction exportPSAction = new GuiAction("PostScript", "Export the net to PostScript format", KeyStroke.getKeyStroke('T', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::exportPS);
        }
    };
    private final GuiAction exportToTikZAction = new GuiAction("TikZ", "Export the net to LaTex (TikZ) format", KeyStroke.getKeyStroke('L', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::exportTIKZ);
        }
    };
    private final GuiAction exportToPNMLAction = new GuiAction("PNML", "Export the net to PNML format", KeyStroke.getKeyStroke('D', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::exportPNML);
        }
    };
    private final GuiAction exportToXMLAction = new GuiAction("XML Queries", "Export the queries to XML format", KeyStroke.getKeyStroke('H', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::exportQueryXML);
        }
    };
    private final GuiAction exportTraceAction = new GuiAction("Export trace", "Export the current trace", "") {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::exportTrace);
        }
    };
    private final GuiAction importTraceAction = new GuiAction("Import trace", "Import trace to simulator", "") {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::importTrace);
        }
    };
    private final GuiAction exportBatchAction = new GuiAction("Batch Export of model and queries", "Export multiple nets and queries for the command line use with the verification engines.", KeyStroke.getKeyStroke('D', (shortcutkey + InputEvent.SHIFT_DOWN_MASK))) {
        public void actionPerformed(ActionEvent e) {
            ExportBatchDialog.ShowExportBatchDialog();
        }
    };

    private final GuiAction cutAction = new GuiAction("Cut", "Cut current selection", KeyStroke.getKeyStroke('X', shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::cut);
        }
    };
    private final GuiAction copyAction = new GuiAction("Copy", "Copy current selection", KeyStroke.getKeyStroke('C', shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::copy);
        }
    };
    private final GuiAction pasteAction = new GuiAction("Paste", "Paste", KeyStroke.getKeyStroke('V', shortcutkey)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::past);
        }
    };
    private final GuiAction undoAction = new GuiAction("Undo", "Undo", KeyStroke.getKeyStroke('Z', shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::undo);
        }
    };
    private final GuiAction redoAction = new GuiAction("Redo", "Redo", KeyStroke.getKeyStroke('Y', shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::redo);
        }
    };
    private final GuiAction toggleGrid = new GuiAction("Cycle grid", "Change the grid size", "G") {
        public void actionPerformed(ActionEvent arg0) {
            Grid.increment();
            repaint();
        }
    };
    private final GuiAction alignToGrid = new GuiAction("Align To Grid", "Align Petri net objects to current grid", KeyStroke.getKeyStroke("shift G")) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::alignToGrid);
        }
    };
    private final GuiAction netStatisticsAction = new GuiAction("Net statistics", "Shows information about the number of transitions, places, arcs, etc.", KeyStroke.getKeyStroke(KeyEvent.VK_I, shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::showStatistics);
        }
    };
    private final GuiAction batchProcessingAction = new GuiAction("Batch processing", "Batch verification of multiple nets and queries", KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::showBatchProcessingDialog);
        }
    };
    private final GuiAction engineSelectionAction = new GuiAction("Engine selection", "View and modify the location of verification engines", KeyStroke.getKeyStroke('E', shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::showEngineDialog);
        }
    };
    private final GuiAction clearPreferencesAction = new GuiAction("Clear all preferences", "Clear all custom preferences to default") {
        public void actionPerformed(ActionEvent actionEvent) {
            guiFrameController.ifPresent(GuiFrameControllerActions::clearPreferences);
        }
    };
    private final GuiAction showColorTypesVariables = new GuiAction("Show color types/variables/constants", "Opens a floating window showing the global color types/variables/constants for the net", KeyStroke.getKeyStroke(("shift F"))) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::showColorTypesVariables);
        }
    };
    private final GuiAction verifyAction = new GuiAction("Verify query", "Verifies the currently selected query", KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::verifySelectedQuery);
        }
    };
    private final GuiAction workflowDialogAction = new GuiAction("Workflow analysis", "Analyse net as a TAWFN", KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::workflowAnalyse);
        }
    };
    private final GuiAction smartDrawAction = new GuiAction("Automatic net layout", "Rearrange the Petri net objects", KeyStroke.getKeyStroke('D', KeyEvent.SHIFT_DOWN_MASK)) {
        public void actionPerformed(ActionEvent e) {
            SmartDrawDialog.showSmartDrawDialog();
        }
    };
    private final GuiAction mergeComponentsDialogAction = new GuiAction("Merge net components", "Open a composed net in a new tab and use approximated net if enabled", KeyStroke.getKeyStroke(KeyEvent.VK_C, (shortcutkey + InputEvent.SHIFT_MASK))) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::mergeNetComponents);
        }
    };
    private final GuiAction zoomOutAction = new GuiAction("Zoom out", "Zoom out by 10% ", KeyStroke.getKeyStroke('K', shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::zoomOut);
        }
    };
    private final GuiAction zoomInAction = new GuiAction("Zoom in", "Zoom in by 10% ", KeyStroke.getKeyStroke('J', shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::zoomIn);
        }
    };
    private final GuiAction zoomToAction = new GuiAction("Zoom", "Select zoom percentage ", "") {
        public void actionPerformed(ActionEvent e) {
            String selectedZoomLevel = (String) zoomComboBox.getSelectedItem();
            //parse selected zoom level, and strip of %.
            int newZoomLevel = Integer.parseInt(selectedZoomLevel.replace("%", ""));

            currentTab.ifPresent(o -> o.zoomTo(newZoomLevel));
        }
    };

    private final GuiAction incSpacingAction = new GuiAction("Increase node spacing", "Increase spacing by 20% ", KeyStroke.getKeyStroke('U', shortcutkey)) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::increaseSpacing);
        }
    };
    private final GuiAction decSpacingAction = new GuiAction("Decrease node spacing", "Decrease spacing by 20% ", KeyStroke.getKeyStroke("shift U")) {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::decreaseSpacing);
        }
    };
    public final GuiAction deleteAction = new GuiAction("Delete", "Delete selection", "DELETE") {
        public void actionPerformed(ActionEvent arg0) {
            currentTab.ifPresent(TabActions::deleteSelection);
        }
    };

    private final GuiAction annotationAction = new GuiAction("Annotation", "Add an annotation (N)", "N", true) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(o -> o.setMode(PetriNetTab.DrawTool.ANNOTATION));
        }
    };

    private final GuiAction showTokenAgeAction = new GuiAction("Display token age", "Show/hide displaying the token age 0.0 (when hidden the age 0.0 is drawn as a dot)", KeyStroke.getKeyStroke('0', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleTokenAge);
        }
    };
    private final GuiAction showComponentsAction = new GuiAction("Display components", "Show/hide the list of components.", KeyStroke.getKeyStroke('1', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleComponents);
        }
    };
    private final GuiAction showSharedPTAction = new GuiAction("Display shared places/transitions", "Show/hide the list of shared places/transitions.", KeyStroke.getKeyStroke('2', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleSharedPT);
        }
    };
    private final GuiAction showQueriesAction = new GuiAction("Display queries", "Show/hide verification queries.", KeyStroke.getKeyStroke('3', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleQueries);
        }
    };
    private final GuiAction showConstantsAction = new GuiAction("Display constants", "Show/hide global constants.", KeyStroke.getKeyStroke('4', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleConstants);
        }
    };
    private final GuiAction showColoredTokensAction = new GuiAction("Display colored tokens", "Show/hide the colored tokens in each place.", KeyStroke.getKeyStroke('5', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleColorTokens);
        }
    };
    private final GuiAction showZeroToInfinityIntervalsAction = new GuiAction("Display intervals [0," + '\u221E' + ")", "Show/hide intervals [0," + Character.toString('\u221E') + ") that do not restrict transition firing in any way.", KeyStroke.getKeyStroke('8', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleZeroToInfinityIntervals);
        }
    };
    private final GuiAction showEnabledTransitionsAction = new GuiAction("Display enabled transitions", "Show/hide the list of enabled transitions", KeyStroke.getKeyStroke('6', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleEnabledTransitionsList);
        }
    };
    private final GuiAction showDelayEnabledTransitionsAction = new GuiAction("Display future-enabled transitions", "Highlight transitions which can be enabled after a delay", KeyStroke.getKeyStroke('7', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleDelayEnabledTransitions);
        }
    };
    private final GuiAction showToolTipsAction = new GuiAction("Display tool tips", "Show/hide tool tips when mouse is over an element", KeyStroke.getKeyStroke('9', shortcutkey), true) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::toggleDisplayToolTips);
        }
    };
    private final GuiAction changeNameVisibility = new GuiAction("Change visibility of transition/place names", "Executing this action will open a dialog where you can hide or show place and transition names", true) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::showChangeNameVisibility);
        }
    };
    private final GuiAction showAdvancedWorkspaceAction = new GuiAction("Show advanced workspace", "Show all panels", false) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::showAdvancedWorkspace);
        }
    };
    private final GuiAction showSimpleWorkspaceAction = new GuiAction("Show simple workspace", "Show only the most important panels", false) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::showSimpleWorkspace);
        }
    };
    private final GuiAction saveWorkSpaceAction = new GuiAction("Save workspace", "Save the current workspace as the default one", false) {
        public void actionPerformed(ActionEvent e) {
            guiFrameController.ifPresent(GuiFrameControllerActions::saveWorkspace);
        }
    };
    private final GuiAction showAboutAction = new GuiAction("About", "Show the About menu") {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(GuiFrameControllerActions::showAbout);
        }
    };
    private final GuiAction showHomepage = new GuiAction("Visit TAPAAL home", "Visit the TAPAAL homepage") {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(o -> o.openURL("http://www.tapaal.net"));
        }
    };
    private final GuiAction showAskQuestionAction = new GuiAction("Ask a question", "Ask a question about TAPAAL") {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(o -> o.openURL("https://answers.launchpad.net/tapaal/+addquestion"));
        }
    };
    private final GuiAction showReportBugAction = new GuiAction("Report bug", "Report a bug in TAPAAL") {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(o -> o.openURL("https://bugs.launchpad.net/tapaal/+filebug"));
        }
    };
    private final GuiAction showFAQAction = new GuiAction("Show FAQ", "See TAPAAL frequently asked questions") {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(o -> o.openURL("https://answers.launchpad.net/tapaal/+faqs"));
        }
    };
    private final GuiAction showWiki = new GuiAction("Visit wiki page", "Visit the TAPAAL wiki page") {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(o -> o.openURL("https://github.com/TAPAAL/TAPAAL/wiki"));
        }
    };
    private final GuiAction showShortcuts = new GuiAction("Show shortcuts", "Visit the TAPAAL wiki page to find a list of shortcuts") {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(o -> o.openURL("https://github.com/TAPAAL/TAPAAL/wiki/Shortcut-keys"));
        }
    };
    private final GuiAction checkUpdate = new GuiAction("Check for updates", "Check if there is a new version of TAPAAL") {
        public void actionPerformed(ActionEvent arg0) {
            guiFrameController.ifPresent(GuiFrameControllerActions::checkForUpdate);
        }
    };

    private final GuiAction selectAllAction = new GuiAction("Select all", "Select all components", KeyStroke.getKeyStroke('A', shortcutkey)) {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::selectAll);
        }
    };

    private final GuiAction startAction = new GuiAction("Simulation mode", "Toggle simulation mode (M)", "M", true) {
        public void actionPerformed(ActionEvent e) {
            //XXX: this needs to be refactored, it breaks the abstraction in a really bad way -- 2022-01-23
            if(getCurrentTab().getLens().isColored() && !getCurrentTab().isInAnimationMode()) {
                PetriNetTab oldTab = getCurrentTab();
                UnfoldDialog.showSimulationDialog(oldTab);

                if(!UnfoldDialog.wasCancelled() && oldTab != getCurrentTab()){
                    currentTab.ifPresent(TabActions::toggleAnimationMode);
                } else {
                    this.setSelected(false);
                }
            } else {
                currentTab.ifPresent(TabActions::toggleAnimationMode);
            }

        }
    };
    public final GuiAction stepforwardAction = new GuiAction("Step forward", "Step forward", "released RIGHT") {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::stepForward);
        }
    };
    public final GuiAction stepbackwardAction = new GuiAction("Step backward", "Step backward", "released LEFT") {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::stepBackwards);
        }
    };
    private final GuiAction prevcomponentAction = new GuiAction("Previous component", "Previous component", "pressed UP") {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::previousComponent);
        }
    };
    private final GuiAction nextcomponentAction = new GuiAction("Next component", "Next component", "pressed DOWN") {
        public void actionPerformed(ActionEvent e) {
            currentTab.ifPresent(TabActions::nextComponent);
        }
    };

    private final GuiAction changeTimeFeatureAction = new GuiAction("Time", "Change time semantics") {
        public void actionPerformed(ActionEvent e) {
            boolean isTime = timeFeatureOptions.getSelectedIndex() != 0;
            currentTab.ifPresent(o -> o.changeTimeFeature(isTime));
            refreshLensConstraints();
        }
    };

    private final GuiAction changeGameFeatureAction = new GuiAction("Game", "Change game semantics") {
        public void actionPerformed(ActionEvent e) {
            boolean isGame = gameFeatureOptions.getSelectedIndex() != 0;
            currentTab.ifPresent(o -> o.changeGameFeature(isGame));
            refreshLensConstraints();
        }
    };

    private final GuiAction changeColorFeatureAction = new GuiAction("Color", "Change color semantics") {
        public void actionPerformed(ActionEvent e) {
            boolean isColor = colorFeatureOptions.getSelectedIndex() != 0;
            currentTab.ifPresent(o -> o.changeColorFeature(isColor));
            refreshLensConstraints();
        }
    };

    private final GuiAction changeStochasticFeatureAction = new GuiAction("Stochastic", "Change stochastic semantics") {
        public void actionPerformed(ActionEvent actionEvent) {
            boolean isStochastic = stochasticFeatureOptions.getSelectedIndex() != 0;
            currentTab.ifPresent(o -> o.changeStochasticFeature(isStochastic));
            refreshLensConstraints();
        }
    };

    public enum GUIMode {
        draw, animation, noNet
    }

    private JCheckBoxMenuItem showZeroToInfinityIntervalsCheckBox;
    private JCheckBoxMenuItem showTokenAgeCheckBox;
    private JCheckBoxMenuItem showDelayEnabledTransitionsCheckbox;
    private JCheckBoxMenuItem showColoredTokensCheckbox;

    private JMenu zoomMenu;

    public GuiFrame(String title) {
        // HAK-arrange for frameTitle to be initialized and the default file
        // name to be appended to basic window title

        checkJavaVersion();

        frameTitle = title;
        setTitle(null);
        trySetLookAndFeel();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width * 80 / 100, screenSize.height * 80 / 100);
        this.setLocationRelativeTo(null);
        this.setMinimumSize(new Dimension(825, 480));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        appTab = new ExtendedJTabbedPane<PetriNetTab>() {
            @Override
            public Component generator() {
                return new TabComponent(this) {
                    @Override
                    protected void closeTab(PetriNetTab tab) {
                        GuiFrame.this.guiFrameController.ifPresent(o -> o.closeTab(tab));
                    }
                };
            }
        };
        getContentPane().add(appTab);
        setChangeListenerOnTab(); // sets Tab properties

        Grid.enableGrid();

        buildMenus();

        // Status bar...
        statusBar = new StatusBar();

        // Net Type
        JPanel featurePanel = new JPanel();
        featurePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        featurePanel.add(new JLabel("Timed: "));
        featurePanel.add(timeFeatureOptions);
        featurePanel.add(new JLabel("   Game: "));
        featurePanel.add(gameFeatureOptions);
        featurePanel.add(new JLabel("   Color: "));
        featurePanel.add(colorFeatureOptions);
        featurePanel.add(new JLabel("   Stochastic: "));
        featurePanel.add(stochasticFeatureOptions);
        timeFeatureOptions.addActionListener(changeTimeFeatureAction);
        gameFeatureOptions.addActionListener(changeGameFeatureAction);
        colorFeatureOptions.addActionListener(changeColorFeatureAction);
        stochasticFeatureOptions.addActionListener(changeStochasticFeatureAction);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 2));
        bottomPanel.add(statusBar);
        bottomPanel.add(featurePanel);
        getContentPane().add(bottomPanel, BorderLayout.PAGE_END);

        // Build menus
        buildToolbar();

        addWindowListener(new WindowAdapter() {
            // Handler for window closing event
            public void windowClosing(WindowEvent e) {
                exitAction.actionPerformed(null);
            }
        });

        this.setForeground(java.awt.Color.BLACK);

        // Set GUI mode
        setGUIMode(GUIMode.noNet);

        //XXX 2018-05-23 kyrke: Moved from CreatGUI (static), needs further refactoring to separate concerns
        Verifyta.trySetup();
        VerifyTAPN.trySetup();
        VerifyDTAPN.trySetup();
        VerifyPN.trySetup();


        this.setDropTarget(new FileDropTarget() {
            @Override
            public void onDrop(List<? extends File> files) {
                // I guess the event should be files dropped, not the open action
                guiFrameController.ifPresent(p->p.openFiles((List<File>) files));
            }
        });

    }

    private void checkJavaVersion() {
        int version = JavaUtil.getJREMajorVersion();

        if (version < 11) {
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "You are using an older version of Java than 11. Some of the functionalities may not be shown correctly.");
            System.out.println("You are using an older version of Java than 11. Some of the functionalities may not be shown correctly.");
        }
    }

    private void trySetLookAndFeel() {
        try {
            // Set the Look and Feel native for the system.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Set enter to select focus button rather than default (makes ENTER selection key on all LAFs)
            UIManager.put("Button.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{
                    "SPACE", "pressed",
                    "released SPACE", "released",
                    "ENTER", "pressed",
                    "released ENTER", "released"
                })
            );
            UIManager.put("OptionPane.informationIcon", ResourceManager.infoIcon());
            UIManager.put("Slider.paintValue", false);

        } catch (Exception exc) {
            Logger.log("Error loading L&F: " + exc);
        }

        if (Platform.isMac()) {

            //Set specific settings
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", TAPAAL.TOOLNAME);

            // Use native file chooser
            System.setProperty("apple.awt.fileDialogForDirectories", "false");

            // Grow size of boxes to add room for the resizer
            System.setProperty("apple.awt.showGrowBox", "true");

        }

        this.setIconImage(ResourceManager.getIcon("icon.png").getImage());
        //This makes it look slightly better in ubuntu dark mode
        //By removing a white bar around the whole drawing surface
        //https://bugs.launchpad.net/tapaal/+bug/1902226
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
    }


    @Override
    public void setWindowSize(Dimension dimension) {
        if (dimension == null) return;

        this.setSize(dimension);
    }

    /**
     * Build the menus and actions
     **/
    private void buildMenus() {
        menuBar = new JMenuBar();
        menuBar.add(buildMenuFiles());
        menuBar.add(buildMenuEdit());
        menuBar.add(buildMenuView());
        menuBar.add(buildMenuDraw());

        menuBar.add(buildMenuAnimation());
        menuBar.add(buildMenuTools());

        if (TAPAAL.IS_DEV) {
            menuBar.add(DEBUG.buildMenuDEBUG());
        }

        menuBar.add(buildMenuHelp());

        setJMenuBar(menuBar);
    }



    private JMenu buildMenuEdit() {

        /* Edit Menu */
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        editMenu.add(undoAction);


        editMenu.add(redoAction);
        editMenu.addSeparator();

        editMenu.add(cutAction);
        editMenu.add(copyAction);
        editMenu.add(pasteAction);

        editMenu.addSeparator();

        editMenu.add(deleteAction);

        // Bind delete to backspace also
        editMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("BACK_SPACE"), "Delete");
        editMenu.getActionMap().put("Delete", deleteAction);

        editMenu.addSeparator();


        editMenu.add(selectAllAction);
        editMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('A', shortcutkey), "SelectAll");
        editMenu.getActionMap().put("SelectAll", selectAllAction);

        return editMenu;
    }

    private JMenu buildMenuDraw() {
        /* Draw menu */
        drawMenu = new JMenu("Draw");
        drawMenu.setMnemonic('D');
        return drawMenu;
    }

    private JMenu buildMenuView() {
        /* ViewMenu */
        viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');

        zoomMenu = new JMenu("Zoom");
        zoomMenu.setIcon(ResourceManager.getIcon("Zoom.png"));

        addZoomMenuItems(zoomMenu);

        viewMenu.add(zoomInAction);

        viewMenu.add(zoomOutAction);
        viewMenu.add(zoomMenu);

        viewMenu.addSeparator();

        viewMenu.add(incSpacingAction);

        viewMenu.add(decSpacingAction);


        viewMenu.addSeparator();

        viewMenu.add(toggleGrid);

        viewMenu.add(alignToGrid);


        viewMenu.addSeparator();

        addCheckboxMenuItem(viewMenu, showComponentsAction);

        addCheckboxMenuItem(viewMenu, showSharedPTAction);

        addCheckboxMenuItem(viewMenu, showQueriesAction);

        addCheckboxMenuItem(viewMenu, showConstantsAction);

        showColoredTokensCheckbox = addCheckboxMenuItem(viewMenu, showColoredTokensAction);

        addCheckboxMenuItem(viewMenu, showEnabledTransitionsAction);

        showDelayEnabledTransitionsCheckbox = addCheckboxMenuItem(viewMenu, showDelayEnabledTransitionsAction);

        showZeroToInfinityIntervalsCheckBox = addCheckboxMenuItem(viewMenu, showZeroToInfinityIntervals(), showZeroToInfinityIntervalsAction);

        addCheckboxMenuItem(viewMenu, showToolTipsAction);

        showTokenAgeCheckBox = addCheckboxMenuItem(viewMenu, showTokenAge(), showTokenAgeAction);

        viewMenu.addSeparator();

        viewMenu.add(changeNameVisibility);

        viewMenu.addSeparator();

        viewMenu.add(showSimpleWorkspaceAction);
        viewMenu.add(showAdvancedWorkspaceAction);
        viewMenu.add(saveWorkSpaceAction);
        return viewMenu;
    }


    private JMenu buildMenuAnimation() {
        /* Simulator */
        animateMenu = new JMenu("Simulator");
        animateMenu.setMnemonic('A');
        animateMenu.add(startAction);


        animateMenu.add(stepbackwardAction);
        animateMenu.add(stepforwardAction);

        animateMenu.add(prevcomponentAction);

        animateMenu.add(nextcomponentAction);

        animateMenu.addSeparator();

        animateMenu.add(exportTraceAction);
        animateMenu.add(importTraceAction);


        return animateMenu;
    }

    private JMenu buildMenuHelp() {
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        helpMenu.add(showHomepage);

        helpMenu.add(checkUpdate);

        helpMenu.addSeparator();

        helpMenu.add(showFAQAction);
        helpMenu.add(showAskQuestionAction);
        helpMenu.add(showReportBugAction);

        helpMenu.addSeparator();

        helpMenu.add(showWiki);
        helpMenu.add(showShortcuts);

        helpMenu.addSeparator();

        helpMenu.add(showAboutAction);
        return helpMenu;
    }


    private JMenu buildMenuTools() {
        toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('t');

        toolsMenu.add(verifyAction).setMnemonic('m');

        toolsMenu.add(netStatisticsAction).setMnemonic('i');

        JMenuItem workflowDialog = new JMenuItem(workflowDialogAction);
        workflowDialog.setMnemonic('f');
        toolsMenu.add(workflowDialog);

        JMenuItem smartDrawDialog = new JMenuItem(smartDrawAction);
        smartDrawDialog.setMnemonic('D');
        toolsMenu.add(smartDrawDialog);

        JMenuItem mergeComponentsDialog = new JMenuItem(mergeComponentsDialogAction);
        mergeComponentsDialog.setMnemonic('c');
        toolsMenu.add(mergeComponentsDialog);

        toolsMenu.add(showColorTypesVariables);

        toolsMenu.addSeparator();

        JMenuItem batchProcessing = new JMenuItem(batchProcessingAction);
        batchProcessing.setMnemonic('b');
        toolsMenu.add(batchProcessing);

        toolsMenu.addSeparator();

        JMenuItem engineSelection = new JMenuItem(engineSelectionAction);
        toolsMenu.add(engineSelection);

        JMenuItem clearPreferences = new JMenuItem(clearPreferencesAction);
        toolsMenu.add(clearPreferences);

        return toolsMenu;
    }

    private void buildToolbar() {

        //XXX .setRequestFocusEnabled(false), removed "border" around toolbar buttons when selected/focus
        // https://stackoverflow.com/questions/9361658/disable-jbutton-focus-border and
        //https://stackoverflow.com/questions/20169436/how-to-prevent-toolbar-button-focus-in-java-swing

        // Create the toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);// Inhibit toolbar floating
        toolBar.setRequestFocusEnabled(false);

        // Basis file operations
        toolBar.add(createAction).setRequestFocusEnabled(false);
        toolBar.add(openAction).setRequestFocusEnabled(false);
        toolBar.add(saveAction).setRequestFocusEnabled(false);
        toolBar.add(saveAsAction).setRequestFocusEnabled(false);

        // Print
        toolBar.addSeparator();
        toolBar.add(printAction).setRequestFocusEnabled(false);

        // Copy/past
        toolBar.add(copyAction);
        toolBar.add(cutAction);
        toolBar.add(pasteAction);

        // Undo/redo
        toolBar.addSeparator();
        toolBar.add(deleteAction).setRequestFocusEnabled(false);
        toolBar.add(undoAction).setRequestFocusEnabled(false);
        toolBar.add(redoAction).setRequestFocusEnabled(false);

        // Zoom
        toolBar.addSeparator();
        toolBar.add(zoomOutAction).setRequestFocusEnabled(false);
        addZoomComboBox(toolBar, zoomToAction);
        toolBar.add(zoomInAction).setRequestFocusEnabled(false);

        // Modes

        toolBar.addSeparator();
        toolBar.add(toggleGrid).setRequestFocusEnabled(false);

        toolBar.add(new ToggleButtonWithoutText(startAction));

        // Start drawingToolBar
        drawingToolBar = new JToolBar();
        drawingToolBar.setFloatable(false);
        drawingToolBar.addSeparator();
        drawingToolBar.setRequestFocusEnabled(false);

        // Create panel to put toolbars in
        JPanel toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // Add toolbars to pane
        toolBarPanel.add(toolBar);
        toolBarPanel.add(drawingToolBar);

        // Create a toolBarPaneltmp using borderLayout and a spacer to get
        // toolbar to fill the screen
        JPanel toolBarPaneltmp = new JPanel();
        toolBarPaneltmp.setLayout(new BorderLayout());
        toolBarPaneltmp.add(toolBarPanel, BorderLayout.WEST);
        JToolBar spacer = new JToolBar();
        spacer.addSeparator();
        spacer.setFloatable(false);
        toolBarPaneltmp.add(spacer, BorderLayout.CENTER);

        // Add to GUI
        getContentPane().add(toolBarPaneltmp, BorderLayout.PAGE_START);
    }

    private void addZoomMenuItems(JMenu zoomMenu) {
        for (int i = 0; i <= zoomLevels.length - 1; i++) {

            final int zoomper = zoomLevels[i];
            GuiAction newZoomAction = new GuiAction(zoomLevels[i] + "%", "Select zoom percentage", "") {
                public void actionPerformed(ActionEvent e) {
                    currentTab.ifPresent(o -> o.zoomTo(zoomper));
                }
            };

            zoomMenu.add(newZoomAction);
        }
    }

    private void addZoomComboBox(JToolBar toolBar, Action action) {
        Dimension zoomComboBoxDimension = new Dimension(100, 28);

        String[] zoomExamplesStrings = new String[zoomLevels.length];
        int i;
        for (i = 0; i < zoomLevels.length; i++) {
            zoomExamplesStrings[i] = zoomLevels[i] + "%";
        }

        zoomComboBox = new JComboBox<>(zoomExamplesStrings);
        zoomComboBox.setEditable(true);
        zoomComboBox.setSelectedItem("100%");
        zoomComboBox.setMaximumRowCount(zoomLevels.length);
        SwingHelper.setPreferredWidth(zoomComboBox,zoomComboBoxDimension.width);
        zoomComboBox.setAction(action);
        zoomComboBox.setFocusable(false);
        toolBar.add(zoomComboBox);
    }

    private JCheckBoxMenuItem addCheckboxMenuItem(JMenu menu, Action action) {
        return addCheckboxMenuItem(menu, true, action);
    }

    private JCheckBoxMenuItem addCheckboxMenuItem(JMenu menu, boolean selected, Action action) {

        JCheckBoxMenuItem checkBoxItem = new JCheckBoxMenuItem();

        checkBoxItem.setAction(action);
        checkBoxItem.setSelected(selected);
        JMenuItem item = menu.add(checkBoxItem);
        KeyStroke keystroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

        if (keystroke != null) {
            item.setAccelerator(keystroke);
        }
        return checkBoxItem;
    }

    /**
     * Sets all buttons to enabled or disabled according to the current GUImode.
     * Reimplementation of old enableGUIActions(bool status) method.
     */
    private void enableGUIActions(GUIMode mode) {
        switch (mode) {
            case draw:
                enableAllActions(true);
                exportTraceAction.setEnabled(false);
                importTraceAction.setEnabled(false);

                annotationAction.setEnabled(true);
                deleteAction.setEnabled(true);
                selectAllAction.setEnabled(true);

                stepbackwardAction.setEnabled(false);
                stepforwardAction.setEnabled(false);
                prevcomponentAction.setEnabled(false);
                nextcomponentAction.setEnabled(false);

                deleteAction.setEnabled(true);
                showEnabledTransitionsAction.setEnabled(false);
                showDelayEnabledTransitionsAction.setEnabled(false);

                verifyAction.setEnabled(getCurrentTab().isQueryPossible());

                showColorTypesVariables.setEnabled(getCurrentTab().lens.isColored());

                smartDrawAction.setEnabled(true);
                mergeComponentsDialogAction.setEnabled(true);
                if (gameFeatureOptions.getSelectedIndex() == 1 || colorFeatureOptions.getSelectedIndex() == 1) {
                    workflowDialogAction.setEnabled(false);
                } else {
                    workflowDialogAction.setEnabled(true);
                }

                timeFeatureOptions.setEnabled(true);
                gameFeatureOptions.setEnabled(true);
                colorFeatureOptions.setEnabled(true);
                stochasticFeatureOptions.setEnabled(true);
                refreshLensConstraints();

                //Enable editor focus traversal policy
                setFocusTraversalPolicy(new EditorFocusTraversalPolicy());
                fixBug812694GrayMenuAfterSimulationOnMac();
                break;

            case animation:
                enableAllActions(true);

                annotationAction.setEnabled(false);
                deleteAction.setEnabled(false);
                selectAllAction.setEnabled(false);

                alignToGrid.setEnabled(false);

                showSharedPTAction.setEnabled(false);
                showConstantsAction.setEnabled(false);
                showColoredTokensAction.setEnabled(false);
                showQueriesAction.setEnabled(false);

                stepbackwardAction.setEnabled(true);
                stepforwardAction.setEnabled(true);
                prevcomponentAction.setEnabled(true);
                nextcomponentAction.setEnabled(true);

                deleteAction.setEnabled(false);
                undoAction.setEnabled(false);
                redoAction.setEnabled(false);
                verifyAction.setEnabled(false);

                showColorTypesVariables.setEnabled(getCurrentTab().lens.isColored());

                smartDrawAction.setEnabled(false);
                mergeComponentsDialogAction.setEnabled(false);
                workflowDialogAction.setEnabled(false);

                timeFeatureOptions.setEnabled(false);
                gameFeatureOptions.setEnabled(false);
                colorFeatureOptions.setEnabled(false);
                stochasticFeatureOptions.setEnabled(false);

                //Enable simulator focus traversal policy
                setFocusTraversalPolicy(new SimulatorFocusTraversalPolicy(getCurrentTab().getAnimationController().TimeDelayField));

                break;
            case noNet:
                exportTraceAction.setEnabled(false);
                importTraceAction.setEnabled(false);
                verifyAction.setEnabled(false);

                showColorTypesVariables.setEnabled(false);

                annotationAction.setEnabled(false);
                selectAllAction.setEnabled(false);

                stepbackwardAction.setEnabled(false);
                stepforwardAction.setEnabled(false);

                deleteAction.setEnabled(false);
                undoAction.setEnabled(false);
                redoAction.setEnabled(false);
                prevcomponentAction.setEnabled(false);
                nextcomponentAction.setEnabled(false);

                smartDrawAction.setEnabled(false);
                mergeComponentsDialogAction.setEnabled(false);
                workflowDialogAction.setEnabled(false);

                timeFeatureOptions.setEnabled(false);
                gameFeatureOptions.setEnabled(false);
                colorFeatureOptions.setEnabled(false);
                stochasticFeatureOptions.setEnabled(false);


                enableAllActions(false);

                // Disable All Actions
                statusBar.changeText("Open a net to start editing");
                setFocusTraversalPolicy(null);

                break;
        }

    }

    /**
     * Helperfunction for disabling/enabling all actions when we are in noNet GUImode
     */
    private void enableAllActions(boolean enable) {

        // File
        closeAction.setEnabled(enable);

        saveAction.setEnabled(enable);
        saveAsAction.setEnabled(enable);

        exportPNGAction.setEnabled(enable);
        exportPSAction.setEnabled(enable);
        exportToTikZAction.setEnabled(enable);
        exportToPNMLAction.setEnabled(enable);
        exportToXMLAction.setEnabled(enable);

        exportTraceAction.setEnabled(enable);
        importTraceAction.setEnabled(enable);

        printAction.setEnabled(enable);

        // View
        zoomInAction.setEnabled(enable);
        zoomOutAction.setEnabled(enable);
        zoomComboBox.setEnabled(enable);
        zoomMenu.setEnabled(enable);

        decSpacingAction.setEnabled(enable);
        incSpacingAction.setEnabled(enable);

        toggleGrid.setEnabled(enable);
        alignToGrid.setEnabled(enable);

        showComponentsAction.setEnabled(enable);
        showSharedPTAction.setEnabled(enable);
        showConstantsAction.setEnabled(enable);
        showColoredTokensAction.setEnabled(enable);
        showQueriesAction.setEnabled(enable);
        showZeroToInfinityIntervalsAction.setEnabled(enable);
        showEnabledTransitionsAction.setEnabled(enable);
        showDelayEnabledTransitionsAction.setEnabled(enable);
        showToolTipsAction.setEnabled(enable);
        showTokenAgeAction.setEnabled(enable);
        changeNameVisibility.setEnabled(enable);
        showAdvancedWorkspaceAction.setEnabled(enable);
        showSimpleWorkspaceAction.setEnabled(enable);
        saveWorkSpaceAction.setEnabled(enable);

        // Simulator
        startAction.setEnabled(enable);

        // Tools
        netStatisticsAction.setEnabled(enable);

    }

    // set tabbed pane properties and add change listener that updates tab with
    // linked model and view
    public void setChangeListenerOnTab() {
        appTab.addChangeListener(e -> {
                //This event will only fire if the tab index is changed, so it won't trigger if once
                // also if code calls setSelectedIndex(index), thereby avoiding a loop.
                PetriNetTab tab = (PetriNetTab) appTab.getSelectedComponent();

                if (tab != null) {
                    guiFrameController.ifPresent(o -> o.changeToTab(tab));
                }
            }
        );
    }

    @Override
    public void updatedTabName(PetriNetTab tab) {
        int index = appTab.indexOfComponent(tab);

        appTab.setTitleAt(index, tab.getTabTitle());

        // resize "header" of current tab immediately to fit the length of the model name (if it shorter)
        appTab.getTabComponentAt(index).doLayout();

        if (index >= 0 && index == appTab.getSelectedIndex()) {
            setTitle(tab.getTabTitle()); // Change the window title
        }
    }

    @Override
    public void attachTabToGuiFrame(PetriNetTab tab) {
        appTab.addTab(tab.getTabTitle(), tab);
    }

    @Override
    public void detachTabFromGuiFrame(PetriNetTab tab) {
        appTab.remove(tab);

        if (appTab.getTabCount() == 0) {
           for (GuiAction action : tab.getAvailableToolActions()) {
                action.setEnabled(false);
            }
            setGUIMode(GUIMode.noNet);
        }
    }

    /**
     * Set the current mode of the GUI, and changes possible actions
     *
     * @param mode change GUI to this mode
     * @author Kenneth Yrke Joergensen (kyrke)
     */
    //TODO
    @Override
    public void setGUIMode(GUIMode mode) {
        switch (mode) {
            case draw:
                // Enable all draw actions
                startAction.setSelected(false);
                pasteAction.setEnabled(true);
                copyAction.setEnabled(true);
                cutAction.setEnabled(true);

                break;
            case animation:
                startAction.setSelected(true);
                pasteAction.setEnabled(false);
                copyAction.setEnabled(false);
                cutAction.setEnabled(false);

                break;
            case noNet:
                setFeatureInfoText(null);
                registerDrawingActions(List.of());
                registerAnimationActions(List.of());
                //registerViewActions(List.of());
                pasteAction.setEnabled(false);
                copyAction.setEnabled(false);
                cutAction.setEnabled(false);
                break;

            default:
                break;
        }

        // Enable actions based on GUI mode
        enableGUIActions(mode);
        if (currentTab != null) {
            currentTab.ifPresent(o -> o.updateEnabledActions(mode));
        }
    }

    @Override
    public void registerDrawingActions(@NotNull List<GuiAction> drawActions) {

        drawingToolBar.removeAll();
        drawMenu.removeAll();

        if (drawActions.size() > 0) {
            drawMenu.setEnabled(true);
            drawingToolBar.addSeparator();

            for (GuiAction action : drawActions) {
                drawingToolBar.add(new ToggleButtonWithoutText(action));
                drawMenu.add(action);
            }

            drawingToolBar.add(featureInfoText);
        } else {
            drawMenu.setEnabled(false);
        }

    }

    @Override
    public void registerToolsActions(@NotNull List<GuiAction> toolsActions){
        toolsMenu.removeAll();
        toolsMenu.setEnabled(true);
        toolsMenu.add(verifyAction).setMnemonic('m');

        toolsMenu.add(netStatisticsAction).setMnemonic('i');

        JMenuItem workflowDialog = new JMenuItem(workflowDialogAction);
        workflowDialog.setMnemonic('f');
        toolsMenu.add(workflowDialog);

        JMenuItem smartDrawDialog = new JMenuItem(smartDrawAction);
        smartDrawDialog.setMnemonic('D');
        toolsMenu.add(smartDrawDialog);

        JMenuItem mergeComponentsDialog = new JMenuItem(mergeComponentsDialogAction);
        mergeComponentsDialog.setMnemonic('c');
        toolsMenu.add(mergeComponentsDialog);

        toolsMenu.add(showColorTypesVariables);

        for(GuiAction action : toolsActions){
            toolsMenu.add(action);
        }

        toolsMenu.addSeparator();

        JMenuItem batchProcessing = new JMenuItem(batchProcessingAction);
        batchProcessing.setMnemonic('b');
        toolsMenu.add(batchProcessing);

        toolsMenu.addSeparator();

        JMenuItem engineSelection = new JMenuItem(engineSelectionAction);
        toolsMenu.add(engineSelection);

        JMenuItem clearPreferences = new JMenuItem(clearPreferencesAction);
        toolsMenu.add(clearPreferences);

    }
    @Override
    public void registerAnimationActions(@NotNull List<GuiAction> animationActions) {

        animateMenu.removeAll();

        if (animationActions.size() > 0) {

            animateMenu.setEnabled(true);
            animateMenu.add(startAction);

            animateMenu.add(stepbackwardAction);
            animateMenu.add(stepforwardAction);

            for (GuiAction action : animationActions) {
                animateMenu.add(action);
            }

            animateMenu.add(prevcomponentAction);
            animateMenu.add(nextcomponentAction);

            animateMenu.addSeparator();
            animateMenu.add(exportTraceAction);
            animateMenu.add(importTraceAction);
        } else {
            animateMenu.setEnabled(false);
        }
    }

    @Override
    public void registerViewActions(@NotNull List<GuiAction> viewActions) {
        //TODO: This is a temporary implementation until view actions can be moved to tab content

        if (!getCurrentTab().getLens().isTimed()) {
            showZeroToInfinityIntervalsCheckBox.setVisible(false);
            showTokenAgeCheckBox.setVisible(false);
            showDelayEnabledTransitionsCheckbox.setVisible(false);
        } else {
            showZeroToInfinityIntervalsCheckBox.setVisible(true);
            showTokenAgeCheckBox.setVisible(true);
            showDelayEnabledTransitionsCheckbox.setVisible(true);
        }

        if(!getCurrentTab().getLens().isColored()) {
            showColoredTokensCheckbox.setVisible(false);
        } else {
            showColoredTokensCheckbox.setVisible(true);
        }
    }

    private void fixBug812694GrayMenuAfterSimulationOnMac() {
        // XXX
        // This is a fix for bug #812694 where on mac some menues are gray after
        // changing from simulation mode, when displaying a trace. Showing and
        // hiding a menu seems to fix this problem
        JDialog a = new JDialog(this, false);
        a.setUndecorated(true);
        a.setVisible(true);
        a.dispose();
    }

    @Override
    public void setStatusBarText(String s) {
        statusBar.changeText(Objects.requireNonNullElse(s, ""));
    }


    Reference<TabActions> currentTab = null;

    @Override
    public void registerController(GuiFrameControllerActions guiFrameController, Reference<TabActions> currentTab) {
        this.guiFrameController.setReference(guiFrameController);
        this.currentTab = currentTab;
    }

    @Override
    public void changeToTab(PetriNetTab tab) {
        if (tab != null) {
            //Change tab event will only fire if index != currentIndex, to changing it via setSelectIndex will not
            // create a tabChanged event loop.
            // Throw exception if tab is not found
            appTab.setSelectedComponent(tab);
        }
    }

    @Override
    public void setShowComponentsSelected(boolean b) {
        showComponentsAction.setSelected(b);
    }

    @Override
    public void setShowSharedPTSelected(boolean b) {
        showSharedPTAction.setSelected(b);
    }

    @Override
    public void setShowConstantsSelected(boolean b) {
        showConstantsAction.setSelected(b);
    }

    @Override
    public void setShowColoredTokensSelected(boolean b) { showColoredTokensAction.setSelected(b); }

    @Override
    public void setShowQueriesSelected(boolean b) {
        showQueriesAction.setSelected(b);
    }

    @Override
    public void setShowEnabledTransitionsSelected(boolean b) {
        showEnabledTransitionsAction.setSelected(b);
    }

    @Override
    public void setShowDelayEnabledTransitionsSelected(boolean b) {
        showDelayEnabledTransitionsAction.setSelected(b);
    }

    @Override
    public void setShowToolTipsSelected(boolean b) {
        showToolTipsAction.setSelected(b);
    }

    @Override
    public void setShowZeroToInfinityIntervalsSelected(boolean b) {
        showZeroToInfinityIntervalsAction.setSelected(b);
    }

    @Override
    public void setShowTokenAgeSelected(boolean b) {
        showTokenAgeAction.setSelected(b);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle((title == null) ? frameTitle : frameTitle + ": " + title);
    }

    @Deprecated
    public boolean isEditionAllowed() {
        return !getCurrentTab().isInAnimationMode();
    }

    @Override
    public void setUndoActionEnabled(boolean flag) {
        undoAction.setEnabled(flag);
    }

    @Override
    public void setRedoActionEnabled(boolean flag) {
        redoAction.setEnabled(flag);
    }

    /**
     * @author Ben Kirby Remove the listener from the zoomComboBox, so that when
     * the box's selected item is updated to keep track of ZoomActions
     * called from other sources, a duplicate ZoomAction is not called
     */
    @Override
    public void updateZoomCombo(int zoom) {
        ActionListener zoomComboListener = (zoomComboBox.getActionListeners())[0];
        zoomComboBox.removeActionListener(zoomComboListener);
        zoomComboBox.setSelectedItem(zoom + "%");
        zoomComboBox.addActionListener(zoomComboListener);
    }

    private JMenu buildMenuFiles() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        fileMenu.add(createAction);

        fileMenu.add(openAction);

        fileMenu.add(closeAction);

        fileMenu.addSeparator();

        fileMenu.add(saveAction);


        fileMenu.add(saveAsAction);


        // Import menu
        JMenu importMenu = new JMenu("Import");
        importMenu.setIcon(ResourceManager.getIcon("Export.png"));

        importMenu.add(importPNMLAction);


        importMenu.add(importSUMOAction);

        importMenu.add(importXMLAction);
        fileMenu.add(importMenu);

        // Export menu
        JMenu exportMenu = new JMenu("Export");
        exportMenu.setIcon(ResourceManager.getIcon("Export.png"));

        exportMenu.add(exportPNGAction);

        exportMenu.add(exportPSAction);

        exportMenu.add(exportToTikZAction);

        exportMenu.add(exportToPNMLAction);

        exportMenu.add(exportToXMLAction);

        exportMenu.add(exportBatchAction);


        fileMenu.add(exportMenu);

        fileMenu.addSeparator();
        fileMenu.add(printAction);

        fileMenu.addSeparator();

        JMenu exampleMenu = buildExampleMenu();
        if (exampleMenu != null) {
            fileMenu.add(exampleMenu);
            fileMenu.addSeparator();
        }

        fileMenu.add(exitAction);

        return fileMenu;
    }

    private JMenu buildExampleMenu() {
        // Loads example files, returns null if not found
        String[] nets = loadTestNets();

        // Oliver Haggarty - fixed code here so that if folder contains non
        // .xml file the Example x counter is not incremented when that file
        // is ignored
        if (nets != null && nets.length > 0) {
            TAPNLens untimedLens = new TAPNLens(false, false, false, false);
            TAPNLens timedLens = new TAPNLens(true, false, false, false);
            TAPNLens untimedGameLens = new TAPNLens(false, true, false, false);
            TAPNLens timedGameLens = new TAPNLens(true, true, false, false);
            TAPNLens untimedColorLens = new TAPNLens(false, false, true, false);
            TAPNLens timedColorLens = new TAPNLens(true, false, true, false);
            TAPNLens timedStochasticLens = new TAPNLens(true, false, false, true);

            HashMap<TAPNLens, List<String>> netMap = new HashMap<>(){{
                    put(untimedLens, new ArrayList<>());
                    put(timedLens, new ArrayList<>());
                    put(untimedGameLens, new ArrayList<>());
                    put(timedGameLens, new ArrayList<>());
                    put(untimedColorLens, new ArrayList<>());
                    put(timedColorLens, new ArrayList<>());
                    put(timedStochasticLens, new ArrayList<>());
            }};

            for (String filename : nets) {
                if (filename.toLowerCase().endsWith(".tapn")) {
                    final String filenameFinal = filename;

                    InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("Example nets/" + filenameFinal);
                    TAPNLens lens;
                    try {
                        lens = PetriNetTab.getFileLens(file);
                        if (lens == null) {
                            lens = new TAPNLens(true, false, false, false);
                        }
                        TAPNLens tmp = lens;
                        netMap.forEach((v, k) -> {
                            if (v.isTimed() == tmp.isTimed() && v.isGame() == tmp.isGame() && v.isColored() == tmp.isColored() && v.isStochastic() == tmp.isStochastic()) k.add(filename);
                        });
                    } catch (Exception e) {
                        if (netMap.containsKey(timedLens)) netMap.get(timedLens).add(filename);
                        e.printStackTrace();
                    }
                }
            }
            JMenu exampleMenu = new JMenu("Example nets");
            exampleMenu.setIcon(ResourceManager.getIcon("Example.png"));

            int charKey = 'A';
            int modifier = InputEvent.ALT_MASK + InputEvent.SHIFT_MASK;
            exampleMenu.add(addExampleNets(netMap.get(untimedLens), "P/T nets", charKey, modifier));

            modifier = getModifier(modifier, charKey, netMap.get(untimedLens).size());
            charKey = countCharKey(charKey, netMap.get(untimedLens).size());
            exampleMenu.add(addExampleNets(netMap.get(timedLens), "Timed-Arc Petri nets", charKey, modifier));

            modifier = getModifier(modifier, charKey, netMap.get(timedLens).size());
            charKey = countCharKey(charKey, netMap.get(timedLens).size());
            exampleMenu.add(addExampleNets(netMap.get(untimedGameLens), "P/T net games", charKey, modifier));

            modifier = getModifier(modifier, charKey, netMap.get(untimedGameLens).size());
            charKey = countCharKey(charKey, netMap.get(untimedGameLens).size());
            exampleMenu.add(addExampleNets(netMap.get(timedGameLens), "Timed-Arc Petri net games", charKey, modifier));

            modifier = getModifier(modifier, charKey, netMap.get(timedGameLens).size());
            charKey = countCharKey(charKey, netMap.get(timedGameLens).size());
            exampleMenu.add(addExampleNets(netMap.get(untimedColorLens), "Colored P/T nets", charKey, modifier));

            modifier = getModifier(modifier, charKey, netMap.get(untimedColorLens).size());
            charKey = countCharKey(charKey, netMap.get(untimedColorLens).size());
            exampleMenu.add(addExampleNets(netMap.get(timedColorLens), "Timed-Arc Colored Petri nets", charKey, modifier));

            return exampleMenu;
        }
        return null;
    }

    private JMenu addExampleNets(List<String> fileNames, String menuName, int charKey, int modifier) {
        JMenu menu = new JMenu(menuName);

        for (String filename : fileNames) {
            if (filename.toLowerCase().endsWith(".tapn")) {
                final String netname = filename.replace(".tapn", "");
                final String filenameFinal = filename;

                GuiAction tmp = new GuiAction(netname, "Open example file \"" + netname + "\"", KeyStroke.getKeyStroke(charKey, modifier)) {
                    public void actionPerformed(ActionEvent arg0) {
                        InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("Example nets/" + filenameFinal);
                        try {
                            PetriNetTab net = PetriNetTab.createNewTabFromInputStream(file, netname);
                            guiFrameController.ifPresent(o -> o.setEnableSidepanel(net));
                            guiFrameController.ifPresent(o -> o.openTab(net));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                };

                tmp.putValue(Action.SMALL_ICON, ResourceManager.getIcon("Net.png"));
                menu.add(tmp);

                if (charKey == 'Z') {
                    charKey = '0';
                } else if (charKey == '9') {
                    charKey = 'A';
                    modifier = InputEvent.ALT_MASK;
                } else {
                    charKey++;
                }
            }
        }
        return menu;
    }

    private int countCharKey(int previousKey, int previousSize) {
        int currentKey = previousKey + previousSize;
        int addedSize = 0;
        int missingSize = 0;

        if (currentKey > 'Z') {
            addedSize = 'Z' - previousKey;
            missingSize = previousSize - addedSize;
            currentKey = ('0' - 1) + missingSize;
            if (currentKey > '9') {
                missingSize -= 10;
                currentKey = countCharKey('A'-1, missingSize);
            }
        } else if (currentKey > '9' && currentKey < 'A') {
            addedSize = '9' - previousKey;
            missingSize = previousSize - addedSize;
            currentKey = ('A' - 1) + missingSize;
            if (currentKey > 'Z') {
                missingSize -= 26;
                currentKey = countCharKey('0'-1, missingSize);
            }
        }
        return currentKey;
    }

    private int getModifier(int currentModifier, int charKey, int difference) {
        if (currentModifier == InputEvent.ALT_MASK + InputEvent.SHIFT_MASK) {
            if (charKey + difference > 'Z') {
                int used = 'Z' - charKey;
                if (difference - used > 10) {
                    return InputEvent.ALT_MASK;
                }
            } else if (charKey < 'A' && (charKey + difference) > '9') {
               return InputEvent.ALT_MASK;
            }
        }
        return currentModifier;
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
            URL dirURL = Thread.currentThread().getContextClassLoader().getResource("Example nets/");
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                /* A file path: easy enough */
                nets = new File(dirURL.toURI()).list();
            }

            if (dirURL == null) {
                /*
                 * In case of a jar file, we can't actually find a directory. Have to assume the
                 * same jar as clazz.
                 */
                String me = this.getName().replace(".", "/") + ".class";
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
                    if (name.startsWith("Example nets/")) { // filter according to the path
                        String entry = name.substring("Example nets/".length());
                        int checkSubdir = entry.indexOf('/');
                        if (checkSubdir >= 0) {
                            // if it is a subdirectory, we just return the directory name
                            entry = entry.substring(0, checkSubdir);
                        }
                        result.add(entry);
                    }
                }
                nets = result.toArray(new String[0]);
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

    /**
     * @Deprecated Use tab.getTabTitle() instead
     */
    @Deprecated
    public String getCurrentTabName() {
        return appTab.getTitleAt(appTab.getSelectedIndex());
    }

    //XXX: Needs further cleanup
    @Deprecated
    public boolean isShowingDelayEnabledTransitions() {
        return showDelayEnabledTransitionsAction.isSelected();
    }

    public boolean showZeroToInfinityIntervals() {
        return Preferences.getInstance().getShowZeroInfIntervals();
    }

    public boolean showTokenAge() {
        return Preferences.getInstance().getShowTokenAge();
    }

    public boolean showColoredTokens() {
        return Preferences.getInstance().getShowColoredTokens();
    }

    /**
     * @Deprecated Only to be used in TAPAALGUI
     */
    @Deprecated
    public int getSelectedTabIndex() {
        return appTab.getSelectedIndex();
    }

    //XXX usage of this function should be replaced by currentTab and not assume PetriNetTab
    @Deprecated
    private PetriNetTab getCurrentTab() {
        return TAPAALGUI.getCurrentTab();
    }

    @Override
    public void setFeatureInfoText(boolean[] features) {
        if (features != null) {
            timeFeatureOptions.setSelectedIndex(features[0] ? 1 : 0);
            gameFeatureOptions.setSelectedIndex(features[1] ? 1 : 0);
            colorFeatureOptions.setSelectedIndex(features[2] ? 1 : 0);
            stochasticFeatureOptions.setSelectedIndex(features[3] ? 1 : 0);
        }
        refreshLensConstraints();
    }

    public void refreshLensConstraints() {
        boolean stochasticEnabled = stochasticFeatureOptions.getSelectedIndex() != 0;
        timeFeatureOptions.setEnabled(!stochasticEnabled);
        gameFeatureOptions.setEnabled(!stochasticEnabled);
        stochasticFeatureOptions.setEnabled(
            timeFeatureOptions.getSelectedIndex() != 0 &&
                gameFeatureOptions.getSelectedIndex() == 0
        );
    }
}
