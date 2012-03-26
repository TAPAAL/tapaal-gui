package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.tapaal.TAPAAL;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.PNMLWriter;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.Pipe.ElementType;
import pipe.gui.action.GuiAction;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.handler.SpecialMacHandler;
import pipe.gui.widgets.EngineDialogPanel;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.FileBrowser;
import pipe.gui.widgets.NewTAPNPanel;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.BatchProcessingDialog;
import dk.aau.cs.gui.TabComponent;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.components.StatisticsPanel;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.ModelLoader;
import dk.aau.cs.io.ResourceManager;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;


public class GuiFrame extends JFrame implements Observer {

	private static final long serialVersionUID = 7509589834941127217L;
	// for zoom combobox and dropdown
	private final String[] zoomExamples = { "40%", "60%", "80%", "100%",
			"120%", "140%", "160%", "180%", "200%", "300%" };
	private String frameTitle; // Frame title
	private GuiFrame appGui;
	private DrawingSurfaceImpl appView;
	private Pipe.ElementType mode, prev_mode, old_mode; // *** mode WAS STATIC ***
	private int newNameCounter = 1;
	private JTabbedPane appTab;
	private StatusBar statusBar;
	private JMenuBar menuBar;
	private JToolBar drawingToolBar;
	private JComboBox zoomComboBox;

	private FileAction createAction, openAction, closeAction, saveAction,
	saveAsAction, exitAction, printAction, exportPNGAction,
	exportPSAction, exportToTikZAction;
	
	private VerificationAction runUppaalVerification;

	private EditAction /* copyAction, cutAction, pasteAction, */undoAction, redoAction;
	private GridAction toggleGrid;
	private ToolAction netStatisticsAction, batchProcessingAction, engineSelectionAction;
	private ZoomAction zoomOutAction, zoomInAction;
	private DeleteAction deleteAction;
	private TypeAction annotationAction, arcAction, inhibarcAction,
	placeAction, transAction, timedtransAction, tokenAction,
	selectAction, deleteTokenAction, dragAction, timedPlaceAction;
	private ViewAction showComponentsAction, showQueriesAction, showConstantsAction,showZeroToInfinityIntervalsAction,showEnabledTransitionsAction;
	private HelpAction showAboutAction, showHomepage, showAskQuestionAction, showReportBugAction, showFAQAction;
	
	private JMenuItem statistics;
	
	private TypeAction timedArcAction;
	private TypeAction transportArcAction;


	public AnimateAction startAction, stepforwardAction, stepbackwardAction,
	randomAction, randomAnimateAction, timeAction;

	public boolean dragging = false;

	private boolean editionAllowed = true;

	public enum GUIMode {
		draw, animation, noNet
	}

	private boolean showComponents = true;
	private boolean showConstants = true;
	private boolean showQueries = true;
	private boolean showEnabledTransitions = true;

	
	private GUIMode guiMode = GUIMode.noNet;
	private JMenu exportMenu, zoomMenu;

	
	public boolean isMac(){
		return System.getProperty("mrj.version") != null;
	}
	
	public GuiFrame(String title) {
		// HAK-arrange for frameTitle to be initialized and the default file
		// name to be appended to basic window title
		
		frameTitle = title;
		setTitle(null);
		try {
			// Set the Look and Feel native for the system.
			setLookAndFeel();
			UIManager.put("OptionPane.informationIcon", ResourceManager.infoIcon());

			// 2010-05-07, Kenneth Yrke Jørgensen:
			// If the native look and feel is GTK replace the useless open
			// dialog,
			// with a java-reimplementation.

			if ("GTK look and feel".equals(UIManager.getLookAndFeel().getName())){
				try {
					//Load class to see if its there
					Class.forName("com.google.code.gtkjfilechooser.ui.GtkFileChooserUI", false, this.getClass().getClassLoader());
					
					UIManager.put("FileChooserUI", "com.google.code.gtkjfilechooser.ui.GtkFileChooserUI");
				} catch (ClassNotFoundException exc){
					System.err.println("Error loading GtkFileChooserUI Look and Feel, using default jvm GTK look and feel instead");
					CreateGui.setUsingGTKFileBrowser(false);
				}

			}


		} catch (Exception exc) {
			System.err.println("Error loading L&F: " + exc);
		}
		
		if (isMac()){ 
			new SpecialMacHandler();
		}

		this.setIconImage(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "icon.png")).getImage());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize.width * 80 / 100, screenSize.height * 80 / 100);
		this.setLocationRelativeTo(null);
		this.setMinimumSize(new Dimension(825, 480));

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		buildMenus();

		// Status bar...
		statusBar = new StatusBar();
		getContentPane().add(statusBar, BorderLayout.PAGE_END);

		// Build menus
		buildToolbar();

		addWindowListener(new WindowHandler());

		this.setForeground(java.awt.Color.BLACK);
		this.setBackground(java.awt.Color.WHITE);

		// Set GUI mode
		setGUIMode(GUIMode.noNet);
	}
	
	private void setLookAndFeel() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		if(UIManager.getLookAndFeel().getName().equals("Windows")){
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		}
	}

	/**
	 * This method does build the menus
	 * 
	 * @author Dave Patterson - fixed problem on OSX due to invalid character in
	 *         URI caused by unescaped blank. The code changes one blank
	 *         character if it exists in the string version of the URL. This way
	 *         works safely in both OSX and Windows. I also added a
	 *         printStackTrace if there is an exception caught in the setup for
	 *         the "Example nets" folder.
	 * @author Kenneth Yrke Joergensen <kenneth@yrke.dk>, 2011-06-28
	 * 	       Code cleanup, removed unused parts, Refactored help menu, Fixed 
	 *         loading of Example Nets to work if we create a Jar. 
	 **/
	private void buildMenus() {
		menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');

		int shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
		addMenuItem(fileMenu, createAction = new FileAction("New",
				"Create a new Petri net", "ctrl N"));
		createAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', shortcutkey));
		
		addMenuItem(fileMenu, openAction = new FileAction("Open", "Open",
		"ctrl O"));
		openAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', shortcutkey));
		
		addMenuItem(fileMenu, closeAction = new FileAction("Close",
				"Close the current tab", "ctrl W"));
		closeAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('W', shortcutkey));
		fileMenu.addSeparator();

		addMenuItem(fileMenu, saveAction = new FileAction("Save", "Save",
		"ctrl S"));
		saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('S', shortcutkey));
		addMenuItem(fileMenu, saveAsAction = new FileAction("Save as",
				"Save as...", null));
		saveAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('S', (shortcutkey + InputEvent.SHIFT_MASK)));

		// Export menu
		exportMenu = new JMenu("Export");

		exportMenu.setIcon(new ImageIcon(Thread.currentThread()
				.getContextClassLoader().getResource(
						CreateGui.imgPath + "Export.png")));
		addMenuItem(exportMenu, exportPNGAction = new FileAction("PNG",
				"Export the net to PNG format", "ctrl G"));
		exportPNGAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('G', shortcutkey));
		addMenuItem(exportMenu, exportPSAction = new FileAction("PostScript",
				"Export the net to PostScript format", "ctrl T"));
		exportPSAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('T', shortcutkey));
		addMenuItem(exportMenu, exportToTikZAction = new FileAction("TikZ",
				"Export the net to PNG format", "ctrl L"));
		exportToTikZAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('L', shortcutkey));

		fileMenu.add(exportMenu);

		fileMenu.addSeparator();
		addMenuItem(fileMenu, printAction = new FileAction("Print", "Print",
		"ctrl P"));
		printAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('P', shortcutkey));
		fileMenu.addSeparator();

		// Example files menu
		try {

			/**
			 * The next block loads the example nets as InputStream from the resources 
			 * Notice the check for if we are inside a jar file, as files inside a jar cant
			 * be listed in the normal way.
			 * 
			 *  @author Kenneth Yrke Joergensen <kenneth@yrke.dk>, 2011-06-27
			 */

			String[] nets = null;
			
			URL dirURL = Thread.currentThread().getContextClassLoader().getResource("resources/Example nets/");
		      if (dirURL != null && dirURL.getProtocol().equals("file")) {
		        /* A file path: easy enough */
		        nets = new File(dirURL.toURI()).list();
		      } 

		      if (dirURL == null) {
		        /* 
		         * In case of a jar file, we can't actually find a directory.
		         * Have to assume the same jar as clazz.
		         */
		        String me = this.getName().replace(".", "/")+".class";
		        dirURL = Thread.currentThread().getContextClassLoader().getResource(me);
		      }
		      
		      if (dirURL.getProtocol().equals("jar")) {
		        /* A JAR path */
		        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf('!')); //strip out only the JAR file
		        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
		        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
		        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
		        while(entries.hasMoreElements()) {
		          String name = entries.nextElement().getName();
		          if (name.startsWith("resources/Example nets/")) { //filter according to the path
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
		      } 
		        
			Arrays.sort(nets, new Comparator<String>() {
				public int compare(String one, String two) {

					int toReturn = one.compareTo(two);
					// Special hack to get intro-example first
					if (one.equals("intro-example.xml")) {
						toReturn = -1;
					}
					if (two.equals("intro-example.xml")) {
						toReturn = 1;
					}
					return toReturn;
				}
			});

			// Oliver Haggarty - fixed code here so that if folder contains non
			// .xml file the Example x counter is not incremented when that file
			// is ignored
			if (nets.length > 0) {
				JMenu exampleMenu = new JMenu("Example nets");
				exampleMenu.setIcon(new ImageIcon(Thread.currentThread()
						.getContextClassLoader().getResource(
								CreateGui.imgPath + "Example.png")));
				int k = 0;
				for (int i = 0; i < nets.length; i++) {
					if (nets[i].toLowerCase().endsWith(".xml")) {
						//addMenuItem(exampleMenu, new ExampleFileAction(nets[i], (k < 10) ? "ctrl " + (k++) : null));
						
						ExampleFileAction tmp = new ExampleFileAction(nets[i], nets[i].replace(".xml", ""), null);
						if (k < 10) {
						
							//tmp.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyStroke.getKeyStroke(""+k).getKeyCode(), shortcutkey));
							
						}
						k++;
						
						addMenuItem(exampleMenu, tmp);
					}
				}
				fileMenu.add(exampleMenu);
				fileMenu.addSeparator();
			}
		} catch (Exception e) {
			Logger.log("Error getting example files:" + e);
			e.printStackTrace();
		}
		addMenuItem(fileMenu, exitAction = new FileAction("Exit",
				"Close the program", "ctrl Q"));
		exitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('Q', shortcutkey));

		/* Edit Menu */
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		addMenuItem(editMenu, undoAction = new EditAction("Undo",
				"Undo", "ctrl Z"));
		undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('Z', shortcutkey));
		addMenuItem(editMenu, redoAction = new EditAction("Redo",
				"Redo", "ctrl Y"));
		redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('Y', shortcutkey));
		editMenu.addSeparator();

		 addMenuItem(editMenu, deleteAction = new DeleteAction("Delete",
				 "Delete selection", "DELETE"));

		 // Bind delete to backspace also
		 editMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				 KeyStroke.getKeyStroke("BACK_SPACE"), "Delete");
		 editMenu.getActionMap().put("Delete", deleteAction);

		 /* Draw menu */
		 JMenu drawMenu = new JMenu("Draw");
		 drawMenu.setMnemonic('D');
		 addMenuItem(drawMenu, selectAction = new TypeAction("Select",
				 ElementType.SELECT, "Select components (S)", "S", true));
		 drawMenu.addSeparator();

		 addMenuItem(drawMenu, timedPlaceAction = new TypeAction("Place",
				 ElementType.TAPNPLACE, "Add a place (P)", "P", true));

		 addMenuItem(drawMenu, transAction = new TypeAction("Transition",
				 ElementType.TAPNTRANS, "Add a transition (T)", "T", true));

		 addMenuItem(drawMenu, timedArcAction = new TypeAction("Arc",
				 ElementType.TAPNARC, "Add an arc (A)", "A", true));

		 addMenuItem(drawMenu, transportArcAction = new TypeAction(
				 "Transport arc", ElementType.TRANSPORTARC, "Add a transport arc (R)", "R",
				 true));

		 addMenuItem(drawMenu, inhibarcAction = new TypeAction("Inhibitor arc",
				 ElementType.TAPNINHIBITOR_ARC, "Add an inhibitor arc (I)", "I", true));

		 addMenuItem(drawMenu, annotationAction = new TypeAction("Annotation",
				 ElementType.ANNOTATION, "Add an annotation (N)", "N", true));
		 
		 drawMenu.addSeparator();
		 
		 addMenuItem(drawMenu, tokenAction = new TypeAction("Add token",
				 ElementType.ADDTOKEN, "Add a token (+)", "typed +", true));

		 addMenuItem(drawMenu, deleteTokenAction = new TypeAction(
				 "Delete token", ElementType.DELTOKEN, "Delete a token (-)", "typed -",
				 true));

		 /* ViewMenu */
		 JMenu viewMenu = new JMenu("View");
		 viewMenu.setMnemonic('V');

		 zoomMenu = new JMenu("Zoom");
		 zoomMenu.setIcon(new ImageIcon(Thread.currentThread()
				 .getContextClassLoader().getResource(
						 CreateGui.imgPath + "Zoom.png")));
		 addZoomMenuItems(zoomMenu);

		 addMenuItem(viewMenu, zoomInAction = new ZoomAction("Zoom in",
				 "Zoom in by 10% ", "ctrl J"));
		 zoomInAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyStroke.getKeyStroke("J").getKeyCode(), shortcutkey));

		 addMenuItem(viewMenu, zoomOutAction = new ZoomAction("Zoom out",
				 "Zoom out by 10% ", "ctrl K"));
		 zoomOutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyStroke.getKeyStroke("K").getKeyCode(), shortcutkey));
		 viewMenu.add(zoomMenu);

		 viewMenu.addSeparator();
		 addMenuItem(viewMenu, toggleGrid = new GridAction("Cycle grid",
				 "Change the grid size", "G"));
		 addMenuItem(viewMenu, dragAction = new TypeAction("Drag", ElementType.DRAG,
				 "Drag the drawing", "D", true));
		 
		 viewMenu.addSeparator();
		 
		 	addCheckboxMenuItem(viewMenu, showComponentsAction = new ViewAction("Display components", 
				 453243, "Show/hide the list of components.", "ctrl 1", true));
                                 showComponentsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('1', shortcutkey));

		 	addCheckboxMenuItem(viewMenu, showQueriesAction = new ViewAction("Display queries", 
				 453244, "Show/hide verification queries.", "ctrl 2", true));
				 showQueriesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('2', shortcutkey));

		 	addCheckboxMenuItem(viewMenu, showConstantsAction = new ViewAction("Display constants", 
				 453245, "Show/hide global constants.", "ctrl 3", true));
	  	         	 showConstantsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('3', shortcutkey));
			
		 	addCheckboxMenuItem(viewMenu, showEnabledTransitionsAction = new ViewAction("Display enabled transitions",
				 453247, "Show/hide the list of enabled transitions","ctrl 4",true));
				 showEnabledTransitionsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('4', shortcutkey));

		 	addCheckboxMenuItem(viewMenu, showZeroToInfinityIntervalsAction = new ViewAction("Display intervals [0,inf)",
				 453246, "Show/hide intervals [0,inf) that do not restrict transition firing in any way.","ctrl 5",true));
    				 showZeroToInfinityIntervalsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('5', shortcutkey));

		 
		 /* Simulator */
		 JMenu animateMenu = new JMenu("Simulator");
		 animateMenu.setMnemonic('A');
		 addMenuItem(animateMenu, startAction = new AnimateAction(
				 "Simulation mode", ElementType.START, "Toggle simulation mode (M)",
				 "M", true));
		 animateMenu.addSeparator();
		 addMenuItem(animateMenu, stepbackwardAction = new AnimateAction("Step backward",
				 ElementType.STEPBACKWARD, "Step backward", "typed 4"));
		 addMenuItem(animateMenu,
				 stepforwardAction = new AnimateAction("Step forward",
						 ElementType.STEPFORWARD, "Step forward", "typed 6"));

		 addMenuItem(animateMenu, timeAction = new AnimateAction("Delay one time unit",
				 ElementType.TIMEPASS, "Let time pass one time unit", "typed 1"));

		 /*
		  * addMenuItem(animateMenu, randomAction = new AnimateAction("Random",
		  * Pipe.RANDOM, "Randomly fire a transition", "typed 5"));
		  * addMenuItem(animateMenu, randomAnimateAction = new
		  * AnimateAction("Simulate", Pipe.ANIMATE,
		  * "Randomly fire a number of transitions", "typed 7",true));
		  */
		 randomAction = new AnimateAction("Random", ElementType.RANDOM,
				 "Randomly fire a transition", "typed 5");
		 randomAnimateAction = new AnimateAction("Simulate", ElementType.ANIMATE,
				 "Randomly fire a number of transitions", "typed 7", true);

		 
		 /* The help part */
		 JMenu helpMenu = new JMenu("Help");
		 helpMenu.setMnemonic('H');
		 
		 addMenuItem(helpMenu, showHomepage = new HelpAction("Visit TAPAAL home",
				 453257, "Visit the TAPAAL homepage", "_"));
		 
		 helpMenu.addSeparator();
		 
		 addMenuItem(helpMenu, showFAQAction = new HelpAction("Show FAQ",
				 454256, "See TAPAAL frequently asked questions", "_"));
		 addMenuItem(helpMenu, showAskQuestionAction = new HelpAction("Ask a Question",
				 453256, "Ask a question about TAPAAL", "_"));
		 addMenuItem(helpMenu, showReportBugAction = new HelpAction("Report Bug",
				 453254, "Report a bug in TAPAAL", "_"));
		 
		 helpMenu.addSeparator();
		 
		 addMenuItem(helpMenu, showAboutAction = new HelpAction("About",
				 453246, "Show the About Menu", "_"));

		 menuBar.add(fileMenu);
		 menuBar.add(editMenu);
		 menuBar.add(viewMenu);
		 menuBar.add(drawMenu);
		 menuBar.add(animateMenu);
		 menuBar.add(buildToolsMenu());
		 menuBar.add(helpMenu);
		 
		 setJMenuBar(menuBar);

	}

	private JMenu buildToolsMenu() {
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		
		int shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		

        //statistics = new JMenuItem("Net statistics");	
		statistics = new JMenuItem(netStatisticsAction = new ToolAction("Net statistics", "Shows information about the number of transitions, places, arcs, etc.","ctrl I"));				
		netStatisticsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('I', shortcutkey));
		statistics.setMnemonic('n');		
		statistics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StatisticsPanel.showStatisticsPanel();
			}
		});		
		toolsMenu.add(statistics);		
		
		
		//JMenuItem batchProcessing = new JMenuItem("Batch processing");
		JMenuItem batchProcessing = new JMenuItem(batchProcessingAction = new ToolAction("Batch processing", "Batch verification of multiple nets and queries","ctrl B"));				
		batchProcessingAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('B', shortcutkey));
		
		batchProcessing.setMnemonic('b');				
		batchProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkForSaveAll();
				BatchProcessingDialog dialog = new BatchProcessingDialog(CreateGui.getApp(), "Batch Processing", true);
				dialog.pack();
				dialog.setPreferredSize(dialog.getSize());
				//Set the minimum size to 150 less than the preferred, to be consistent with the minimum size of the result panel
				dialog.setMinimumSize(new Dimension(dialog.getWidth(), dialog.getHeight()-150));
				dialog.setLocationRelativeTo(null);
				dialog.setResizable(true);
				dialog.setVisible(true);
			}
		});
		toolsMenu.add(batchProcessing);
		
		
		toolsMenu.addSeparator();
		
		//JMenuItem engineSelection = new JMenuItem("Verification engines");
		JMenuItem engineSelection = new JMenuItem(engineSelectionAction = new ToolAction("Engine selection", "View and modify the location of verification engines","ctrl E"));				
		engineSelectionAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', shortcutkey));
		
		engineSelection.setMnemonic('v');		
		engineSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new EngineDialogPanel().showDialog();				
			}
		});
		toolsMenu.add(engineSelection);
		return toolsMenu;
	}

	private void buildToolbar() {
		// Create the toolbar
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);// Inhibit toolbar floating

		// Basis file operations
		toolBar.add(createAction);
		toolBar.add(openAction);
		toolBar.add(saveAction);
		toolBar.add(saveAsAction);

		// Print
		toolBar.addSeparator();
		toolBar.add(printAction);

		// Copy/past
		/*
		 * Removed copy/past button toolBar.addSeparator();
		 * toolBar.add(cutAction); toolBar.add(copyAction);
		 * toolBar.add(pasteAction);
		 */

		// Undo/redo
		toolBar.addSeparator();
		toolBar.add(deleteAction);
		toolBar.add(undoAction);
		toolBar.add(redoAction);

		// Zoom
		toolBar.addSeparator();
		toolBar.add(zoomOutAction);
		addZoomComboBox(toolBar, new ZoomAction("Zoom",
				"Select zoom percentage ", ""));
		toolBar.add(zoomInAction);

		// Modes

		toolBar.addSeparator();
		toolBar.add(toggleGrid);
		toolBar.add(new ToggleButton(dragAction));
		toolBar.add(new ToggleButton(startAction));

		// Start drawingToolBar
		drawingToolBar = new JToolBar();
		drawingToolBar.setFloatable(false);

		drawingToolBar.addSeparator();

		// Normal arraw

		drawingToolBar.add(new ToggleButton(selectAction));

		// Drawing elements
		drawingToolBar.addSeparator();
		drawingToolBar.add(new ToggleButton(timedPlaceAction));
		drawingToolBar.add(new ToggleButton(transAction));
		drawingToolBar.add(new ToggleButton(timedArcAction));
		drawingToolBar.add(new ToggleButton(transportArcAction));
		drawingToolBar.add(new ToggleButton(inhibarcAction));

		drawingToolBar.add(new ToggleButton(annotationAction));

		// Tokens
		drawingToolBar.addSeparator();
		drawingToolBar.add(new ToggleButton(tokenAction));
		drawingToolBar.add(new ToggleButton(deleteTokenAction));

		// Create panel to put toolbars in
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new FlowLayout(0, 0, 0));

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
	 * @param JMenu
	 *            - the menu to add the submenu to
	 */
	private void addZoomMenuItems(JMenu zoomMenu) {
		for (int i = 0; i <= zoomExamples.length - 1; i++) {
			ZoomAction a = new ZoomAction(zoomExamples[i], "Select zoom percentage", "");
			
			JMenuItem newItem = new JMenuItem(a);
			
			zoomMenu.add(newItem);
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
		zoomComboBox = new JComboBox(zoomExamples);
		zoomComboBox.setEditable(true);
		zoomComboBox.setSelectedItem("100%");
		zoomComboBox.setMaximumRowCount(zoomExamples.length);
		zoomComboBox.setMaximumSize(zoomComboBoxDimension);
		zoomComboBox.setMinimumSize(zoomComboBoxDimension);
		zoomComboBox.setPreferredSize(zoomComboBoxDimension);
		zoomComboBox.setAction(action);
		toolBar.add(zoomComboBox);
	}

	private JMenuItem addMenuItem(JMenu menu, Action action) {
		JMenuItem item = menu.add(action);
		KeyStroke keystroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

		if (keystroke != null) {
			item.setAccelerator(keystroke);
		}
		return item;
	}
	
	private JMenuItem addCheckboxMenuItem(JMenu menu, Action action) {
		JCheckBoxMenuItem checkBoxItem = new JCheckBoxMenuItem(action);
		checkBoxItem.setSelected(true);
		JMenuItem item = menu.add(checkBoxItem);
		KeyStroke keystroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

		if (keystroke != null) {
			item.setAccelerator(keystroke);
		}
		return item;
	}

	/**
	 * Sets all buttons to enabled or disabled according to the current GUImode.
	 * 
	 * Reimplementation of old enableGUIActions(bool status)
	 * 
	 * @author Kenneth Yrke Joergensen (kyrke)
	 * */
	private void enableGUIActions() {

		switch (getGUIMode()) {
		case draw:

			enableAllActions(true);

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
			selectAction.setEnabled(true);
			deleteTokenAction.setEnabled(true);

			timeAction.setEnabled(false);
			stepbackwardAction.setEnabled(false);
			stepforwardAction.setEnabled(false);

			deleteAction.setEnabled(true);
			showEnabledTransitionsAction.setEnabled(false);

			// Undo/Redo is enabled based on undo/redo manager
			appView.getUndoManager().setUndoRedoStatus();

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
			selectAction.setEnabled(false);
			deleteTokenAction.setEnabled(false);
			
			showConstantsAction.setEnabled(false);
			showQueriesAction.setEnabled(false);

			// Only enable this if it is not an untimed net.
			if (CreateGui.getModel().netType() != NetType.UNTIMED) {
				timeAction.setEnabled(true);
			}

			stepbackwardAction.setEnabled(true);
			stepforwardAction.setEnabled(true);

			deleteAction.setEnabled(false);
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);

			break;
		case noNet:

			timedPlaceAction.setEnabled(false);
			timedArcAction.setEnabled(false);
			inhibarcAction.setEnabled(false);
			transportArcAction.setEnabled(false);

			annotationAction.setEnabled(false);
			transAction.setEnabled(false);
			tokenAction.setEnabled(false);
			deleteAction.setEnabled(false);
			selectAction.setEnabled(false);
			deleteTokenAction.setEnabled(false);

			timeAction.setEnabled(false);
			stepbackwardAction.setEnabled(false);
			stepforwardAction.setEnabled(false);

			deleteAction.setEnabled(false);
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);

			enableAllActions(false);
			break;
		}

	}

	/**
	 * Helperfunction for disabeling/enabeling all actions when we are in noNet
	 * GUImode
	 * 
	 * @return
	 */
	private void enableAllActions(boolean enable) {

		// File
		closeAction.setEnabled(enable);

		saveAction.setEnabled(enable);
		saveAsAction.setEnabled(enable);

		exportMenu.setEnabled(enable);
		exportPNGAction.setEnabled(enable);
		exportPSAction.setEnabled(enable);
		exportToTikZAction.setEnabled(enable);

		printAction.setEnabled(enable);

		// View
		zoomInAction.setEnabled(enable);
		zoomOutAction.setEnabled(enable);
		zoomComboBox.setEnabled(enable);
		zoomMenu.setEnabled(enable);

		toggleGrid.setEnabled(enable);
		dragAction.setEnabled(enable);
		
		showComponentsAction.setEnabled(enable);
		showConstantsAction.setEnabled(enable);
		showQueriesAction.setEnabled(enable);
		showZeroToInfinityIntervalsAction.setEnabled(enable);
		showEnabledTransitionsAction.setEnabled(enable);

		// Simulator
		startAction.setEnabled(enable);
		
		// Tools
		statistics.setEnabled(enable);

	}

	// set frame objects by array index
	private void setObjects(int index) {
		appView = CreateGui.getDrawingSurface(index);
	}

	// set tabbed pane properties and add change listener that updates tab with
	// linked model and view
	public void setTab() {

		appTab = CreateGui.getTab();
		appTab.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

				int index = appTab.getSelectedIndex();
				setObjects(index);
				if (appView != null) {
					appView.setVisible(true);
					appView.repaint();
					updateZoomCombo();

					setTitle(appTab.getTitleAt(index));
					setGUIMode(GUIMode.draw);

					// TODO: change this code... it's ugly :)
					if (appGui.getMode() == ElementType.SELECT) {
						appGui.activateSelectAction();
					}

				} else {
					setTitle(null);
				}

			}

		});
		appGui = CreateGui.getApp();
		appView = CreateGui.getView();
		
	}



	// HAK Method called by netModel object when it changes
	public void update(Observable o, Object obj) {
		if ((mode != ElementType.CREATING) && (!appView.isInAnimationMode())) {
			appView.setNetChanged(true);
		}
	}

	public void showQueries(boolean enable){
		showQueries = enable;
		CreateGui.getCurrentTab().showQueries(enable);
	}
	public void toggleQueries(){
		showQueries(!showQueries);
	}

	public void showConstants(boolean enable){
		showConstants = enable;
		CreateGui.getCurrentTab().showConstantsPanel(enable);
	}
	public void toggleConstants(){
		showConstants(!showConstants);
	}
	
	public void toggleZeroToInfinityIntervals() {
		CreateGui.toggleShowZeroToInfinityIntervals();
		appView.repaintAll();
	}
	
	public void showComponents(boolean enable){
		showComponents = enable;
		CreateGui.getCurrentTab().showComponents(enable);
	}
	public void toggleComponents(){
		showComponents(!showComponents);
	}
	
	public void showEnabledTransitionsList(boolean enable){
		showEnabledTransitions = enable;
		CreateGui.getCurrentTab().showEnabledTransitionsList(enable);
	}
	public void toggleEnabledTransitionsList(){
		showEnabledTransitionsList(!showEnabledTransitions);
	}
	
	public void saveOperation(boolean forceSave){
		saveOperation(appTab.getSelectedIndex(), forceSave);
	}

	public boolean saveOperation(int index, boolean forceSaveAs) {
		File modelFile = CreateGui.getFile(index);
		boolean result = false;
		if (!forceSaveAs && modelFile != null) { // ordinary save
			saveNet(index, modelFile);
			result = true;
		} else { // save as
			String path = null;
			if (modelFile != null) {
				path = modelFile.toString();
			} else {
				path = appTab.getTitleAt(index);
			}
			String filename = new FileBrowser(path).saveFile();
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
			TabContent currentTab = CreateGui.getTab(index);
			NetworkMarking currentMarking = null;
			if(getGUIMode().equals(GUIMode.animation)){
				currentMarking = currentTab.network().marking();
				currentTab.network().setMarking(CreateGui.getAnimator().getInitialMarking());
			}

			PNMLWriter tapnWriter = new TimedArcPetriNetNetworkWriter(
					currentTab.network(),
					currentTab.allTemplates(), 
					currentTab.queries(), 
					currentTab.network().constants()
			);

			tapnWriter.savePNML(outFile);

			CreateGui.setFile(outFile, index);

			CreateGui.getDrawingSurface(index).setNetChanged(false);
			appTab.setTitleAt(index, outFile.getName());
			if(index == appTab.getSelectedIndex()) setTitle(outFile.getName()); // Change the window title
			CreateGui.getDrawingSurface(index).getUndoManager().clear();
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
			if(getGUIMode().equals(GUIMode.animation)){
				currentTab.network().setMarking(currentMarking);
			}
		} catch (Exception e) {
			System.err.println(e);
			JOptionPane.showMessageDialog(GuiFrame.this, e.toString(),
					"File Output Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	public void createNewTab(String name, NetType netType) {
		int freeSpace = CreateGui.getFreeSpace();

		setObjects(freeSpace);
		CreateGui.getModel(freeSpace).setNetType(netType);

		if (name == null || name.isEmpty()) {
			name = "New Petri net " + (newNameCounter++) + ".xml";
		}

		TabContent tab = CreateGui.getTab(freeSpace);
		appTab.addTab(name, tab);
		appTab.setTabComponentAt(freeSpace, new TabComponent(appTab));
		appTab.setSelectedIndex(freeSpace);

		String templateName = tab.drawingSurface().getNameGenerator().getNewTemplateName();
		Template template = new Template(new TimedArcPetriNet(templateName), new DataLayer(), new Zoomer());
		tab.addTemplate(template);

		tab.setCurrentTemplate(template);

		appView.setNetChanged(false); // Status is unchanged
		appView.updatePreferredSize();

		setTitle(name);// Change the program caption
		appTab.setTitleAt(freeSpace, name);
		selectAction.actionPerformed(null);
	}

	
	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 * 
	 * @param filename
	 *            Filename of net to load, or <b>null</b> to create a new, empty
	 *            tab
	 */
	public void createNewTabFromFile(InputStream file, String namePrefix) {
		int freeSpace = CreateGui.getFreeSpace();
		String name = "";

		setObjects(freeSpace);
		int currentlySelected = appTab.getSelectedIndex();

		
		if (namePrefix == null || namePrefix.equals("")) {
			name = "New Petri net " + (newNameCounter++) + ".xml";
		} else {
			name = namePrefix + ".xml";
		}

		TabContent tab = CreateGui.getTab(freeSpace);
		appTab.addTab(name, null, tab, null);
		appTab.setTabComponentAt(freeSpace, new TabComponent(appTab));
		appTab.setSelectedIndex(freeSpace);

		if (file != null) {
			try {
				TabContent currentTab = (TabContent) appTab.getSelectedComponent();
				if (CreateGui.getApp() != null) {
					// Notifies used to indicate new instances.
					CreateGui.getApp().setMode(ElementType.CREATING);
				}

				ModelLoader loader = new ModelLoader(currentTab.drawingSurface());
				LoadedModel loadedModel = loader.load(file);
								
				currentTab.setNetwork(loadedModel.network(), loadedModel.templates());
				currentTab.setQueries(loadedModel.queries());
				currentTab.setConstants(loadedModel.network().constants());
				currentTab.setupNameGeneratorsFromTemplates(loadedModel.templates());

				currentTab.selectFirstElements();
				
				if (CreateGui.getApp() != null) {
					CreateGui.getApp().restoreMode();
				}

				CreateGui.setFile(null, freeSpace);
			} catch (Exception e) {
				undoAddTab(currentlySelected);
				JOptionPane.showMessageDialog(GuiFrame.this,
						"TAPAAL encountered an error while loading the file: " + name + "\n\nPossible explanations:\n  - " + e.toString(), 
						"Error loading file: " + name, 
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		appView.setNetChanged(false); // Status is unchanged
		appView.updatePreferredSize();
		setTitle(name);// Change the program caption
		appTab.setTitleAt(freeSpace, name);
		selectAction.actionPerformed(null);
	}
	
	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 * 
	 * @param filename
	 *            Filename of net to load, or <b>null</b> to create a new, empty
	 *            tab
	 */
	public void createNewTabFromFile(File file) {
		int freeSpace = CreateGui.getFreeSpace();
		String name = "";

		setObjects(freeSpace);
		int currentlySelected = appTab.getSelectedIndex();

		if (file == null) {
			name = "New Petri net " + (newNameCounter++) + ".xml";
		} else {
			name = file.getName();
		}

		TabContent tab = CreateGui.getTab(freeSpace);
		appTab.addTab(name, null, tab, null);
		appTab.setTabComponentAt(freeSpace, new TabComponent(appTab));
		appTab.setSelectedIndex(freeSpace);

		if (file != null) {
			try {
				TabContent currentTab = (TabContent) appTab.getSelectedComponent();
				if (CreateGui.getApp() != null) {
					// Notifies used to indicate new instances.
					CreateGui.getApp().setMode(ElementType.CREATING);
				}

				ModelLoader loader = new ModelLoader(currentTab.drawingSurface());
				LoadedModel loadedModel = loader.load(file);
								
				currentTab.setNetwork(loadedModel.network(), loadedModel.templates());
				currentTab.setQueries(loadedModel.queries());
				currentTab.setConstants(loadedModel.network().constants());
				currentTab.setupNameGeneratorsFromTemplates(loadedModel.templates());

				currentTab.selectFirstElements();
				
				if (CreateGui.getApp() != null) {
					CreateGui.getApp().restoreMode();
				}

				CreateGui.setFile(file, freeSpace);
			} catch (Exception e) {
				undoAddTab(currentlySelected);
				JOptionPane.showMessageDialog(GuiFrame.this,
						"TAPAAL encountered an error while loading the file: " + name + "\n\nPossible explanations:\n  - " + e.toString(), 
						"Error loading file: " + name, 
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		appView.setNetChanged(false); // Status is unchanged
		appView.updatePreferredSize();
		setTitle(name);// Change the program caption
		appTab.setTitleAt(freeSpace, name);
		selectAction.actionPerformed(null);
		
		
	}

	private void undoAddTab(int currentlySelected) {
		CreateGui.undoGetFreeSpace();
		appTab.removeTabAt(appTab.getTabCount() - 1);
		appTab.setSelectedIndex(currentlySelected);

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

	public boolean checkForSave(int index) {
		
		if(index < 0) return false;
				
		if (CreateGui.getDrawingSurface(index).getNetChanged()) {
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

	public void setRandomAnimationMode(boolean on) {

		if (!(on)) {
			stepforwardAction.setEnabled(CreateGui.getAnimationHistory().isStepForwardAllowed());
			stepbackwardAction.setEnabled(CreateGui.getAnimationHistory().isStepBackAllowed());

			CreateGui.getAnimationController().setAnimationButtonsEnabled();

		} else {
			stepbackwardAction.setEnabled(false);
			stepforwardAction.setEnabled(false);
		}
		randomAction.setEnabled(!on);
		randomAnimateAction.setSelected(on);
	}

	/**
	 * @deprecated Replaced with setGUIMode
	 * @param on
	 *            enable or disable animation mode
	 */
	public void setAnimationMode(boolean on) {

		if (on) {
			setGUIMode(GUIMode.animation);
		} else {
			setGUIMode(GUIMode.draw);
		}

	}

	/**
	 * Returns the current GUIMode
	 * 
	 * @author Kenneth Yrke Joergensen (kyrke)
	 * @return the current GUIMode
	 */
	public GUIMode getGUIMode() {
		return guiMode;
	}

	/**
	 * Set the current mode of the GUI, and changes possible actions
	 * 
	 * @param mode
	 *            change GUI to this mode
	 * @author Kenneth Yrke Joergensen (kyrke)
	 */
	//TODO
	public void setGUIMode(GUIMode mode) {
		switch (mode) {
		case draw:
			// Enable all draw actions
			startAction.setSelected(false);
			CreateGui.getView().changeAnimationMode(false);

			setEditionAllowed(true);
			statusBar.changeText(statusBar.textforDrawing);
			if (this.guiMode.equals(GUIMode.animation))
				CreateGui.getAnimator().restoreModel();

			CreateGui.switchToEditorComponents();
			showComponents(showComponents);
			showQueries(showQueries);
			showConstants(showConstants);
			
			CreateGui.getView().setBackground(Pipe.ELEMENT_FILL_COLOUR);
			
			activateSelectAction();
			selectAction.setSelected(true);
			break;
		case animation:
			TabContent tab = (TabContent) appTab.getSelectedComponent();
			CreateGui.getAnimator().setTabContent(tab);
			tab.switchToAnimationComponents();
			showComponents(showComponents);
			
			startAction.setSelected(true);
			tab.drawingSurface().changeAnimationMode(true);
			tab.drawingSurface().repaintAll();
			CreateGui.getAnimator().reset();
			CreateGui.getAnimator().storeModel();
			CreateGui.getAnimator().highlightEnabledTransitions();
			CreateGui.getAnimator().reportBlockingPlaces();
			CreateGui.getAnimator().setFiringmode("Random");

			setEditionAllowed(false);
			statusBar.changeText(statusBar.textforAnimation);
			selectAction.setSelected(false);
			// Set a light blue backgound color for animation mode
			tab.drawingSurface().setBackground(Pipe.ANIMATION_BACKGROUND_COLOR);
			break;
		case noNet:
			// Disable All Actions
			statusBar.changeText(statusBar.textforNoNet);
			break;

		default:
			break;
		}
		this.guiMode = mode;
		// Enable actions based on GUI mode
		enableGUIActions();

	}

	public void resetMode() {
		setMode(old_mode);
	}

	public void setFastMode(Pipe.ElementType _mode) {
		old_mode = mode;
		setMode(_mode);
	}

	public void setMode(Pipe.ElementType _mode) {
		// Don't bother unless new mode is different.
		if (mode != _mode) {
			prev_mode = mode;
			mode = _mode;
		}
	}

	public Pipe.ElementType getMode() {
		return mode;
	}

	public void restoreMode() {
		// xxx - This must be refactored when someone findes out excatly what is
		// gowing on
		mode = prev_mode;

		if (placeAction != null) {
			placeAction.setSelected(mode == ElementType.PLACE);
		}
		if (transAction != null) {
			transAction.setSelected(mode == ElementType.IMMTRANS);
		}

		if (timedtransAction != null) {
			timedtransAction.setSelected(mode == ElementType.TIMEDTRANS);
		}

		if (arcAction != null) {
			arcAction.setSelected(mode == ElementType.ARC);
		}

		if (timedArcAction != null)
			timedArcAction.setSelected(mode == ElementType.TAPNARC);

		if (transportArcAction != null)
			transportArcAction.setSelected(mode == ElementType.TRANSPORTARC);

		if (timedPlaceAction != null)
			timedPlaceAction.setSelected(mode == ElementType.TAPNPLACE);

		if (tokenAction != null)
			tokenAction.setSelected(mode == ElementType.ADDTOKEN);

		if (deleteTokenAction != null)
			deleteTokenAction.setSelected(mode == ElementType.DELTOKEN);

		if (selectAction != null)
			selectAction.setSelected(mode == ElementType.SELECT);

		if (annotationAction != null)
			annotationAction.setSelected(mode == ElementType.ANNOTATION);

	}

	public void setTitle(String title) {
		super
		.setTitle((title == null) ? frameTitle : frameTitle + ": "
				+ title);
	}

	public boolean isEditionAllowed() {
		return editionAllowed;
	}

	public void setEditionAllowed(boolean flag) {
		editionAllowed = flag;
	}

	public void setUndoActionEnabled(boolean flag) {
		undoAction.setEnabled(flag);
	}

	public void setRedoActionEnabled(boolean flag) {
		redoAction.setEnabled(flag);
	}

	public void activateSelectAction() {
		// Set selection mode at startup
		setMode(ElementType.SELECT);
		selectAction.actionPerformed(null);
	}

	/**
	 * @author Ben Kirby Remove the listener from the zoomComboBox, so that when
	 *         the box's selected item is updated to keep track of ZoomActions
	 *         called from other sources, a duplicate ZoomAction is not called
	 */
	public void updateZoomCombo() {
		ActionListener zoomComboListener = (zoomComboBox.getActionListeners())[0];
		zoomComboBox.removeActionListener(zoomComboListener);
		zoomComboBox.setSelectedItem(String.valueOf(appView.getZoomController().getPercent()) + "%");
		zoomComboBox.addActionListener(zoomComboListener);
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	class AnimateAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8582324286370859664L;
		private ElementType typeID;
		private AnimationHistoryComponent animBox;

		AnimateAction(String name, ElementType typeID, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
			this.typeID = typeID;
		}

		AnimateAction(String name, ElementType typeID, String tooltip,
				String keystroke, boolean toggleable) {
			super(name, tooltip, keystroke, toggleable);
			this.typeID = typeID;
		}

		public AnimateAction(String name, ElementType typeID, String tooltip,
				KeyStroke keyStroke) {
			super(name, tooltip, keyStroke);
			this.typeID = typeID;

		}

		public void actionPerformed(ActionEvent ae) {
			if (appView == null) {
				return;
			}

			animBox = CreateGui.getAnimationHistory();

			switch (typeID) {
			case START:
				try {
					
					if (!appView.isInAnimationMode()) {
						if (CreateGui.getCurrentTab().numberOfActiveTemplates() > 0) {
							CreateGui.getCurrentTab().rememberSelectedTemplate();
							if (CreateGui.getCurrentTab().currentTemplate().isActive()){
								CreateGui.getCurrentTab().setSelectedTemplateWasActive();
							}
							restoreMode();
							PetriNetObject.ignoreSelection(true);
							setAnimationMode(!appView.isInAnimationMode());
							if (CreateGui.getCurrentTab().templateWasActiveBeforeSimulationMode()) {								
								CreateGui.getCurrentTab().restoreSelectedTemplate();
								CreateGui.getCurrentTab().resetSelectedTemplateWasActive();
							}
							else {
								CreateGui.getCurrentTab().selectFirstActiveTemplate();
							}
						} else {
							JOptionPane.showMessageDialog(GuiFrame.this, 
									"You need at least one active template to enter simulation mode",
									"Simulation Mode Error", JOptionPane.ERROR_MESSAGE);
						}
					} else {

						setMode(typeID);
						PetriNetObject.ignoreSelection(false);
						appView.getSelectionObject().clearSelection();
						setAnimationMode(!appView.isInAnimationMode());
						CreateGui.getCurrentTab().restoreSelectedTemplate();
					}
				} catch (Exception e) {
					System.err.println(e);
					JOptionPane.showMessageDialog(GuiFrame.this, e.toString(),
							"Simulation Mode Error", JOptionPane.ERROR_MESSAGE);
					startAction.setSelected(false);
					appView.changeAnimationMode(false);
				}
				
				stepforwardAction.setEnabled(false);
				stepbackwardAction.setEnabled(false);
					
				// XXX
				// This is a fix for bug #812694 where on mac some menues are gray after
				// changing from simulation mode, when displaying a trace. Showing and 
				// hiding a menu seems to fix this problem 
				Dialog a = new Dialog(CreateGui.appGui);
				a.setVisible(true);
				a.setVisible(false);
				a.dispose();
				if(getGUIMode().equals(GUIMode.draw)){
					activateSelectAction();
				}				
				
				break;

			case TIMEPASS:
				animBox.clearStepsForward();
				CreateGui.getAnimator().letTimePass(BigDecimal.ONE);
				CreateGui.getAnimationController().setAnimationButtonsEnabled();
				break;

			case STEPFORWARD:
				animBox.stepForward();
				CreateGui.getAnimator().stepForward();
				// update mouseOverView
				for (pipe.gui.graphicElements.Place p : CreateGui.getModel().getPlaces()) {
					if (((TimedPlaceComponent) p).isAgeOfTokensShown()) {
						((TimedPlaceComponent) p).showAgeOfTokens(true);
					}
				}
				CreateGui.getAnimationController().setAnimationButtonsEnabled();
				break;

			case STEPBACKWARD:
				animBox.stepBackwards();
				CreateGui.getAnimator().stepBack();
				// update mouseOverView
				for (pipe.gui.graphicElements.Place p : CreateGui.getModel().getPlaces()) {
					if (((TimedPlaceComponent) p).isAgeOfTokensShown()) {
						((TimedPlaceComponent) p).showAgeOfTokens(true);
					}
				}
				CreateGui.getAnimationController().setAnimationButtonsEnabled();
				break;
			default:
				break;
			}
		}

	}

	class ExampleFileAction extends GuiAction {

		private static final long serialVersionUID = -5983638671592349736L;
		private String filename;
		private String name;

		ExampleFileAction(String file, String name, String keyStroke) {
			super(name.replace(".xml", ""), "Open example file \""
					+ name.replace(".xml", "") + "\"", keyStroke);
			this.filename = file;
			putValue(SMALL_ICON, new ImageIcon(Thread.currentThread()
					.getContextClassLoader().getResource(
							CreateGui.imgPath + "Net.png")));
			this.name = name;
		}

		public void actionPerformed(ActionEvent e) {
			InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/Example nets/" + filename);
			createNewTabFromFile(file, name);
		}

	}

	class DeleteAction extends GuiAction {

		private static final long serialVersionUID = -8592450390507637174L;

		DeleteAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			// check if queries need to be removed
			ArrayList<PetriNetObject> selection = CreateGui.getView().getSelectionObject().getSelection();
			Iterable<TAPNQuery> queries = ((TabContent) appTab.getSelectedComponent()).queries();
			HashSet<TAPNQuery> queriesToDelete = new HashSet<TAPNQuery>();

			boolean queriesAffected = false;
			for (PetriNetObject pn : selection) {
				if (pn instanceof TimedPlaceComponent) {
					TimedPlaceComponent place = (TimedPlaceComponent)pn;
					if(!place.underlyingPlace().isShared()){
						for (TAPNQuery q : queries) {
							if (q.getProperty().containsAtomicPropositionWithSpecificPlaceInTemplate(((LocalTimedPlace)place.underlyingPlace()).model().name(),place.underlyingPlace().name())) {
								queriesAffected = true;
								queriesToDelete.add(q);
							}
						}
					}
				}
			}
			StringBuilder s = new StringBuilder();
			s.append("The following queries are associated with the currently selected objects:\n\n");
			for (TAPNQuery q : queriesToDelete) {
				s.append(q.getName());
				s.append('\n');
			}
			s.append("\nAre you sure you want to remove the current selection and all associated queries?");

			int choice = queriesAffected ? JOptionPane.showConfirmDialog(
					CreateGui.getApp(), s.toString(), "Warning",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
					: JOptionPane.YES_OPTION;

			if (choice == JOptionPane.YES_OPTION) {
				appView.getUndoManager().newEdit(); // new "transaction""
				if (queriesAffected) {
					TabContent currentTab = ((TabContent) CreateGui.getTab().getSelectedComponent());
					for (TAPNQuery q : queriesToDelete) {
						currentTab.removeQuery(q);
					}
				}
				
				// remove the places from the list of inclusion places
				for (PetriNetObject p : selection) {
					if (p instanceof TimedPlaceComponent) {
						for (TAPNQuery q : queries) {
							TimedPlace place = ((TimedPlaceComponent)p).underlyingPlace();
							q.inclusionPlaces().removePlace(place);
						}
					}
				}
				
				appView.getUndoManager().deleteSelection(appView.getSelectionObject().getSelection());
				appView.getSelectionObject().deleteSelection();
				appView.repaint();
				CreateGui.getCurrentTab().network().buildConstraints();
			}
		}

	}

	class TypeAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1333311291148756241L;
		private Pipe.ElementType typeID;

		TypeAction(String name, Pipe.ElementType typeID, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
			this.typeID = typeID;
		}

		TypeAction(String name, Pipe.ElementType typeID, String tooltip, String keystroke,
				boolean toggleable) {
			super(name, tooltip, keystroke, toggleable);
			this.typeID = typeID;
		}

		public void actionPerformed(ActionEvent e) {
			
			this.setSelected(true);

			// deselect other actions
			if (this != transAction) {
				transAction.setSelected(false);
			}

			if (this != timedArcAction) {
				timedArcAction.setSelected(false);
			}
			
			if (this != timedPlaceAction) {
				timedPlaceAction.setSelected(false);
			}
			if (this != transportArcAction) {
				transportArcAction.setSelected(false);
			}

			if (this != inhibarcAction) {
				inhibarcAction.setSelected(false);
			}

			if (this != tokenAction) {
				tokenAction.setSelected(false);
			}
			if (this != deleteTokenAction) {
				deleteTokenAction.setSelected(false);
			}

			if (this != selectAction) {
				selectAction.setSelected(false);
			}
			if (this != annotationAction) {
				annotationAction.setSelected(false);
			}
			if (this != dragAction) {
				dragAction.setSelected(false);
			}

			if (appView == null) {
				return;
			}

			appView.getSelectionObject().disableSelection();

			setMode(typeID);
			statusBar.changeText(typeID);

			if ((typeID != ElementType.ARC) && (appView.createArc != null)) {

				appView.createArc.delete();
				appView.createArc = null;
				appView.repaint();

			}

			if (typeID == ElementType.SELECT) {
				// disable drawing to eliminate possiblity of connecting arc to
				// old coord of moved component
				statusBar.changeText(typeID);
				appView.getSelectionObject().enableSelection();
				appView.setCursorType("arrow");
			} else if (typeID == ElementType.DRAG) {
				appView.setCursorType("move");
			} else {
				appView.setCursorType("crosshair");
			}
		}
		// }

	}

	class GridAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5654512618471549653L;

		GridAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			Grid.increment();
			repaint();
		}

	}
	
	class ToolAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8910743226610517225L;

		ToolAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}


		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}

	class ZoomAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 549331166742882564L;

		ZoomAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			// This is set to true if a valid zoom action is performed
			boolean didZoom = false;
			try {
				String actionName = (String) getValue(NAME);
				Zoomer zoomer = appView.getZoomController();
				TabContent tabContent = (TabContent) appTab.getSelectedComponent();
				JViewport thisView = tabContent.drawingSurfaceScrollPane().getViewport();
				String selectedZoomLevel = null;
				int newZoomLevel = Pipe.ZOOM_DEFAULT;

				/*
				 * Zoom action name overview
				 * Zoom in: the zoom IN icon in panel has been pressed
				 * Zoom out: the zoom OUT icon in panel has been pressed
				 * Zoom: a specific zoom level has been chosen in drop down or in the menu.
				 */
				if (actionName.equals("Zoom in")) {
					didZoom = zoomer.zoomIn();
				} else if (actionName.equals("Zoom out")) {
					didZoom = zoomer.zoomOut();
				} else {
					if (actionName.equals("Zoom")) {
						selectedZoomLevel = (String) zoomComboBox.getSelectedItem();
					}
					if (e.getSource() instanceof JMenuItem) {
						selectedZoomLevel = ((JMenuItem) e.getSource()).getText();
					}
					
					//parse selected zoom level, and strip of %.
					newZoomLevel = Integer.parseInt(selectedZoomLevel.replace("%",""));
					
					didZoom = zoomer.setZoom(newZoomLevel);
				}
				if (didZoom) {
					updateZoomCombo();
					
					double midpointX = Zoomer.getUnzoomedValue(thisView.getViewPosition().x
										+ (thisView.getWidth() * 0.5), zoomer.getPercent());
					double midpointY = Zoomer.getUnzoomedValue(thisView.getViewPosition().y
										+ (thisView.getHeight() * 0.5), zoomer.getPercent());
					
					java.awt.Point midpoint = new java.awt.Point((int) midpointX, (int) midpointY);
					
					appView.zoomTo(midpoint);
				}
			} catch (ClassCastException cce) {
				// zoom
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	class VerificationAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4588356505465429153L;

		VerificationAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			if (this == runUppaalVerification) {
				throw new RuntimeException("Dont think this is used");// QueryDialogue.ShowUppaalQueryDialogue(QueryDialogueOption.VerifyNow,
				// null);
			}
		}

	}
	
	class ViewAction extends GuiAction {

		private static final long serialVersionUID = -5145846750992454638L;
		ViewAction(String name, int typeID, String tooltip, String keystroke,
				boolean toggleable) {
			super(name, tooltip, keystroke, toggleable);
		}
		
		
		public void actionPerformed(ActionEvent arg0) {
			
			if (this == showComponentsAction){
				toggleComponents();
			} else if (this == showQueriesAction){
				toggleQueries();
			} else if (this == showConstantsAction){
				toggleConstants();
			} else if (this == showZeroToInfinityIntervalsAction) {
				toggleZeroToInfinityIntervals();
			} else if (this == showEnabledTransitionsAction) {
				toggleEnabledTransitionsList();
			}
		}
		
	}
	public void showAbout() {
		StringBuffer buffer = new StringBuffer("About " + TAPAAL.getProgramName());
		buffer.append("\n\n");
		buffer.append("TAPAAL is a tool for editing, simulation and verification of timed-arc Petri nets.\n");
		buffer.append("The GUI is based on PIPE2: http://pipe2.sourceforge.net/\n\n");
		buffer.append("License information and more is avaialbe at: www.tapaal.net\n\n");
		buffer.append("Credits\n\n");
		buffer.append("TAPAAL GUI and Translations:\n");
		buffer.append("Mathias Andersen, Joakim Byg, Lasse Jacobsen, Morten Jacobsen,\n");
		buffer.append("Kenneth Yrke Joergensen, Mikael H. Moeller, Jiri Srba and Jakob H. Taankvist\n");
		buffer.append("Aalborg University 2009-2012\n\n");
		buffer.append("TAPAAL Engine:\n");
		buffer.append("Alexandre David, Lasse Jacobsen, Morten Jacobsen and Jiri Srba\n");
		buffer.append("Aalborg University 2011-2012\n\n");
		buffer.append("\n");
		

		JOptionPane.showMessageDialog(null, buffer.toString(), "About " + TAPAAL.getProgramName(),
				JOptionPane.INFORMATION_MESSAGE, ResourceManager.appIcon());
	}
	
	
	public static void openBrowser(URI url){
		//open the default bowser on this page
		
		try {
			java.awt.Desktop.getDesktop().browse(url);
		} catch (IOException e) {
			Logger.log("Cannot open the browser.");
		//	JOptionPane.showMessageDialog(this, "There was a problem opening the default bowser \n" +
		//			"Please open the url in your browser by entering " + url.toString(), 
		//			"Error opening browser", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public static void showInBrowser(String address) {
		try {
			URI url = new URI(address);
			openBrowser(url);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			Logger.log("Error convering to URL");
			e.printStackTrace();
		}
	}
	

	class HelpAction extends GuiAction {

		private static final long serialVersionUID = -5145846750992454639L;
		HelpAction(String name, int typeID, String tooltip, String keystroke) {
			super(name, tooltip, keystroke, false);
		}
		
		
		// Less sucky yet far, far simpler to code About dialogue
		public void actionPerformed(ActionEvent e) {
			if (this == showAboutAction){
				showAbout();
			} else if (this == showAskQuestionAction){ 
				showInBrowser("https://answers.launchpad.net/tapaal/+addquestion");
			} else if (this == showReportBugAction){
				showInBrowser("https://bugs.launchpad.net/tapaal/+filebug");
			} else if (this == showFAQAction){
				showInBrowser("https://answers.launchpad.net/tapaal/+faqs");
			} else if (this == showHomepage){
				showInBrowser("http://www.tapaal.net");
			}
		}
		
		
		
	}

	
	public void exit(){
		if (checkForSaveAll()) {
			dispose();
			System.exit(0);
		}
	}
	
	class FileAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1438830908690683060L;

		// constructor
		FileAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			if (this == saveAction) {
				saveOperation(false); // code for Save operation
			} else if (this == saveAsAction) {
				saveOperation(true); // code for Save As operations
			} else if (this == openAction) { // code for Open operation
				File filePath = new FileBrowser(CreateGui.userPath).openFile();
				if ((filePath != null) && filePath.exists()
						&& filePath.isFile() && filePath.canRead()) {
					CreateGui.userPath = filePath.getParent();
					createNewTabFromFile(filePath);

				}
			} else if (this == createAction) {
				showNewPNDialog();
			} else if ((this == exitAction)) {
				exit();
			} else if ((this == closeAction) && (appTab.getTabCount() > 0)
					&& checkForSave()) {
				// Set GUI mode to noNet
				setGUIMode(GUIMode.noNet);

				int index = appTab.getSelectedIndex();
				appTab.remove(index);
				CreateGui.removeTab(index);
				
				// Disable all action not available when no net is opend
			} else if (this == exportPNGAction) {
				Export.exportGuiView(appView, Export.PNG, null);
			} else if (this == exportToTikZAction) {
				Export.exportGuiView(appView, Export.TIKZ, appView
						.getGuiModel());
			} else if (this == exportPSAction) {
				Export.exportGuiView(appView, Export.POSTSCRIPT, null);
			} else if (this == printAction) {
				Export.exportGuiView(appView, Export.PRINTER, null);
			}
		}

	}

	class EditAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2402602825981305085L;

		EditAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {

			if (CreateGui.getApp().isEditionAllowed()) {
				/*
				 * if (this == cutAction) { ArrayList selection =
				 * appView.getSelectionObject().getSelection();
				 * appGui.getCopyPasteManager().setUpPaste(selection, appView);
				 * appView.getUndoManager().newEdit(); // new "transaction""
				 * appView.getUndoManager().deleteSelection(selection);
				 * appView.getSelectionObject().deleteSelection();
				 * pasteAction.setEnabled
				 * (appGui.getCopyPasteManager().pasteEnabled()); } else if
				 * (this == copyAction) {
				 * appGui.getCopyPasteManager().setUpPaste(
				 * appView.getSelectionObject().getSelection(), appView);
				 * pasteAction
				 * .setEnabled(appGui.getCopyPasteManager().pasteEnabled()); }
				 * else if (this == pasteAction) {
				 * appView.getSelectionObject().clearSelection();
				 * appGui.getCopyPasteManager().startPaste(appView); } else
				 */if (this == undoAction) {
					 appView.getUndoManager().undo();
					 CreateGui.getCurrentTab().network().buildConstraints();
				 } else if (this == redoAction) {
					 appView.getUndoManager().redo();
					 CreateGui.getCurrentTab().network().buildConstraints();
				 }
			}
		}
	}

	/**
	 * A JToggleButton that watches an Action for selection change
	 * 
	 * @author Maxim
	 * 
	 *         Selection must be stored in the action using
	 *         putValue("selected",Boolean);
	 */
	class ToggleButton extends JToggleButton implements PropertyChangeListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5085200741780612997L;

		public ToggleButton(Action a) {
			super(a);
			if (a.getValue(Action.SMALL_ICON) != null) {
				// toggle buttons like to have images *and* text, nasty
				setText(null);
			}
			a.addPropertyChangeListener(this);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("selected")) {
				Boolean b = (Boolean) evt.getNewValue();
				if (b != null) {
					setSelected(b.booleanValue());
				}
			}
		}

	}

	class WindowHandler extends WindowAdapter {
		// Handler for window closing event
		public void windowClosing(WindowEvent e) {
			exitAction.actionPerformed(null);
		}
	}

	public void setEnabledStepForwardAction(boolean b) {
		stepforwardAction.setEnabled(b);
	}

	public void showNewPNDialog() {
		// Build interface
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(),
				"Create a New Petri Net", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add Place editor
		contentPane.add(new NewTAPNPanel(guiDialog.getRootPane(), this));

		guiDialog.setResizable(false);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

	}

	public void setEnabledStepBackwardAction(boolean b) {
		stepbackwardAction.setEnabled(b);
	}

	public int getNameCounter() {
		return newNameCounter;
	}

	public void incrementNameCounter() {
		newNameCounter++;
	}

}
