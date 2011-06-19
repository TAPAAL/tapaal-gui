package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.PNMLWriter;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.gui.action.GuiAction;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.FileBrowser;
import pipe.gui.widgets.NewTAPNPanel;
import dk.aau.cs.gui.BatchProcessingDialog;
import dk.aau.cs.gui.TabComponent;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.ModelLoader;
import dk.aau.cs.io.ResourceManager;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.verification.UPPAAL.Verifyta;

public class GuiFrame extends JFrame implements ActionListener, Observer {

	private static final long serialVersionUID = 7509589834941127217L;
	// for zoom combobox and dropdown
	private final String[] zoomExamples = { "40%", "60%", "80%", "100%",
			"120%", "140%", "160%", "180%", "200%", "300%" };
	private String frameTitle; // Frame title
	private GuiFrame appGui;
	private DrawingSurfaceImpl appView;
	private int mode, prev_mode, old_mode; // *** mode WAS STATIC ***
	private int newNameCounter = 1;
	private JTabbedPane appTab;
	private StatusBar statusBar;
	private JMenuBar menuBar;
	private JToolBar drawingToolBar;
	// private Map actions = new HashMap();
	private JComboBox zoomComboBox;

	// XXX kyrke testing
	private FileAction createAction, openAction, closeAction, saveAction,
	saveAsAction, exitAction, printAction, exportPNGAction,
	exportPSAction, exportToTikZAction;
	
	private VerificationAction runUppaalVerification;

	private EditAction /* copyAction, cutAction, pasteAction, */undoAction,
	redoAction;
	private GridAction toggleGrid;
	private ZoomAction zoomOutAction, zoomInAction;
	private DeleteAction deleteAction;
	private TypeAction annotationAction, arcAction, inhibarcAction,
	placeAction, transAction, timedtransAction, tokenAction,
	selectAction, deleteTokenAction, dragAction, timedPlaceAction;
	private ViewAction showComponentsAction, showQueriesAction, showConstantsAction;
	
	/* CB Joakim Byg - tries both */
	private TypeAction timedArcAction;

	private TypeAction transportArcAction;

	/* EOC */
	private AnimateAction startAction, stepforwardAction, stepbackwardAction,
	randomAction, randomAnimateAction, timeAction;

	public boolean dragging = false;

	private boolean editionAllowed = true;

	public enum GUIMode {
		draw, animation, noNet
	}

	private boolean showComponents = true;
	private boolean showConstants = true;
	private boolean showQueries = true;

	
	private GUIMode guiMode = GUIMode.noNet;
	private JMenu exportMenu, zoomMenu;

	public GuiFrame(String title) {
		// HAK-arrange for frameTitle to be initialized and the default file
		// name
		// to be appended to basic window title
		frameTitle = title;
		setTitle(null);
		try {
			// Set the Look and Feel native for the system.
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("OptionPane.informationIcon", ResourceManager.infoIcon());

			// 2010-05-07, Kenneth Yrke Jørgensen:
			// If the native look and feel is GTK replace the useless open
			// dialog,
			// with a java-reimplementation.

			if ("GTK look and feel".equals(UIManager.getLookAndFeel().getName())){
				try {
					//Load class to see if its there
					Class.forName("eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI", false, this.getClass().getClassLoader());

					UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
				} catch (ClassNotFoundException exc){
					System.err.println("Error loading GtkFileChooserUI Look and Feel, using default jvm GTK look and feel instead");
					CreateGui.setUsingGTKFileBrowser(false);
				}

			}


		} catch (Exception exc) {
			System.err.println("Error loading L&F: " + exc);
		}

		this.setIconImage(new ImageIcon(Thread.currentThread()
				.getContextClassLoader().getResource(
						CreateGui.imgPath + "icon.png")).getImage());

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

	/**
	 * This method does build the menus.
	 * 
	 * @author unknown
	 * 
	 * @author Dave Patterson - fixed problem on OSX due to invalid character in
	 *         URI caused by unescaped blank. The code changes one blank
	 *         character if it exists in the string version of the URL. This way
	 *         works safely in both OSX and Windows. I also added a
	 *         printStackTrace if there is an exception caught in the setup for
	 *         the "Example nets" folder.
	 **/
	private void buildMenus() {
		menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');

		addMenuItem(fileMenu, createAction = new FileAction("New",
				"Create a new Petri net", "ctrl N"));
		addMenuItem(fileMenu, openAction = new FileAction("Open", "Open",
		"ctrl O"));
		addMenuItem(fileMenu, closeAction = new FileAction("Close",
				"Close the current tab", "ctrl W"));
		fileMenu.addSeparator();
		/*
		 * addMenuItem(fileMenu, importAction = new FileAction("Import",
		 * "Import from Timenet",""));
		 */
		addMenuItem(fileMenu, saveAction = new FileAction("Save", "Save",
		"ctrl S"));
		addMenuItem(fileMenu, saveAsAction = new FileAction("Save as",
				"Save as...", "shift ctrl S"));

		// Export menu
		exportMenu = new JMenu("Export");

		exportMenu.setIcon(new ImageIcon(Thread.currentThread()
				.getContextClassLoader().getResource(
						CreateGui.imgPath + "Export.png")));
		addMenuItem(exportMenu, exportPNGAction = new FileAction("PNG",
				"Export the net to PNG format", "ctrl G"));
		addMenuItem(exportMenu, exportPSAction = new FileAction("PostScript",
				"Export the net to PostScript format", "ctrl T"));
		addMenuItem(exportMenu, exportToTikZAction = new FileAction("TikZ",
				"Export the net to PNG format", "ctrl L"));

		fileMenu.add(exportMenu);

		fileMenu.addSeparator();
		addMenuItem(fileMenu, printAction = new FileAction("Print", "Print",
		"ctrl P"));
		fileMenu.addSeparator();

		// Example files menu
		try {

			URL examplesDirURL = Thread.currentThread().getContextClassLoader().
			getResource("resources/Example nets" + System.getProperty("file.separator"));



			File examplesDir = new File(examplesDirURL.toURI());
			/**
			 * The next block fixes a problem that surfaced on Mac OSX with PIPE
			 * 2.4. In that environment (and not in Windows) any blanks in the
			 * project name in Eclipse are property converted to '%20' but the
			 * blank in "Example nets" is not. The following code will do
			 * nothing on a Windows machine or if the logic on OSX changess. I
			 * also added a stack trace so if the problem occurs for another
			 * environment (perhaps multiple blanks need to be manually changed)
			 * it can be easily fixed. DP
			 */
			// examplesDir = new File(new URI(examplesDirURL.toString()));
			String dirURLString = examplesDirURL.toString();
			int index = dirURLString.indexOf(" ");
			if (index > 0) {
				StringBuffer sb = new StringBuffer(dirURLString);
				sb.replace(index, index + 1, "%20");
				dirURLString = sb.toString();
			}

			examplesDir = new File(new URI(dirURLString));

			File[] nets = examplesDir.listFiles();

			Arrays.sort(nets, new Comparator<File>() {
				public int compare(File one, File two) {

					int toReturn = ((File) one).getName().compareTo(
							((File) two).getName());
					// Special hack to get intro-example first
					if (one.getName().equals("intro-example.xml")) {
						toReturn = -1;
					}
					if (two.getName().equals("intro-example.xml")) {
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
					if (nets[i].getName().toLowerCase().endsWith(".xml")) {
						addMenuItem(exampleMenu, new ExampleFileAction(nets[i],
								(k < 10) ? "ctrl " + (k++) : null));
					}
				}
				fileMenu.add(exampleMenu);
				fileMenu.addSeparator();
			}
		} catch (Exception e) {
			System.err.println("Error getting example files:" + e);
			e.printStackTrace();
		}
		addMenuItem(fileMenu, exitAction = new FileAction("Exit",
				"Close the program", "ctrl Q"));

		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		addMenuItem(editMenu, undoAction = new EditAction("Undo",
				"Undo (Ctrl-Z)", "ctrl Z"));
		addMenuItem(editMenu, redoAction = new EditAction("Redo",
				"Redo (Ctrl-Y)", "ctrl Y"));
		editMenu.addSeparator();
		/*
		 * addMenuItem(editMenu, cutAction = new EditAction("Cut",
		 * "Cut (Ctrl-X)","ctrl X")); addMenuItem(editMenu, copyAction = new
		 * EditAction("Copy", "Copy (Ctrl-C)","ctrl C")); addMenuItem(editMenu,
		 * pasteAction = new EditAction("Paste", "Paste (Ctrl-V)","ctrl V"));
		 */addMenuItem(editMenu, deleteAction = new DeleteAction("Delete",
				 "Delete selection", "DELETE"));

		 // Bind delete to backspace also
		 editMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				 KeyStroke.getKeyStroke("BACK_SPACE"), "Delete");
		 editMenu.getActionMap().put("Delete", deleteAction);

		 JMenu drawMenu = new JMenu("Draw");
		 drawMenu.setMnemonic('D');
		 addMenuItem(drawMenu, selectAction = new TypeAction("Select",
				 Pipe.SELECT, "Select components", "S", true));
		 drawMenu.addSeparator();

		 // kyrke inserted timed place
		 addMenuItem(drawMenu, timedPlaceAction = new TypeAction("Place",
				 Pipe.TAPNPLACE, "Add a place", "L", true));
		 // jokke removed normal places
		 // addMenuItem(drawMenu, placeAction =
		 // new TypeAction("Place", Pipe.PLACE, "Add a place","P",true));

		 addMenuItem(drawMenu, transAction = new TypeAction("Transition",
				 Pipe.TAPNTRANS, "Add a transition", "I", true));

		 /*
		  * addMenuItem(drawMenu, transAction = new TypeAction("Transition",
		  * Pipe.IMMTRANS, "Add an immediate transition","I",true));
		  */
		 /*
		  * CB Joakim Byg - Not part of the model addMenuItem(drawMenu,
		  * timedtransAction = new TypeAction("Timed transition",
		  * Pipe.TIMEDTRANS, "Add a timed transition","T",true)); EOC
		  */
		 /*
		  * CB Jiri Srba - Not part of the model addMenuItem(drawMenu, arcAction
		  * = new TypeAction("Arc", Pipe.ARC, "Add an arc","A",true));" + EOC
		  */

		 /* CB Joakim Byg - Adding timed arcs */
		 addMenuItem(drawMenu, timedArcAction = new TypeAction("Arc",
				 Pipe.TAPNARC, "Add an arc", "R", true));

		 addMenuItem(drawMenu, transportArcAction = new TypeAction(
				 "Transport Arc", Pipe.TRANSPORTARC, "Add a transport arc", "",
				 true));
		 /* EOC */

		 addMenuItem(drawMenu, inhibarcAction = new TypeAction("Inhibitor Arc",
				 Pipe.TAPNINHIBITOR_ARC, "Add an inhibitor arc", "H", true));

		 addMenuItem(drawMenu, annotationAction = new TypeAction("Annotation",
				 Pipe.ANNOTATION, "Add an annotation", "N", true));
		 drawMenu.addSeparator();
		 addMenuItem(drawMenu, tokenAction = new TypeAction("Add token",
				 Pipe.ADDTOKEN, "Add a token", "ADD", true));
		 addMenuItem(drawMenu, deleteTokenAction = new TypeAction(
				 "Delete token", Pipe.DELTOKEN, "Delete a token", "SUBTRACT",
				 true));

		 /* drawMenu.addSeparator(); */
		 /*
		  * addMenuItem(drawMenu, rateAction = new TypeAction("Rate Parameter",
		  * Pipe.RATE, "Rate Parameter", "R",true));
		  */
		 /*
		  * addMenuItem(drawMenu, markingAction = new
		  * TypeAction("Marking Parameter", Pipe.MARKING, "Marking Parameter",
		  * "M",true));
		  */

		 JMenu viewMenu = new JMenu("View");
		 viewMenu.setMnemonic('V');

		 zoomMenu = new JMenu("Zoom");
		 zoomMenu.setIcon(new ImageIcon(Thread.currentThread()
				 .getContextClassLoader().getResource(
						 CreateGui.imgPath + "Zoom.png")));
		 addZoomMenuItems(zoomMenu);

		 addMenuItem(viewMenu, zoomOutAction = new ZoomAction("Zoom out",
				 "Zoom out by 10% ", "ctrl MINUS"));
		 addMenuItem(viewMenu, zoomInAction = new ZoomAction("Zoom in",
				 "Zoom in by 10% ", "ctrl PLUS"));
		 viewMenu.add(zoomMenu);

		 viewMenu.addSeparator();
		 addMenuItem(viewMenu, toggleGrid = new GridAction("Cycle grid",
				 "Change the grid size", "G"));
		 addMenuItem(viewMenu, dragAction = new TypeAction("Drag", Pipe.DRAG,
				 "Drag the drawing", "D", true));
		 
		 
		 viewMenu.addSeparator();
		 addMenuItem(viewMenu, showComponentsAction = new ViewAction("Show Components", 
				 453243, "Show/Hide componens", "", true));
		 addMenuItem(viewMenu, showQueriesAction = new ViewAction("Show Queries", 
				 453244, "Show/Hide componens", "", true));
		 addMenuItem(viewMenu, showConstantsAction = new ViewAction("Show Constants", 
				 453245, "Show/Hide componens", "", true));

		 JMenu animateMenu = new JMenu("Simulator");
		 animateMenu.setMnemonic('A');
		 addMenuItem(animateMenu, startAction = new AnimateAction(
				 "Simulation mode", Pipe.START, "Toggle Simulation Mode",
				 "Ctrl A", true));
		 animateMenu.addSeparator();
		 addMenuItem(animateMenu, stepbackwardAction = new AnimateAction("Back",
				 Pipe.STEPBACKWARD, "Step backward a firing", "typed 4"));
		 addMenuItem(animateMenu,
				 stepforwardAction = new AnimateAction("Forward",
						 Pipe.STEPFORWARD, "Step forward a firing", "typed 6"));

		 addMenuItem(animateMenu, timeAction = new AnimateAction("Delay 1",
				 Pipe.TIMEPASS, "Let time pass 1 unit", "_"));

		 /*
		  * addMenuItem(animateMenu, randomAction = new AnimateAction("Random",
		  * Pipe.RANDOM, "Randomly fire a transition", "typed 5"));
		  * addMenuItem(animateMenu, randomAnimateAction = new
		  * AnimateAction("Simulate", Pipe.ANIMATE,
		  * "Randomly fire a number of transitions", "typed 7",true));
		  */
		 randomAction = new AnimateAction("Random", Pipe.RANDOM,
				 "Randomly fire a transition", "typed 5");
		 randomAnimateAction = new AnimateAction("Simulate", Pipe.ANIMATE,
				 "Randomly fire a number of transitions", "typed 7", true);

		 JMenu helpMenu = new JMenu("Help");
		 helpMenu.setMnemonic('H');

		 JMenuItem aboutItem = helpMenu.add("About");
		 aboutItem.addActionListener(this); // Help - About is implemented
		 // differently

		 URL iconURL = Thread.currentThread().getContextClassLoader()
		 .getResource(CreateGui.imgPath + "About.png");
		 if (iconURL != null) {
			 aboutItem.setIcon(new ImageIcon(iconURL));
		 }

		 menuBar.add(fileMenu);
		 menuBar.add(editMenu);
		 menuBar.add(viewMenu);
		 menuBar.add(drawMenu);
		 menuBar.add(animateMenu);
		 
		 menuBar.add(buildToolsMenu());
		 // menuBar.add(experimentMenu);
		 menuBar.add(helpMenu);
		 setJMenuBar(menuBar);

	}

	private JMenu buildToolsMenu() {
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		
		JMenuItem batchProcessing = new JMenuItem("Batch Processing");
		batchProcessing.setMnemonic('b');
		
		batchProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BatchProcessingDialog dialog = new BatchProcessingDialog(CreateGui.getApp(), "Batch Processing", true);
				dialog.pack();
				dialog.setMinimumSize(dialog.getSize());
				dialog.setLocationRelativeTo(null);
				dialog.setResizable(false);
				dialog.setVisible(true);
			}
		});
		
		toolsMenu.add(batchProcessing);
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
		drawingToolBar.add(tokenAction);
		drawingToolBar.add(deleteTokenAction);

		// Create panel to put toolbars in
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new FlowLayout(0, 0, 0));

		// Add toolbars to pane
		toolBarPanel.add(toolBar);
		toolBarPanel.add(drawingToolBar);

		// Create a toolBarPaneltmp usign broderlayout and a spacer to get
		// toolbar to fill
		// the screen
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
			JMenuItem newItem = new JMenuItem(new ZoomAction(zoomExamples[i],
					"Select zoom percentage", i < 10 ? "ctrl shift " + i : ""));
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
		KeyStroke keystroke = (KeyStroke) action
		.getValue(Action.ACCELERATOR_KEY);

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

		// Simulator
		startAction.setEnabled(enable);

	}

	// set frame objects by array index
	private void setObjects(int index) {
		appView = CreateGui.getDrawingSurface(index);
	}

	private void setObjectsNull(int index) {
		CreateGui.removeTab(index);
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
					if (appGui.getMode() == Pipe.SELECT) {
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

	// Less sucky yet far, far simpler to code About dialogue
	public void actionPerformed(ActionEvent e) {
		StringBuffer buffer = new StringBuffer(Pipe.getProgramName());
		buffer.append("\n\n");
		buffer.append("Joakim Byg, Lasse Jacobsen, Morten Jacobsen \n");
		buffer
		.append("Kenneth Yrke Joergensen, Mikael H. Moeller and Jiri Srba \n");
		buffer.append("Aalborg University 2009-2011 \n\n");
		buffer.append("Read more at: www.tapaal.net \n\n");

		Verifyta verifyta = new Verifyta();// TODO: MJ -- fix this

		String verifytaPath = verifyta.getPath();
		String verifytaversion = "";

		if (verifytaPath == null || verifytaPath.isEmpty()) {
			verifytaPath = "Not setup";
			verifytaversion = "N/A";
		} else {
			verifytaversion = verifyta.getVersion();
		}

		buffer.append("Verifyta Information:\n");
		buffer.append("   Located: ");
		buffer.append(verifytaPath);
		buffer.append("\n");
		buffer.append("   Version: ");
		buffer.append(verifytaversion);

		buffer.append("  \n\n");
		buffer.append("Based on PIPE2:\n");
		buffer.append("http://pipe2.sourceforge.net/");

		JOptionPane.showMessageDialog(this, buffer.toString(), "About TAPAAL",
				JOptionPane.INFORMATION_MESSAGE);
	}

	// HAK Method called by netModel object when it changes
	public void update(Observable o, Object obj) {
		if ((mode != Pipe.CREATING) && (!appView.isInAnimationMode())) {
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
	
	public void showComponents(boolean enable){
		showComponents = enable;
		CreateGui.getCurrentTab().showComponents(enable);
	}
	public void toggleComponents(){
		showComponents(!showComponents);
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
		Template template = new Template(new TimedArcPetriNet(templateName), new DataLayer());
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
					CreateGui.getApp().setMode(Pipe.CREATING);
				}

				ModelLoader loader = new ModelLoader(currentTab.drawingSurface());
				LoadedModel loadedModel = loader.load(file);
								
				currentTab.setNetwork(loadedModel.network(), loadedModel.templates());
				currentTab.setQueries(loadedModel.queries());
				currentTab.setConstants(loadedModel.network().constants());
				currentTab.setupNameGeneratorsFromTemplates(loadedModel.templates());

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
		if (CreateGui.getDrawingSurface(index).getNetChanged()) {
			int result = JOptionPane.showConfirmDialog(GuiFrame.this,
					"Current file has changed. Save current file?",
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
			if (checkForSave() == false) {
				return false;
			}
		}
		return true;
	}

	public void setRandomAnimationMode(boolean on) {

		if (on == false) {
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
			CreateGui.getAnimator().setFiringmode("Random");

			setEditionAllowed(false);
			statusBar.changeText(statusBar.textforAnimation);

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

	public void setFastMode(int _mode) {
		old_mode = mode;
		setMode(_mode);
	}

	public void setMode(int _mode) {
		// Don't bother unless new mode is different.
		if (mode != _mode) {
			prev_mode = mode;
			mode = _mode;
		}
	}

	public int getMode() {
		return mode;
	}

	public void restoreMode() {
		// xxx - This must be refactored when someone findes out excatly what is
		// gowing on
		mode = prev_mode;

		if (placeAction != null) {
			placeAction.setSelected(mode == Pipe.PLACE);
		}
		if (transAction != null) {
			transAction.setSelected(mode == Pipe.IMMTRANS);
		}

		if (timedtransAction != null) {
			timedtransAction.setSelected(mode == Pipe.TIMEDTRANS);
		}

		if (arcAction != null) {
			arcAction.setSelected(mode == Pipe.ARC);
		}

		if (timedArcAction != null)
			timedArcAction.setSelected(mode == Pipe.TAPNARC);

		if (transportArcAction != null)
			transportArcAction.setSelected(mode == Pipe.TRANSPORTARC);

		if (timedPlaceAction != null)
			timedPlaceAction.setSelected(mode == Pipe.TAPNPLACE);

		// if (inhibarcAction != null)
		// inhibarcAction.setSelected(mode == Pipe.TAPNINHIBITOR_ARC);

		if (tokenAction != null)
			tokenAction.setSelected(mode == Pipe.ADDTOKEN);

		if (deleteTokenAction != null)
			deleteTokenAction.setSelected(mode == Pipe.DELTOKEN);

		if (selectAction != null)
			selectAction.setSelected(mode == Pipe.SELECT);

		if (annotationAction != null)
			annotationAction.setSelected(mode == Pipe.ANNOTATION);

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
		setMode(Pipe.SELECT);
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

	private Component c = null; // arreglantzoom
	private Component p = new BlankLayer(this);

	/* */
	void hideNet(boolean doHide) {
		if (doHide) {
			c = appTab.getComponentAt(appTab.getSelectedIndex());
			appTab.setComponentAt(appTab.getSelectedIndex(), p);
		} else {
			if (c != null) {
				appTab.setComponentAt(appTab.getSelectedIndex(), c);
				c = null;
			}
		}
		appTab.repaint();
	}

	class AnimateAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8582324286370859664L;
		private int typeID;
		private AnimationHistoryComponent animBox;

		AnimateAction(String name, int typeID, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
			this.typeID = typeID;
		}

		AnimateAction(String name, int typeID, String tooltip,
				String keystroke, boolean toggleable) {
			super(name, tooltip, keystroke, toggleable);
			this.typeID = typeID;
		}

		public AnimateAction(String name, int typeID, String tooltip,
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
			case Pipe.START:
				try {
					setAnimationMode(!appView.isInAnimationMode());
					if (!appView.isInAnimationMode()) {
						restoreMode();
						PetriNetObject.ignoreSelection(false);
					} else {
						setMode(typeID);
						PetriNetObject.ignoreSelection(true);
						// Do we keep the selection??
						appView.getSelectionObject().clearSelection();
					}
				} catch (Exception e) {
					System.err.println(e);
					JOptionPane.showMessageDialog(GuiFrame.this, e.toString(),
							"Animation Mode Error", JOptionPane.ERROR_MESSAGE);
					startAction.setSelected(false);
					appView.changeAnimationMode(false);
				}
				stepforwardAction.setEnabled(false);
				stepbackwardAction.setEnabled(false);
				break;

			case Pipe.TIMEPASS:
				CreateGui.getAnimator().letTimePass(BigDecimal.ONE);
				break;

			case Pipe.STEPFORWARD:
				animBox.stepForward();
				CreateGui.getAnimator().stepForward();
				// update mouseOverView
				for (pipe.dataLayer.Place p : CreateGui.getModel().getPlaces()) {
					if (((TimedPlaceComponent) p).isAgeOfTokensShown()) {
						((TimedPlaceComponent) p).showAgeOfTokens(true);
					}
				}
				CreateGui.getAnimationController().setAnimationButtonsEnabled();
				break;

			case Pipe.STEPBACKWARD:
				animBox.stepBackwards();
				CreateGui.getAnimator().stepBack();
				// update mouseOverView
				for (pipe.dataLayer.Place p : CreateGui.getModel().getPlaces()) {
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

		/**
		 * 
		 */
		private static final long serialVersionUID = -5983638671592349736L;
		private File filename;

		ExampleFileAction(File file, String keyStroke) {
			super(file.getName().replace(".xml", ""), "Open example file \""
					+ file.getName().replace(".xml", "") + "\"", keyStroke);
			filename = file;// .getAbsolutePath();
			putValue(SMALL_ICON, new ImageIcon(Thread.currentThread()
					.getContextClassLoader().getResource(
							CreateGui.imgPath + "Net.png")));
		}

		public void actionPerformed(ActionEvent e) {
			createNewTabFromFile(filename);
		}

	}

	class DeleteAction extends GuiAction {

		/**
		 * 
		 */
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
				s.append("\n");
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
		private int typeID;

		TypeAction(String name, int typeID, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
			this.typeID = typeID;
		}

		TypeAction(String name, int typeID, String tooltip, String keystroke,
				boolean toggleable) {
			super(name, tooltip, keystroke, toggleable);
			this.typeID = typeID;
		}

		public void actionPerformed(ActionEvent e) {
			// if (!isSelected()){
			this.setSelected(true);

			// deselect other actions
			/*
			 * if (this != placeAction) { placeAction.setSelected(false); }
			 */
			if (this != transAction) {
				transAction.setSelected(false);
			}
			/*
			 * CB - Joakim Byg - Is not instansiated, since it is not needed in
			 * the model if (this != timedtransAction) {
			 * timedtransAction.setSelected(false); } EOC
			 */
			/*
			 * CB - Jiri Srba - Not part of the model if (this != arcAction) {
			 * arcAction.setSelected(false); } EOC
			 */
			/* CB - Joakim Byg - adding timed arcs */
			if (this != timedArcAction) {
				timedArcAction.setSelected(false);
			}
			/* EOC */
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

			if ((typeID != Pipe.ARC) && (appView.createArc != null)) {
				appView.createArc.delete();
				appView.createArc = null;
				appView.repaint();
				// Also handel trasport arcs (if any)

				if (appView.transportArcPart1 != null) {
					appView.transportArcPart1.delete();
					appView.transportArcPart1 = null;
					appView.repaint();
				}
			}

			// XXX - kyrke - Dont think this code will ever be runned, as the
			// above code vill handel it
			/* CB Joakim Byg - adding timed arc */
			if ((typeID != Pipe.TAPNARC) && (appView.createArc != null)) {
				appView.createArc.delete();
				appView.createArc = null;
				appView.repaint();

			}
			/* EOC */

			if (typeID == Pipe.SELECT) {
				// disable drawing to eliminate possiblity of connecting arc to
				// old coord of moved component
				statusBar.changeText(typeID);
				appView.getSelectionObject().enableSelection();
				appView.setCursorType("arrow");
			} else if (typeID == Pipe.DRAG) {
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

	class ZoomAction extends GuiAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 549331166742882564L;

		ZoomAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			boolean doZoom = false;
			try {
				String actionName = (String) getValue(NAME);
				Zoomer zoomer = appView.getZoomController();
				TabContent tabContent = (TabContent) appTab
				.getSelectedComponent();
				JViewport thisView = tabContent.drawingSurfaceScrollPane()
				.getViewport();
				String selection = null, strToTest = null;

				double midpointX = Zoomer.getUnzoomedValue(thisView
						.getViewPosition().x
						+ (thisView.getWidth() * 0.5), zoomer.getPercent());
				double midpointY = Zoomer.getUnzoomedValue(thisView
						.getViewPosition().y
						+ (thisView.getHeight() * 0.5), zoomer.getPercent());

				if (actionName.equals("Zoom in")) {
					doZoom = zoomer.zoomIn();
				} else if (actionName.equals("Zoom out")) {
					doZoom = zoomer.zoomOut();
				} else {
					if (actionName.equals("Zoom")) {
						selection = (String) zoomComboBox.getSelectedItem();
					}
					if (e.getSource() instanceof JMenuItem) {
						selection = ((JMenuItem) e.getSource()).getText();
					}
					strToTest = validatePercent(selection);

					if (strToTest != null) {
						// BK: no need to zoom if already at that level
						if (zoomer.getPercent() == Integer.parseInt(strToTest)) {
							return;
						} else {
							zoomer.setZoom(Integer.parseInt(strToTest));
							doZoom = true;
						}
					} else {
						return;
					}
				}
				if (doZoom == true) {
					updateZoomCombo();
					appView.zoomTo(new java.awt.Point((int) midpointX,
							(int) midpointY));
				}
			} catch (ClassCastException cce) {
				// zoom
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private String validatePercent(String selection) {

			try {
				String toTest = selection;

				if (selection.endsWith("%")) {
					toTest = selection.substring(0, (selection.length()) - 1);
				}

				if (Integer.parseInt(toTest) < Pipe.ZOOM_MIN
						|| Integer.parseInt(toTest) > Pipe.ZOOM_MAX) {
					throw new Exception();
				} else {
					return toTest;
				}
			} catch (Exception e) {
				zoomComboBox.setSelectedItem("");
				return null;
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
		private int typeID;

		ViewAction(String name, int typeID, String tooltip, String keystroke,
				boolean toggleable) {
			super(name, tooltip, keystroke, toggleable);
			this.typeID = typeID;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			if (this == showComponentsAction){
				toggleComponents();
			} else if (this == showQueriesAction){
				toggleQueries();
			} else if (this == showConstantsAction){
				toggleConstants();
			}
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

					// TODO make update leftPane work better
					// CreateGui.updateLeftPanel();
				}
			} else if (this == createAction) {
				showNewPNDialog();
			} else if ((this == exitAction) && checkForSaveAll()) {
				dispose();
				System.exit(0);
			} else if ((this == closeAction) && (appTab.getTabCount() > 0)
					&& checkForSave()) {
				// Set GUI mode to noNet
				setGUIMode(GUIMode.noNet);

				setObjectsNull(appTab.getSelectedIndex());
				appTab.remove(appTab.getSelectedIndex());

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
			if (evt.getPropertyName() == "selected") {
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
				Pipe.getProgramName(), true);

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
