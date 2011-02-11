package dk.aau.cs.gui;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.AnimationController;
import pipe.gui.AnimationHistoryComponent;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.widgets.JSplitPaneFix;
import pipe.gui.widgets.LeftConstantsPane;
import pipe.gui.widgets.LeftQueryPane;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class TabContent extends JSplitPane {
	private static final long serialVersionUID = -648006317150905097L;

	private static final double DIVIDER_LOCATION = 0.5;

	private TimedArcPetriNetNetwork tapnNetwork = new TimedArcPetriNetNetwork();
	private HashMap<TimedArcPetriNet, DataLayer> guiModels = new HashMap<TimedArcPetriNet, DataLayer>();
	private JScrollPane drawingSurfaceScroller;
	private DrawingSurfaceImpl drawingSurface;
	private File appFile;

	// Normal mode
	private JSplitPane editorLeftPane;
	private JSplitPane queryConstantsSplit;
	private LeftQueryPane queries;
	private LeftConstantsPane constantsPanel;
	private TemplateExplorer templateExplorer;

	// / Animation
	private AnimationHistoryComponent animBox;
	private AnimationController animControlerBox;
	private JScrollPane animationHistoryScrollPane;
	private JScrollPane animationControllerScrollPane;
	private AnimationHistoryComponent abstractAnimationPane = null;

	private JSplitPane controllerAndHistoryPanel;

	public TabContent() {

		for (TimedArcPetriNet net : tapnNetwork.templates()) {
			guiModels.put(net, new DataLayer());
		}

		drawingSurface = new DrawingSurfaceImpl(new DataLayer(), this);
		drawingSurfaceScroller = new JScrollPane(drawingSurface);
		// make it less bad on XP
		drawingSurfaceScroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		drawingSurfaceScroller.setWheelScrollingEnabled(true);

		createEditorLeftPane();

		this.setOrientation(HORIZONTAL_SPLIT);
		this.setLeftComponent(editorLeftPane);
		this.setRightComponent(drawingSurfaceScroller);

		this.setContinuousLayout(true);
		this.setOneTouchExpandable(true);
		this.setBorder(null); // avoid multiple borders
		this.setDividerSize(8);
	}

	public void createEditorLeftPane() {
		editorLeftPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
		editorLeftPane.setPreferredSize(new Dimension(262, 100)); // height is
																	// ignored
																	// because
																	// the
																	// component
																	// is
																	// stretched
		editorLeftPane.setMinimumSize(new Dimension(175, 100));
		boolean enableAddButton = getModel() == null ? true : !getModel()
				.netType().equals(NetType.UNTIMED);
		constantsPanel = new LeftConstantsPane(enableAddButton, this);
		queries = new LeftQueryPane(new ArrayList<TAPNQuery>(), this);

		templateExplorer = new TemplateExplorer(this);

		queryConstantsSplit = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
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

	public void updateConstantsList() {
		constantsPanel.showConstants();
	}

	public void updateLeftPanel() {
		editorLeftPane.validate();
	}

	public DataLayer getModel() {
		return drawingSurface.getGuiModel();
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

			animationHistoryScrollPane = new JScrollPane(animBox);
			animationHistoryScrollPane.setBorder(BorderFactory
					.createCompoundBorder(BorderFactory
							.createTitledBorder("Simulation History"),
							BorderFactory.createEmptyBorder(3, 3, 3, 3)));
	}

	public void switchToAnimationComponents() {
		createAnimationHistory();
		createAnimationController();

		JSplitPane animatorLeftPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT);
		animatorLeftPane.setPreferredSize(animControlerBox.getPreferredSize()); // height is ignored because the component is stretched
		animatorLeftPane.setMinimumSize(animControlerBox.getMinimumSize());

		animatorLeftPane.setDividerLocation(0.25);
		animatorLeftPane.setResizeWeight(0);
		templateExplorer.hideButtons();
		animatorLeftPane.setTopComponent(templateExplorer);

		controllerAndHistoryPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		controllerAndHistoryPanel.setResizeWeight(0);
		controllerAndHistoryPanel.setDividerLocation(-1);
		controllerAndHistoryPanel.setTopComponent(animControlerBox);
		controllerAndHistoryPanel.setBottomComponent(animationHistoryScrollPane);

		animatorLeftPane.setBottomComponent(controllerAndHistoryPanel);
		this.setLeftComponent(animatorLeftPane);
		this.setDividerLocation(-1);

		drawingSurface.repaintAll();
	}

	public void switchToEditorComponents() {
		templateExplorer.showButtons();
		editorLeftPane.setTopComponent(templateExplorer);
		this.setLeftComponent(editorLeftPane);

		drawingSurface.repaintAll();
	}

	public AnimationHistoryComponent getUntimedAnimationHistory() {
		return abstractAnimationPane;
	}

	public AnimationController getAnimationController() {
		return animControlerBox;
	}

	public void addAbstractAnimationPane() {
		abstractAnimationPane = new AnimationHistoryComponent();
		animBox = new AnimationHistoryComponent();
		animationHistoryScrollPane = new JScrollPane(animBox);
		animationHistoryScrollPane.setBorder(BorderFactory
				.createCompoundBorder(BorderFactory
						.createTitledBorder("Simulation History"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		
		JScrollPane untimedAnimationHistoryScrollPane = new JScrollPane(abstractAnimationPane);
		untimedAnimationHistoryScrollPane.setBorder(BorderFactory
				.createCompoundBorder(BorderFactory
						.createTitledBorder("Untimed Trace"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		JSplitPane pane2 = new JSplitPaneFix(JSplitPane.HORIZONTAL_SPLIT, animationHistoryScrollPane, untimedAnimationHistoryScrollPane);

		pane2.setContinuousLayout(true);
		pane2.setOneTouchExpandable(true);
		pane2.setBorder(null); // avoid multiple borders
		pane2.setDividerSize(8);
		pane2.setDividerLocation(0.5);

		controllerAndHistoryPanel.setBottomComponent(pane2);
	}

	public void removeAbstractAnimationPane() {
		abstractAnimationPane = null;
		animationHistoryScrollPane = new JScrollPane(animBox);
		animationHistoryScrollPane.setBorder(BorderFactory
				.createCompoundBorder(BorderFactory
						.createTitledBorder("Simulation History"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		controllerAndHistoryPanel.setBottomComponent(animationHistoryScrollPane);
	}

	private void createAnimationController() {
		animControlerBox = new AnimationController();

		animationControllerScrollPane = new JScrollPane(animControlerBox);
		animationControllerScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0)); // make
																				// it
																				// less
																				// bad
																				// on
																				// XP
	
		animControlerBox.requestFocus(true);
	}

	public AnimationHistoryComponent getAnimationHistory() {
		return animBox;
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

	public Iterable<Template<TimedArcPetriNet>> templates() {
		ArrayList<Template<TimedArcPetriNet>> list = new ArrayList<Template<TimedArcPetriNet>>();
		for (TimedArcPetriNet net : tapnNetwork.templates()) {
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
		return templateExplorer.selectedModel();
	}

	public void setActiveTemplate(Template<TimedArcPetriNet> template) {
		drawingSurface.setModel(template.guiModel(), template.model());
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

	public void setConstants(Iterable<Constant> constants) {
		tapnNetwork.setConstants(constants);
		constantsPanel.showConstants();
	}

	public void setupNameGeneratorsFromTemplates(Iterable<Template<TimedArcPetriNet>> templates) {
		drawingSurface.setupNameGeneratorsFromTemplates(templates);
		
	}
}
