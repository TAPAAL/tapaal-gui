package pipe.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;

import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.TransportArc;
import pipe.dataLayer.colors.ColoredInhibitorArc;
import pipe.dataLayer.colors.ColoredInputArc;
import pipe.dataLayer.colors.ColoredOutputArc;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.gui.widgets.JSplitPaneFix;
import pipe.gui.widgets.LeftConstantsPane;
import pipe.gui.widgets.LeftQueryPane;
import dk.aau.cs.verification.UPPAAL.Verifyta;


public class CreateGui {

	private static final double DIVIDER_LOCATION = 0.5;
	public static GuiFrame appGui;
	private static Animator animator;
	private static JTabbedPane appTab;
	private static ArrayList<TabData> tabs = new ArrayList<TabData>();

	public static String imgPath, userPath; // useful for stuff

	public static class TabData { // a structure for holding a tab's data
		public DataLayer appModel;
		public HashMap<PetriNetObject, String> oldGuards;
		public GuiView appView;
		public File appFile;
	}

	/** The Module will go in the top pane, the animation window in the bottom pane */
	private static JSplitPane leftPane;
	private static AnimationHistory animBox;
	static AnimationController animControlerBox;
	private static JScrollPane scroller;
	private static JScrollPane scroller2;
	private static LeftQueryPane queries;
	private static LeftConstantsPane leftBottomPanel;
	private static JSplitPane pane;
	private static AnimationHistory abstractAnimationPane=null;
	
	public static void init() {
		imgPath = "Images" + System.getProperty("file.separator");

		// make the initial dir for browsing be My Documents (win), ~ (*nix), etc
		userPath = null; 


		appGui = new GuiFrame(Pipe.TOOL + " " + Pipe.VERSION);



		Grid.enableGrid();

		appTab = new JTabbedPane();

		animator = new Animator();
		appGui.setTab();   // sets Tab properties


		queries = new LeftQueryPane(new ArrayList<TAPNQuery>());
		leftPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
		leftPane.setPreferredSize(new Dimension(262, 100)); // height is ignored because the component is stretched
		leftPane.setMinimumSize(new Dimension(175,100));
		
		createLeftPane();
		pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftPane,appTab);
				
		pane.setContinuousLayout(true);
		pane.setOneTouchExpandable(true);
		pane.setBorder(null); // avoid multiple borders

		pane.setDividerSize(8);


		appGui.getContentPane().add(pane);

		//appGui.createNewTabFromFile(null);

		appGui.setVisible(true);
		appGui.init();
		emptyLeftPane();
		Verifyta.trySetupFromEnvironmentVariable();

		VersionChecker versionChecker = new VersionChecker();
		if(versionChecker.checkForNewVersion()){
			StringBuffer message = new StringBuffer(
			"There is a new version of TAPAAL available at www.tapaal.net.");
			message.append("\n\nCurrent version: ");
			message.append(Pipe.VERSION);
			message.append("\nNew version: ");
			message.append(versionChecker.getNewVersionNumber());			

			JOptionPane.showMessageDialog(appGui, 
					message .toString(),
					"New version available!",
					JOptionPane.INFORMATION_MESSAGE);			
		}
	}


	public static GuiFrame getApp() {  //returns a reference to the application
		return appGui;
	}


	public static DataLayer getModel() {
		return getModel(appTab.getSelectedIndex());
	}

	public static DataLayer getModel(int index) {
		if (index < 0) {
			return null;
		}

		TabData tab = (getTabDataForTab(index));
		if (tab.appModel == null) {
			tab.appModel = new DataLayer();
		}
		return tab.appModel;
	}


	static TabData getTabDataForTab(int index) {
		return tabs.get(index);
	}


	public static GuiView getView(int index) {
		if (index < 0) {
			return null;
		}

		TabData tab = (getTabDataForTab(index));
		while (tab.appView == null) {
			try {
				tab.appView = new GuiView(tab.appModel);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return tab.appView;
	}


	public static GuiView getView() {
		return getView(appTab.getSelectedIndex());
	}


	public static File getFile() {
		TabData tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.appFile;
	}


	public static void setFile(File modelfile, int fileNo) {
		if (fileNo >= tabs.size()) {
			return;
		}
		TabData tab = (getTabDataForTab(fileNo));
		tab.appFile = modelfile;
	}


	public static int getFreeSpace() {
		tabs.add(new TabData());
		return tabs.size() - 1;
	}


	public static void removeTab(int index) {
		tabs.remove(index);
		if(tabs.isEmpty()){
			emptyLeftPane();
		}
	}


	public static JTabbedPane getTab() {
		return appTab;
	}

	public static Animator getAnimator() {
		return animator;
	}

	/** returns the current dataLayer object - 
	 *  used to get a reference to pass to the modules */
	public static DataLayer currentPNMLData() {
		if (appTab.getSelectedIndex() < 0) {
			return null;
		}
		TabData tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.appModel;
	}

	public static void setupModelForSimulation(){
		if (appTab.getSelectedIndex() >= 0) {
			TabData tab = (tabs.get(appTab.getSelectedIndex()));
			//DataLayer model = tab.appModel.clone();
			if(!tab.appModel.isUsingColors()){
				HashMap<PetriNetObject, String> oldGuards = transformToModelWithoutConstants(tab.appModel);
				tab.oldGuards = oldGuards;
			}else{
				configureNetToShowValues(tab.appModel, true);
			}
		}
	}

	private static void configureNetToShowValues(DataLayer model, boolean showValues) {
		for(Place tp : model.getPlaces()){
			ColoredTimedPlace place = (ColoredTimedPlace)tp;
			place.displayValues(showValues);
		}		

		for(Arc arc : model.getArcs()){
			if(arc instanceof ColoredTransportArc){
				((ColoredTransportArc)arc).displayValues(showValues);
			}else if(arc instanceof ColoredInputArc){
				((ColoredInputArc)arc).displayValues(showValues);
			}else if(arc instanceof ColoredInhibitorArc){
				((ColoredInhibitorArc)arc).displayValues(showValues);
			}else if(arc instanceof ColoredOutputArc){
				((ColoredOutputArc)arc).displayValues(showValues);
			}
		}
	}


	public static void restoreModelForEditing(){
		if (appTab.getSelectedIndex() >= 0) {
			TabData tab = (tabs.get(appTab.getSelectedIndex()));
			if(tab.appModel.isUsingColors()){
				configureNetToShowValues(tab.appModel, false);
			}else{
				if(tab.oldGuards != null){
					setupModelWithOldGuards(tab.appModel, tab.oldGuards);
					tab.oldGuards = null;
				}
			}
		}
	}

	static void setupModelWithOldGuards(DataLayer model,
			HashMap<PetriNetObject, String> oldGuards) {
		for(Place p : model.getPlaces()){
			if(p instanceof TimedPlace){
				String inv = oldGuards.get(p);
				((TimedPlace)p).setInvariant(inv);
			}
		}

		for(Arc arc : model.getArcs()){
			if(arc instanceof TimedArc || arc instanceof TransportArc){
				TimedArc tarc = (TimedArc)arc;
				String guard = oldGuards.get(arc);
				tarc.setGuard(guard);
			}
		}
	}


	public static HashMap<PetriNetObject, String> transformToModelWithoutConstants(DataLayer model) {
		HashMap<PetriNetObject, String> oldGuards = new HashMap<PetriNetObject, String>();

		for(Place p : model.getPlaces()){
			if(p instanceof TimedPlace){
				oldGuards.put(p, ((TimedPlace) p).getInvariant());
				String inv = getInvariant(p, model);
				((TimedPlace)p).setInvariant(inv);
			}
		}

		for(Arc arc : model.getArcs()){
			if(arc instanceof TimedArc || arc instanceof TransportArc){
				oldGuards.put(arc, ((TimedArc) arc).getGuard());
				TimedArc tarc = (TimedArc)arc;
				String guard = getGuard(tarc, model);
				tarc.setGuard(guard);
			}
		}

		return oldGuards;
	}

	private static String getGuard(TimedArc arc, DataLayer model) {
		String guard = arc.getGuard();
		String leftDelim = guard.substring(0,1);
		String rightDelim = guard.substring(guard.length()-1, guard.length());
		String first = guard.substring(1, guard.indexOf(","));
		String second = guard.substring(guard.indexOf(",")+1, guard.length()-1);

		boolean isFirstConstant = false;
		boolean isSecondConstant = false;

		try{
			Integer.parseInt(first);
		}catch(NumberFormatException e){
			isFirstConstant = true;
		}

		try{
			Integer.parseInt(second);
		}catch(NumberFormatException e){
			if(!second.equals("inf")) isSecondConstant = true;
		}

		if(isFirstConstant){
			first = String.valueOf(model.getConstantValue(first));
		}

		if(isSecondConstant){
			second = String.valueOf(model.getConstantValue(second));
		}

		return leftDelim + first + "," + second + rightDelim;
	}

	private static String getInvariant(Place place, DataLayer model) {
		String inv = ((TimedPlace)place).getInvariant();
		String operator = inv.contains("<=") ? "<=" : "<";

		String bound = inv.substring(operator.length());

		boolean isConstant = false;
		try{
			Integer.parseInt(bound);
		}catch(NumberFormatException e){
			if(!bound.equals("inf")) isConstant = true;
		}

		if(isConstant)
			bound = String.valueOf(model.getConstantValue(bound));

		return operator + bound;
	}


	/** Creates a new animationHistory text area, and returns a reference to it*/
	public static void addAnimationHistory() {
		try {
			animBox = new AnimationHistory();
			//animBox.setEditable(false);

			scroller = new JScrollPane(animBox);
			scroller.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Simulation History"),
					BorderFactory.createEmptyBorder(3,3,3,3)));
			leftPane.setBottomComponent(scroller);
			//animBox.setScroller(scroller);
			//         leftPane.setDividerLocation(0.5);
			leftPane.setResizeWeight(0.05f);

			leftPane.setDividerSize(8);		
		} catch (javax.swing.text.BadLocationException be) {
			be.printStackTrace();
		}
		
	}

	public static AnimationHistory getAbstractAnimationPane(){
		return abstractAnimationPane;
	}

	public static void addAbstractAnimationPane() {
		

		try {
			abstractAnimationPane=new AnimationHistory();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//abstractAnimationPane.setVerticalAlignment(SwingConstants.TOP);

		//Create a new empty animBox
		try {
			animBox = new AnimationHistory();
			//animBox.setEditable(false);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		JSplitPane pane2 = 
			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,animBox,abstractAnimationPane);

		pane2.setContinuousLayout(true);
		pane2.setOneTouchExpandable(true);
		pane2.setBorder(null); // avoid multiple borders

		pane2.setDividerSize(8);

		leftPane.setBottomComponent(pane2);
		abstractAnimationPane.setBorder(new LineBorder(Color.black));

	}

	public static void removeAbstractAnimationPane() {
		abstractAnimationPane=null;
		scroller = new JScrollPane(animBox);
		scroller.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP
		leftPane.setBottomComponent(scroller);
	}

	public static void addAnimationControler() {
		try {
			animControlerBox = new AnimationController("Simulation Controler\n");

			scroller2 = new JScrollPane(animControlerBox);
			scroller2.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP
			scroller2.setMinimumSize(new Dimension(200,95));
			scroller2.setPreferredSize(new Dimension(200,95));
			leftPane.setTopComponent(scroller2);

			//         leftPane.setDividerLocation(0.5);
			leftPane.setDividerSize(8);
			leftPane.resetToPreferredSizes();
			//shortcutBottons should be usable from start of
			animControlerBox.requestFocus(true);
		} catch (javax.swing.text.BadLocationException be) {
			be.printStackTrace();
			System.out.println("There where an error in creating the AnimationControler");
		}
	}

	public static void removeAnimationHistory() {
		if (scroller != null) {
			leftPane.remove(scroller);
			leftPane.setDividerLocation(DIVIDER_LOCATION);
			leftPane.setDividerSize(0);
		}
	}
	public static void removeAnimationControler() {
		if (scroller != null) {
			leftPane.remove(scroller2);
			leftPane.setDividerLocation(DIVIDER_LOCATION);
			leftPane.setDividerSize(0);
		}
	}


	public static AnimationHistory getAnimationHistory() {
		return animBox;
	}

	public static void createLeftPane(){
		DataLayer model = CreateGui.getModel();
		boolean enableAddButton = model == null ? true : !model.netType().equals(NetType.UNTIMED);
		leftBottomPanel = new LeftConstantsPane(enableAddButton);
		queries = new LeftQueryPane(
				getModel() == null ? new ArrayList<TAPNQuery>() : getModel().getQueries()
		);
		leftPane.setDividerLocation(DIVIDER_LOCATION);
		leftPane.setResizeWeight(0.5);
		leftPane.setTopComponent(queries);
		leftPane.setBottomComponent(leftBottomPanel);
		leftPane.setContinuousLayout(true);
		leftPane.setDividerSize(0);
		leftPane.setPreferredSize(new Dimension(290,400));
		updateLeftPanel();
	}

	public static void emptyLeftPane(){
		leftPane.setTopComponent(null);
		leftPane.setBottomComponent(null);
	}

	public static void updateConstantsList(){
		leftBottomPanel.showConstants();
	}

	public static void updateLeftPanel() {
		leftPane.validate();
	}


	public static void undoGetFreeSpace() {
		tabs.remove(tabs.size()-1);
	}
}
