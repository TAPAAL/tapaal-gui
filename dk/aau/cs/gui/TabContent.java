package dk.aau.cs.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
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
import pipe.gui.AnimationHistoryComponent;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.widgets.JSplitPaneFix;
import pipe.gui.widgets.LeftConstantsPane;
import pipe.gui.widgets.LeftQueryPane;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class TabContent extends JSplitPane {
	private static final long serialVersionUID = -648006317150905097L;

	private static final double DIVIDER_LOCATION = 0.5;
	
	private TimedArcPetriNetNetwork tapnNetwork = new TimedArcPetriNetNetwork();
	private HashMap<TimedArcPetriNet, DataLayer> guiModels = new HashMap<TimedArcPetriNet, DataLayer>();
	private HashMap<PetriNetObject, TimeInterval> oldGuards;
	private HashMap<PetriNetObject, TimeInvariant> oldInvariants;
	private JScrollPane drawingSurfaceScroller;
	private DrawingSurfaceImpl drawingSurface;
	private File appFile;
	
	// Normal mode
	private JSplitPane editorLeftPane;
	private JSplitPane queryConstantsSplit;
	private LeftQueryPane queries;
	private LeftConstantsPane constantsPanel;
	private TemplateExplorer templateExplorer;
	
	/// Animation
	private AnimationHistoryComponent animBox;
	private AnimationController animControlerBox;
	private JScrollPane animationHistoryScrollPane;
	private JScrollPane animationControllerScrollPane;
	private AnimationHistoryComponent abstractAnimationPane=null;
		
	public TabContent()
	{
		
		for(TimedArcPetriNet net : tapnNetwork.templates()){
			guiModels.put(net, new DataLayer());
		}
				
		drawingSurface = new DrawingSurfaceImpl(new DataLayer(), this);
		drawingSurfaceScroller = new JScrollPane(drawingSurface);
		// make it less bad on XP
		drawingSurfaceScroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		createEditorLeftPane();
				
		this.setOrientation(HORIZONTAL_SPLIT);
		this.setLeftComponent(editorLeftPane);
		this.setRightComponent(drawingSurfaceScroller);
		
		this.setContinuousLayout(true);
		this.setOneTouchExpandable(true);
		this.setBorder(null); // avoid multiple borders
		this.setDividerSize(8);
	}
	
	public void createEditorLeftPane(){
		editorLeftPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
		editorLeftPane.setPreferredSize(new Dimension(262, 100)); // height is ignored because the component is stretched
		editorLeftPane.setMinimumSize(new Dimension(175,100));
		boolean enableAddButton = getModel() == null ? true : !getModel().netType().equals(NetType.UNTIMED);
		constantsPanel = new LeftConstantsPane(enableAddButton,this);
		queries = new LeftQueryPane(new ArrayList<TAPNQuery>(), this);

		templateExplorer = new TemplateExplorer(this);
		
		queryConstantsSplit = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
//		queryConstantsSplit.setPreferredSize(new Dimension(262, 100)); // height is ignored because the component is stretched
//		queryConstantsSplit.setMinimumSize(new Dimension(175,100));
		queryConstantsSplit.setDividerLocation(DIVIDER_LOCATION);
		queryConstantsSplit.setResizeWeight(0.5);
		queryConstantsSplit.setTopComponent(queries);
		queryConstantsSplit.setBottomComponent(constantsPanel);
		queryConstantsSplit.setContinuousLayout(true);
		queryConstantsSplit.setDividerSize(0);
		
		editorLeftPane.setDividerLocation(0.3);
		editorLeftPane.setResizeWeight(0.5);
		editorLeftPane.setTopComponent(templateExplorer);
		editorLeftPane.setBottomComponent(queryConstantsSplit);
		editorLeftPane.setContinuousLayout(true);
		editorLeftPane.setDividerSize(0);
		
		updateLeftPanel();
	}

	public void updateConstantsList(){
		constantsPanel.showConstants();
	}

	public void updateLeftPanel() {
		editorLeftPane.validate();
	}
	
	public DataLayer getModel()
	{
		return drawingSurface.getGuiModel();
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

	/** Creates a new animationHistory text area, and returns a reference to it*/
	private void createAnimationHistory() {
		try {
			animBox = new AnimationHistoryComponent();

			animationHistoryScrollPane = new JScrollPane(animBox);
			animationHistoryScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Simulation History"),
				BorderFactory.createEmptyBorder(3,3,3,3)
			)); 
//			leftPane.setBottomComponent(scroller);
//
//			//         leftPane.setDividerLocation(0.5);
//			leftPane.setResizeWeight(0.05f);
//
//			leftPane.setDividerSize(8);
		} catch (javax.swing.text.BadLocationException be) {
			be.printStackTrace();
		}
	}
	
	public void switchToAnimationComponents(){
		createAnimationHistory();
		createAnimationController();
		
		JSplitPane animatorLeftPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
		//animatorLeftPane.setLayout(new BorderLayout());
		animatorLeftPane.setPreferredSize(animControlerBox.getPreferredSize()); // height is ignored because the component is stretched
		animatorLeftPane.setMinimumSize(animControlerBox.getMinimumSize());
		
		animatorLeftPane.setDividerLocation(0.25);
		animatorLeftPane.setResizeWeight(0);
		templateExplorer.hideButtons();
		animatorLeftPane.setTopComponent(templateExplorer);
		
		JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		panel.setResizeWeight(0);
		panel.setDividerLocation(-1);
		panel.setTopComponent(animControlerBox);
		panel.setBottomComponent(animationHistoryScrollPane);
		
		animatorLeftPane.setBottomComponent(panel);
		this.setLeftComponent(animatorLeftPane);
		this.setDividerLocation(-1);
		
		drawingSurface.repaintAll();
	}
	
	public void switchToEditorComponents(){
		templateExplorer.showButtons();
		editorLeftPane.setTopComponent(templateExplorer);
		this.setLeftComponent(editorLeftPane);
		
		drawingSurface.repaintAll();
	}

	public AnimationHistoryComponent getAbstractAnimationPane(){
		return abstractAnimationPane;
	}
	
	public AnimationController getAnimationController(){
		return animControlerBox;
	}

	public void addAbstractAnimationPane() {
		

		try {
			abstractAnimationPane=new AnimationHistoryComponent();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//abstractAnimationPane.setVerticalAlignment(SwingConstants.TOP);

		//Create a new empty animBox
		try {
			animBox = new AnimationHistoryComponent();
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

		editorLeftPane.setBottomComponent(pane2);
		abstractAnimationPane.setBorder(new LineBorder(Color.black));

	}

	public void removeAbstractAnimationPane() {
		abstractAnimationPane=null;
		animationHistoryScrollPane = new JScrollPane(animBox);
		animationHistoryScrollPane.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP
		editorLeftPane.setBottomComponent(animationHistoryScrollPane);
	}

	private void createAnimationController() {
			animControlerBox = new AnimationController();

			animationControllerScrollPane = new JScrollPane(animControlerBox);
			animationControllerScrollPane.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP
//
//			leftPane.setTopComponent(scroller2);
//
//			//         leftPane.setDividerLocation(0.5);
//			leftPane.setDividerSize(8);
//			leftPane.resetToPreferredSizes();
//			//shortcutBottons should be usable from start of
			animControlerBox.requestFocus(true);
	}

//	public void removeAnimationHistory() {
//		if (scroller != null) {
//			leftPane.remove(scroller);
//			leftPane.setDividerLocation(DIVIDER_LOCATION);
//			leftPane.setDividerSize(0);
//		}
//	}
//	public void removeAnimationController() {
//		if (scroller != null) {
//			leftPane.remove(scroller2);
//			leftPane.setDividerLocation(DIVIDER_LOCATION);
//			leftPane.setDividerSize(0);
//		}
//	}


	public AnimationHistoryComponent getAnimationHistory() {
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
		templateExplorer.updateTemplateList();
	}


	public Template<TimedArcPetriNet> activeTemplate() {
//		if(CreateGui.getApp().getGUIMode().equals(GUIMode.animation)){
//			return animationTemplateExplorer.selectedModel();
//		}else{
			return templateExplorer.selectedModel();
		//}
	}

	public void setActiveTemplate(Template<TimedArcPetriNet> template){
		drawingSurface.setModel(template.guiModel(), template.model());
	}

	public Iterable<TAPNQuery> queries() {
		return queries.getQueries();
	}

	public void setQueries(Iterable<TAPNQuery> queries) {
		this.queries.setQueries(queries);
		
	}

	public void removeQuery(TAPNQuery queryToCreateFrom) {
		queries.removeQuery(queryToCreateFrom);
		
	}

	public void setConstants(Iterable<Constant> constants) {
		tapnNetwork.setConstants(constants);
		constantsPanel.showConstants();
	}
}
