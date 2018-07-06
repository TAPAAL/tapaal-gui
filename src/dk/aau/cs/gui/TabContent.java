package dk.aau.cs.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Split;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.AnimationController;
import pipe.gui.AnimationHistoryComponent;
import pipe.gui.Animator;
import pipe.gui.DelayEnabledTransitionControl;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Zoomer;
import pipe.gui.widgets.ConstantsPane;
import pipe.gui.widgets.JSplitPaneFix;
import pipe.gui.widgets.QueryPane;
import pipe.gui.widgets.WorkflowDialog;
import dk.aau.cs.gui.components.BugHandledJXMultisplitPane;
import dk.aau.cs.gui.components.TransitionFireingComponent;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Require;

public class TabContent extends JSplitPane {
	private static final long serialVersionUID = -648006317150905097L;

	private TimedArcPetriNetNetwork tapnNetwork = new TimedArcPetriNetNetwork();
	private HashMap<TimedArcPetriNet, DataLayer> guiModels = new HashMap<TimedArcPetriNet, DataLayer>();
	private HashMap<TimedArcPetriNet, Zoomer> zoomLevels = new HashMap<TimedArcPetriNet, Zoomer>();
	private JScrollPane drawingSurfaceScroller;
	private JScrollPane editorSplitPaneScroller;
	private JScrollPane animatorSplitPaneScroller;
	private DrawingSurfaceImpl drawingSurface;
	private File appFile;
	private JPanel drawingSurfaceDummy;
	
	// Normal mode
	private BugHandledJXMultisplitPane editorSplitPane;
	private static Split editorModelroot = null;
	private static Split simulatorModelRoot = null;

	private QueryPane queries;
	private ConstantsPane constantsPanel;
	private TemplateExplorer templateExplorer;
	private SharedPlacesAndTransitionsPanel sharedPTPanel;

	private static final String constantsName = "constants";
	private static final String queriesName = "queries";
	private static final String templateExplorerName = "templateExplorer";
	private static final String sharedPTName = "sharedPT";

	// / Animation
	private AnimationHistoryComponent animBox;
	private AnimationController animControlerBox;
	private JScrollPane animationHistoryScrollPane;
	private JScrollPane animationControllerScrollPane;
	private AnimationHistoryComponent abstractAnimationPane = null;
	private JPanel animationControlsPanel;
	private TransitionFireingComponent transitionFireing;

	private static final String transitionFireingName = "enabledTransitions";
	private static final String animControlName = "animControl";

	private JSplitPane animationHistorySplitter;

	private BugHandledJXMultisplitPane animatorSplitPane;

	private Integer selectedTemplate = 0;
	private Boolean selectedTemplateWasActive = false;
	
	private WorkflowDialog workflowDialog = null;

	public TabContent(NetType netType) {
		for (TimedArcPetriNet net : tapnNetwork.allTemplates()) {
			guiModels.put(net, new DataLayer());
			zoomLevels.put(net, new Zoomer());
		}
		
		drawingSurface = new DrawingSurfaceImpl(new DataLayer());
		drawingSurfaceScroller = new JScrollPane(drawingSurface);
		// make it less bad on XP
		drawingSurfaceScroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		drawingSurfaceScroller.setWheelScrollingEnabled(true);
		drawingSurfaceScroller.getVerticalScrollBar().setUnitIncrement(10);
		drawingSurfaceScroller.getHorizontalScrollBar().setUnitIncrement(10);

		// Make clicking the drawing area move focus to GuiFrame
		drawingSurface.addMouseListener(new MouseAdapter() {	
			@Override
			public void mouseClicked(MouseEvent e) {
				CreateGui.getApp().requestFocus();
			}
		});
		
		drawingSurfaceDummy = new JPanel(new GridBagLayout());
		GridBagConstraints gc=new GridBagConstraints();
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.gridx=0;
		gc.gridy=0;
		drawingSurfaceDummy.add(new JLabel("The net is too big to be drawn"), gc);
		
		createEditorLeftPane();
		createAnimatorSplitPane(netType);

		this.setOrientation(HORIZONTAL_SPLIT);
		this.setLeftComponent(editorSplitPaneScroller);
		this.setRightComponent(drawingSurfaceScroller);

		this.setContinuousLayout(true);
		this.setOneTouchExpandable(true);
		this.setBorder(null); // avoid multiple borders
		this.setDividerSize(8);	
	}
	
	public SharedPlacesAndTransitionsPanel getSharedPlacesAndTransitionsPanel(){
		return sharedPTPanel;
	}
	
	public TemplateExplorer getTemplateExplorer(){
		return templateExplorer;
	}
	
	public void createEditorLeftPane() {
		boolean enableAddButton = getModel() == null ? true : !getModel()
				.netType().equals(NetType.UNTIMED);

		constantsPanel = new ConstantsPane(enableAddButton, this);
		constantsPanel.setPreferredSize(new Dimension(constantsPanel
				.getPreferredSize().width,
				constantsPanel.getMinimumSize().height));
		queries = new QueryPane(new ArrayList<TAPNQuery>(), this);
		queries.setPreferredSize(new Dimension(
				queries.getPreferredSize().width,
				queries.getMinimumSize().height));
		templateExplorer = new TemplateExplorer(this);
		templateExplorer.setPreferredSize(new Dimension(templateExplorer
				.getPreferredSize().width,
				templateExplorer.getMinimumSize().height));
		sharedPTPanel = new SharedPlacesAndTransitionsPanel(this);
		sharedPTPanel.setPreferredSize(new Dimension(sharedPTPanel
				.getPreferredSize().width,
				sharedPTPanel.getMinimumSize().height));
		
		boolean floatingDividers = false;
		if(editorModelroot == null){
			Leaf constantsLeaf = new Leaf(constantsName);
			Leaf queriesLeaf = new Leaf(queriesName);
			Leaf templateExplorerLeaf = new Leaf(templateExplorerName);
			Leaf sharedPTLeaf = new Leaf(sharedPTName);

			constantsLeaf.setWeight(0.25);
			queriesLeaf.setWeight(0.25);
			templateExplorerLeaf.setWeight(0.25);
			sharedPTLeaf.setWeight(0.25);

			editorModelroot = new Split(templateExplorerLeaf, new Divider(),
					sharedPTLeaf, new Divider(), queriesLeaf, new Divider(),
					constantsLeaf);
			editorModelroot.setRowLayout(false);
			// The modelroot needs to have a parent when we remove all its children
			// (bug in the swingx package)
			editorModelroot.setParent(new Split());
			floatingDividers = true;
		} 
		editorSplitPane = new BugHandledJXMultisplitPane();
		editorSplitPane.getMultiSplitLayout().setFloatingDividers(floatingDividers);
		editorSplitPane.getMultiSplitLayout().setLayoutByWeight(false);
		
		editorSplitPane.setSize(editorModelroot.getBounds().width, editorModelroot.getBounds().height);
		
		editorSplitPane.getMultiSplitLayout().setModel(editorModelroot);

		editorSplitPane.add(templateExplorer, templateExplorerName);
		editorSplitPane.add(sharedPTPanel, sharedPTName);
		editorSplitPane.add(queries, queriesName);
		editorSplitPane.add(constantsPanel, constantsName);
		
		editorSplitPaneScroller = createLeftScrollPane(editorSplitPane);
		this.setLeftComponent(editorSplitPaneScroller);
		
		editorSplitPane.repaint();
	}
	
	private JScrollPane createLeftScrollPane(JPanel panel){
		JScrollPane scroller = new JScrollPane(panel);
		scroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		scroller.setWheelScrollingEnabled(true);
		scroller.getVerticalScrollBar().setUnitIncrement(10);
		scroller.getHorizontalScrollBar().setUnitIncrement(10);
		scroller.setBorder(null);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setMinimumSize(new Dimension(
				panel.getMinimumSize().width,
				panel.getMinimumSize().height));
		return scroller;
	}

	public void selectFirstActiveTemplate() {
		templateExplorer.selectFirst();
	}

	public Boolean templateWasActiveBeforeSimulationMode() {
		return selectedTemplateWasActive;
	}

	public void resetSelectedTemplateWasActive() {
		selectedTemplateWasActive = false;
	}

	public void setSelectedTemplateWasActive() {
		selectedTemplateWasActive = true;
	}

	public void rememberSelectedTemplate() {
		selectedTemplate = templateExplorer.indexOfSelectedTemplate();
	}

	public void restoreSelectedTemplate() {
		templateExplorer.restoreSelectedTemplate(selectedTemplate);
	}

	public void updateConstantsList() {
		constantsPanel.showConstants();
	}
	
	public void removeConstantHighlights() {
		constantsPanel.removeConstantHighlights();
	}

	public void updateQueryList() {
		queries.updateQueryButtons();
		queries.repaint();
	}

	public DataLayer getModel() {
		return drawingSurface.getGuiModel();
	}
	
	public HashMap<TimedArcPetriNet, DataLayer> getGuiModels() {
		return this.guiModels;
	}

	public void setDrawingSurface(DrawingSurfaceImpl drawingSurface) {
		this.drawingSurface = drawingSurface;
	}

	public File getFile() {
		return appFile;
	}

	public void setFile(File file) {
		appFile = file;
	}

	/** Creates a new animationHistory text area, and returns a reference to it */
	private void createAnimationHistory() {
		animBox = new AnimationHistoryComponent();
		animBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					int selected = animBox.getSelectedIndex();
					int clicked = animBox.locationToIndex(e.getPoint());
					if (clicked != -1) {
						int steps = clicked - selected;
						Animator anim = CreateGui.getAnimator();
						if (steps < 0) {
							for (int i = 0; i < Math.abs(steps); i++) {
								animBox.stepBackwards();
								anim.stepBack();
								CreateGui.getCurrentTab().getAnimationController()
								.setAnimationButtonsEnabled();
							}
						} else {
							for (int i = 0; i < Math.abs(steps); i++) {
								animBox.stepForward();
								anim.stepForward();
								CreateGui.getCurrentTab().getAnimationController()
								.setAnimationButtonsEnabled();
							}
						}
						
						anim.blinkSelected((String)animBox.getSelectedValue());
					}
				}
				// Remove focus
				CreateGui.getApp().requestFocus();
			}
		});
		animationHistoryScrollPane = new JScrollPane(animBox);
		animationHistoryScrollPane.setBorder(BorderFactory
				.createCompoundBorder(
						BorderFactory.createTitledBorder("Simulation History"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		//Add 10 pixel to the minimumsize of the scrollpane
		animationHistoryScrollPane.setMinimumSize(new Dimension(animationHistoryScrollPane.getMinimumSize().width, animationHistoryScrollPane.getMinimumSize().height + 20));
	}

	private void createAnimatorSplitPane(NetType netType) {
		if (animBox == null)
			createAnimationHistory();
		if (animControlerBox == null)
			createAnimationController(netType);
		if (transitionFireing == null)
			createTransitionFireing();
		
		boolean floatingDividers = false;
		if(simulatorModelRoot == null){
			Leaf templateExplorerLeaf = new Leaf(templateExplorerName);
			Leaf enabledTransitionsListLeaf = new Leaf(transitionFireingName);
			Leaf animControlLeaf = new Leaf(animControlName);

			templateExplorerLeaf.setWeight(0.25);
			enabledTransitionsListLeaf.setWeight(0.25);
			animControlLeaf.setWeight(0.5);

			simulatorModelRoot = new Split(templateExplorerLeaf, new Divider(),
					enabledTransitionsListLeaf, new Divider(), animControlLeaf);
			simulatorModelRoot.setRowLayout(false);
			floatingDividers = true;
		}
		animatorSplitPane = new BugHandledJXMultisplitPane();
		
		animatorSplitPane.getMultiSplitLayout().setFloatingDividers(floatingDividers);
		animatorSplitPane.setSize(simulatorModelRoot.getBounds().width, simulatorModelRoot.getBounds().height);
		
		animatorSplitPane.getMultiSplitLayout().setModel(simulatorModelRoot);

		animationControlsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		animationControlsPanel.add(animControlerBox, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		animationControlsPanel.add(animationHistoryScrollPane, gbc);

		animationControlsPanel.setPreferredSize(new Dimension(
				animationControlsPanel.getPreferredSize().width,
				animationControlsPanel.getMinimumSize().height));
		transitionFireing.setPreferredSize(new Dimension(
				transitionFireing.getPreferredSize().width,
				transitionFireing.getMinimumSize().height));
		animatorSplitPane.add(animationControlsPanel, animControlName);
		animatorSplitPane.add(transitionFireing, transitionFireingName);
		
		animatorSplitPaneScroller = createLeftScrollPane(animatorSplitPane);
	}

	public void switchToAnimationComponents(boolean showEnabledTransitions) {
		
		//Remove dummy
		Component dummy = animatorSplitPane.getMultiSplitLayout().getComponentForNode(animatorSplitPane.getMultiSplitLayout().getNodeForName(templateExplorerName));
		if(dummy != null){
			animatorSplitPane.remove(dummy);
		}
		
		//Add the templateExplorer
		animatorSplitPane.add(templateExplorer, templateExplorerName);

		// Inserts dummy to avoid nullpointerexceptions from the displaynode
		// method. A component can only be on one splitpane at the time
		dummy = new JButton("EditorDummy");
		dummy.setMinimumSize(templateExplorer.getMinimumSize());
		dummy.setPreferredSize(templateExplorer.getPreferredSize());
		editorSplitPane.add(dummy, templateExplorerName);

		templateExplorer.switchToAnimationMode();
		showEnabledTransitionsList(showEnabledTransitions);
		
		this.setLeftComponent(animatorSplitPaneScroller);

	}

	public void switchToEditorComponents() {
		
		//Remove dummy
		Component dummy = editorSplitPane.getMultiSplitLayout().getComponentForNode(editorSplitPane.getMultiSplitLayout().getNodeForName(templateExplorerName));
		if(dummy != null){
			editorSplitPane.remove(dummy);
		}
		
		//Add the templateexplorer again
		editorSplitPane.add(templateExplorer, templateExplorerName);
		if (animatorSplitPane != null) {

			// Inserts dummy to avoid nullpointerexceptions from the displaynode
			// method. A component can only be on one splitpane at the time
			dummy = new JButton("AnimatorDummy");
			dummy.setMinimumSize(templateExplorer.getMinimumSize());
			dummy.setPreferredSize(templateExplorer.getPreferredSize());
			animatorSplitPane.add(new JPanel(), templateExplorerName);
		}

		templateExplorer.switchToEditorMode();
		this.setLeftComponent(editorSplitPaneScroller);
		drawingSurface.repaintAll();
	}

	public AnimationHistoryComponent getUntimedAnimationHistory() {
		return abstractAnimationPane;
	}

	public AnimationController getAnimationController() {
		return animControlerBox;
	}
	
	public DelayEnabledTransitionControl getDelayEnabledTransitionControl(){
		return transitionFireing.getDelayEnabledTransitionControl();
	}

	public void addAbstractAnimationPane() {
		animationControlsPanel.remove(animationHistoryScrollPane);
		abstractAnimationPane = new AnimationHistoryComponent();

		JScrollPane untimedAnimationHistoryScrollPane = new JScrollPane(
				abstractAnimationPane);
		untimedAnimationHistoryScrollPane.setBorder(BorderFactory
				.createCompoundBorder(
						BorderFactory.createTitledBorder("Untimed Trace"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		animationHistorySplitter = new JSplitPaneFix(
				JSplitPane.HORIZONTAL_SPLIT, animationHistoryScrollPane,
				untimedAnimationHistoryScrollPane);

		animationHistorySplitter.setContinuousLayout(true);
		animationHistorySplitter.setOneTouchExpandable(true);
		animationHistorySplitter.setBorder(null); // avoid multiple borders
		animationHistorySplitter.setDividerSize(8);
		animationHistorySplitter.setDividerLocation(0.5);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		animationControlsPanel.add(animationHistorySplitter, gbc);
	}

	public void removeAbstractAnimationPane() {
		animationControlsPanel.remove(animationHistorySplitter);
		abstractAnimationPane = null;

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		animationControlsPanel.add(animationHistoryScrollPane, gbc);
		animatorSplitPane.validate();
	}

	private void createAnimationController(NetType netType) {
		animControlerBox = new AnimationController(netType);

		animationControllerScrollPane = new JScrollPane(animControlerBox);
		animationControllerScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		animControlerBox.requestFocus(true);
	}

	public AnimationHistoryComponent getAnimationHistory() {
		return animBox;
	}

	private void createTransitionFireing() {
		transitionFireing = new TransitionFireingComponent(CreateGui.getApp().isShowingDelayEnabledTransitions());
	}

	public TransitionFireingComponent getTransitionFireingComponent() {
		return transitionFireing;
	}

	public JScrollPane drawingSurfaceScrollPane() {
		return drawingSurfaceScroller;
	}

	public TimedArcPetriNetNetwork network() {
		return tapnNetwork;
	}

	public DrawingSurfaceImpl drawingSurface() {
		return drawingSurface;
	}

	public Iterable<Template> allTemplates() {
		ArrayList<Template> list = new ArrayList<Template>();
		for (TimedArcPetriNet net : tapnNetwork.allTemplates()) {
			list.add(new Template(net, guiModels.get(net), zoomLevels.get(net)));
		}
		return list;
	}

	public Iterable<Template> activeTemplates() {
		ArrayList<Template> list = new ArrayList<Template>();
		for (TimedArcPetriNet net : tapnNetwork.activeTemplates()) {
			list.add(new Template(net, guiModels.get(net), zoomLevels.get(net)));
		}
		return list;
	}

	public int numberOfActiveTemplates() {
		int count = 0;
		for (TimedArcPetriNet net : tapnNetwork.activeTemplates()) {
			if (net.isActive())
				count++;
		}
		return count;
	}

	/*
		XXX: 2018-05-07 kyrke, added a version of addTemplate that does not call templatExplorer.updatTemplateList
		used in createNewTab (as updateTamplateList expects the current tab to be selected)
		this needs to be refactored asap. but the is the only way I could get it to work for now.
		The code is very unclean on what is a template, TimeArcPetriNetNetwork, seems to mix concerns about
		gui/controller/model. Further refactoring is needed to clean up this mess.
	 */
	public void addTemplate(Template template, boolean updateTemplateExplorer) {
		tapnNetwork.add(template.model());
		guiModels.put(template.model(), template.guiModel());
		zoomLevels.put(template.model(), template.zoomer());
		if (updateTemplateExplorer) {
			templateExplorer.updateTemplateList();
		}
	}

	public void addTemplate(Template template) {
		addTemplate(template, true);
	}

	public void addGuiModel(TimedArcPetriNet net, DataLayer guiModel) {
		guiModels.put(net, guiModel);
	}

	public void removeTemplate(Template template) {
		tapnNetwork.remove(template.model());
		guiModels.remove(template.model());
		zoomLevels.remove(template.model());
		templateExplorer.updateTemplateList();
	}

	public Template currentTemplate() {
		return templateExplorer.selectedModel();
	}

	public void setCurrentTemplate(Template template) {
		drawingSurface.setModel(template.guiModel(), template.model(),
				template.zoomer());
	}

	public Iterable<TAPNQuery> queries() {
		return queries.getQueries();
	}

	public void setQueries(Iterable<TAPNQuery> queries) {
		this.queries.setQueries(queries);
	}

	public void removeQuery(TAPNQuery queryToRemove) {
		queries.removeQuery(queryToRemove);
	}

	public void addQuery(TAPNQuery query) {
		queries.addQuery(query);
	}

	public void setConstants(Iterable<Constant> constants) {
		tapnNetwork.setConstants(constants);
	}

	public void setupNameGeneratorsFromTemplates(Iterable<Template> templates) {
		drawingSurface.setupNameGeneratorsFromTemplates(templates);
	}

	public void setNetwork(TimedArcPetriNetNetwork network,
			Collection<Template> templates) {
		Require.that(network != null, "network cannot be null");
		tapnNetwork = network;

		guiModels.clear();
		for (Template template : templates) {
			addGuiModel(template.model(), template.guiModel());
			zoomLevels.put(template.model(), template.zoomer());
		}

		sharedPTPanel.setNetwork(network);
		templateExplorer.updateTemplateList();

		constantsPanel.setNetwork(tapnNetwork);
		
		if(network.paintNet()){
			this.setRightComponent(drawingSurfaceScroller);
		} else {
			this.setRightComponent(drawingSurfaceDummy);
		}
	}

	public void swapTemplates(int currentIndex, int newIndex) {
		tapnNetwork.swapTemplates(currentIndex, newIndex);
	}

	public TimedArcPetriNet[] sortTemplates() {
		return tapnNetwork.sortTemplates();
	}

	public void undoSort(TimedArcPetriNet[] l) {
		tapnNetwork.undoSort(l);
	}

	public void swapConstants(int currentIndex, int newIndex) {
		tapnNetwork.swapConstants(currentIndex, newIndex);

	}

	public Constant[] sortConstants() {
		return tapnNetwork.sortConstants();
	}

	public void undoSort(Constant[] oldOrder) {
		tapnNetwork.undoSort(oldOrder);
	}

	public void showComponents(boolean enable) {
		if (enable != templateExplorer.isVisible()) {
			editorSplitPane.getMultiSplitLayout().displayNode(
					templateExplorerName, enable);
			editorSplitPane.getMultiSplitLayout().displayNode(sharedPTName,
					enable);
			if (animatorSplitPane != null) {
				animatorSplitPane.getMultiSplitLayout().displayNode(
						templateExplorerName, enable);
			}
			makeSureEditorPanelIsVisible(templateExplorer);
		}
	}

	public void showQueries(boolean enable) {
		if (enable != queries.isVisible()) {
			editorSplitPane.getMultiSplitLayout().displayNode(queriesName,
					enable);
			makeSureEditorPanelIsVisible(queries);
			this.repaint();
		}
	}

	public void showConstantsPanel(boolean enable) {
		if (enable != constantsPanel.isVisible()) {
			editorSplitPane.getMultiSplitLayout().displayNode(constantsName,
					enable);
			makeSureEditorPanelIsVisible(constantsPanel);
		}		
	}

	public void showEnabledTransitionsList(boolean enable) {
		if (transitionFireing != null && !(enable && transitionFireing.isVisible())) {
			animatorSplitPane.getMultiSplitLayout().displayNode(
					transitionFireingName, enable);
		}
	}
	
	public void showDelayEnabledTransitions(boolean enable){
		transitionFireing.showDelayEnabledTransitions(enable);
		drawingSurface.repaint();
		
		CreateGui.getAnimator().updateFireableTransitions();
	}
	
	public void selectFirstElements() {
		templateExplorer.selectFirst();
		queries.selectFirst();
		constantsPanel.selectFirst();
	}	
	
	public boolean isQueryPossible() {
		return queries.isQueryPossible();
	}
	
	public void verifySelectedQuery() {
		queries.verifySelectedQuery();
	}
	
	public void editSelectedQuery(){
		queries.showEditDialog();
	}

	public void makeSureEditorPanelIsVisible(Component c){
		//If you "show" a component and the main divider is all the way to the left, make sure it's moved such that the component is actually shown
		if(c.isVisible()){
			if(this.getDividerLocation() == 0){
				this.setDividerLocation(c.getPreferredSize().width);
			}
		}
	}
	
	public void setResizeingDefault(){
		if(animatorSplitPane != null){
			animatorSplitPane.getMultiSplitLayout().setFloatingDividers(true);
			animatorSplitPane.getMultiSplitLayout().layoutByWeight(animatorSplitPane);
			animatorSplitPane.getMultiSplitLayout().setFloatingDividers(false);
		} else {
			simulatorModelRoot = null;
		}
		editorSplitPane.getMultiSplitLayout().setFloatingDividers(true);
		editorSplitPane.getMultiSplitLayout().layoutByWeight(editorSplitPane);
		editorSplitPane.getMultiSplitLayout().setFloatingDividers(false);
	}
	
	public static Split getEditorModelRoot(){
		return editorModelroot;
	}
	
	public static void setEditorModelRoot(Split model){
		editorModelroot = model;
	}
	
	public static Split getSimulatorModelRoot(){
		return simulatorModelRoot;
	}
	
	public static void setSimulatorModelRoot(Split model){
		simulatorModelRoot = model;
	}
	
	public boolean restoreWorkflowDialog(){
		return workflowDialog != null && workflowDialog.restoreWindow();
	}
	
	public WorkflowDialog getWorkflowDialog() {
		return workflowDialog;
	}
	
	public void setWorkflowDialog(WorkflowDialog dialog) {
		this.workflowDialog = dialog;
	}
}
