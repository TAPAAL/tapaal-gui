package pipe.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.apple.eawt.Application;
import dk.aau.cs.gui.*;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import net.tapaal.Preferences;
import com.sun.jna.Platform;
import net.tapaal.TAPAAL;
import net.tapaal.helpers.Reference.MutableReference;
import net.tapaal.swinghelpers.ExtendedJTabbedPane;
import net.tapaal.swinghelpers.ToggleButtonWithoutText;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.NetWriter;
import pipe.dataLayer.Template;
import pipe.gui.Pipe.ElementType;
import pipe.gui.action.GuiAction;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPathPoint;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.handler.SpecialMacHandler;
import pipe.gui.undo.ChangeSpacingEdit;
import pipe.gui.widgets.filebrowser.FileBrowser;
import pipe.gui.widgets.QueryDialog;
import pipe.gui.widgets.WorkflowDialog;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.BatchProcessingDialog;
import dk.aau.cs.gui.TabComponent;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.smartDraw.SmartDrawDialog;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.ModelLoader;
import dk.aau.cs.io.PNMLoader;
import dk.aau.cs.io.ResourceManager;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.simulation.ShortestDelayMode;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;


public class GuiFrame extends JFrame implements GuiFrameActions  {

	private static final long serialVersionUID = 7509589834941127217L;
	// for zoom combobox and dropdown
	private final int[] zoomExamples = { 40, 60, 80, 100,
			120, 140, 160, 180, 200, 300 };
	
	private String frameTitle;

	private Pipe.ElementType mode;

	private int newNameCounter = 1;

	final MutableReference<GuiFrameControllerActions> guiFrameController = new MutableReference<>();

	private ExtendedJTabbedPane<TabContent> appTab;

	private StatusBar statusBar;
	private JMenuBar menuBar;
	private JToolBar drawingToolBar;
	private JComboBox<String> zoomComboBox;

	private GuiAction createAction;
	private GuiAction openAction;
	private GuiAction closeAction;
	private GuiAction saveAction;
	private GuiAction saveAsAction;
	private GuiAction exitAction;
	private GuiAction printAction;
	private GuiAction importPNMLAction;
	private GuiAction importSUMOAction;
	private GuiAction importXMLAction;
	private GuiAction exportPNGAction;
	private GuiAction exportPSAction;
	private GuiAction exportToTikZAction;
	private GuiAction exportToPNMLAction;
	private GuiAction exportToXMLAction;
	private GuiAction exportTraceAction;
	private GuiAction importTraceAction;
	private GuiAction exportBatchAction;

	private GuiAction /* copyAction, cutAction, pasteAction, */undoAction, redoAction;
	private GuiAction toggleGrid;
	private GuiAction alignToGrid;
	private GuiAction netStatisticsAction;
	private GuiAction batchProcessingAction;
	private GuiAction engineSelectionAction;
	private GuiAction clearPreferencesAction;

	private GuiAction verifyAction;
	private GuiAction workflowDialogAction;
	private GuiAction smartDrawAction;
	private GuiAction stripTimeDialogAction;

	private GuiAction zoomOutAction;
	private GuiAction zoomInAction;
	private GuiAction zoomToAction;

	private GuiAction incSpacingAction;
	private GuiAction decSpacingAction;
	public GuiAction deleteAction;

	private GuiAction annotationAction;
	private GuiAction inhibarcAction;
	private GuiAction transAction;
	private GuiAction tokenAction;
	private GuiAction selectAction;
	private GuiAction deleteTokenAction;
	private GuiAction timedPlaceAction;

	private GuiAction timedArcAction;
	private GuiAction transportArcAction;

	private GuiAction showTokenAgeAction;
	private GuiAction showComponentsAction;
	private GuiAction showQueriesAction;
	private GuiAction showConstantsAction;
	private GuiAction showZeroToInfinityIntervalsAction;
	private GuiAction showEnabledTransitionsAction;
	private GuiAction showDelayEnabledTransitionsAction;
	private GuiAction showToolTipsAction;
	private GuiAction showAdvancedWorkspaceAction;
	private GuiAction showSimpleWorkspaceAction;
	private GuiAction saveWorkSpaceAction;
	private GuiAction showAboutAction;
	private GuiAction showHomepage;
	private GuiAction showAskQuestionAction;
	private GuiAction showReportBugAction;
	private GuiAction showFAQAction;
	private GuiAction checkUpdate;
	

	private GuiAction selectAllAction;

	private GuiAction startAction;
	public GuiAction stepforwardAction;
	public GuiAction stepbackwardAction;
	private GuiAction timeAction;
	private GuiAction delayFireAction;
	private GuiAction prevcomponentAction;
	private GuiAction nextcomponentAction;


	public enum GUIMode {
		draw, animation, noNet
	}

	private JCheckBoxMenuItem showZeroToInfinityIntervalsCheckBox;
	private JCheckBoxMenuItem showTokenAgeCheckBox;

	//XXX should be private and should prop. live in controllers not GUI, tmp while refactoring //kyrke 2019-11-05
	boolean showComponents = true;
	boolean showConstants = true;
	boolean showQueries = true;
	boolean showEnabledTransitions = true;
	boolean showDelayEnabledTransitions = true;
	private boolean showToolTips = true;
	private boolean showZeroToInfinityIntervals = true;
	private boolean showTokenAge = true;

	private JMenu zoomMenu;

	public GuiFrame(String title) {
		// HAK-arrange for frameTitle to be initialized and the default file
		// name to be appended to basic window title

		frameTitle = title;
		setTitle(null);
		trySetLookAndFeel();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize.width * 80 / 100, screenSize.height * 80 / 100);
		this.setLocationRelativeTo(null);
		this.setMinimumSize(new Dimension(825, 480));

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		//XXX: Moved appTab from creategui needs further refacotring
		//kyrke 2018-05-20
		appTab = new ExtendedJTabbedPane<TabContent>() {
			@Override
			public Component generator() {
				return new TabComponent(this) {
                    @Override
                    protected void closeTab(int index) {
                        GuiFrame.this.closeTab(index);
                    }
                };
			}
		};
		getContentPane().add(appTab);
		setChangeListenerOnTab(); // sets Tab properties

		Grid.enableGrid();

		loadPrefrences();

		buildMenus();

		// Status bar...
		statusBar = new StatusBar();
		getContentPane().add(statusBar, BorderLayout.PAGE_END);

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

		//XXX 2018-05-23 kyrke: Moved from CreatGUI (static), needs further refactoring to seperate conserns
		Verifyta.trySetup();
		VerifyTAPN.trySetup();
		VerifyTAPNDiscreteVerification.trySetup();
		VerifyPN.trySetup();

	}

	private void trySetLookAndFeel() {
		try {
			// Set the Look and Feel native for the system.
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			//XXX Bug made us select non-native L&F for Windows, bug seems to be no longer present.
//			if(UIManager.getLookAndFeel().getName().equals("Windows")){
//				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//					if ("Nimbus".equals(info.getName())) {
//						UIManager.setLookAndFeel(info.getClassName());
//						UIManager.getLookAndFeelDefaults().put("List[Selected].textBackground", new Color(57, 105, 138));
//						UIManager.getLookAndFeelDefaults().put("List[Selected].textForeground", new Color(255,255,255));
//						UIManager.getLookAndFeelDefaults().put("List.background", new Color(255,255,255));
//
//						break;
//					}
//				}
//			}

			// Set enter to select focus button rather than default (makes ENTER selection key on all LAFs)
			UIManager.put("Button.focusInputMap", new UIDefaults.LazyInputMap(new Object[]
					{
					"SPACE", "pressed",
					"released SPACE", "released",
					"ENTER", "pressed",
					"released ENTER", "released"
					}));
			UIManager.put("OptionPane.informationIcon", ResourceManager.infoIcon());
                        UIManager.put("Slider.paintValue", false);

			// 2010-05-07, Kenneth Yrke Joergensen:
			// If the native look and feel is GTK replace the useless open
			// dialog, with a java-reimplementation.

			if ("GTK look and feel".equals(UIManager.getLookAndFeel().getName())){
				try {
					//Load class to see if its there
					Class.forName("com.google.code.gtkjfilechooser.ui.GtkFileChooserUI", false, this.getClass().getClassLoader());
					UIManager.put("FileChooserUI", "com.google.code.gtkjfilechooser.ui.GtkFileChooserUI");
				} catch (ClassNotFoundException exc){
					Logger.log("Error loading GtkFileChooserUI Look and Feel, using default jvm GTK look and feel instead");
				}

			}


		} catch (Exception exc) {
			Logger.log("Error loading L&F: " + exc);
		}

		if (Platform.isMac()){

			try{
				new SpecialMacHandler(guiFrameController);
			} catch (NoClassDefFoundError e) {
				//Failed loading special mac handler, ignore and run program without MacOS integration
			}

			//XXX Refactor to sperate function, only a test to see of this fixes issues for TAPAAL on Java9 bug #1764383
			Application app = Application.getApplication();
			try {
				Image appImage;
				appImage = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(
					CreateGui.imgPath + "icon.png"));
				app.setDockIconImage(appImage);
			} catch (IOException e) {
				Logger.log("Error loading Image");
			}

			//Set specific settings
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", TAPAAL.TOOLNAME);

			// Use native file chooser
			System.setProperty("apple.awt.fileDialogForDirectories", "false");

			// Grow size of boxes to add room for the resizer
			System.setProperty("apple.awt.showGrowBox", "true");

		}

		this.setIconImage(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "icon.png")).getImage());
	}

	private void loadPrefrences() {
		Preferences prefs = Preferences.getInstance();

		QueryDialog.setAdvancedView(prefs.getAdvancedQueryView());
		TabContent.setEditorModelRoot(prefs.getEditorModelRoot());
		TabContent.setSimulatorModelRoot(prefs.getSimulatorModelRoot());
		showComponents = prefs.getShowComponents();
		showQueries = prefs.getShowQueries();
		showConstants = prefs.getShowConstants();

		showEnabledTransitions = prefs.getShowEnabledTransitions();
		showDelayEnabledTransitions = prefs.getShowDelayEnabledTransitions();
		DelayEnabledTransitionControl.setDefaultDelayMode(prefs.getDelayEnabledTransitionDelayMode());
		DelayEnabledTransitionControl.setDefaultGranularity(prefs.getDelayEnabledTransitionGranularity());
		DelayEnabledTransitionControl.setDefaultIsRandomTransition(prefs.getDelayEnabledTransitionIsRandomTransition());

		showToolTips = prefs.getShowToolTips();

		if(showZeroToInfinityIntervals() != prefs.getShowZeroInfIntervals()){
			toggleShowZeroToInfinityIntervals();
		}

		if(showTokenAge() != prefs.getShowTokenAge()){
			toggleShowTokenAge();
		}

		Dimension dimension = prefs.getWindowSize();
		if(dimension != null){
			this.setSize(dimension);
		}

	}

	/**
	 * Build the menues and actions
	 **/
	private void buildMenus() {
		menuBar = new JMenuBar();

		int shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		menuBar.add(buildMenuFiles(shortcutkey));
		menuBar.add(buildMenuEdit(shortcutkey));
		menuBar.add(buildMenuView(shortcutkey));
		menuBar.add(buildMenuDraw());
		menuBar.add(buildMenuAnimation());
		menuBar.add(buildMenuTools());
		menuBar.add(buildMenuHelp());

		setJMenuBar(menuBar);

	}

	private JMenu buildMenuEdit(int shortcutkey) {

		/* Edit Menu */
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		editMenu.add( undoAction = new GuiAction("Undo",
				"Undo", KeyStroke.getKeyStroke('Z', shortcutkey)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(TabContentActions::undo);
			}
		});
		
		
		editMenu.add( redoAction = new GuiAction("Redo",
				"Redo", KeyStroke.getKeyStroke('Y', shortcutkey)) {
			@Override
			public void actionPerformed(ActionEvent e) {

				currentTab.ifPresent(TabContentActions::redo);

			}
		});
		editMenu.addSeparator();

		editMenu.add( deleteAction = new GuiAction("Delete", "Delete selection", "DELETE") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				currentTab.ifPresent(TabContentActions::deleteSelection);
			}

		});

		// Bind delete to backspace also
		editMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("BACK_SPACE"), "Delete");
		editMenu.getActionMap().put("Delete", deleteAction);

		editMenu.addSeparator();


		editMenu.add(selectAllAction = new GuiAction("Select all", "Select all components",  KeyStroke.getKeyStroke('A', shortcutkey )) {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(TabContentActions::selectAll);
			}
		});
		editMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('A', shortcutkey), "SelectAll");
		editMenu.getActionMap().put("SelectAll", selectAllAction);

		return editMenu;
	}

	private JMenu buildMenuDraw() {
		/* Draw menu */
		JMenu drawMenu = new JMenu("Draw");
		drawMenu.setMnemonic('D');

		drawMenu.add( selectAction = new GuiAction("Select", "Select components (S)", "S", true) {
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(o->o.setMode(ElementType.SELECT));
			}
		});
		drawMenu.addSeparator();

		drawMenu.add( timedPlaceAction = new GuiAction("Place", "Add a place (P)", "P", true) {
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(o->o.setMode(ElementType.TAPNPLACE));
			}
		});

		drawMenu.add( transAction = new GuiAction("Transition", "Add a transition (T)", "T", true) {
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(o->o.setMode(ElementType.TAPNTRANS));
			}
		});

		drawMenu.add( timedArcAction = new GuiAction("Arc", "Add an arc (A)", "A", true) {
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(o->o.setMode(ElementType.TAPNARC));
			}
		});

		drawMenu.add( transportArcAction = new GuiAction("Transport arc", "Add a transport arc (R)", "R", true) {
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(o->o.setMode(ElementType.TRANSPORTARC));
			}
		});

		drawMenu.add( inhibarcAction = new GuiAction("Inhibitor arc", "Add an inhibitor arc (I)", "I", true) {
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(o->o.setMode(ElementType.TAPNINHIBITOR_ARC));
			}
		});

		drawMenu.add(annotationAction = new GuiAction("Annotation", "Add an annotation (N)", "N", true) {
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(o->o.setMode(ElementType.ANNOTATION));
			}
		});

		drawMenu.addSeparator();

		drawMenu.add( tokenAction = new GuiAction("Add token", "Add a token (+)", "typed +", true) {
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(o->o.setMode(ElementType.ADDTOKEN));
			}
		});

		drawMenu.add( deleteTokenAction = new GuiAction("Delete token", "Delete a token (-)", "typed -", true) {
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(o->o.setMode(ElementType.DELTOKEN));
			}
		});
		return drawMenu;
	}

	private JMenu buildMenuView(int shortcutkey) {
		/* ViewMenu */
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');

		zoomMenu = new JMenu("Zoom");
		zoomMenu.setIcon(new ImageIcon(Thread.currentThread()
				.getContextClassLoader().getResource(
						CreateGui.imgPath + "Zoom.png")));
		
		addZoomMenuItems(zoomMenu);

		viewMenu.add( zoomInAction = new GuiAction("Zoom in",
				"Zoom in by 10% ", KeyStroke.getKeyStroke('J', shortcutkey)) {
			@Override
			public void actionPerformed(ActionEvent e) {
                currentTab.ifPresent(TabContentActions::zoomIn);
			}
		});

		viewMenu.add( zoomOutAction = new GuiAction("Zoom out",
				"Zoom out by 10% ", KeyStroke.getKeyStroke('K', shortcutkey)) {
			@Override
			public void actionPerformed(ActionEvent e) {
                currentTab.ifPresent(TabContentActions::zoomOut);
			}
		});
		viewMenu.add(zoomMenu);

		viewMenu.addSeparator();
		
		viewMenu.add(incSpacingAction = new GuiAction("Increase node spacing", "Increase spacing by 20% ",
				KeyStroke.getKeyStroke('U', shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				double factor = 1.25;
				changeSpacing(factor);
				getCurrentTab().getUndoManager().addNewEdit(new ChangeSpacingEdit(factor));
			}
		});

		viewMenu.add(decSpacingAction = new GuiAction("Decrease node spacing", "Decrease spacing by 20% ",
				KeyStroke.getKeyStroke("shift U")) {
			public void actionPerformed(ActionEvent arg0) {
				double factor = 0.8;
				changeSpacing(factor);
				getCurrentTab().getUndoManager().addNewEdit(new ChangeSpacingEdit(factor));
			}
		});
		

		
		viewMenu.addSeparator();
		
		viewMenu.add( toggleGrid = new GuiAction("Cycle grid",
				"Change the grid size", "G") {
					public void actionPerformed(ActionEvent arg0) {
						Grid.increment();
						repaint();			
					}		
		});
		
		viewMenu.add(alignToGrid = new GuiAction("Align To Grid", "Align Petri net objects to current grid", 
				KeyStroke.getKeyStroke("shift G")) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Grid.alignPNObjectsToGrid();
			}
		});
		

		viewMenu.addSeparator();

		showComponentsAction = new GuiAction("Display components", "Show/hide the list of components.",
				KeyStroke.getKeyStroke('1', shortcutkey), true) {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleComponents();
			}
		};
		addCheckboxMenuItem(viewMenu, showComponents, showComponentsAction);

		showQueriesAction = new GuiAction("Display queries", "Show/hide verification queries.",
				KeyStroke.getKeyStroke('2', shortcutkey), true) {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleQueries();
			}
		};
		addCheckboxMenuItem(viewMenu, showQueries, showQueriesAction);

		showConstantsAction = new GuiAction("Display constants", "Show/hide global constants.",
				KeyStroke.getKeyStroke('3', shortcutkey), true) {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleConstants();
			}
		};
		addCheckboxMenuItem(viewMenu, showConstants, showConstantsAction);

		showEnabledTransitionsAction = new GuiAction("Display enabled transitions",
				"Show/hide the list of enabled transitions", KeyStroke.getKeyStroke('4', shortcutkey), true) {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleEnabledTransitionsList();
			}
		};
		addCheckboxMenuItem(viewMenu, showEnabledTransitions, showEnabledTransitionsAction);

		showDelayEnabledTransitionsAction = new GuiAction("Display future-enabled transitions",
				"Highlight transitions which can be enabled after a delay", KeyStroke.getKeyStroke('5', shortcutkey),
				true) {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleDelayEnabledTransitions();
			}
		};
		addCheckboxMenuItem(viewMenu, showDelayEnabledTransitions, showDelayEnabledTransitionsAction);

		showZeroToInfinityIntervalsAction = new GuiAction("Display intervals [0,inf)",
				"Show/hide intervals [0,inf) that do not restrict transition firing in any way.",
				KeyStroke.getKeyStroke('6', shortcutkey), true) {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleZeroToInfinityIntervals();
			}
		};
		showZeroToInfinityIntervalsCheckBox = addCheckboxMenuItem(viewMenu, showZeroToInfinityIntervals(),
				showZeroToInfinityIntervalsAction);

		showToolTipsAction = new GuiAction("Display tool tips", "Show/hide tool tips when mouse is over an element",
				KeyStroke.getKeyStroke('7', shortcutkey), true) {
			@Override
			public void actionPerformed(ActionEvent e) {
				showToolTips(!showToolTips);
			}
		};
		addCheckboxMenuItem(viewMenu, showToolTips, showToolTipsAction);

		showTokenAgeAction = new GuiAction("Display token age",
				"Show/hide displaying the token age 0.0 (when hidden the age 0.0 is drawn as a dot)",
				KeyStroke.getKeyStroke('8', shortcutkey), true) {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleTokenAge();
			}
		};
		showTokenAgeCheckBox = addCheckboxMenuItem(viewMenu, showTokenAge(), showTokenAgeAction);

		viewMenu.addSeparator();

		viewMenu.add( showSimpleWorkspaceAction = new GuiAction("Show simple workspace", "Show only the most important panels", false) {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAdvancedWorkspace(false);
			}
		});
		viewMenu.add( showAdvancedWorkspaceAction = new GuiAction("Show advanced workspace", "Show all panels", false) {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAdvancedWorkspace(true);
			}
		});
		viewMenu.add( saveWorkSpaceAction = new GuiAction("Save workspace", "Save the current workspace as the default one", false) {
			@Override
			public void actionPerformed(ActionEvent e) {
				guiFrameController.ifPresent(GuiFrameControllerActions::saveWorkspace);
			}
		});
		return viewMenu;
	}

	private JMenu buildMenuAnimation() {
		/* Simulator */
		JMenu animateMenu = new JMenu("Simulator");
		animateMenu.setMnemonic('A');
		animateMenu.add( startAction = new GuiAction(
				"Simulation mode", "Toggle simulation mode (M)",
				"M", true) {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(TabContentActions::toggleAnimationMode);
			}
		});
		
		
		animateMenu.add( stepbackwardAction = new GuiAction("Step backward",
				"Step backward", "pressed LEFT") {
			@Override
			public void actionPerformed(ActionEvent e) {
                currentTab.ifPresent(TabContentActions::stepBackwards);
			}
		});
		animateMenu.add(
				stepforwardAction = new GuiAction("Step forward", "Step forward", "pressed RIGHT") {
					@Override
					public void actionPerformed(ActionEvent e) {
                        currentTab.ifPresent(TabContentActions::stepForward);
					}
				});

		animateMenu.add( timeAction = new GuiAction("Delay one time unit",
				"Let time pass one time unit", "W") {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(TabContentActions::timeDelay);
			}
		});

		animateMenu.add( delayFireAction = new GuiAction("Delay and fire",
				"Delay and fire selected transition", "F") {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(TabContentActions::delayAndFire);
			}
		});

		animateMenu.add( prevcomponentAction = new GuiAction("Previous component",
				"Previous component", "pressed UP") {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(TabContentActions::previousComponent);
			}
		});

		animateMenu.add( nextcomponentAction = new GuiAction("Next component",
				"Next component", "pressed DOWN") {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(TabContentActions::nextComponent);
			}
		});

		animateMenu.addSeparator();

		animateMenu.add( exportTraceAction = new GuiAction("Export trace",
				"Export the current trace","") {
					public void actionPerformed(ActionEvent arg0) {
						currentTab.ifPresent(TabContentActions::exportTrace);
					}		
		});
		animateMenu.add( importTraceAction = new GuiAction("Import trace",
				"Import trace to simulator",""){
			public void actionPerformed(ActionEvent arg0) {
				currentTab.ifPresent(TabContentActions::importTrace);
			}		
		});


		return animateMenu;
	}

	private JMenu buildMenuHelp() {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');

		helpMenu.add(showHomepage = new GuiAction("Visit TAPAAL home", "Visit the TAPAAL homepage") {
			public void actionPerformed(ActionEvent arg0) {
				guiFrameController.ifPresent(o->o.openURL("http://www.tapaal.net"));
			}
		});

		helpMenu.add(checkUpdate = new GuiAction("Check for updates", "Check if there is a new version of TAPAAL") {
			public void actionPerformed(ActionEvent arg0) {
				guiFrameController.ifPresent(GuiFrameControllerActions::checkForUpdate);
			}
		});

		helpMenu.addSeparator();

		helpMenu.add(showFAQAction = new GuiAction("Show FAQ", "See TAPAAL frequently asked questions") {
			public void actionPerformed(ActionEvent arg0) {
				guiFrameController.ifPresent(o->o.openURL("https://answers.launchpad.net/tapaal/+faqs"));
			}
		});
		helpMenu.add(showAskQuestionAction = new GuiAction("Ask a question", "Ask a question about TAPAAL") {
			public void actionPerformed(ActionEvent arg0) {
				guiFrameController.ifPresent(o->o.openURL("https://answers.launchpad.net/tapaal/+addquestion"));
			}
		});
		helpMenu.add(showReportBugAction = new GuiAction("Report bug", "Report a bug in TAPAAL") {
			public void actionPerformed(ActionEvent arg0) {
				guiFrameController.ifPresent(o->o.openURL("https://bugs.launchpad.net/tapaal/+filebug"));
			}
		});

		helpMenu.addSeparator();

		helpMenu.add(showAboutAction = new GuiAction("About", "Show the About menu") {
			public void actionPerformed(ActionEvent arg0) {
				guiFrameController.ifPresent(GuiFrameControllerActions::showAbout);
			}
		});
		return helpMenu;
	}

	

	private JMenu buildMenuTools() {
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');

		int shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		verifyAction = new GuiAction("Verify query", "Verifies the currently selected query", KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				currentTab.ifPresent(TabContentActions::verifySelectedQuery);
			}
		};
		toolsMenu.add(verifyAction).setMnemonic('m');

		netStatisticsAction = new GuiAction("Net statistics", "Shows information about the number of transitions, places, arcs, etc.", KeyStroke.getKeyStroke(KeyEvent.VK_I, shortcutkey)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(TabContentActions::showStatistics);
			}
		};
		toolsMenu.add(netStatisticsAction).setMnemonic('i');


		//JMenuItem batchProcessing = new JMenuItem("Batch processing");
		JMenuItem batchProcessing = new JMenuItem(batchProcessingAction = new GuiAction("Batch processing", "Batch verification of multiple nets and queries", KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcutkey)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkForSaveAll()){
					BatchProcessingDialog.showBatchProcessingDialog(new JList(new DefaultListModel()));
				}
			}
		});
		batchProcessing.setMnemonic('b');
		toolsMenu.add(batchProcessing);

		JMenuItem workflowDialog = new JMenuItem(workflowDialogAction = new GuiAction("Workflow analysis", "Analyse net as a TAWFN", KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutkey)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTab.ifPresent(TabContentActions::workflowAnalyse);
			}
		});
		workflowDialog.setMnemonic('f');
		toolsMenu.add(workflowDialog);
		
		JMenuItem smartDrawDialog = new JMenuItem(smartDrawAction = new GuiAction("Automatic Net Layout", "Rearrange the Petri net objects", KeyStroke.getKeyStroke('D', KeyEvent.SHIFT_DOWN_MASK)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				SmartDrawDialog.showSmartDrawDialog();
			}
		});
		smartDrawDialog.setMnemonic('D');
		toolsMenu.add(smartDrawDialog);
		
		//Stip off timing information
		JMenuItem stripTimeDialog = new JMenuItem(stripTimeDialogAction = new GuiAction("Remove timing information", "Remove all timing information from the net in the active tab and open it as a P/T net in a new tab.", KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutkey)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				duplicateAndConvertUntimed();
			}
		});
		stripTimeDialog.setMnemonic('e');
		toolsMenu.add(stripTimeDialog);

		toolsMenu.addSeparator();

		JMenuItem engineSelection = new JMenuItem(engineSelectionAction = new GuiAction("Engine selection", "View and modify the location of verification engines") {
			@Override
			public void actionPerformed(ActionEvent e) {
				guiFrameController.ifPresent(GuiFrameControllerActions::showEngineDialog);
			}
		});
		toolsMenu.add(engineSelection);



		JMenuItem clearPreferences = new JMenuItem(clearPreferencesAction = new GuiAction("Clear all preferences", "Clear all custom preferences to default") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				guiFrameController.ifPresent(GuiFrameControllerActions::clearPreferences);
			}
		});
		toolsMenu.add(clearPreferences);

		return toolsMenu;
	}

	private void duplicateAndConvertUntimed() {
		TabContent duplicate = duplicateTab((TabContent) appTab.getSelectedComponent());
		convertToUntimedTab(duplicate);
	}

	private void showAdvancedWorkspace(boolean advanced){
		QueryDialog.setAdvancedView(advanced);
		showComponents(advanced);
		showConstants(advanced);
		showConstantsAction.setSelected(advanced);
		showComponentsAction.setSelected(advanced);
		showQueriesAction.setSelected(true);
		//Queries and enabled transitions should always be shown
		showQueries(true);
		showEnabledTransitionsList(true);
		showToolTips(true);

		getCurrentTab().setResizeingDefault();

		if(advanced) {
			
			if(!showZeroToInfinityIntervals()){
				showZeroToInfinityIntervalsCheckBox.doClick();
			}
			if(!showTokenAge()){
				showTokenAgeCheckBox.doClick();
			}
			
		} else {
			if(showZeroToInfinityIntervals()) {
				showZeroToInfinityIntervalsCheckBox.doClick();
			}
			if(showTokenAge()) {
				showTokenAgeCheckBox.doClick();
			}
		}

		//Delay-enabled Transitions
		//showDelayEnabledTransitions(advanced);
		DelayEnabledTransitionControl.getInstance().setValue(new BigDecimal("0.1"));
		DelayEnabledTransitionControl.getInstance().setDelayMode(ShortestDelayMode.getInstance());
		DelayEnabledTransitionControl.getInstance().setRandomTransitionMode(false);
	}

	private void buildToolbar() {

		//XXX .setRequestFocusEnabled(false), removed "border" around tollbar buttons when selcted/focus
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
		/*
		 * Removed copy/past button toolBar.addSeparator();
		 * toolBar.add(cutAction); toolBar.add(copyAction);
		 * toolBar.add(pasteAction);
		 */

		// Undo/redo
		toolBar.addSeparator();
		toolBar.add(deleteAction).setRequestFocusEnabled(false);
		toolBar.add(undoAction).setRequestFocusEnabled(false);
		toolBar.add(redoAction).setRequestFocusEnabled(false);

		// Zoom
		toolBar.addSeparator();
		toolBar.add(zoomOutAction).setRequestFocusEnabled(false);
		addZoomComboBox(toolBar, zoomToAction = new GuiAction("Zoom", "Select zoom percentage ", "") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedZoomLevel = (String) zoomComboBox.getSelectedItem();
				//parse selected zoom level, and strip of %.
				int newZoomLevel = Integer.parseInt(selectedZoomLevel.replace("%", ""));

				currentTab.ifPresent(o->o.zoomTo(newZoomLevel));
			}
		});
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

		// Normal arraw
		drawingToolBar.add(new ToggleButtonWithoutText(selectAction));


		// Drawing elements
		drawingToolBar.addSeparator();
		drawingToolBar.add(new ToggleButtonWithoutText(timedPlaceAction));
		drawingToolBar.add(new ToggleButtonWithoutText(transAction));
		drawingToolBar.add(new ToggleButtonWithoutText(timedArcAction));
		drawingToolBar.add(new ToggleButtonWithoutText(transportArcAction));
		drawingToolBar.add(new ToggleButtonWithoutText(inhibarcAction));

		drawingToolBar.add(new ToggleButtonWithoutText(annotationAction));

		// Tokens
		drawingToolBar.addSeparator();
		drawingToolBar.add(new ToggleButtonWithoutText(tokenAction));
		drawingToolBar.add(new ToggleButtonWithoutText(deleteTokenAction));

		// Create panel to put toolbars in
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		// Add toolbars to pane
		toolBarPanel.add(toolBar);
		toolBarPanel.add(drawingToolBar);

		// Create a toolBarPaneltmp usign broderlayout and a spacer to get
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

	/**
	 * @author Ben Kirby Takes the method of setting up the Zoom menu out of the
	 *         main buildMenus method.
	 * @param zoomMenu
	 *            - the menu to add the submenu to
	 */
	private void addZoomMenuItems(JMenu zoomMenu) {
		for (int i = 0; i <= zoomExamples.length - 1; i++) {

			final int zoomper = zoomExamples[i];
			GuiAction newZoomAction = new GuiAction(zoomExamples[i] + "%", "Select zoom percentage", "") {
				@Override
				public void actionPerformed(ActionEvent e) {
					currentTab.ifPresent(o->o.zoomTo(zoomper));
				}
			};

			//JMenuItem newItem = new JMenuItem(a);

			zoomMenu.add(newZoomAction);
		}


	}

	/**
	 * @author Ben Kirby Just takes the long-winded method of setting up the
	 *         ComboBox out of the main buildToolbar method. Could be adapted
	 *         for generic addition of comboboxes
	 * @param toolBar
	 *            the JToolBar to add the button to
	 * @param action
	 *            the action that the ZoomComboBox performs
	 */
	private void addZoomComboBox(JToolBar toolBar, Action action) {
		Dimension zoomComboBoxDimension = new Dimension(75, 28);

		String[] zoomExamplesStrings = new String[zoomExamples.length];
		int i;
		for (i=0; i < zoomExamples.length; i++) {
			zoomExamplesStrings[i] = zoomExamples[i] + "%";
		}

		zoomComboBox = new JComboBox<String>(zoomExamplesStrings);
		zoomComboBox.setEditable(true);
		zoomComboBox.setSelectedItem("100%");
		zoomComboBox.setMaximumRowCount(zoomExamples.length);
		zoomComboBox.setMaximumSize(zoomComboBoxDimension);
		zoomComboBox.setMinimumSize(zoomComboBoxDimension);
		zoomComboBox.setPreferredSize(zoomComboBoxDimension);
		zoomComboBox.setAction(action);
		zoomComboBox.setFocusable(false);
		toolBar.add(zoomComboBox);
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
	 * 
	 * Reimplementation of old enableGUIActions(bool status)
	 * 
	 * @author Kenneth Yrke Joergensen (kyrke)
	 * */
	private void enableGUIActions(GUIMode mode) {
		switch (mode) {
		case draw:
			enableAllActions(true);
			exportTraceAction.setEnabled(false);
			importTraceAction.setEnabled(false);

			timedPlaceAction.setEnabled(true);
			timedArcAction.setEnabled(true);
			inhibarcAction.setEnabled(true);
			if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
				transportArcAction.setEnabled(true);
			} else {
				transportArcAction.setEnabled(false);
			}

			annotationAction.setEnabled(true);
			transAction.setEnabled(true);
			tokenAction.setEnabled(true);
			deleteAction.setEnabled(true);
			selectAllAction.setEnabled(true);
			selectAction.setEnabled(true);
			deleteTokenAction.setEnabled(true);

			timeAction.setEnabled(false);
			delayFireAction.setEnabled(false);
			stepbackwardAction.setEnabled(false);
			stepforwardAction.setEnabled(false);
			prevcomponentAction.setEnabled(false);
			nextcomponentAction.setEnabled(false);

			deleteAction.setEnabled(true);
			showEnabledTransitionsAction.setEnabled(false);
			showDelayEnabledTransitionsAction.setEnabled(false);

			verifyAction.setEnabled(getCurrentTab().isQueryPossible());

			smartDrawAction.setEnabled(true);

			workflowDialogAction.setEnabled(true);
			stripTimeDialogAction.setEnabled(true);

			if(getCurrentTab().restoreWorkflowDialog()){
				WorkflowDialog.showDialog();
			}

			statusBar.changeText(statusBar.textforDrawing);
			//Enable editor focus traversal policy
			setFocusTraversalPolicy(new EditorFocusTraversalPolicy());
			fixBug812694GrayMenuAfterSimulationOnMac();
			break;

		case animation:
			enableAllActions(true);

			timedPlaceAction.setEnabled(false);
			timedArcAction.setEnabled(false);
			inhibarcAction.setEnabled(false);
			transportArcAction.setEnabled(false);

			annotationAction.setEnabled(false);
			transAction.setEnabled(false);
			tokenAction.setEnabled(false);
			deleteAction.setEnabled(false);
			selectAllAction.setEnabled(false);
			selectAction.setEnabled(false);
			deleteTokenAction.setEnabled(false);

			showConstantsAction.setEnabled(false);
			showQueriesAction.setEnabled(false);

			// Only enable this if it is not an untimed net.
			if (CreateGui.getModel().netType() != NetType.UNTIMED) {
				timeAction.setEnabled(true);
			}
			delayFireAction.setEnabled(true);
			stepbackwardAction.setEnabled(true);
			stepforwardAction.setEnabled(true);
			prevcomponentAction.setEnabled(true);
			nextcomponentAction.setEnabled(true);

			deleteAction.setEnabled(false);
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
			verifyAction.setEnabled(false);
			
			smartDrawAction.setEnabled(false);
			workflowDialogAction.setEnabled(false);
			stripTimeDialogAction.setEnabled(false);

			// Remove constant highlight
			getCurrentTab().removeConstantHighlights();

			getCurrentTab().getAnimationController().requestFocusInWindow();

			statusBar.changeText(statusBar.textforAnimation);
			//Enable simulator focus traversal policy
			setFocusTraversalPolicy(new SimulatorFocusTraversalPolicy());

			break;
		case noNet:
			exportTraceAction.setEnabled(false);
			importTraceAction.setEnabled(false);
			verifyAction.setEnabled(false);

			timedPlaceAction.setEnabled(false);
			timedArcAction.setEnabled(false);
			inhibarcAction.setEnabled(false);
			transportArcAction.setEnabled(false);

			annotationAction.setEnabled(false);
			transAction.setEnabled(false);
			tokenAction.setEnabled(false);
			deleteAction.setEnabled(false);
			selectAllAction.setEnabled(false);
			selectAction.setEnabled(false);
			deleteTokenAction.setEnabled(false);

			timeAction.setEnabled(false);
			delayFireAction.setEnabled(false);
			stepbackwardAction.setEnabled(false);
			stepforwardAction.setEnabled(false);

			deleteAction.setEnabled(false);
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
			prevcomponentAction.setEnabled(false);
			nextcomponentAction.setEnabled(false);
			
			smartDrawAction.setEnabled(false);
			workflowDialogAction.setEnabled(false);
			stripTimeDialogAction.setEnabled(false);

			enableAllActions(false);

			// Disable All Actions
			statusBar.changeText(statusBar.textforNoNet);
			setFocusTraversalPolicy(null);

			break;
		}

	}

	/**
	 * Helperfunction for disabeling/enabeling all actions when we are in noNet GUImode
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
		showConstantsAction.setEnabled(enable);
		showQueriesAction.setEnabled(enable);
		showZeroToInfinityIntervalsAction.setEnabled(enable);
		showEnabledTransitionsAction.setEnabled(enable);
		showDelayEnabledTransitionsAction.setEnabled(enable);
		showToolTipsAction.setEnabled(enable);
		showTokenAgeAction.setEnabled(enable);
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

		//This event will only fire if the tab index is changed, so it won't trigger if once
		// also if code calls setSelectedIndex(index), thereby avoiding a loop.
		appTab.addChangeListener(e -> {

			int index = appTab.getSelectedIndex();
			changeToTab(index);

		});
	}


	Optional<TabContentActions> currentTab = Optional.empty();
	//TODO: 2018-05-07 //kyrke Create CloseTab function, used to close a tab
	//XXX: Temp solution to call getCurrentTab to get new new selected tab (should use index) --kyrke 2019-07-08
	private void changeToTab(int index) {

		//De-register old model
		currentTab.ifPresent(t -> t.setApp(null));

		//Set current tab
		currentTab = Optional.ofNullable(getCurrentTab());

		//Change tab event will only fire if index != currentIndex, to changing it via setSelectIndex will not
		// create a tabChanged event loop.
		appTab.setSelectedIndex(index);


		if (getCurrentTab() != null) {
			currentTab.ifPresent(t->t.setApp(this));

			setTitle(appTab.getTitleAt(index));

		} else {
			setTitle(null);
		}
	}




	private void showQueries(boolean enable){
		showQueries = enable;
		getCurrentTab().showQueries(enable);

	}
	private void toggleQueries(){
		showQueries(!showQueries);
	}

	private void showConstants(boolean enable){
		showConstants = enable;
		getCurrentTab().showConstantsPanel(enable);

	}
	private void toggleConstants(){
		showConstants(!showConstants);
	}

	private void showToolTips(boolean enable){
		showToolTips = enable;
		Preferences.getInstance().setShowToolTips(showToolTips);

		ToolTipManager.sharedInstance().setEnabled(enable);
		ToolTipManager.sharedInstance().setInitialDelay(400);
		ToolTipManager.sharedInstance().setReshowDelay(800);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
	}

	private void toggleTokenAge(){
		toggleShowTokenAge();
		Preferences.getInstance().setShowTokenAge(showTokenAge());
		getCurrentTab().drawingSurface().repaintAll();
	}

	private void toggleZeroToInfinityIntervals() {
		toggleShowZeroToInfinityIntervals();
		Preferences.getInstance().setShowZeroInfIntervals(showZeroToInfinityIntervals());
		getCurrentTab().drawingSurface().repaintAll();
	}

	private void showComponents(boolean enable){
		showComponents = enable;
		getCurrentTab().showComponents(enable);

	}
	private void toggleComponents(){
		showComponents(!showComponents);
	}

	private void showEnabledTransitionsList(boolean enable){
		showEnabledTransitions = enable;
		getCurrentTab().showEnabledTransitionsList(enable);

	}
	private void toggleEnabledTransitionsList(){
		showEnabledTransitionsList(!showEnabledTransitions);
	}

	private void showDelayEnabledTransitions(boolean enable){
		showDelayEnabledTransitions = enable;
		getCurrentTab().showDelayEnabledTransitions(enable);
	}
	private void toggleDelayEnabledTransitions(){
		showDelayEnabledTransitions(!showDelayEnabledTransitions);
	}

	private void saveOperation(boolean forceSave){
		saveOperation(appTab.getSelectedIndex(), forceSave);
	}

	private boolean saveOperation(int index, boolean forceSaveAs) {
		File modelFile = getTab(index).getFile();
		boolean result;
		if (!forceSaveAs && modelFile != null && !(modelFile.getName().endsWith(".xml"))) { // ordinary save
			saveNet(index, modelFile);
			result = true;
		} else { // save as
			String path;
			if (modelFile != null) {
				path = modelFile.getParent();
			} else {
				path = appTab.getTitleAt(index);
			}
			String filename = FileBrowser.constructor("Timed-Arc Petri Net", "tapn", path).saveFile(path);
			if (filename != null) {
				modelFile = new File(filename);
				saveNet(index, modelFile);
				result = true;
			}else{
				result = false;
			}
		}

		// resize "header" of current tab immediately to fit the length of the
		// model name
		appTab.getTabComponentAt(index).doLayout();
		return result;
	}

	private void saveNet(int index, File outFile) {
		try {
			getTab(index).writeNetToFile(outFile, this);

			getTab(index).setFile(outFile);

			getTab(index).setNetChanged(false);
			appTab.setTitleAt(index, outFile.getName());
			if(index == appTab.getSelectedIndex()) setTitle(outFile.getName()); // Change the window title
			getTab(index).getUndoManager().clear();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(GuiFrame.this, e.toString(),
					"File Output Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	;


	public void createNewEmptyTab(String name, NetType netType){
		TabContent tab = new TabContent(NetType.TAPN);

		//Set Default Template
		String templateName = tab.drawingSurface().getNameGenerator().getNewTemplateName();
		Template template = new Template(new TimedArcPetriNet(templateName), new DataLayer(), new Zoomer());
		tab.addTemplate(template, false);

		attachTabToGuiFrame(name, tab);
	}

	@Override
	public void attachTabToGuiFrame(String name, TabContent tab) {

		if (name == null || name.isEmpty()) {
			name = "New Petri net " + (newNameCounter++) + ".tapn";
		}

		CreateGui.addTab(tab);
		appTab.addTab(name, tab);
		int newTabIndex = appTab.getTabCount()-1;

		changeToTab(newTabIndex);
	}


	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 * @throws Exception 
	 */

	public TabContent createNewTabFromFile(InputStream file, String name) throws Exception {
		TabContent tab = new TabContent(NetType.TAPN);

		boolean showFileEndingChangedMessage = false;

		String origName = name;

		int currentlySelected = appTab.getSelectedIndex();

		if (name == null || name.equals("")) {
			name = "New Petri net " + (newNameCounter++) + ".tapn";
		} else if (!name.toLowerCase().endsWith(".tapn")){
			if(name.endsWith(".xml")){
				name = name.replaceAll(".xml", ".tapn");
				showFileEndingChangedMessage = true;
			}else
				name = name + ".tapn";
		}




		try {

			ModelLoader loader = new ModelLoader();
			LoadedModel loadedModel = loader.load(file);

			tab.setNetwork(loadedModel.network(), loadedModel.templates());
			tab.setQueries(loadedModel.queries());
			tab.setConstants(loadedModel.network().constants());

			tab.selectFirstElements();

			tab.setFile(null);
		} catch (Exception e) {
			throw new Exception("TAPAAL encountered an error while loading the file: " + origName + "\n\nPossible explanations:\n  - " + e.toString());
		}

		attachTabToGuiFrame(name, tab);
		showFileEndingChangedMessage(showFileEndingChangedMessage);
		return tab;
	}


	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 * @throws Exception 
	 */

	private void createNewTabFromPNMLFile(File file) throws Exception {
		TabContent tab = new TabContent(NetType.TAPN);

		String name;

		int currentlySelected = appTab.getSelectedIndex();

		if (file == null) {
			name = "New Petri net " + (newNameCounter++) + ".tapn";
		} else {
			name = file.getName().replaceAll(".pnml", ".tapn");
		}

		if (file != null) {
			try {

				LoadedModel loadedModel;

				PNMLoader loader = new PNMLoader();
				loadedModel = loader.load(file);


				tab.setNetwork(loadedModel.network(), loadedModel.templates());
				tab.setQueries(loadedModel.queries());
				tab.setConstants(loadedModel.network().constants());

				tab.selectFirstElements();

				tab.setMode(ElementType.SELECT);


			} catch (Exception e) {
				throw new Exception("TAPAAL encountered an error while loading the file: " + file.getName() + "\n\nPossible explanations:\n  - " + e.toString());
			}
		}

		//appView.updatePreferredSize(); //XXX 2018-05-23 kyrke seems not to be needed
		name = name.replace(".pnml",".tapn"); // rename .pnml input file to .tapn
		attachTabToGuiFrame(name, tab);
	}


	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 * @throws FileNotFoundException 
	 */
	public void createNewTabFromFile(File file) throws Exception {
		try {
			InputStream stream = new FileInputStream(file);
			TabContent tab = createNewTabFromFile(stream, file.getName());
			if (tab != null) tab.setFile(file);
		}catch (FileNotFoundException e) {
			throw new FileNotFoundException("TAPAAL encountered an error while loading the file: " + file.getName() + "\n\nFile not found:\n  - " + e.toString());
		}
	}

	private TabContent duplicateTab(TabContent tabToDuplicate) {
		int index = appTab.indexOfComponent(tabToDuplicate);

		NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(
				tabToDuplicate.network(),
				tabToDuplicate.allTemplates(),
				tabToDuplicate.queries(),
				tabToDuplicate.network().constants()
		);

		try {
			ByteArrayOutputStream outputStream = tapnWriter.savePNML();
			String composedName = appTab.getTitleAt(index);
			composedName = composedName.replace(".tapn", "");
			composedName += "-untimed";
			return createNewTabFromFile(new ByteArrayInputStream(outputStream.toByteArray()), composedName);
		} catch (Exception e1) {
			e1.printStackTrace();
			System.console().printf(e1.getMessage());
		}
		return null;
	}

	private void convertToUntimedTab(TabContent tab){
		TabTransformer.removeTimingInformation(tab);
	}



	/**
	 * If current net has modifications, asks if you want to save and does it if
	 * you want.
	 * 
	 * @return true if handled, false if cancelled
	 */
	private boolean checkForSave() {
		return checkForSave(appTab.getSelectedIndex());
	}

	private boolean checkForSave(int index) {

		if(index < 0) return false;

		if (getTab(index).getNetChanged()) {
			int result = JOptionPane.showConfirmDialog(GuiFrame.this,
					"The net has been modified. Save the current net?",
					"Confirm Save Current File",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);

			switch (result) {
			case JOptionPane.YES_OPTION:
				boolean saved = saveOperation(index, false);
				if(!saved) return false;
				break;
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
	private boolean checkForSaveAll() {
		// Loop through all tabs and check if they have been saved
		for (int counter = 0; counter < appTab.getTabCount(); counter++) {
			appTab.setSelectedIndex(counter);
			if (!(checkForSave())) {
				return false;
			}
		}
		return true;
	}



	/**
	 * Set the current mode of the GUI, and changes possible actions
	 * 
	 * @param mode
	 *            change GUI to this mode
	 * @author Kenneth Yrke Joergensen (kyrke)
	 */
	//TODO
	@Override
	public void setGUIMode(GUIMode mode) {
		switch (mode) {
		case draw:
			// Enable all draw actions
			startAction.setSelected(false);

			if (getCurrentTab().isInAnimationMode()) {
				getCurrentTab().getAnimator().restoreModel();
				hideComponentWindow();
			}

			getCurrentTab().switchToEditorComponents();
			showComponents(showComponents);
			showQueries(showQueries);
			showConstants(showConstants);
			showToolTips(showToolTips);

			break;
		case animation:

			getCurrentTab().switchToAnimationComponents(showEnabledTransitions);

			showComponents(showComponents);

			startAction.setSelected(true);


			break;
		case noNet:
			break;

		default:
			break;
		}

		// Enable actions based on GUI mode
		enableGUIActions(mode);

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

	private void hideComponentWindow(){
		ArrayList<PetriNetObject> selection = getCurrentTab().drawingSurface().getGuiModel().getPNObjects();

		for (PetriNetObject pn : selection) {
			if (pn instanceof TimedPlaceComponent) {
				TimedPlaceComponent place = (TimedPlaceComponent)pn;
				place.showAgeOfTokens(false);
			} else if (pn instanceof TimedTransitionComponent){
				TimedTransitionComponent transition = (TimedTransitionComponent)pn;
				transition.showDInterval(false);
			}
		}
	}

	public void endFastMode(){
		if(timedPlaceAction.isSelected())
			mode=ElementType.TAPNPLACE;
		else if(transAction.isSelected())
			mode=ElementType.TAPNTRANS;
		else
			mode=ElementType.SELECT;
	}

	//XXX temp while refactoring, kyrke - 2019-07-25, should only be called from TabContent
	@Override
	public void updateMode(Pipe.ElementType _mode) {

		mode = _mode;

		// deselect other actions
		transAction.setSelected(mode == ElementType.TAPNTRANS);
		timedPlaceAction.setSelected(mode == ElementType.TAPNPLACE);
		timedArcAction.setSelected(mode == ElementType.TAPNARC);
		transportArcAction.setSelected(mode == ElementType.TRANSPORTARC);
		inhibarcAction.setSelected(mode == ElementType.TAPNINHIBITOR_ARC);
		tokenAction.setSelected(mode == ElementType.ADDTOKEN);
		deleteTokenAction.setSelected(mode == ElementType.DELTOKEN);
		selectAction.setSelected(mode == ElementType.SELECT);
		annotationAction.setSelected(mode == ElementType.ANNOTATION);

		statusBar.changeText(mode);
	}

	@Override
	public void registerController(GuiFrameControllerActions guiFrameController) {
		this.guiFrameController.setReference(guiFrameController);
	}

	public Pipe.ElementType getMode() {
		return mode;
	}

    public void setTitle(String title) {
		super.setTitle((title == null) ? frameTitle : frameTitle + ": " + title);
	}

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
	 *         the box's selected item is updated to keep track of ZoomActions
	 *         called from other sources, a duplicate ZoomAction is not called
	 */
	@Override
	public void updateZoomCombo() {
		ActionListener zoomComboListener = (zoomComboBox.getActionListeners())[0];
		zoomComboBox.removeActionListener(zoomComboListener);
		zoomComboBox.setSelectedItem(getCurrentTab().drawingSurface().getZoomController().getPercent() + "%");
		zoomComboBox.addActionListener(zoomComboListener);
	}
	
	public void changeSpacing(double factor){
		TabContent tabContent = (TabContent) appTab.getSelectedComponent();			
		for(PetriNetObject obj : tabContent.currentTemplate().guiModel().getPetriNetObjects()){
			if(obj instanceof PlaceTransitionObject){
				obj.translate((int) (obj.getLocation().x*factor-obj.getLocation().x), (int) (obj.getLocation().y*factor-obj.getLocation().y));
				
				if(obj instanceof Transition){
					for(Arc arc : ((PlaceTransitionObject) obj).getPreset()){
						for(ArcPathPoint point : arc.getArcPath().getArcPathPoints()){
							point.setPointLocation((float) Math.max(point.getPoint().x*factor, point.getWidth()), (float) Math.max(point.getPoint().y*factor, point.getHeight()));
						}
					}
					for(Arc arc : ((PlaceTransitionObject) obj).getPostset()){
						for(ArcPathPoint point : arc.getArcPath().getArcPathPoints()){
							point.setPointLocation((float) Math.max(point.getPoint().x*factor, point.getWidth()), (float) Math.max(point.getPoint().y*factor, point.getHeight()));
						}
					}
				}
				
				((PlaceTransitionObject) obj).update(true);
			}else{
				obj.setLocation((int) (obj.getLocation().x*factor), (int) (obj.getLocation().y*factor));
			}
		}
		
		tabContent.currentTemplate().guiModel().repaintAll(true);
		getCurrentTab().drawingSurface().updatePreferredSize();
	}
        
        private boolean canNetBeSavedAndShowMessage() {
                if (getCurrentTab().network().paintNet()) {
                        return true;
                } else {
                        String message = "The net is too big and cannot be saved or exported.";
                        Object[] dialogContent = {message};
                        JOptionPane.showMessageDialog(null, dialogContent, "Large net limitation", JOptionPane.WARNING_MESSAGE);
                }
                return false;
        }

	public void exit(){
		if (checkForSaveAll()) {
			dispose();
			System.exit(0);
		}
	}

	
	
	private JMenu buildMenuFiles(int shortcutkey) {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');

		createAction = new GuiAction("New", "Create a new Petri net",  KeyStroke.getKeyStroke('N', shortcutkey )) {
			public void actionPerformed(ActionEvent arg0) {
				guiFrameController.ifPresent(GuiFrameControllerActions::showNewPNDialog);
			}
		};
		fileMenu.add(createAction);

		fileMenu.add(openAction = new GuiAction("Open", "Open",  KeyStroke.getKeyStroke('O', shortcutkey )) {
			public void actionPerformed(ActionEvent arg0) {
				final File[] files = FileBrowser.constructor("Timed-Arc Petri Net","tapn", "xml", FileBrowser.userPath).openFiles();
				//show loading cursor
				CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				//Do loading
			    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			        @Override
			        protected Void doInBackground() throws InterruptedException, Exception, FileNotFoundException {
			        	for(File f : files){
							if(f.exists() && f.isFile() && f.canRead()){
								FileBrowser.userPath = f.getParent();
								createNewTabFromFile(f);
							}
						}
			        	return null;
			        }
			        @Override
			        protected void done() {
					    try {
			        		CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			        		get();
						} catch (Exception e) {
					    	JOptionPane.showMessageDialog(GuiFrame.this,
									e.getMessage(),
									"Error loading file",
									JOptionPane.ERROR_MESSAGE);
							return;
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
		});

		fileMenu.add(closeAction = new GuiAction("Close", "Close the current tab",  KeyStroke.getKeyStroke('W', shortcutkey )) {
			public void actionPerformed(ActionEvent arg0) {

				int index = appTab.getSelectedIndex();
				closeTab(index);

			}

		});

		fileMenu.addSeparator();

		fileMenu.add(saveAction = new GuiAction("Save", "Save", KeyStroke.getKeyStroke('S', shortcutkey )) {
			public void actionPerformed(ActionEvent arg0) {
				 if (canNetBeSavedAndShowMessage()) {
                     saveOperation(false); 
				 }
			}			
		});
		
		
		fileMenu.add(saveAsAction = new GuiAction("Save as", "Save as...", KeyStroke.getKeyStroke('S', (shortcutkey + InputEvent.SHIFT_MASK))) {
			public void actionPerformed(ActionEvent arg0) {
				if (canNetBeSavedAndShowMessage()) {
                    saveOperation(true); 
				}	
			}
		});

				
		// Import menu
		JMenu importMenu = new JMenu("Import");
		importMenu.setIcon(new ImageIcon(
				Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "Export.png")
		));
		
		importMenu.add(importPNMLAction = new GuiAction("PNML untimed net", "Import an untimed net in the PNML format", KeyStroke.getKeyStroke('X', shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				final File[] files = FileBrowser.constructor("Import PNML", "pnml", FileBrowser.userPath).openFiles();
				
				//Show loading cursor
				CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				//Do loading of net
			    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			        @Override
			        protected Void doInBackground() throws InterruptedException, Exception {
			        	for(File f : files){
							if(f.exists() && f.isFile() && f.canRead()){
								FileBrowser.userPath = f.getParent();
								createNewTabFromPNMLFile(f);
							}
						}
			        	return null;
			        }
			        @Override
			        protected void done() {
			        	try {
					    	CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					    	get();
			        	} catch (Exception e) {
			        		JOptionPane.showMessageDialog(GuiFrame.this,
									e.getMessage(),
									"Error loading file",
									JOptionPane.ERROR_MESSAGE);
							return;
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
		});
		

		importMenu.add(importSUMOAction = new GuiAction("SUMO queries (.txt)", "Import SUMO queries in a plain text format") {
			public void actionPerformed(ActionEvent arg0) {
				currentTab.ifPresent(TabContentActions::importSUMOQueries);
			}
		});

		importMenu.add(
				importXMLAction = new GuiAction("XML queries (.xml)", "Import MCC queries in XML format", KeyStroke.getKeyStroke('R', shortcutkey)) {
					public void actionPerformed(ActionEvent arg0) {
						currentTab.ifPresent(TabContentActions::importXMLQueries);
					}	
				});
		fileMenu.add(importMenu);

		// Export menu
		JMenu exportMenu = new JMenu("Export");
		exportMenu.setIcon(new ImageIcon(
				Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "Export.png")));
		
		exportMenu.add(exportPNGAction = new GuiAction("PNG", "Export the net to PNG format", KeyStroke.getKeyStroke('G', shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				if (canNetBeSavedAndShowMessage()) {
                    Export.exportGuiView(getCurrentTab().drawingSurface(), Export.PNG, null);
				}
			}
		});

		exportMenu.add(exportPSAction = new GuiAction("PostScript", "Export the net to PostScript format", KeyStroke.getKeyStroke('T', shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				if (canNetBeSavedAndShowMessage()) {
                    Export.exportGuiView(getCurrentTab().drawingSurface(), Export.POSTSCRIPT, null);
				}
			}
		});


		exportMenu.add(exportToTikZAction = new GuiAction("TikZ", "Export the net to LaTex (TikZ) format", KeyStroke.getKeyStroke('L', shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				if (canNetBeSavedAndShowMessage()) {
                    Export.exportGuiView(getCurrentTab().drawingSurface(), Export.TIKZ, getCurrentTab().drawingSurface().getGuiModel());
				}
			}
		});


		exportMenu.add(exportToPNMLAction = new GuiAction("PNML", "Export the net to PNML format", KeyStroke.getKeyStroke('D', shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				if (canNetBeSavedAndShowMessage()) {
                    if(Preferences.getInstance().getShowPNMLWarning()) {
                            JCheckBox showAgain = new JCheckBox("Do not show this warning.");
                            String message = "In the saved PNML all timing information will be lost\n" +
                                    	"and the components in the net will be merged into one big net.";
                            Object[] dialogContent = {message, showAgain};
                            JOptionPane.showMessageDialog(null, dialogContent, 
                                    "PNML loss of information", JOptionPane.WARNING_MESSAGE);
                            Preferences.getInstance().setShowPNMLWarning(!showAgain.isSelected());
                    }
                    Export.exportGuiView(getCurrentTab().drawingSurface(), Export.PNML, null);
				}
			}
		});
		
		
		exportMenu.add(exportToXMLAction = new GuiAction("XML Queries", "Export the queries to XML format", KeyStroke.getKeyStroke('H', shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				if (canNetBeSavedAndShowMessage()) {
					Export.exportGuiView(getCurrentTab().drawingSurface(), Export.QUERY, null);
				}
			}
		});

		exportMenu.add(exportBatchAction = new GuiAction("Batch Export to PNML and XML Queries", "Export multiple nets into PNML together with the XML queries, while removing the timing information",  KeyStroke.getKeyStroke('D', (shortcutkey + InputEvent.SHIFT_DOWN_MASK))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExportBatchDialog.ShowExportBatchDialog();
			}
		});


		fileMenu.add(exportMenu);

		fileMenu.addSeparator();
		fileMenu.add(printAction = new GuiAction("Print", "Print", KeyStroke.getKeyStroke('P', shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				Export.exportGuiView(getCurrentTab().drawingSurface(), Export.PRINTER, null);
			}
		});
		
		fileMenu.addSeparator();

		// Loads example files, retuns null if not found
		String[] nets = loadTestNets();

		// Oliver Haggarty - fixed code here so that if folder contains non
		// .xml file the Example x counter is not incremented when that file
		// is ignored
		if (nets != null && nets.length > 0) {
			JMenu exampleMenu = new JMenu("Example nets");
			exampleMenu.setIcon(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "Example.png")));
			
			for (String filename : nets) {
				if (filename.toLowerCase().endsWith(".tapn")) {
					
					final String netname = filename.replace(".tapn", "");
					final String filenameFinal = filename;
					GuiAction tmp = new GuiAction(netname, "Open example file \"" + netname + "\"") {
						public void actionPerformed(ActionEvent arg0) {
							InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/Example nets/" + filenameFinal);
							try {
								createNewTabFromFile(file, netname);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					tmp.putValue(Action.SMALL_ICON, new ImageIcon(Thread.currentThread()
							.getContextClassLoader().getResource(
									CreateGui.imgPath + "Net.png")));
					exampleMenu.add(tmp);
				}
			}
			fileMenu.add(exampleMenu);
			fileMenu.addSeparator();

		}





		fileMenu.add(exitAction = new GuiAction("Exit", "Close the program", KeyStroke.getKeyStroke('Q', shortcutkey)) {
			public void actionPerformed(ActionEvent arg0) {
				exit();
			}
		});

		return fileMenu;
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
				String me = this.getName().replace(".", "/") + ".class";
				dirURL = Thread.currentThread().getContextClassLoader().getResource(me);
			}

			if (dirURL.getProtocol().equals("jar")) {
				/* A JAR path */
				String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf('!')); // strip out only the JAR
																								// file
				JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
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
				// Special hack to get intro-example first
				if (one.equals("intro-example.tapn")) {
					toReturn = -1;
				}
				if (two.equals("intro-example.tapn")) {
					toReturn = 1;
				}
				return toReturn;
			});
		} catch (Exception e) {
			Logger.log("Error getting example files:" + e);
			e.printStackTrace();
		}
		return nets;
	}





	public int getNameCounter() {
		return newNameCounter;
	}

	public void incrementNameCounter() {
		newNameCounter++;
	}

	public String getCurrentTabName(){
		return appTab.getTitleAt(appTab.getSelectedIndex());
	}

	public boolean isShowingDelayEnabledTransitions() {
		return showDelayEnabledTransitions;
	}

	private void toggleShowZeroToInfinityIntervals() {
		showZeroToInfinityIntervals = !showZeroToInfinityIntervals;
	}

	public boolean showZeroToInfinityIntervals() {
		return showZeroToInfinityIntervals;
	}

	public boolean showTokenAge(){
		return showTokenAge;
	}

	private void toggleShowTokenAge(){
		showTokenAge = !showTokenAge;
	}

	public int getSelectedTabIndex() { return appTab.getSelectedIndex(); }

	public TabContent getCurrentTab() { return CreateGui.getCurrentTab(); }
	private TabContent getTab(int tabIndex) { return CreateGui.getTab(tabIndex); }

	private void showFileEndingChangedMessage(boolean showMessage) {
		if(showMessage) {
			//We thread this so it does not block the EDT
			new Thread(new Runnable() {
				@Override
				public void run() {
					CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					new MessengerImpl().displayInfoMessage("We have changed the ending of TAPAAL files from .xml to .tapn and the opened file was automatically renamed to end with .tapn.\n"
							+ "Once you save the .tapn model, we recommend that you manually delete the .xml file.", "FILE CHANGED");
				}
			}).start();
		}
	}


	//If needed, add boolean forceClose, where net is not checkedForSave and just closed
	//XXX 2018-05-23 kyrke, implementation close to undoAddTab, needs refactoring
	public void closeTab(int index) {

		if(appTab.getTabCount() > 0 && checkForSave(index)){
			//Close the gui part first, else we get an error bug #826578
			appTab.removeTabAt(index);
			CreateGui.removeTab(index);

			if(appTab.getTabCount() == 0) {
				setGUIMode(GUIMode.noNet);
			} else {
				//XXX: The removeTabAt doews trigger changeToTab via tabChanged listener, but at this time
				//the model is not updated yet, which make it change to a wrong tab, so we change it again
				// --kyrke -2019-07-13
				changeToTab(appTab.getSelectedIndex());
			}



		}

	}

}
