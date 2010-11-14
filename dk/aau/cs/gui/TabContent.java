package dk.aau.cs.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
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
import pipe.gui.AnimationController;
import pipe.gui.AnimationHistory;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.widgets.JSplitPaneFix;
import pipe.gui.widgets.LeftConstantsPane;
import pipe.gui.widgets.LeftQueryPane;

public class TabContent extends JSplitPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -648006317150905097L;

	private static final double DIVIDER_LOCATION = 0.5;
	
	private DataLayer appModel;
	private HashMap<PetriNetObject, String> oldGuards;
	private JScrollPane drawingSurfaceScroller;
	private DrawingSurfaceImpl drawingSurface;
	private File appFile;
	
	// Normal mode
	private JSplitPane leftPane;
	private JSplitPane queryConstantsSplit;
	private LeftQueryPane queries;
	private LeftConstantsPane leftBottomPanel;
	private TemplateExplorer templates;
	
	/// Animation
	private AnimationHistory animBox;
	private AnimationController animControlerBox;
	private JScrollPane scroller;
	private JScrollPane scroller2;
	private AnimationHistory abstractAnimationPane=null;
	
	public TabContent()
	{
		leftPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
		leftPane.setPreferredSize(new Dimension(262, 100)); // height is ignored because the component is stretched
		leftPane.setMinimumSize(new Dimension(175,100));
		
		queryConstantsSplit = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
		queryConstantsSplit.setPreferredSize(new Dimension(262, 100)); // height is ignored because the component is stretched
		queryConstantsSplit.setMinimumSize(new Dimension(175,100));
		createLeftPane();
		
		appModel = new DataLayer();
		
		
		drawingSurface = new DrawingSurfaceImpl(appModel);
		drawingSurfaceScroller = new JScrollPane(drawingSurface);
		// make it less bad on XP
		drawingSurfaceScroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		this.setOrientation(HORIZONTAL_SPLIT);
		this.setLeftComponent(leftPane);
		this.setRightComponent(drawingSurfaceScroller);
		
		this.setContinuousLayout(true);
		this.setOneTouchExpandable(true);
		this.setBorder(null); // avoid multiple borders
		this.setDividerSize(8);
	}
	
	public void createLeftPane(){
		boolean enableAddButton = appModel == null ? true : !appModel.netType().equals(NetType.UNTIMED);
		leftBottomPanel = new LeftConstantsPane(enableAddButton);
		queries = new LeftQueryPane(
				appModel == null ? new ArrayList<TAPNQuery>() : appModel.getQueries()
		);
		templates = new TemplateExplorer();
		
		queryConstantsSplit.setDividerLocation(DIVIDER_LOCATION);
		queryConstantsSplit.setResizeWeight(0.5);
		queryConstantsSplit.setTopComponent(queries);
		queryConstantsSplit.setBottomComponent(leftBottomPanel);
		queryConstantsSplit.setContinuousLayout(true);
		queryConstantsSplit.setDividerSize(0);
		
		leftPane.setDividerLocation(0.3);
		leftPane.setResizeWeight(0.5);
		leftPane.setTopComponent(templates);
		leftPane.setBottomComponent(queryConstantsSplit);
		leftPane.setContinuousLayout(true);
		leftPane.setDividerSize(0);
		
		
		
		updateLeftPanel();
	}

	public void updateConstantsList(){
		leftBottomPanel.showConstants();
	}

	public void updateLeftPanel() {
		leftPane.validate();
	}
	
	public DataLayer getModel()
	{
		return appModel;
	}
	
	public void setModel(DataLayer model)
	{
		appModel = model;
	}
	
	public DrawingSurfaceImpl getDrawingSurface()
	{
		return drawingSurface; 
	}
	
	public void setDrawingSurface(DrawingSurfaceImpl drawingSurface) {
		this.drawingSurface = drawingSurface; 		
	}
	
	public File getFile()
	{
		return appFile;
	}
	
	public void setFile(File file)
	{
		appFile = file;
	}

	public void setupModelForSimulation(){
			if(!appModel.isUsingColors()){
				HashMap<PetriNetObject, String> oldGuards = transformToModelWithoutConstants();
				this.oldGuards = oldGuards;
			}else{
				configureNetToShowValues(true);
			}
		
	}
	
	private void configureNetToShowValues(boolean showValues) {
		for(Place tp : appModel.getPlaces()){
			ColoredTimedPlace place = (ColoredTimedPlace)tp;
			place.displayValues(showValues);
		}		

		for(Arc arc : appModel.getArcs()){
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


	public void restoreModelForEditing(){
			if(appModel.isUsingColors()){
				configureNetToShowValues(false);
			}else{
				if(this.oldGuards != null){
					setupModelWithOldGuards();
					this.oldGuards = null;
				}
			}
	}

	private void setupModelWithOldGuards() {
		for(Place p : appModel.getPlaces()){
			if(p instanceof TimedPlace){
				String inv = oldGuards.get(p);
				((TimedPlace)p).setInvariant(inv);
			}
		}

		for(Arc arc : appModel.getArcs()){
			if(arc instanceof TimedArc || arc instanceof TransportArc){
				TimedArc tarc = (TimedArc)arc;
				String guard = oldGuards.get(arc);
				tarc.setGuard(guard);
			}
		}
	}


	private HashMap<PetriNetObject, String> transformToModelWithoutConstants() {
		HashMap<PetriNetObject, String> oldGuards = new HashMap<PetriNetObject, String>();

		for(Place p : appModel.getPlaces()){
			if(p instanceof TimedPlace){
				oldGuards.put(p, ((TimedPlace) p).getInvariant());
				String inv = getInvariant(p);
				((TimedPlace)p).setInvariant(inv);
			}
		}

		for(Arc arc : appModel.getArcs()){
			if(arc instanceof TimedArc || arc instanceof TransportArc){
				oldGuards.put(arc, ((TimedArc) arc).getGuard());
				TimedArc tarc = (TimedArc)arc;
				String guard = getGuard(tarc);
				tarc.setGuard(guard);
			}
		}

		return oldGuards;
	}

	private String getGuard(TimedArc arc) {
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
			first = String.valueOf(appModel.getConstantValue(first));
		}

		if(isSecondConstant){
			second = String.valueOf(appModel.getConstantValue(second));
		}

		return leftDelim + first + "," + second + rightDelim;
	}

	private String getInvariant(Place place) {
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
			bound = String.valueOf(appModel.getConstantValue(bound));

		return operator + bound;
	}
	
	/** Creates a new animationHistory text area, and returns a reference to it*/
	public void addAnimationHistory() {
		try {
			animBox = new AnimationHistory("Simulation history\n");
			animBox.setEditable(false);

			scroller = new JScrollPane(animBox);
			scroller.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP

			leftPane.setBottomComponent(scroller);

			//         leftPane.setDividerLocation(0.5);
			leftPane.setResizeWeight(0.05f);

			leftPane.setDividerSize(8);
		} catch (javax.swing.text.BadLocationException be) {
			be.printStackTrace();
		}
	}

	public AnimationHistory getAbstractAnimationPane(){
		return abstractAnimationPane;
	}
	
	public AnimationController getAnimationController(){
		return animControlerBox;
	}

	public void addAbstractAnimationPane() {
		

		try {
			abstractAnimationPane=new AnimationHistory("Untimed Trace\n");
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//abstractAnimationPane.setVerticalAlignment(SwingConstants.TOP);

		//Create a new empty animBox
		try {
			animBox = new AnimationHistory("Simulation history\n");
			animBox.setEditable(false);
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

	public void removeAbstractAnimationPane() {
		abstractAnimationPane=null;
		scroller = new JScrollPane(animBox);
		scroller.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP
		leftPane.setBottomComponent(scroller);
	}

	public void addAnimationController() {
		try {
			animControlerBox = new AnimationController("Simulation Controler\n");

			scroller2 = new JScrollPane(animControlerBox);
			scroller2.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP

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

	public void removeAnimationHistory() {
		if (scroller != null) {
			leftPane.remove(scroller);
			leftPane.setDividerLocation(DIVIDER_LOCATION);
			leftPane.setDividerSize(0);
		}
	}
	public void removeAnimationController() {
		if (scroller != null) {
			leftPane.remove(scroller2);
			leftPane.setDividerLocation(DIVIDER_LOCATION);
			leftPane.setDividerSize(0);
		}
	}


	public AnimationHistory getAnimationHistory() {
		return animBox;
	}

}
