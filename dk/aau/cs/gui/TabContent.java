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
import pipe.dataLayer.Template;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.TransportArcComponent;
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
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class TabContent extends JSplitPane {
	private static final long serialVersionUID = -648006317150905097L;

	private static final double DIVIDER_LOCATION = 0.5;
	
	private DataLayer appModel;
	private TimedArcPetriNetNetwork tapnNetwork = new TimedArcPetriNetNetwork();
	private HashMap<TimedArcPetriNet, DataLayer> guiModels = new HashMap<TimedArcPetriNet, DataLayer>();
	private HashMap<PetriNetObject, TimeInterval> oldGuards;
	private HashMap<PetriNetObject, TimeInvariant> oldInvariants;
	private JScrollPane drawingSurfaceScroller;
	private DrawingSurfaceImpl drawingSurface;
	private File appFile;
	
	// Normal mode
	private JSplitPane leftPane;
	private JSplitPane queryConstantsSplit;
	private LeftQueryPane queries;
	private LeftConstantsPane leftBottomPanel;
	private TemplateExplorer templateExplorer;
	
	/// Animation
	private AnimationHistory animBox;
	private AnimationController animControlerBox;
	private JScrollPane scroller;
	private JScrollPane scroller2;
	private AnimationHistory abstractAnimationPane=null;
		
	public TabContent()
	{
		appModel = new DataLayer();		
		
		for(TimedArcPetriNet net : tapnNetwork.templates()){
			guiModels.put(net, new DataLayer());
		}
				
		drawingSurface = new DrawingSurfaceImpl(appModel, this);
		TimedArcPetriNet net = tapnNetwork.templates().get(0);
		drawingSurface.setModel(guiModels.get(net), net);
		drawingSurfaceScroller = new JScrollPane(drawingSurface);
		// make it less bad on XP
		drawingSurfaceScroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		leftPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
		leftPane.setPreferredSize(new Dimension(262, 100)); // height is ignored because the component is stretched
		leftPane.setMinimumSize(new Dimension(175,100));
		
		queryConstantsSplit = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
		queryConstantsSplit.setPreferredSize(new Dimension(262, 100)); // height is ignored because the component is stretched
		queryConstantsSplit.setMinimumSize(new Dimension(175,100));
		createLeftPane();
		
		
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

		templateExplorer = new TemplateExplorer(this);
		
		queryConstantsSplit.setDividerLocation(DIVIDER_LOCATION);
		queryConstantsSplit.setResizeWeight(0.5);
		queryConstantsSplit.setTopComponent(queries);
		queryConstantsSplit.setBottomComponent(leftBottomPanel);
		queryConstantsSplit.setContinuousLayout(true);
		queryConstantsSplit.setDividerSize(0);
		
		leftPane.setDividerLocation(0.3);
		leftPane.setResizeWeight(0.5);
		leftPane.setTopComponent(templateExplorer);
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
				transformToModelWithoutConstants();
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
			if(p instanceof TimedPlaceComponent){
				TimeInvariant inv = oldInvariants.get(p);
				((TimedPlaceComponent)p).setInvariant(inv);
			}
		}

		for(Arc arc : appModel.getArcs()){
			if(arc instanceof TimedInputArcComponent || arc instanceof TransportArcComponent){
				TimedInputArcComponent tarc = (TimedInputArcComponent)arc;
				TimeInterval guard = oldGuards.get(arc);
				tarc.setGuard(guard);
			}
		}
	}


	private void transformToModelWithoutConstants() {
		this.oldGuards = new HashMap<PetriNetObject, TimeInterval>();
		this.oldInvariants = new HashMap<PetriNetObject, TimeInvariant>();
		
		for(Place p : appModel.getPlaces()){
			if(p instanceof TimedPlaceComponent){
				oldInvariants.put(p, ((TimedPlaceComponent) p).getInvariant());
				TimeInvariant inv = getInvariant(p);
				((TimedPlaceComponent)p).setInvariant(inv);
			}
		}

		for(Arc arc : appModel.getArcs()){
			if(arc instanceof TimedInputArcComponent || arc instanceof TransportArcComponent){
				oldGuards.put(arc, ((TimedInputArcComponent) arc).getGuard());
				TimedInputArcComponent tarc = (TimedInputArcComponent)arc;
				TimeInterval guard = getGuard(tarc);
				tarc.setGuard(guard);
			}
		}
	}

	private TimeInterval getGuard(TimedInputArcComponent arc) {
		TimeInterval interval = arc.getGuard();
		
		boolean lowerIncluded = false;
		Bound lower;
		Bound upper;
		boolean upperIncluded = false;
		
		if(interval.lowerBound() instanceof ConstantBound){
			ConstantBound cBound = (ConstantBound)interval.lowerBound();
			lower = new IntBound(cBound.value());
		} 
		else {
			lower = interval.lowerBound();
		}
		lowerIncluded = interval.IsLowerBoundNonStrict();
		
		if(interval.upperBound() instanceof ConstantBound) {
			ConstantBound cBound = (ConstantBound)interval.upperBound();
			upper = new IntBound(cBound.value());
		}
		else {
			upper = interval.upperBound();
		}
		upperIncluded = interval.IsUpperBoundNonStrict();

		return new TimeInterval(lowerIncluded, lower, upper, upperIncluded);
	}

	private TimeInvariant getInvariant(Place place) {
		TimeInvariant inv = ((TimedPlaceComponent)place).getInvariant();
		
		Bound bound;
		
		if(inv.upperBound() instanceof ConstantBound) {
			ConstantBound cBound = (ConstantBound)inv.upperBound();
			bound = new IntBound(cBound.value());
		}
		else {
			bound = inv.upperBound();
		}
		
		return new TimeInvariant(inv.isUpperNonstrict(), bound);
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
	
	public JScrollPane drawingSurfaceScrollPane(){
		return drawingSurfaceScroller;
	}

	public TimedArcPetriNetNetwork network() {
		return tapnNetwork;
	}

	public DrawingSurfaceImpl drawingSurface() {
		return drawingSurface;
	}

	public Iterable<Template<TimedArcPetriNet>> templates() {
		ArrayList<Template<TimedArcPetriNet>> list = new ArrayList<Template<TimedArcPetriNet>>();
		for(TimedArcPetriNet net : tapnNetwork.templates()){
			list.add(new Template<TimedArcPetriNet>(net, guiModels.get(net)));
		}
		return list;
	}


	public void addTemplate(Template<TimedArcPetriNet> template) {
		tapnNetwork.add(template.model());
		guiModels.put(template.model(), template.guiModel());
	}


	public Template<TimedArcPetriNet> activeTemplate() {
		return templateExplorer.selectedModel();
	}

}
