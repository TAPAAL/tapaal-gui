package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DataLayerWriter;
import pipe.dataLayer.PNMLTransformer;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TNTransformer;
import pipe.dataLayer.TimedPlace;
import pipe.experiment.Experiment;
import pipe.experiment.editor.gui.ExperimentEditor;
import pipe.gui.action.GuiAction;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.FileBrowser;
import pipe.gui.widgets.NewTAPNPanel;
import pipe.gui.widgets.QueryDialogue;
import pipe.gui.widgets.QueryDialogue.QueryDialogueOption;
import pipe.io.JarUtilities;
import dk.aau.cs.petrinet.TAPN;


/**
 * @author Edwin Chung changed the code so that the firedTransitions array list
 * is reset when the animation mode is turned off
 *
 * @author Ben Kirby, 10 Feb 2007: Changed the saveNet() method so that it calls
 * new DataLayerWriter class and passes in current net to save.
 *
 * @author Ben Kirby, 10 Feb 2007: Changed the createNewTab method so that it
 * loads an XML file using the new PNMLTransformer class and createFromPNML
 * DataLayer method.
 *
 * @author Edwin Chung modifed the createNewTab method so that it assigns the
 * file name of the newly created DataLayer object in the dataLayer class
 * (Mar 2007)
 *
 * @author Oliver Haggarty modified initaliseActions to fix a bug that meant
 * not all example nets were loaded if there was a non .xml file in the folder
 */
public class GuiFrame
extends JFrame
implements ActionListener, Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7509589834941127217L;
	// for zoom combobox and dropdown
	private final String[] zoomExamples = {"40%","60%","80%","100%","120%",
			"140%","160%","180%","200%","300%"};
	private String frameTitle;  //Frame title
	private DataLayer appModel;
	private GuiFrame appGui;
	private GuiView appView;
	private int mode, prev_mode, old_mode;             // *** mode WAS STATIC ***
	private int newNameCounter = 1;
	private JTabbedPane appTab;
	private StatusBar statusBar;
	private JMenuBar menuBar;
	private JToolBar drawingToolBar;
	//private Map actions = new HashMap();
	private JComboBox zoomComboBox;

	//	XXX kyrke testing 
	private FileAction createAction, openAction, closeAction, saveAction,
	saveAsAction, exitAction, printAction, exportPNGAction,
	exportTNAction, exportPSAction, importAction, exportToUppaal,
	exportToUppaalAdvanced,exportToUppaalSymetric,  exportToTest,exportToTikZAction;

	private VerificationAction runUppaalVerification;

	private EditAction /*copyAction, cutAction, pasteAction,*/ undoAction, redoAction;
	private GridAction toggleGrid;
	private ZoomAction zoomOutAction, zoomInAction, zoomAction;
	private DeleteAction deleteAction;
	private TypeAction annotationAction, arcAction, inhibarcAction, placeAction,
	transAction, timedtransAction, tokenAction, selectAction, rateAction,
	markingAction, deleteTokenAction, dragAction, timedPlaceAction;
	/* CB Joakim Byg - tries both */   
	private TypeAction timedArcAction;

	private TypeAction transportArcAction;

	/*EOC*/   
	private AnimateAction startAction, stepforwardAction, stepbackwardAction,
	randomAction, randomAnimateAction, timeAction;

	public boolean dragging = false;

	private HelpBox helpAction;
	private ExperimentAction loadExperimentAction, experimentEditorAction;


	private boolean editionAllowed = true;

	private CopyPasteManager copyPasteManager;



	public GuiFrame(String title) {
		// HAK-arrange for frameTitle to be initialized and the default file name
		// to be appended to basic window title
		frameTitle = title;
		setTitle(null);

		try {
			//Set the Look and Feel native for the system.
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			//2010-05-07, Kenneth Yrke Jørgensen:
			//If the native look and feel is GTK replace the useless open dialog, 
			//with a java-reimplementation. 		
			if ("GTK look and feel".equals(UIManager.getLookAndFeel().getName())){
				UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
			}
		} catch (Exception exc) {
			System.err.println("Error loading L&F: " + exc);
		}

		this.setIconImage(new ImageIcon(
				Thread.currentThread().getContextClassLoader().getResource(
						CreateGui.imgPath + "icon.png")).getImage());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize.width * 80/100, screenSize.height  * 80/100);
		this.setLocationRelativeTo(null);
		this.setMinimumSize(new Dimension(640,480));


		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		buildMenus();

		// Status bar...
		statusBar = new StatusBar();
		getContentPane().add(statusBar, BorderLayout.PAGE_END);

		// Build menus
		buildToolbar();

		addWindowListener(new WindowHandler());

		//
		copyPasteManager = new CopyPasteManager();

		this.setForeground(java.awt.Color.BLACK);
		this.setBackground(java.awt.Color.WHITE);
	}


	/**
	 * This method does build the menus.
	 *
	 * @author unknown
	 *
	 * @author Dave Patterson - fixed problem on OSX due to invalid character
	 * in URI caused by unescaped blank. The code changes one blank character
	 * if it exists in the string version of the URL. This way works safely in
	 * both OSX and Windows. I also added a printStackTrace if there is an
	 * exception caught in the setup for the "Example nets" folder.
	 **/
	private void buildMenus() {
		menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');

		addMenuItem(fileMenu, createAction =
			new FileAction("New",    "Create a new Petri net","ctrl N"));
		addMenuItem(fileMenu, openAction   =
			new FileAction("Open",   "Open","ctrl O"));
		addMenuItem(fileMenu, closeAction  =
			new FileAction("Close",  "Close the current tab","ctrl W"));
		fileMenu.addSeparator();
		/*addMenuItem(fileMenu, importAction  =
			new FileAction("Import",  "Import from Timenet",""));*/
		addMenuItem(fileMenu, saveAction   =
			new FileAction("Save",   "Save","ctrl S"));
		addMenuItem(fileMenu, saveAsAction =
			new FileAction("Save as","Save as...","shift ctrl S"));

		// Export menu
		JMenu exportMenu=new JMenu("Export");
		exportMenu.setIcon(
				new ImageIcon(Thread.currentThread().getContextClassLoader().
						getResource(CreateGui.imgPath + "Export.png")));
		addMenuItem(exportMenu, exportPNGAction  =
			new FileAction("PNG", "Export the net to PNG format","ctrl G"));
		addMenuItem(exportMenu, exportPSAction  =
			new FileAction("PostScript", "Export the net to PostScript format","ctrl T"));
		addMenuItem(exportMenu, exportToTikZAction  =
			new FileAction("TikZ", "Export the net to PNG format","ctrl L"));
		/*		addMenuItem(exportMenu, exportTNAction =
			new FileAction("Timenet","Export the net to Timenet format",""));
		addMenuItem(exportMenu, exportToUppaal =
			new FileAction("Export to Uppaal","Export the to Uppaal format",""));
		addMenuItem(exportMenu, exportToUppaalAdvanced =
			new FileAction("Export to Uppaal Advanced","Export the to Uppaal format",""));
		addMenuItem(exportMenu, exportToUppaalSymetric =
			new FileAction("Export to Uppaal, Symetric","Export the to Uppaal format, with symetricreduction",""));
		addMenuItem(exportMenu, exportToTest =
			new FileAction("Export a Degree-2 net","Export the to Uppaal format",""));
<<<<<<< TREE
		 */	
		fileMenu.add(exportMenu);

		 fileMenu.addSeparator();
		 addMenuItem(fileMenu, printAction  =
			 new FileAction("Print",  "Print","ctrl P"));
		 fileMenu.addSeparator();

		 // Example files menu
		 try {
			 URL examplesDirURL = Thread.currentThread().getContextClassLoader().
			 getResource("Example nets" + System.getProperty("file.separator"));

			 if (JarUtilities.isJarFile(examplesDirURL)){

				 JarFile jarFile = new JarFile(JarUtilities.getJarName(examplesDirURL));

				 ArrayList <JarEntry> nets =
					 JarUtilities.getJarEntries(jarFile, "Example nets");

				 Arrays.sort(nets.toArray(), new Comparator(){
					 public int compare(Object one, Object two) {
						 return ((JarEntry)one).getName().compareTo(((JarEntry)two).getName());
					 }
				 });

				 if (nets.size() > 0) {
					 JMenu exampleMenu=new JMenu("Example nets");
					 exampleMenu.setIcon(
							 new ImageIcon(Thread.currentThread().getContextClassLoader().
									 getResource(CreateGui.imgPath + "Example.png")));
					 int index = 0;
					 for (int i = 0; i < nets.size(); i++){
						 if (nets.get(i).getName().toLowerCase().endsWith(".xml")){
							 addMenuItem(exampleMenu,
									 new ExampleFileAction(nets.get(i),
											 (index < 10) ?("ctrl " + index) :null));
							 index++;
						 }
					 }
					 fileMenu.add(exampleMenu);
					 fileMenu.addSeparator();
				 }
			 } else {
				 File examplesDir = new File(examplesDirURL.toURI());
				 /**
				  * The next block fixes a problem that surfaced on Mac OSX with
				  * PIPE 2.4. In that environment (and not in Windows) any blanks
				  * in the project name in Eclipse are property converted to '%20'
				  * but the blank in "Example nets" is not. The following code
				  * will do nothing on a Windows machine or if the logic on OSX
				  * changess. I also added a stack trace so if the problem
				  * occurs for another environment (perhaps multiple blanks need
				  * to be manually changed) it can be easily fixed.  DP
				  */
				 // examplesDir = new File(new URI(examplesDirURL.toString()));
				 String dirURLString = examplesDirURL.toString();
				 int index = dirURLString.indexOf( " " );
				 if ( index > 0 ) {
					 StringBuffer sb = new StringBuffer( dirURLString );
					 sb.replace( index, index + 1, "%20" );
					 dirURLString = sb.toString();
				 }

				 examplesDir = new File( new URI(dirURLString ) );

				 File[] nets = examplesDir.listFiles();

				 Arrays.sort(nets,new Comparator(){
					 public int compare(Object one, Object two) {

						 int toReturn=((File)one).getName().compareTo(((File)two).getName());
						 //Special hack to get intro-example first
						 if (((File)one).getName().equals("intro-example.xml")){toReturn=-1;}
						 if (((File)two).getName().equals("intro-example.xml")){toReturn=1;}
						 return toReturn;
					 }
				 });

				 // Oliver Haggarty - fixed code here so that if folder contains non
				 // .xml file the Example x counter is not incremented when that file
				 // is ignored
				 if (nets.length > 0) {
					 JMenu exampleMenu=new JMenu("Example nets");
					 exampleMenu.setIcon(
							 new ImageIcon(Thread.currentThread().getContextClassLoader().
									 getResource(CreateGui.imgPath + "Example.png")));
					 int k = 0;
					 for (int i = 0; i < nets.length; i++){
						 if(nets[i].getName().toLowerCase().endsWith(".xml")){
							 addMenuItem(exampleMenu,
									 new ExampleFileAction(nets[i], (k<10)?"ctrl " + (k++) :null));
						 }
					 }
					 fileMenu.add(exampleMenu);
					 fileMenu.addSeparator();
				 }
			 }
		 } catch (Exception e) {
			 System.err.println("Error getting example files:" + e);
			 e.printStackTrace();
		 }
		 addMenuItem(fileMenu, exitAction =
			 new FileAction("Exit", "Close the program", "ctrl Q"));

		 JMenu editMenu = new JMenu("Edit");
		 editMenu.setMnemonic('E');
		 addMenuItem(editMenu, undoAction =
			 new EditAction("Undo", "Undo (Ctrl-Z)", "ctrl Z"));
		 addMenuItem(editMenu, redoAction =
			 new EditAction("Redo", "Redo (Ctrl-Y)","ctrl Y"));
		 editMenu.addSeparator();
		 /*		
		addMenuItem(editMenu, cutAction =
			new EditAction("Cut", "Cut (Ctrl-X)","ctrl X"));
		addMenuItem(editMenu, copyAction =
			new EditAction("Copy", "Copy (Ctrl-C)","ctrl C"));
		addMenuItem(editMenu, pasteAction =
			new EditAction("Paste", "Paste (Ctrl-V)","ctrl V"));
		  */		addMenuItem(editMenu, deleteAction =
			  new DeleteAction("Delete", "Delete selection","DELETE"));

		  // Bind delete to backspace also
		  editMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("BACK_SPACE"),"Delete");
		  editMenu.getActionMap().put("Delete", deleteAction);


		  JMenu drawMenu = new JMenu("Draw");
		  drawMenu.setMnemonic('D');
		  addMenuItem(drawMenu, selectAction =
			  new TypeAction("Select", Pipe.SELECT, "Select components","S",true));
		  drawMenu.addSeparator();

		  //kyrke inserted timed place
		  addMenuItem(drawMenu, timedPlaceAction =
			  new TypeAction("Place", Pipe.TAPNPLACE, "Add a place","L",true));
		  //jokke removed normal places
		  //		addMenuItem(drawMenu, placeAction =
		  //		new TypeAction("Place", Pipe.PLACE, "Add a place","P",true));

		  addMenuItem(drawMenu, transAction =
			  new TypeAction("Transition", Pipe.TAPNTRANS,
					  "Add a transition","I",true));

		  /*addMenuItem(drawMenu, transAction =
			new TypeAction("Transition", Pipe.IMMTRANS,
					"Add an immediate transition","I",true));*/
		  /* CB Joakim Byg - Not part of the model
      addMenuItem(drawMenu, timedtransAction  =
              new TypeAction("Timed transition", Pipe.TIMEDTRANS,
              "Add a timed transition","T",true));
EOC */              
		  /* CB Jiri Srba - Not part of the model
         addMenuItem(drawMenu, arcAction = new TypeAction("Arc", Pipe.ARC, "Add an arc","A",true));" +
EOC */

		  /*CB Joakim Byg - Adding timed arcs*/
		  addMenuItem(drawMenu, timedArcAction = new TypeAction("Arc", Pipe.TAPNARC, "Add an arc","R",true));

		  addMenuItem(drawMenu, transportArcAction = new TypeAction("Transport Arc", Pipe.TRANSPORTARC, "Add a transport arc", "", true));
		  /*EOC*/

		  addMenuItem(drawMenu, inhibarcAction =
			  new TypeAction("Inhibitor Arc", Pipe.TAPNINHIBITOR_ARC,
					  "Add an inhibitor arc", "H",true));              

		  addMenuItem(drawMenu, annotationAction =
			  new TypeAction("Annotation", Pipe.ANNOTATION,
					  "Add an annotation","N",true));
		  drawMenu.addSeparator();
		  addMenuItem(drawMenu, tokenAction =
			  new TypeAction("Add token", Pipe.ADDTOKEN, "Add a token", "ADD", true));
		  addMenuItem(drawMenu, deleteTokenAction =
			  new TypeAction("Delete token", Pipe.DELTOKEN, "Delete a token",
					  "SUBTRACT",true));
		  /*drawMenu.addSeparator();*/
		  /*		addMenuItem(drawMenu, rateAction =
			new TypeAction("Rate Parameter", Pipe.RATE, "Rate Parameter",
					"R",true));
		   */					
		  /*addMenuItem(drawMenu, markingAction =
			new TypeAction("Marking Parameter", Pipe.MARKING, "Marking Parameter",
					"M",true));*/

		  JMenu viewMenu = new JMenu("View");
		  viewMenu.setMnemonic('V');

		  JMenu zoomMenu=new JMenu("Zoom");
		  zoomMenu.setIcon(
				  new ImageIcon(Thread.currentThread().getContextClassLoader().
						  getResource(CreateGui.imgPath + "Zoom.png")));
		  addZoomMenuItems(zoomMenu);

		  addMenuItem(viewMenu, zoomOutAction =
			  new ZoomAction("Zoom out","Zoom out by 10% ", "ctrl MINUS"));
		  addMenuItem(viewMenu, zoomInAction =
			  new ZoomAction("Zoom in","Zoom in by 10% ", "ctrl PLUS"));
		  viewMenu.add(zoomMenu);

		  viewMenu.addSeparator();
		  addMenuItem(viewMenu, toggleGrid =
			  new GridAction("Cycle grid", "Change the grid size", "G"));
		  addMenuItem(viewMenu, dragAction =
			  new TypeAction("Drag", Pipe.DRAG, "Drag the drawing", "D", true));


		  JMenu animateMenu = new JMenu("Simulator");
		  animateMenu.setMnemonic('A');
		  addMenuItem(animateMenu, startAction =
			  new AnimateAction("Simulation mode", Pipe.START,
					  "Toggle Simulation Mode", "Ctrl A", true));
		  animateMenu.addSeparator();
		  addMenuItem(animateMenu, stepbackwardAction =
			  new AnimateAction("Back", Pipe.STEPBACKWARD,
					  "Step backward a firing", "typed 4"));
		  addMenuItem(animateMenu, stepforwardAction  =
			  new AnimateAction("Forward", Pipe.STEPFORWARD,
					  "Step forward a firing", "typed 6"));

		  addMenuItem(animateMenu, timeAction = new AnimateAction("Time", Pipe.TIMEPASS, "Let time pass", "_"));

		  /*addMenuItem(animateMenu, randomAction =
              new AnimateAction("Random", Pipe.RANDOM,
              "Randomly fire a transition", "typed 5"));
      addMenuItem(animateMenu, randomAnimateAction =
              new AnimateAction("Simulate", Pipe.ANIMATE,
              "Randomly fire a number of transitions", "typed 7",true));*/
		  randomAction =
			  new AnimateAction("Random", Pipe.RANDOM,
					  "Randomly fire a transition", "typed 5");
		  randomAnimateAction =
			  new AnimateAction("Simulate", Pipe.ANIMATE,
					  "Randomly fire a number of transitions", "typed 7",true);      

		  // XXX - kyrke - by some reason the animation actions started to be enabled when 
		  //starting TAPAAL, so for now de just disable them here :s
		  stepbackwardAction.setEnabled(false);
		  stepforwardAction.setEnabled(false);
		  timeAction.setEnabled(false);
		  randomAction.setEnabled(false);
		  randomAnimateAction.setEnabled(false);

		  JMenu helpMenu = new JMenu("Help");
		  helpMenu.setMnemonic('H');
		  helpAction = new HelpBox("Help", "View documentation", "F1", "index.htm");

		  addMenuItem(helpMenu,helpAction);
		  JMenuItem aboutItem = helpMenu.add("About");
		  aboutItem.addActionListener(this); // Help - About is implemented differently

		  URL iconURL = Thread.currentThread().getContextClassLoader().
		  getResource(CreateGui.imgPath + "About.png");
		  if (iconURL != null) {
			  aboutItem.setIcon(new ImageIcon(iconURL));
		  }

		  JMenu experimentMenu = new JMenu("Experiment");
		  addMenuItem(experimentMenu, loadExperimentAction =
			  new ExperimentAction("Load experiment", "Load an experiment file",""));
		  addMenuItem(experimentMenu, experimentEditorAction =
			  new ExperimentAction("Experiment Editor", "Start the experiment editor",""));

		  menuBar.add(fileMenu);
		  menuBar.add(editMenu);
		  menuBar.add(viewMenu);
		  menuBar.add(drawMenu);

		  menuBar.add(animateMenu);

		  //menuBar.add(experimentMenu);
		  menuBar.add(helpMenu);
		  setJMenuBar(menuBar);


	}


	private void buildToolbar() {
		// Create the toolbar
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);//Inhibit toolbar floating
		
		//Basis file operations
		toolBar.add(createAction);
		toolBar.add(openAction);
		toolBar.add(saveAction);
		toolBar.add(saveAsAction);
		toolBar.add(closeAction);
		
		//Print
		toolBar.addSeparator();	
		toolBar.add(printAction);
							
		//Copy/past
		/* Removed copy/past button
		toolBar.addSeparator();			
		toolBar.add(cutAction);
	    toolBar.add(copyAction);
	    toolBar.add(pasteAction);
		 */
		
		//Undo/redo
		toolBar.addSeparator();	
		toolBar.add(deleteAction);
		toolBar.add(undoAction);
		toolBar.add(redoAction);
		
		//Zoom	
		toolBar.addSeparator();
		toolBar.add(zoomOutAction);
		addZoomComboBox(toolBar,
				zoomAction = new ZoomAction("Zoom","Select zoom percentage ", ""));
		toolBar.add(zoomInAction);
		
		//Modes
		toolBar.addSeparator();
		toolBar.add(toggleGrid);
		toolBar.add(new ToggleButton(dragAction));
		toolBar.add(new ToggleButton(startAction));

		//Start drawingToolBar
		drawingToolBar = new JToolBar();
		drawingToolBar.setFloatable(false);

		//Normal arraw
		drawingToolBar.addSeparator();
		drawingToolBar.add(new ToggleButton(selectAction));
		
		//Drawing elements
		drawingToolBar.addSeparator();
		drawingToolBar.add(new ToggleButton(timedPlaceAction));
		drawingToolBar.add(new ToggleButton(transAction));
		drawingToolBar.add(new ToggleButton(timedArcAction));
		drawingToolBar.add(new ToggleButton(transportArcAction));
		drawingToolBar.add(new ToggleButton(inhibarcAction));
		drawingToolBar.add(new ToggleButton(annotationAction));
		
		//Tokens
		drawingToolBar.addSeparator();
		drawingToolBar.add(tokenAction);
		drawingToolBar.add(deleteTokenAction);

		//Add drawingtoolbar to toolbar
		drawingToolBar.addSeparator();
		toolBar.add(drawingToolBar);

		for(int i=0;i<toolBar.getComponentCount();i++){
			toolBar.getComponent(i).setFocusable(false);
		}

		getContentPane().add(toolBar,BorderLayout.PAGE_START);

	}

	/**
	 * @author Ben Kirby
	 * Takes the method of setting up the Zoom menu out of the main buildMenus method.
	 * @param JMenu - the menu to add the submenu to
	 */
	private void addZoomMenuItems(JMenu zoomMenu) {
		for(int i=0; i <= zoomExamples.length-1; i++) {
			JMenuItem newItem = new JMenuItem(
					new ZoomAction(zoomExamples[i],"Select zoom percentage",
							i<10 ? "ctrl shift " + i: ""));
			zoomMenu.add(newItem);
		}
	}


	/**
	 * @author Ben Kirby
	 * Just takes the long-winded method of setting up the ComboBox out of the
	 * main buildToolbar method.
	 * Could be adapted for generic addition of comboboxes
	 * @param toolBar the JToolBar to add the button to
	 * @param action the action that the ZoomComboBox performs
	 */
	private void addZoomComboBox(JToolBar toolBar, Action action) {
		Dimension zoomComboBoxDimension = new Dimension(75,28);
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


	private JMenuItem addMenuItem(JMenu menu, Action action){
		JMenuItem item = menu.add(action);
		KeyStroke keystroke = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);

		if (keystroke != null) {
			item.setAccelerator(keystroke);
		}
		return item;
	}


	/* sets all buttons to enabled or disabled according to status. */
	private void enableActions(boolean status){

		saveAction.setEnabled(status);
		saveAsAction.setEnabled(status);

		timedPlaceAction.setEnabled(status);
		timedArcAction.setEnabled(status);
		transportArcAction.setEnabled(status);

		//		placeAction.setEnabled(status);
		//		arcAction.setEnabled(status);
		//inhibarcAction.setEnabled(status);
		annotationAction.setEnabled(status);
		transAction.setEnabled(status);
		//		timedtransAction.setEnabled(status);
		tokenAction.setEnabled(status);
		deleteAction.setEnabled(status);
		selectAction.setEnabled(status);
		deleteTokenAction.setEnabled(status);
		//		rateAction.setEnabled(status);
		//		markingAction.setEnabled(status);

		//      toggleGrid.setEnabled(status);

		if (status) {
			startAction.setSelected(false);
			randomAnimateAction.setSelected(false);
			timeAction.setEnabled(false);
			stepbackwardAction.setEnabled(!status);
			stepforwardAction.setEnabled(!status);
			drawingToolBar.setVisible(true);
			
		}
		randomAction.setEnabled(!status);
		randomAnimateAction.setEnabled(!status);


		if (!status){
			drawingToolBar.setVisible(false);
			timeAction.setEnabled(true);
			//			pasteAction.setEnabled(status);
			undoAction.setEnabled(status);
			redoAction.setEnabled(status);
		} else {
			//			pasteAction.setEnabled(getCopyPasteManager().pasteEnabled());
		}
		//		copyAction.setEnabled(status);
		//		cutAction.setEnabled(status);
		deleteAction.setEnabled(status);
	}

	//set frame objects by array index
	private void setObjects(int index){
		appModel = CreateGui.getModel(index);
		appView = CreateGui.getView(index);
	}


	//HAK set current objects in Frame
	public void setObjects(){
		appModel = CreateGui.getModel();
		appView = CreateGui.getView();
	}


	private void setObjectsNull(int index){
		CreateGui.removeTab(index);
	}


	// set tabbed pane properties and add change listener that updates tab with
	// linked model and view
	public void setTab(){

		appTab = CreateGui.getTab();
		appTab.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

				if (appGui.getCopyPasteManager().pasteInProgress()) {
					appGui.getCopyPasteManager().cancelPaste();
				}

				int index = appTab.getSelectedIndex();
				setObjects(index);
				if (appView != null) {
					appView.setVisible(true);
					appView.repaint();
					updateZoomCombo();

					//					enableActions(!appView.isInAnimationMode());
					//					CreateGui.getAnimator().restoreModel();
					//					CreateGui.removeAnimationHistory();

					setTitle(appTab.getTitleAt(index));

					setAnimationMode(false);

					// TODO: change this code... it's ugly :)
					if (appGui.getMode() == Pipe.SELECT) {
						appGui.init();
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
	public void actionPerformed(ActionEvent e){
		StringBuffer buffer = new StringBuffer(Pipe.getProgramName());
		buffer.append("\n\n");
		buffer.append("Joakim Byg, Lasse Jacobsen, Morten Jacobsen \n");
		buffer.append("Kenneth Yrke Joergensen, Mikael H. Moeller and Jiri Srba \n");
		buffer.append("Aalborg University 2009 \n\n");
		buffer.append("Read more at: www.tapaal.net \n\n");
		buffer.append("verifyta:\n");
		buffer.append("   Version: ");
		buffer.append(Verification.getVerifytaVersion());
		buffer.append("\n");
		buffer.append("   Located: ");

		if(Verification.verifytapath == null || Verification.verifytapath.equals(""))
			buffer.append("N/A");
		else
			buffer.append(Verification.verifytapath);

		buffer.append("  \n\n"); 
		buffer.append("Based on PIPE2:\n");
		buffer.append("http://pipe2.sourceforge.net/");


		JOptionPane.showMessageDialog(this,
				buffer.toString(),
				"About TAPAAL",
				JOptionPane.INFORMATION_MESSAGE);
	}


	// HAK Method called by netModel object when it changes
	public void update(Observable o, Object obj){
		if ((mode != Pipe.CREATING) && (!appView.isInAnimationMode())) {
			appView.setNetChanged(true);
		}
	}


	public void saveOperation(boolean forceSaveAs){

		if (appView == null) {
			return;
		}

		File modelFile = CreateGui.getFile();
		if (!forceSaveAs && modelFile != null) { // ordinary save
			/*
      //Disabled as currently ALWAYS prevents the net from being saved - Nadeem 26/05/2005
         if (!appView.netChanged) {
            return;
         }
			 */
			saveNet(modelFile);
		} else {                              // save as
			String path = null;
			if (modelFile != null) {
				path = modelFile.toString();
			} else {
				path = appTab.getTitleAt(appTab.getSelectedIndex());
			}
			String filename = new FileBrowser(path).saveFile();
			if (filename != null) {
				saveNet(new File(filename));
			}
		}
	}


	private void saveNet(File outFile){
		try{
			// BK 10/02/07:
			// changed way of saving to accomodate new DataLayerWriter class
			DataLayerWriter saveModel = new DataLayerWriter(appModel);
			saveModel.savePNML(outFile);
			//appModel.savePNML(outFile);

			CreateGui.setFile(outFile,appTab.getSelectedIndex());
			appView.setNetChanged(false);
			appTab.setTitleAt(appTab.getSelectedIndex(),outFile.getName());
			setTitle(outFile.getName());  // Change the window title
			appView.getUndoManager().clear();
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
		} catch (Exception e) {
			System.err.println(e);
			JOptionPane.showMessageDialog(GuiFrame.this,
					e.toString(),
					"File Output Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	public void createNewTab(String name){
		int freeSpace = CreateGui.getFreeSpace();

		// if we are in the middle of a paste action, we cancel it because we will
		// create a new tab now
		if (this.getCopyPasteManager().pasteInProgress()) {
			this.getCopyPasteManager().cancelPaste();
		}

		setObjects(freeSpace);
		int currentlySelected = appTab.getSelectedIndex();

		if (name == null || name.isEmpty()) {
			name = "New Petri net " + (newNameCounter++) + ".xml";
		}

		JScrollPane scroller = new JScrollPane(appView);
		// make it less bad on XP
		scroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		appTab.addTab(name,null,scroller,null);
		appTab.setSelectedIndex(freeSpace);


		appModel.addObserver((Observer)appView); // Add the view as Observer
		appModel.addObserver((Observer)appGui);  // Add the app window as observer

		appView.setNetChanged(false);   // Status is unchanged
		appView.updatePreferredSize();

		//appView.add( new ViewExpansionComponent(appView.getWidth(),
		//        appView.getHeight());

		setTitle(name);// Change the program caption
		appTab.setTitleAt(freeSpace, name);
		selectAction.actionPerformed(null);
	}

	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 * @param filename Filename of net to load, or <b>null</b> to create a new,
	 *                 empty tab
	 */
	public void createNewTabFromFile(File file, boolean isTN) {
		int freeSpace = CreateGui.getFreeSpace();
		String name="";

		// if we are in the middle of a paste action, we cancel it because we will
		// create a new tab now
		if (this.getCopyPasteManager().pasteInProgress()) {
			this.getCopyPasteManager().cancelPaste();
		}

		setObjects(freeSpace);
		int currentlySelected = appTab.getSelectedIndex();

		if (file == null) {
			name = "New Petri net " + (newNameCounter++) + ".xml";
		} else {
			name = file.getName();
		}

		JScrollPane scroller = new JScrollPane(appView);
		// make it less bad on XP
		scroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		appTab.addTab(name,null,scroller,null);
		appTab.setSelectedIndex(freeSpace);


		appModel.addObserver((Observer)appView); // Add the view as Observer
		appModel.addObserver((Observer)appGui);  // Add the app window as observer

		if(file != null){
			try {
				//BK 10/02/07: Changed loading of PNML to accomodate new
				//PNMLTransformer class
				CreateGui.setFile(file,freeSpace);
				if (isTN) {
					TNTransformer transformer=new TNTransformer();
					appModel.createFromPNML(
							transformer.transformTN(file.getPath()));
				} else {
					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document doc = builder.parse(file);

					Node top = doc.getElementsByTagName("net").item(0);
					boolean colors = ((Element)top).getAttribute("type").equals("Colored P/T net");

					if(colors){
						appModel.createFromPNML(doc,true);
					}else{
						PNMLTransformer transformer = new PNMLTransformer();
						Document PNMLDoc = transformer.transformPNML(file.getPath()); // TODO: loads the file a second time
						appModel.createFromPNML(PNMLDoc, false);
					}

					appView.scrollRectToVisible(new Rectangle(0,0,1,1));
				}

				CreateGui.setFile(file,freeSpace);
			} catch(Exception e) {
				undoAddTab(currentlySelected);
				JOptionPane.showMessageDialog(
						GuiFrame.this,
						"Error loading file:\n" + name + "\nGuru meditation:\n"
						+ e.toString(),
						"File load error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();				

				return;
			}
		}

		appView.setNetChanged(false);   // Status is unchanged



		appView.updatePreferredSize();

		//appView.add( new ViewExpansionComponent(appView.getWidth(),
		//        appView.getHeight());

		setTitle(name);// Change the program caption
		appTab.setTitleAt(freeSpace, name);
		selectAction.actionPerformed(null);
	}

	private void undoAddTab(int currentlySelected) {
		CreateGui.undoGetFreeSpace();
		appTab.removeTabAt(appTab.getTabCount()-1);
		appTab.setSelectedIndex(currentlySelected);

	}


	// XXX - testting kyrke
	/**
	 * Creates a new tab with a given AppModel 
	 * @param model Appmodel of a PN
	 * @author kyrke@cs.aau.dk
	 */
	public void createNewTab(TAPN model) {
		int freeSpace = CreateGui.getFreeSpace();
		String name="";

		// if we are in the middle of a paste action, we cancel it because we will
		// create a new tab now
		if (this.getCopyPasteManager().pasteInProgress()) {
			this.getCopyPasteManager().cancelPaste();
		}

		setObjects(freeSpace);

		appModel.addObserver((Observer)appView); // Add the view as Observer
		appModel.addObserver((Observer)appGui);  // Add the app window as observer

		appModel.addPetriNetObject(new Place(100.0, 100.0));
		try {



			appModel.createFromTAPN(model);

			appView.scrollRectToVisible(new Rectangle(0,0,1,1));

			name = "Converted";
		} catch(Exception e) {
			JOptionPane.showMessageDialog(
					GuiFrame.this,
					"Error loading file:\n" + name + "\nGuru meditation:\n"
					+ e.toString(),
					"File load error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}


		appView.setNetChanged(false);   // Status is unchanged

		JScrollPane scroller = new JScrollPane(appView);
		// make it less bad on XP
		scroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		appTab.addTab(name,null,scroller,null);
		appTab.setSelectedIndex(freeSpace);

		appView.updatePreferredSize();
		//appView.add( new ViewExpansionComponent(appView.getWidth(),
		//        appView.getHeight());

		setTitle(name);// Change the program caption
		appTab.setTitleAt(freeSpace, name);
		selectAction.actionPerformed(null);

	}


	/**Loads an Experiment XML file and shows a suitable message in case of error.
	 * @param path the absolute path to the experiment file.
	 */
	private void loadExperiment(String path){
		Experiment exp = new Experiment(path,appModel);
		try{
			exp.Load();
		} catch(org.xml.sax.SAXParseException spe) {
			//if the experiment file does not fit the schema.
			JOptionPane.showMessageDialog(GuiFrame.this,
					"The Experiment file is not valid."+
					System.getProperty("line.separator")+
					"Line "+spe.getLineNumber()+": "+
					spe.getMessage(),
					"Experiment Input Error",
					JOptionPane.ERROR_MESSAGE);
		} catch(pipe.experiment.validation.NotMatchingException nme) {
			//if the experiment file does not match with the current net.
			JOptionPane.showMessageDialog(GuiFrame.this,
					"The Experiment file is not valid."+
					System.getProperty("line.separator")+
					nme.getMessage(),
					"Experiment Input Error",
					JOptionPane.ERROR_MESSAGE);
		} catch(pipe.experiment.InvalidExpressionException iee) {
			JOptionPane.showMessageDialog(GuiFrame.this,
					"The Experiment file is not valid."+
					System.getProperty("line.separator")+
					iee.getMessage(),
					"Experiment Input Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}


	/**
	 * If current net has modifications, asks if you want to save and does it if
	 * you want.
	 * @return true if handled, false if cancelled
	 */
	private boolean checkForSave() {

		if (appView.getNetChanged()) {
			int result=JOptionPane.showConfirmDialog(GuiFrame.this,
					"Current file has changed. Save current file?",
					"Confirm Save Current File",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			switch(result) {
			case JOptionPane.YES_OPTION:
				saveOperation(false);
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
	 * @return true if handled, false if cancelled
	 */
	private boolean checkForSaveAll(){
		// Loop through all tabs and check if they have been saved
		for (int counter = 0; counter < appTab.getTabCount(); counter++) {
			appTab.setSelectedIndex(counter);
			if (checkForSave() == false){
				return false;
			}
		}
		return true;
	}


	public void setRandomAnimationMode(boolean on) {

		if (on == false){
			stepforwardAction.setEnabled(
					CreateGui.getAnimationHistory().isStepForwardAllowed());
			stepbackwardAction.setEnabled(
					CreateGui.getAnimationHistory().isStepBackAllowed());

			CreateGui.animControlerBox.setAnimationButtonsEnabled();

		} else {
			stepbackwardAction.setEnabled(false);
			stepforwardAction.setEnabled(false);
		}
		randomAction.setEnabled(!on);
		randomAnimateAction.setSelected(on);
	}


	public void setAnimationMode(boolean on) {
		randomAnimateAction.setSelected(false);
		CreateGui.getAnimator().setNumberSequences(0);
		startAction.setSelected(on);
		CreateGui.getView().changeAnimationMode(on);
		if (on) {
			CreateGui.getAnimator().storeModel();
			CreateGui.currentPNMLData().setEnabledTransitions();
			CreateGui.getAnimator().highlightEnabledTransitions();
			CreateGui.addAnimationHistory();
			CreateGui.addAnimationControler();

			enableActions(false);//disables all non-animation buttons
			setEditionAllowed(false);

			statusBar.changeText(statusBar.textforAnimation);
		} else {
			setEditionAllowed(true);
			statusBar.changeText(statusBar.textforDrawing);
			CreateGui.getAnimator().restoreModel();
			//         CreateGui.removeAnimationHistory();
			//         CreateGui.removeAnimationControler();
			enableActions(true); //renables all non-animation buttons

			//If abstract animation pane is shown, remove it when 
			// Gowing out of animation mode.
			CreateGui.removeAbstractAnimationPane();

			CreateGui.createLeftPane();
		}
	}


	public void resetMode(){
		setMode(old_mode);
	}


	public void setFastMode(int _mode){
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
		// xxx - This must be refactored when someone findes out excatly what is gowing on
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

		//		if (inhibarcAction != null) 
		//			inhibarcAction.setSelected(mode == Pipe.TAPNINHIBITOR_ARC);

		if (tokenAction != null) 
			tokenAction.setSelected(mode == Pipe.ADDTOKEN);

		if (deleteTokenAction != null) 
			deleteTokenAction.setSelected(mode == Pipe.DELTOKEN);

		if (rateAction != null) 
			rateAction.setSelected(mode == Pipe.RATE);

		if (markingAction != null) 
			//markingAction.setSelected(mode == Pipe.MARKING);

			if (selectAction != null) 
				selectAction.setSelected(mode == Pipe.SELECT);

		if (annotationAction != null) 
			annotationAction.setSelected(mode == Pipe.ANNOTATION);

	}


	public void setTitle(String title) {
		super.setTitle((title == null) ? frameTitle : frameTitle + ": " + title);
	}


	public boolean isEditionAllowed(){
		return editionAllowed;
	}


	public void setEditionAllowed(boolean flag){
		editionAllowed = flag;
	}


	public void setUndoActionEnabled(boolean flag) {
		undoAction.setEnabled(flag);
	}


	public void setRedoActionEnabled(boolean flag) {
		redoAction.setEnabled(flag);
	}


	public CopyPasteManager getCopyPasteManager() {
		return copyPasteManager;
	}


	public void init() {
		// Set selection mode at startup
		setMode(Pipe.SELECT);
		selectAction.actionPerformed(null);
	}


	/**
	 * @author Ben Kirby
	 * Remove the listener from the zoomComboBox, so that when the box's selected
	 * item is updated to keep track of ZoomActions called from other sources, a
	 * duplicate ZoomAction is not called
	 */
	public void updateZoomCombo() {
		ActionListener zoomComboListener=(zoomComboBox.getActionListeners())[0];
		zoomComboBox.removeActionListener(zoomComboListener);
		zoomComboBox.setSelectedItem(String.valueOf(appView.getZoomController().getPercent())+"%");
		zoomComboBox.addActionListener(zoomComboListener);
	}


	public  StatusBar getStatusBar(){
		return statusBar;
	}


	private Component c = null; //arreglantzoom
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

		private int typeID;
		private AnimationHistory animBox;


		AnimateAction(String name, int typeID, String tooltip, String keystroke){
			super(name, tooltip, keystroke);
			this.typeID = typeID;
		}


		AnimateAction(String name, int typeID, String tooltip, String keystroke,
				boolean toggleable){
			super(name, tooltip, keystroke, toggleable);
			this.typeID = typeID;
		}


		public AnimateAction(String name, int typeID, String tooltip,
				KeyStroke keyStroke) {
			super(name, tooltip, keyStroke);
			this.typeID = typeID;

		}


		public void actionPerformed(ActionEvent ae){
			if (appView == null) {
				return;
			}

			animBox = CreateGui.getAnimationHistory();

			switch(typeID){
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
				animBox.clearStepsForward();
				//CreateGui.getAnimator().letTimePass(1f);
				CreateGui.animControlerBox.setAnimationButtonsEnabled();
				break;

			case Pipe.RANDOM:
				animBox.clearStepsForward();
				CreateGui.getAnimator().doRandomFiring();
				//update mouseOverView
				for (pipe.dataLayer.Place p : CreateGui.getModel().getPlaces() ){
					if (((TimedPlace)p).isAgeOfTokensShown()){
						((TimedPlace)p).showAgeOfTokens(true);
					}
				}
				CreateGui.animControlerBox.setAnimationButtonsEnabled();
				break;

			case Pipe.STEPFORWARD:
				animBox.stepForward();
				CreateGui.getAnimator().stepForward();
				//update mouseOverView
				for (pipe.dataLayer.Place p : CreateGui.getModel().getPlaces() ){
					if (((TimedPlace)p).isAgeOfTokensShown()){
						((TimedPlace)p).showAgeOfTokens(true);
					}
				}
				CreateGui.animControlerBox.setAnimationButtonsEnabled();
				break;

			case Pipe.STEPBACKWARD:
				animBox.stepBackwards();
				CreateGui.getAnimator().stepBack();
				//update mouseOverView
				for (pipe.dataLayer.Place p : CreateGui.getModel().getPlaces() ){
					if (((TimedPlace)p).isAgeOfTokensShown()){
						((TimedPlace)p).showAgeOfTokens(true);
					}
				}
				CreateGui.animControlerBox.setAnimationButtonsEnabled();
				break;

			case Pipe.ANIMATE:
				Animator a = CreateGui.getAnimator();

				if (a.getNumberSequences() > 0) {
					a.setNumberSequences(0); // stop animation
					setSelected(false);
				} else {
					stepbackwardAction.setEnabled(false);
					stepforwardAction.setEnabled(false);
					randomAction.setEnabled(false);
					setSelected(true);
					animBox.clearStepsForward();
					CreateGui.getAnimator().startRandomFiring();
				}
				break;

			default:
				break;
			}
		}

	}


	class ExampleFileAction extends GuiAction {

		private File filename;


		ExampleFileAction(File file, String keyStroke) {
			super(file.getName().replace(".xml", ""), "Open example file \"" + file.getName().replace(".xml", "") +
					"\"", keyStroke);
			filename = file;//.getAbsolutePath();
			putValue(SMALL_ICON,
					new ImageIcon(Thread.currentThread().getContextClassLoader().
							getResource(CreateGui.imgPath + "Net.png")));
		}


		ExampleFileAction(JarEntry entry, String keyStroke) {
			super(entry.getName().substring(1 + entry.getName().
					indexOf(System.getProperty("file.separator"))),
					"Open example file \"" + entry.getName() + "\"",
					keyStroke);
			putValue(SMALL_ICON,
					new ImageIcon(Thread.currentThread().getContextClassLoader().
							getResource(CreateGui.imgPath + "Net.png")));

			filename = JarUtilities.getFile(entry);//.getPath();
		}


		public void actionPerformed(ActionEvent e){
			createNewTabFromFile(filename, false);
		}

	}



	class DeleteAction extends GuiAction {

		DeleteAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}


		public void actionPerformed(ActionEvent e){
			// check if queries need to be removed
			ArrayList<PetriNetObject> selection = CreateGui.getView().getSelectionObject().getSelection();
			ArrayList<TAPNQuery> queries = CreateGui.getModel().getQueries();
			ArrayList<TAPNQuery> queriesToDelete = new ArrayList<TAPNQuery>();

			boolean queriesAffected = false;
			for (PetriNetObject pn : selection) {
				if(pn instanceof TimedPlace)
				{
					for (TAPNQuery q : queries) {
						if(q.query.matches(".*" + pn.getName() + "[^\\_a-zA-Z0-9].*")){
							queriesAffected = true;
							queriesToDelete.add(q);
						}
					}
				}
			}
			int choice = queriesAffected ? JOptionPane.showConfirmDialog(CreateGui.getApp(), "All queries associated with the currently selected objects will also be removed.\n\n Are you sure you want to remove the current selection and all associated queries?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) : JOptionPane.YES_OPTION;

			if(choice == JOptionPane.YES_OPTION)
			{
				appView.getUndoManager().newEdit(); // new "transaction""

				if(queriesAffected){
					CreateGui.getModel().getQueries().removeAll(queriesToDelete);
					CreateGui.createLeftPane();	
				}

				appView.getUndoManager().deleteSelection(appView.getSelectionObject().getSelection());
				appView.getSelectionObject().deleteSelection();			
				CreateGui.getModel().buildConstraints();
			}
		}

	}



	class TypeAction extends GuiAction {

		private int typeID;

		TypeAction(String name, int typeID, String tooltip, String keystroke){
			super(name, tooltip, keystroke);
			this.typeID = typeID;
		}


		TypeAction(String name, int typeID, String tooltip, String keystroke,
				boolean toggleable){
			super(name, tooltip, keystroke, toggleable);
			this.typeID = typeID;
		}


		public void actionPerformed(ActionEvent e){
			//			if (!isSelected()){
			this.setSelected(true);

			// deselect other actions
			/*	if (this != placeAction) {
		placeAction.setSelected(false);
	}
			 */	
			if (this != transAction) {
				transAction.setSelected(false);
			}
			/*CB - Joakim Byg - Is not instansiated, since it is not needed in the model         
         if (this != timedtransAction) {
            timedtransAction.setSelected(false);
         }
EOC */
			/*CB - Jiri Srba - Not part of the model
          if (this != arcAction) {
            arcAction.setSelected(false);
         }
EOC */
			/*CB - Joakim Byg - adding timed arcs*/         
			if (this != timedArcAction) {
				timedArcAction.setSelected(false);
			}
			/*EOC */
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
			/*	
	if (this != rateAction) {
		rateAction.setSelected(false);
	}
			 */	
			if (this != markingAction) {
				//markingAction.setSelected(false);
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

			if (appView == null){
				return;
			}

			appView.getSelectionObject().disableSelection();
			//appView.getSelectionObject().clearSelection();

			setMode(typeID);
			statusBar.changeText(typeID);



			if ((typeID != Pipe.ARC) && (appView.createArc != null)) {
				appView.createArc.delete();
				appView.createArc=null;
				appView.repaint();
				//Also handel trasport arcs (if any)

				if (appView.transportArcPart1!=null){
					appView.transportArcPart1.delete();
					appView.transportArcPart1=null;
					appView.repaint();
				}
			}

			// XXX - kyrke - Dont think this code will ever be runned, as the above code vill handel it 
			/*CB Joakim Byg - adding timed arc*/         
			if ((typeID != Pipe.TAPNARC) && (appView.createArc != null)) {
				appView.createArc.delete();
				appView.createArc=null;
				appView.repaint();

			}
			/*EOC*/



			if (typeID == Pipe.SELECT) {
				//disable drawing to eliminate possiblity of connecting arc to
				//old coord of moved component
				statusBar.changeText(typeID);
				appView.getSelectionObject().enableSelection();
				appView.setCursorType("arrow");
			} else if (typeID == Pipe.DRAG) {
				appView.setCursorType("move");
			} else {
				appView.setCursorType("crosshair");
			}
		}
		//		}

	}



	class GridAction extends GuiAction {

		GridAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}


		public void actionPerformed(ActionEvent e) {
			Grid.increment();
			repaint();
		}

	}



	class ZoomAction extends GuiAction {

		ZoomAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}


		public void actionPerformed(ActionEvent e) {
			boolean doZoom = false;
			try {
				String actionName = (String)getValue(NAME);
				Zoomer zoomer = appView.getZoomController();
				JViewport thisView =
					((JScrollPane)appTab.getSelectedComponent()).getViewport();
				String selection = null, strToTest = null;

				double midpointX = Zoomer.getUnzoomedValue(
						thisView.getViewPosition().x + (thisView.getWidth() * 0.5),
						zoomer.getPercent());
				double midpointY = Zoomer.getUnzoomedValue(
						thisView.getViewPosition().y + (thisView.getHeight() * 0.5),
						zoomer.getPercent());

				if (actionName.equals("Zoom in")){
					doZoom = zoomer.zoomIn();
				} else if (actionName.equals("Zoom out")) {
					doZoom = zoomer.zoomOut();
				} else {
					if (actionName.equals("Zoom")) {
						selection = (String)zoomComboBox.getSelectedItem();
					}
					if(e.getSource() instanceof JMenuItem){
						selection = ((JMenuItem)e.getSource()).getText();
					}
					strToTest = validatePercent(selection);


					if (strToTest!=null) {
						//BK: no need to zoom if already at that level
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
					appView.zoomTo(new java.awt.Point((int)midpointX, (int)midpointY));
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

				if(selection.endsWith("%")){
					toTest=selection.substring(0, (selection.length())-1);
				}

				if (Integer.parseInt(toTest) < Pipe.ZOOM_MIN ||
						Integer.parseInt(toTest) > Pipe.ZOOM_MAX) {
					throw new Exception();
				} else {
					return toTest;
				}
			} catch(Exception e) {
				zoomComboBox.setSelectedItem("");
				return null;
			}
		}

	}



	class ExperimentAction extends GuiAction{

		ExperimentAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			if (this == loadExperimentAction) {
				File filePath = new FileBrowser(CreateGui.userPath).openFile();
				if ((filePath != null) && filePath.exists()
						&& filePath.isFile() && filePath.canRead()) {
					loadExperiment(filePath.getAbsolutePath());
				}
			}
			if (this == experimentEditorAction) {
				ExperimentEditor ee = new ExperimentEditor(appModel);
				//ee.start();
			}
		}

	}


	class VerificationAction extends GuiAction{

		VerificationAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			if (this == runUppaalVerification) {
				QueryDialogue.ShowUppaalQueryDialogue(QueryDialogueOption.VerifyNow, null);
			}
		}

	}

	class FileAction extends GuiAction {

		//constructor
		FileAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			if (this == saveAction) {
				saveOperation(false);            	  // code for Save operation
			} else if(this == saveAsAction) {
				saveOperation(true);                  // code for Save As operations
			} else if (this == openAction) {         // code for Open operation
				File filePath = new FileBrowser(CreateGui.userPath).openFile();
				if ((filePath != null) && filePath.exists()
						&& filePath.isFile() && filePath.canRead()) {
					CreateGui.userPath = filePath.getParent();
					createNewTabFromFile(filePath, false);

					//TODO make update leftPane work better
					CreateGui.createLeftPane();
				}
			} else if (this == importAction) {
				File filePath = new FileBrowser(CreateGui.userPath).openFile();
				if ((filePath != null) && filePath.exists() && filePath.isFile() &&
						filePath.canRead()) {
					CreateGui.userPath = filePath.getParent();
					createNewTabFromFile(filePath,true);
					appView.getSelectionObject().enableSelection();
				}
			} else if (this == createAction) {
				showNewPNDialog();
			} else if ((this == exitAction) && checkForSaveAll()) {
				dispose();
				System.exit(0);
			} else if ((this == closeAction) && (appTab.getTabCount() > 0)
					&& checkForSave()) {
				setObjectsNull(appTab.getSelectedIndex());
				appTab.remove(appTab.getSelectedIndex());
			} else if (this == exportPNGAction) {
				Export.exportGuiView(appView, Export.PNG, null);
			} else if (this == exportToTikZAction) {
				Export.exportGuiView(appView, Export.TIKZ, appModel);
			} else if (this == exportPSAction) {
				Export.exportGuiView(appView, Export.POSTSCRIPT, null);
			} else if (this == exportTNAction){
				Export.exportGuiView(appView, Export.TN, appModel);
			} else if (this == exportToUppaal) {
				Export.exportUppaalGuiView(appView, appModel);
			} else if (this == exportToUppaalAdvanced) {
				Export.exportUppaalAdvancedGuiView(appView, appModel);
			} else if (this == exportToUppaalSymetric) {
				Export.exportUppaalSymetricGuiView(appView, appModel);
			} else if (this == exportToTest) {
				Export.exportDegree2(appView, appModel);
			} else if (this == printAction) {
				Export.exportGuiView(appView, Export.PRINTER, null);
			}
		}

	}



	class EditAction extends GuiAction {

		EditAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}


		public void actionPerformed(ActionEvent e){

			if (CreateGui.getApp().isEditionAllowed()) {
				/*				if (this == cutAction) {
					ArrayList selection = appView.getSelectionObject().getSelection();
					appGui.getCopyPasteManager().setUpPaste(selection, appView);
					appView.getUndoManager().newEdit(); // new "transaction""
					appView.getUndoManager().deleteSelection(selection);
					appView.getSelectionObject().deleteSelection();
					pasteAction.setEnabled(appGui.getCopyPasteManager().pasteEnabled());
				} else if (this == copyAction) {
					appGui.getCopyPasteManager().setUpPaste(
							appView.getSelectionObject().getSelection(), appView);
					pasteAction.setEnabled(appGui.getCopyPasteManager().pasteEnabled());
				} else if (this == pasteAction) {
					appView.getSelectionObject().clearSelection();
					appGui.getCopyPasteManager().startPaste(appView);
				} else*/ if (this == undoAction) {
					appView.getUndoManager().undo();
					CreateGui.getModel().buildConstraints();
				} else if (this == redoAction) {
					appView.getUndoManager().redo();
					CreateGui.getModel().buildConstraints();
				}
			}
		}
	}



	/**
	 * A JToggleButton that watches an Action for selection change
	 * @author Maxim
	 *
	 * Selection must be stored in the action using putValue("selected",Boolean);
	 */
	class ToggleButton extends JToggleButton implements PropertyChangeListener {

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
				Boolean b = (Boolean)evt.getNewValue();
				if (b != null) {
					setSelected(b.booleanValue());
				}
			}
		}

	}



	class WindowHandler extends WindowAdapter{
		//Handler for window closing event
		public void windowClosing(WindowEvent e){
			exitAction.actionPerformed(null);
		}
	}

	public void setEnabledStepForwardAction(boolean b) {
		stepforwardAction.setEnabled(b);
	}


	public void showNewPNDialog() {
		// Build interface
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.getProgramName(), true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));      

		// 2 Add Place editor
		contentPane.add( new NewTAPNPanel(guiDialog.getRootPane(), this));

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

	public int getNameCounter(){
		return newNameCounter;
	}

	public void incrementNameCounter(){
		newNameCounter++;
	}

}
