package pipe.gui.petrinet;

import dk.aau.cs.TCTL.Parsing.ParseException;
import dk.aau.cs.TCTL.*;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import net.tapaal.gui.*;
import net.tapaal.gui.petrinet.*;
import net.tapaal.gui.petrinet.model.ModelViolation;
import net.tapaal.gui.petrinet.model.Result;
import net.tapaal.gui.petrinet.smartdraw.Boundary;
import net.tapaal.gui.petrinet.smartdraw.Quadtree;
import net.tapaal.gui.petrinet.editor.TemplateExplorer;
import net.tapaal.gui.petrinet.model.GuiModelManager;
import net.tapaal.gui.swingcomponents.BugHandledJXMultisplitPane;
import net.tapaal.gui.petrinet.dialog.NameVisibilityPanel;
import net.tapaal.gui.petrinet.dialog.StatisticsPanel;
import net.tapaal.gui.petrinet.animation.TransitionFiringComponent;
import dk.aau.cs.io.*;
import dk.aau.cs.io.queries.SUMOQueryLoader;
import dk.aau.cs.io.queries.XMLQueryLoader;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Pair;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import net.tapaal.Preferences;
import net.tapaal.copypaste.CopyPastImportExport;
import net.tapaal.gui.DrawingSurfaceManager.AbstractDrawingSurfaceManager;
import net.tapaal.gui.petrinet.animation.DelayEnabledTransitionControl;

import net.tapaal.gui.petrinet.dialog.ColoredSimulationDialog;
import net.tapaal.gui.petrinet.dialog.WorkflowDialog;
import net.tapaal.gui.petrinet.editor.ConstantsPane;
import net.tapaal.gui.petrinet.editor.SharedPlacesAndTransitionsPanel;

import net.tapaal.gui.petrinet.undo.ChangeSpacingEditCommand;
import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.gui.petrinet.undo.MovePetriNetObjectCommand;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.verification.*;
import net.tapaal.gui.petrinet.widgets.QueryPane;
import net.tapaal.helpers.Reference.MutableReference;
import net.tapaal.swinghelpers.JSplitPaneFix;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Split;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.action.GuiAction;
import pipe.gui.Constants;
import pipe.gui.GuiFrame;
import pipe.gui.MessengerImpl;
import pipe.gui.TAPAALGUI;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.canvas.Grid;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.animation.AnimationControlSidePanel;
import pipe.gui.petrinet.animation.AnimationHistoryList;
import pipe.gui.petrinet.animation.AnimationHistorySidePanel;
import pipe.gui.petrinet.animation.Animator;
import pipe.gui.petrinet.graphicElements.*;
import pipe.gui.petrinet.graphicElements.tapn.*;
import pipe.gui.petrinet.undo.UndoManager;
import pipe.gui.swingcomponents.filebrowser.FileBrowser;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeListener;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.Component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

public class PetriNetTab extends JSplitPane implements TabActions {

    final AbstractDrawingSurfaceManager notingManager = new AbstractDrawingSurfaceManager(){
        @Override
        public void registerEvents() {}
    };

    private final MutableReference<GuiFrameControllerActions> guiFrameControllerActions = new MutableReference<>();

    public void setGuiFrameControllerActions(GuiFrameControllerActions guiFrameControllerActions) {
        this.guiFrameControllerActions.setReference(guiFrameControllerActions);
    }

    //Enum for all actions and types of elements
    public enum DrawTool {
        ANNOTATION,
        PLACE,
        TRANSITION,
        UNCONTROLLABLE_TRANSITION,
        ARC,
        TRANSPORT_ARC,
        INHIBITOR_ARC,
        URGENT_TRANSITION,
        URGENT_UNCONTROLLABLE_TRANSITION,
        ADD_TOKEN,
        REMOVE_TOKEN,
        SELECT,
        DRAW,
        DRAG,
    }

    public final TAPNLens lens;

	//Model and state
	private final TimedArcPetriNetNetwork tapnNetwork;

	//XXX: Replace with bi-map
	private final HashMap<TimedArcPetriNet, DataLayer> guiModels = new HashMap<>();
	public final HashMap<DataLayer, TimedArcPetriNet> guiModelToModel = new HashMap<>();

	//XXX: should be replaced iwth DataLayer->Zoomer, TimedArcPetriNet has nothing to do with zooming
	private final HashMap<TimedArcPetriNet, Zoomer> zoomLevels = new HashMap<>();

    private boolean alreadyFitToScreen;

	final UndoManager undoManager = new UndoManager(this); //warning leaking this, should be ok as it only used after construction

    private final MutableReference<GuiFrameActions> app = new MutableReference<>();
    private final MutableReference<SafeGuiFrameActions> safeApp = new MutableReference<>();

    final MutableReference<AbstractDrawingSurfaceManager> managerRef = new MutableReference<>(notingManager);
	public final GuiModelManager guiModelManager = new GuiModelManager(this);

    private final Animator animator = new Animator(this);
    private boolean netChanged = false;

    @Override
    public boolean getNetChanged() {
        return netChanged;
    }

    public void setNetChanged(boolean _netChanged) {
        netChanged = _netChanged;
    }
    private final NameGenerator nameGenerator = new NameGenerator();

    public NameGenerator getNameGenerator() {
        return nameGenerator;
    }

    /**
     * Creates a new tab with the selected filestream
	 */
    public static PetriNetTab createNewTabFromInputStream(InputStream file, String name) throws Exception {

        try {
            ModelLoader loader = new ModelLoader();
			LoadedModel loadedModel = loader.load(file);

			if (loadedModel == null) {
                throw new Exception("Could not open the selected file, as it does not have the correct format.");
			}

			if (loadedModel.getMessages().size() != 0) {
                new Thread(() -> {
                    TAPAALGUI.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    StringBuilder message = new StringBuilder("While loading the net we found one or more warnings: \n\n");
                    for (String s : loadedModel.getMessages()) {
                        message.append(s).append("\n\n");
                    }

                    new MessengerImpl().displayInfoMessage(message.toString(), "Warning");
                }).start();
            }

            PetriNetTab tab = new PetriNetTab(loadedModel.network(), loadedModel.templates(), loadedModel.queries(), loadedModel.getLens());

            checkQueries(tab);

            tab.setInitialName(name);

			tab.selectFirstElements();

			tab.setFile(null);

            return tab;
		} catch (ParseException e) {
            throw new Exception("TAPAAL encountered an error while loading the file: " + name + "\n\nPossible explanations:\n  - " + e.getMessage(), e);
        }
	}

    public static TAPNLens getFileLens(InputStream file) throws Exception {
        ModelLoader loader = new ModelLoader();
        return loader.loadLens(file);
    }

    public static void checkQueries(PetriNetTab tab) {
        List<TAPNQuery> queriesToRemove = new ArrayList<>();
        boolean gameChanged = false;

        EngineSupportOptions verifyTAPNOptions = new VerifyTAPNEngineOptions();
        EngineSupportOptions UPPAALCombiOptions = new UPPAALCombiOptions();
        EngineSupportOptions UPPAALOptimizedStandardOptions = new UPPAALOptimizedStandardOptions();
        EngineSupportOptions UPPAALStandardOptions = new UPPAALStandardOptions();
        EngineSupportOptions UPPAALBroadcastOptions = new UPPAALBroadcastOptions();
        EngineSupportOptions UPPAALBroadcastDegree2Options = new UPPAALBroadcastDegree2Options();
        EngineSupportOptions verifyDTAPNOptions = new VerifyDTAPNEngineOptions();
        EngineSupportOptions verifyPNOptions = new VerifyPNEngineOptions();
        EngineSupportOptions[] engineSupportOptions = new EngineSupportOptions[]{verifyDTAPNOptions,verifyTAPNOptions,UPPAALCombiOptions,UPPAALOptimizedStandardOptions,UPPAALStandardOptions,UPPAALBroadcastOptions,UPPAALBroadcastDegree2Options,verifyPNOptions};

        TimedArcPetriNetNetwork net = tab.network();
        for (TAPNQuery q : tab.queries()) {
            boolean smcQuery = q.getCategory() == TAPNQuery.QueryCategory.SMC;
            boolean[] queryOptions = new boolean[]{
                q.getTraceOption() == TAPNQuery.TraceOption.FASTEST,
                (q.getProperty() instanceof TCTLDeadlockNode && (q.getProperty() instanceof TCTLEFNode || q.getProperty() instanceof TCTLAGNode) && net.getHighestNetDegree() <= 2),
                (q.getProperty() instanceof TCTLDeadlockNode && (q.getProperty() instanceof TCTLEGNode || q.getProperty() instanceof TCTLAFNode)),
                (q.getProperty() instanceof TCTLDeadlockNode && net.hasInhibitorArcs()),
                net.hasWeights(),
                net.hasInhibitorArcs(),
                net.hasUrgentTransitions(),
                (q.getProperty() instanceof TCTLEGNode || q.getProperty() instanceof TCTLAFNode),
                !net.isNonStrict(),
                tab.lens.isTimed(),
                (q.getProperty() instanceof TCTLDeadlockNode && net.getHighestNetDegree() > 2),
                tab.lens.isGame(),
                (q.getProperty() instanceof TCTLEGNode || q.getProperty() instanceof TCTLAFNode) && net.getHighestNetDegree() > 2,
                q.hasUntimedOnlyProperties(),
                tab.lens.isColored(),
                tab.lens.isColored() && !tab.lens.isTimed(),
                smcQuery
            };

            boolean hasEngine = tab.checkCurrentEngine(q.getReductionOption(), queryOptions);
            if (!hasEngine) {
                for(EngineSupportOptions engine : engineSupportOptions){
                    if(engine.areOptionsSupported(queryOptions)){
                        q = tab.setQueryEngine(q, engine);
                        hasEngine = true;
                        break;
                    }
                }
            }
            if (!hasEngine) {
                queriesToRemove.add(q);
                tab.removeQuery(q);
            } else if (tab.lens.isGame()) {
                if (q.getProperty() instanceof TCTLEFNode || q.getProperty() instanceof TCTLEGNode) {
                    queriesToRemove.add(q);
                    tab.removeQuery(q);
                }
                if (q.getSearchOption().equals(TAPNQuery.SearchOption.HEURISTIC)) {
                    q.setSearchOption(TAPNQuery.SearchOption.DFS);
                    gameChanged = true;
                }
                if (q.useGCD() || q.useTimeDarts() || q.getTraceOption().equals(TAPNQuery.TraceOption.FASTEST) || q.isOverApproximationEnabled() || q.isUnderApproximationEnabled()) {
                    q.setUseGCD(false);
                    q.setUseTimeDarts(false);
                    q.setTraceOption(TAPNQuery.TraceOption.NONE);
                    q.setUseOverApproximationEnabled(false);
                    q.setUseUnderApproximationEnabled(false);
                    gameChanged = true;
                }
            } else if (!tab.lens.isTimed()) {
                q.setReductionOption(ReductionOption.VerifyPN);
                q.setUseOverApproximationEnabled(false);
                q.setUseUnderApproximationEnabled(false);
                if (q.getCategory() == TAPNQuery.QueryCategory.Default)
                q.setCategory(TAPNQuery.QueryCategory.CTL);
            } else {
                if (q.getCategory() == TAPNQuery.QueryCategory.LTL) {
                    queriesToRemove.add(q);
                    tab.removeQuery(q);
                }
            }
        }
        StringBuilder message = new StringBuilder();
        if (!queriesToRemove.isEmpty()) {
            message = new StringBuilder("The following queries will be removed in the conversion:");
            for (TAPNQuery q : queriesToRemove) {
                message.append("\n").append(q.getName());
            }
        }
        if (gameChanged) {
            message.append(message.length() == 0 ? "" : "\n\n");
            message.append("Some options may have been changed to make the query compatible with the net features.");
        }
        if(message.length() > 0){
            final String fmessage = message.toString();
            //XXX: we should not do pop-up form there! I think these check should be part of loading a net.
            new Thread(() -> {
                TAPAALGUI.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                new MessengerImpl().displayInfoMessage(fmessage, "Information");
            }).start();
        }
	}

	private boolean checkCurrentEngine(ReductionOption reductionOption, boolean[] queryOptions) {
        EngineSupportOptions engine;
        switch (reductionOption) {
            case VerifyDTAPN:
                engine = new VerifyDTAPNEngineOptions(); 
                break;
            case VerifyPN:
                engine = new VerifyPNEngineOptions();
                break;
            case VerifyTAPN:
                engine = new VerifyTAPNEngineOptions();
                break;
            case BROADCAST:
                engine = new UPPAALBroadcastOptions();
                break;
            case DEGREE2BROADCAST:
                engine = new UPPAALBroadcastDegree2Options();
                break;
            case COMBI:
                engine = new UPPAALCombiOptions();
                break;
            case STANDARD:
                engine = new UPPAALStandardOptions();
                break;
            case OPTIMIZEDSTANDARD:
                engine = new UPPAALOptimizedStandardOptions();
                break;
            default:
                return false;
        }
        return engine.areOptionsSupported(queryOptions);
    }

    private TAPNQuery setQueryEngine(TAPNQuery query, EngineSupportOptions engine) {
	    if (engine instanceof VerifyDTAPNEngineOptions) {
	        query.setReductionOption(ReductionOption.VerifyDTAPN);
        } else if (engine instanceof VerifyPNEngineOptions) {
            query.setReductionOption(ReductionOption.VerifyPN);
        } else if (engine instanceof VerifyTAPNEngineOptions) {
            query.setReductionOption(ReductionOption.VerifyTAPN);
        } else if (engine instanceof UPPAALBroadcastDegree2Options) {
            query.setReductionOption(ReductionOption.DEGREE2BROADCAST);
        } else if (engine instanceof UPPAALBroadcastOptions) {
            query.setReductionOption(ReductionOption.BROADCAST);
        } else if (engine instanceof UPPAALCombiOptions) {
            query.setReductionOption(ReductionOption.COMBI);
        } else if (engine instanceof UPPAALOptimizedStandardOptions) {
            query.setReductionOption(ReductionOption.OPTIMIZEDSTANDARD);
        } else if (engine instanceof UPPAALStandardOptions) {
            query.setReductionOption(ReductionOption.STANDARD);
        }
	    return query;
    }

	public static PetriNetTab createNewEmptyTab(String name, boolean isTimed, boolean isGame, boolean isColored, boolean isStochastic){
        PetriNetTab tab = new PetriNetTab(isTimed, isGame, isColored, isStochastic);
		tab.setInitialName(name);

		//Set Default Template
		String templateName = tab.getNameGenerator().getNewTemplateName();
		Template template = new Template(new TimedArcPetriNet(templateName), new DataLayer(), new Zoomer());
		tab.addTemplate(template);

		return tab;
	}

	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 */

	public static PetriNetTab createNewTabFromPNMLFile(File file) throws Exception {

		if (file != null) {
			try {

				LoadedModel loadedModel;

				PNMLoader loader = new PNMLoader();
				loadedModel = loader.load(file);
                if (loadedModel == null) return null;

                PetriNetTab tab = new PetriNetTab(loadedModel.network(), loadedModel.templates(), loadedModel.queries(), loadedModel.getLens());

                String name = file.getName().replaceAll(".pnml", ".tapn");
                tab.setInitialName(name);

				tab.selectFirstElements();

				tab.setMode(DrawTool.SELECT);
                return tab;

			} catch (Exception e) {
				throw new Exception("TAPAAL encountered an error while loading the file: " + file.getName() + "\n\nPossible explanations:\n  - " + e);
			}
		}
		return null;
	}

	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 */
	//XXX should properly be in controller?
	public static PetriNetTab createNewTabFromFile(File file) throws Exception {
		try {
			String name = file.getName();
			boolean showFileEndingChangedMessage = false;

			if (name.toLowerCase().endsWith(".xml")) {
				name = name.substring(0, name.lastIndexOf('.')) + ".tapn";
				showFileEndingChangedMessage = true;
			}

			InputStream stream = new FileInputStream(file);
			PetriNetTab tab = createNewTabFromInputStream(stream, name);
			if (!showFileEndingChangedMessage) tab.setFile(file);

			showFileEndingChangedMessage(showFileEndingChangedMessage);

			return tab;
		}catch (FileNotFoundException e) {
			throw new FileNotFoundException("TAPAAL encountered an error while loading the file: " + file.getName() + "\n\nFile not found:\n  - " + e);
		}
	}

	private static void showFileEndingChangedMessage(boolean showMessage) {
		if(showMessage) {
			//We thread this so it does not block the EDT
			new Thread(() -> {
                TAPAALGUI.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                new MessengerImpl().displayInfoMessage("We have changed the ending of TAPAAL files from .xml to .tapn and the opened file was automatically renamed to end with .tapn.\n"
                        + "Once you save the .tapn model, we recommend that you manually delete the .xml file.", "FILE CHANGED");
            }).start();
		}
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

    final AbstractDrawingSurfaceManager animationModeController;

	//GUI
	private final HashMap<TimedArcPetriNet, Boolean> hasPositionalInfos = new HashMap<>();

	private final JScrollPane drawingSurfaceScroller;
	private JScrollPane editorSplitPaneScroller;
	private JScrollPane animatorSplitPaneScroller;
	private final DrawingSurfaceImpl drawingSurface;
	private File appFile;
	private final JPanel drawingSurfaceDummy;

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
	private AnimationControlSidePanel animControllerBox;

    private AnimationHistorySidePanel animationHistorySidePanel;

	private AnimationHistoryList abstractAnimationPane = null;
	private JPanel animationControlsPanel;
	private TransitionFiringComponent transitionFiring;

	private static final String transitionFiringName = "enabledTransitions";
	private static final String animControlName = "animControl";

	private JSplitPane animationHistorySplitter;

	private BugHandledJXMultisplitPane animatorSplitPane;

	private Integer selectedTemplate = 0;
	private Boolean selectedTemplateWasActive = false;

	private WorkflowDialog workflowDialog = null;

    private Boolean showNamesOption = null;
    private Boolean isSelectedComponentOption = null;
    private Boolean isPlaceOption = null;
    private Boolean isTransitionOption = null;

    private PetriNetTab(boolean isTimed, boolean isGame, boolean isColored, boolean isStochastic) {
	    this(new TimedArcPetriNetNetwork(), new ArrayList<>(), new TAPNLens(isTimed, isGame, isColored, isStochastic));
    }

	private PetriNetTab(TimedArcPetriNetNetwork network, Collection<Template> templates, TAPNLens lens) {
        Require.that(network != null, "network cannot be null");
        Require.notNull(lens, "Lens can't be null");

        tapnNetwork = network;
        this.lens = lens;

        guiModels.clear();
        for (Template template : templates) {
            TimedArcPetriNet net = template.model();
            DataLayer guiModel = template.guiModel();

            guiModels.put(net, guiModel);
            guiModelToModel.put(guiModel, net);
            zoomLevels.put(template.model(), template.zoomer());
            hasPositionalInfos.put(template.model(), template.getHasPositionalInfo());

            for(PetriNetObject o : template.guiModel().getPetriNetObjects()){
                o.setLens(this.lens);
            }
        }

        drawingSurface = new DrawingSurfaceImpl(new DataLayer(), this, managerRef);
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
                TAPAALGUI.getAppGui().requestFocus();
            }
        });

        drawingSurfaceDummy = new JPanel(new GridBagLayout());
        GridBagConstraints gc=new GridBagConstraints();
        gc.fill=GridBagConstraints.HORIZONTAL;
        gc.gridx=0;
        gc.gridy=0;
        drawingSurfaceDummy.add(new JLabel("The net is too big to be drawn"), gc);

        createEditorLeftPane();
        createAnimatorSplitPane();

        this.setOrientation(HORIZONTAL_SPLIT);
        this.setLeftComponent(editorSplitPaneScroller);
        this.setRightComponent(drawingSurfaceScroller);

        this.setContinuousLayout(true);
        this.setOneTouchExpandable(true);
        this.setBorder(null); // avoid multiple borders
        this.setDividerSize(8);
        //XXX must be after the animationcontroller is created
        animationModeController = new CanvasAnimationController(getAnimator());
    }

    public PetriNetTab(TimedArcPetriNetNetwork network, Collection<Template> templates, Iterable<TAPNQuery> tapnqueries, TAPNLens lens) {
        this(network, templates, lens);

        sharedPTPanel.setNetwork(network);
        templateExplorer.updateTemplateList();

        constantsPanel.setNetwork(tapnNetwork);

        if (network.paintNet()) {
            this.setRightComponent(drawingSurfaceScroller);
        } else {
            this.setRightComponent(drawingSurfaceDummy);
        }
        this.queries.setQueries(tapnqueries);
    }

	public SharedPlacesAndTransitionsPanel getSharedPlacesAndTransitionsPanel(){
		return sharedPTPanel;
	}

	public TemplateExplorer getTemplateExplorer(){
		return templateExplorer;
	}

    public void selectTemplate(Template template) {
        templateExplorer.selectTemplate(template);
    }

	public void createEditorLeftPane() {
		constantsPanel = new ConstantsPane(this);
		constantsPanel.setPreferredSize(
		    new Dimension(constantsPanel.getPreferredSize().width, constantsPanel.getMinimumSize().height)
        );

		queries = new QueryPane(this);
		queries.setPreferredSize(
            new Dimension(queries.getPreferredSize().width, queries.getMinimumSize().height)
		);

		templateExplorer = new TemplateExplorer(this);
		templateExplorer.setPreferredSize(
            new Dimension(templateExplorer.getPreferredSize().width, templateExplorer.getMinimumSize().height)
		);

		sharedPTPanel = new SharedPlacesAndTransitionsPanel(this);
		sharedPTPanel.setPreferredSize(
            new Dimension(sharedPTPanel.getPreferredSize().width, sharedPTPanel.getMinimumSize().height)
		);

        boolean floatingDividers = false;
		if (editorModelroot == null) {
			Leaf constantsLeaf = new Leaf(constantsName);
			Leaf queriesLeaf = new Leaf(queriesName);
			Leaf templateExplorerLeaf = new Leaf(templateExplorerName);
			Leaf sharedPTLeaf = new Leaf(sharedPTName);

			constantsLeaf.setWeight(0.25);
			queriesLeaf.setWeight(0.25);
			templateExplorerLeaf.setWeight(0.25);
			sharedPTLeaf.setWeight(0.25);

			editorModelroot = new Split(
                templateExplorerLeaf,
                new Divider(),
                sharedPTLeaf,
                new Divider(),
                queriesLeaf,
                new Divider(),
                constantsLeaf
			);
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
				panel.getPreferredSize().width,
				panel.getMinimumSize().height
		));
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

    private int newNameCounter = 1;


    //XXX this is a temp solution while refactoring
	// to keep the name of the net when the when a file is not set.
	String initialName = "";
	public void setInitialName(String name) {
		if (name == null || name.isEmpty()) {
			name = "New Petri net " + (newNameCounter++) + ".tapn";
		} else if (!name.toLowerCase().endsWith(".tapn")){
			name = name + ".tapn";
		}
		this.initialName = name;

		safeApp.ifPresent(tab -> tab.updatedTabName(this));
	}

    @Override
	public String getTabTitle() {
		if (getFile()!=null) {
			return getFile().getName();
		} else {
			return initialName;
		}
	}

	@Override
	public File getFile() {
		return appFile;
	}

	public void setFile(File file) {
		appFile = file;
		safeApp.ifPresent(tab -> tab.updatedTabName(this));
	}

    private void createAnimatorSplitPane() {

        animationHistorySidePanel = new AnimationHistorySidePanel(animator);

        if (animControllerBox == null) {
            animControllerBox = new AnimationControlSidePanel(animator, lens);

        }
		if (transitionFiring == null) {
            transitionFiring = new TransitionFiringComponent(TAPAALGUI.getAppGui().isShowingDelayEnabledTransitions(), lens, animator);
        }

		boolean floatingDividers = false;
		if(simulatorModelRoot == null){
			Leaf templateExplorerLeaf = new Leaf(templateExplorerName);
			Leaf enabledTransitionsListLeaf = new Leaf(transitionFiringName);
			Leaf animControlLeaf = new Leaf(animControlName);

			templateExplorerLeaf.setWeight(0.25);
			enabledTransitionsListLeaf.setWeight(0.25);
			animControlLeaf.setWeight(0.5);

			simulatorModelRoot = new Split(
			    templateExplorerLeaf,
                new Divider(),
                enabledTransitionsListLeaf,
                new Divider(),
                animControlLeaf
            );
			simulatorModelRoot.setRowLayout(false);
			floatingDividers = true;
		}
		animatorSplitPane = new BugHandledJXMultisplitPane();
		animatorSplitPane.getMultiSplitLayout().setFloatingDividers(floatingDividers);
        animatorSplitPane.getMultiSplitLayout().setLayoutByWeight(false);

		animatorSplitPane.setSize(simulatorModelRoot.getBounds().width, simulatorModelRoot.getBounds().height);

		animatorSplitPane.getMultiSplitLayout().setModel(simulatorModelRoot);

		animationControlsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.2;
		animationControlsPanel.add(animControllerBox, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		animationControlsPanel.add(animationHistorySidePanel, gbc);

		animationControlsPanel.setPreferredSize(
		    new Dimension(
				animationControlsPanel.getPreferredSize().width,
				animationControlsPanel.getMinimumSize().height
            )
        );
		transitionFiring.setPreferredSize(
		    new Dimension(
				transitionFiring.getPreferredSize().width,
				transitionFiring.getMinimumSize().height
            )
        );

        animatorSplitPane.add(new JPanel(), templateExplorerName);

		animatorSplitPane.add(animationControlsPanel, animControlName);
		animatorSplitPane.add(transitionFiring, transitionFiringName);

		animatorSplitPaneScroller = createLeftScrollPane(animatorSplitPane);
		animatorSplitPane.repaint();
	}

	public void switchToAnimationComponents() {

		//Remove dummy
		Component dummy = animatorSplitPane.getMultiSplitLayout().getComponentForNode(animatorSplitPane.getMultiSplitLayout().getNodeForName(templateExplorerName));
		if(dummy != null){
			animatorSplitPane.remove(dummy);
		}

		//Add the templateExplorer
        var t = new TemplateExplorer(this);
        t.switchToAnimationMode();
		animatorSplitPane.add(t, templateExplorerName);

		this.setLeftComponent(animatorSplitPaneScroller);
	}

    public AnimationHistoryList getUntimedAnimationHistory() {
		return abstractAnimationPane;
	}

	public AnimationControlSidePanel getAnimationController() {
		return animControllerBox;
	}

	public DelayEnabledTransitionControl getDelayEnabledTransitionControl(){
		return transitionFiring.getDelayEnabledTransitionControl();
	}

	public void addAbstractAnimationPane() {
		animationControlsPanel.remove(animationHistorySidePanel);
		abstractAnimationPane = new AnimationHistoryList();

		JScrollPane untimedAnimationHistoryScrollPane = new JScrollPane(abstractAnimationPane);
		untimedAnimationHistoryScrollPane.setBorder(
		    BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Untimed Trace"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)
            )
        );
		animationHistorySplitter = new JSplitPaneFix(
		    JSplitPane.HORIZONTAL_SPLIT,
            animationHistorySidePanel,
            untimedAnimationHistoryScrollPane
        );

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
		animationControlsPanel.add(animationHistorySidePanel, gbc);
		animatorSplitPane.validate();
	}

    public AnimationHistoryList getAnimationHistorySidePanel() {
		return animationHistorySidePanel.getAnimationHistoryList();
	}

    public TransitionFiringComponent getTransitionFiringComponent() {
		return transitionFiring;
	}

    public TimedArcPetriNetNetwork network() {
		return tapnNetwork;
	}

	public DrawingSurfaceImpl drawingSurface() {
		return drawingSurface;
	}

	public Iterable<Template> allTemplates() {
		ArrayList<Template> list = new ArrayList<>();
		for (TimedArcPetriNet net : tapnNetwork.allTemplates()) {
			Template template = new Template(net, guiModels.get(net), zoomLevels.get(net));
			template.setHasPositionalInfo(hasPositionalInfos.get(net));
			list.add(template);
		}
		return list;
	}

	public Iterable<Template> activeTemplates() {
		ArrayList<Template> list = new ArrayList<>();
		for (TimedArcPetriNet net : tapnNetwork.activeTemplates()) {
			Template template = new Template(net, guiModels.get(net), zoomLevels.get(net));
			template.setHasPositionalInfo(hasPositionalInfos.get(net));
			list.add(template);
		}
		return list;
	}

	public int numberOfActiveTemplates() {
		int count = 0;
		for (TimedArcPetriNet net : tapnNetwork.activeTemplates()) {
			if (net.isActive()) {
                count++;
            }
		}
		return count;
	}

	public void addTemplate(Template template) {
		tapnNetwork.add(template.model());
		guiModels.put(template.model(), template.guiModel());
        guiModelToModel.put(template.guiModel(), template.model());
		zoomLevels.put(template.model(), template.zoomer());
		hasPositionalInfos.put(template.model(), template.getHasPositionalInfo());
		templateExplorer.updateTemplateList();
	}

    public void removeTemplate(Template template) {
		tapnNetwork.remove(template.model());
		guiModels.remove(template.model());
		guiModelToModel.remove(template.guiModel());
		zoomLevels.remove(template.model());
		hasPositionalInfos.remove(template.model());
		templateExplorer.updateTemplateList();
	}

	public Template currentTemplate() {
		return templateExplorer.selectedModel();
	}

	public Iterable<TAPNQuery> queries() {
		return queries.getQueries();
	}

    public QueryPane getQueryPane() {
        return queries;
    }

    public void removeQuery(TAPNQuery queryToRemove) {
		queries.removeQuery(queryToRemove);
	}

	public void addQuery(TAPNQuery query) {
		queries.addQuery(query);
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

    @Override
	public void showComponents(boolean enable) {
		if (enable != templateExplorer.isVisible()) {

			editorSplitPane.getMultiSplitLayout().displayNode(templateExplorerName, enable);

			if (animatorSplitPane != null) {
				animatorSplitPane.getMultiSplitLayout().displayNode(templateExplorerName, enable);
			}
			makeSureEditorPanelIsVisible(templateExplorer);
		}
	}

    @Override
	public void showSharedPT(boolean enable) {
	    if (enable != sharedPTPanel.isVisible()) {
            editorSplitPane.getMultiSplitLayout().displayNode(sharedPTName, enable);
            makeSureEditorPanelIsVisible(sharedPTPanel);
        }
    }

    @Override
	public void showQueries(boolean enable) {
		if (enable != queries.isVisible()) {
			editorSplitPane.getMultiSplitLayout().displayNode(queriesName, enable);
			makeSureEditorPanelIsVisible(queries);
			this.repaint();
		}
	}

	//XXX not sure about this
    @Override
    public void repaintAll() {
		drawingSurface().repaintAll();
    }

    public void showConstantsPanel(boolean enable) {
		if (enable != constantsPanel.isVisible()) {
			editorSplitPane.getMultiSplitLayout().displayNode(constantsName, enable);
			makeSureEditorPanelIsVisible(constantsPanel);
		}
	}

    @Override
	public void showEnabledTransitionsList(boolean enable) {
	    //displayNode fires and relayout, so we check of value is changed
        // else elements will be set to default size.
		if (transitionFiring.isVisible() != enable) {
			animatorSplitPane.getMultiSplitLayout().displayNode(transitionFiringName, enable);
		}
	}

    @Override
	public void showDelayEnabledTransitions(boolean enable){
		transitionFiring.showDelayEnabledTransitions(enable);
		drawingSurface.repaint();

        if (getAnimator() != null && animationmode) {
		    getAnimator().updateFireableTransitions();
        }
	}

	public void selectFirstElements() {
		templateExplorer.selectFirst();
		queries.selectFirst();
		constantsPanel.selectFirst();
	}

	public boolean isQueryPossible() {
		return queries.isQueryPossible();
	}

	@Override
	public void verifySelectedQuery() {
		queries.verifySelectedQuery();
	}

	@Override
	public void previousComponent() {
		getTemplateExplorer().selectPrevious();
	}

	@Override
	public void nextComponent() {
		getTemplateExplorer().selectNext();
	}

	@Override
	public void exportTrace() {
		TraceImportExport.exportTrace(this);
	}

	@Override
	public void importTrace() {
		TraceImportExport.importTrace(animator, this);
	}

	@Override
	public void zoomTo(int newZoomLevel) {
		boolean didZoom = drawingSurface().getZoomController().setZoom(newZoomLevel);
		if (didZoom) {
            app.ifPresent(gfa -> {
                JSlider zoomSlider = gfa.getZoomSlider();

                // Remove change listeners to avoid recursive calls
                ChangeListener[] listeners = zoomSlider.getChangeListeners();
                for (ChangeListener listener : listeners) {
                    zoomSlider.removeChangeListener(listener);
                }

                zoomSlider.setValue(newZoomLevel);

                for (ChangeListener listener : listeners) {
                    zoomSlider.addChangeListener(listener);
                }
            });

			drawingSurface().zoomToMidPoint(); //Do Zoom
		}
	}

    @Override
    public void search(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }
        
        List<Pair<?, String>> searchableItems = new ArrayList<>();
        for (Template template : allTemplates()) {
            TimedArcPetriNet model = template.model();
            for (TimedPlace place : model.places()) {
                searchableItems.add(new Pair<>(place, template.toString()));
            }

            for (TimedTransition transition : model.transitions()) {
                searchableItems.add(new Pair<>(transition, template.toString()));
            }
        }

        Searcher<Pair<?, String>> searcher = new Searcher<>(searchableItems, obj -> {
            Object element = obj.getFirst();            
            String name = element.toString();
            if (name.contains(".")) {
                name = name.split("\\.")[1];
            }

            return name;
        });
    
        var matches = searcher.findAllMatches(query);
        app.ifPresent(gfa -> {
            SearchBar searchBar = gfa.getSearchBar();
            if (searchBar != null) {
                searchBar.showResults(matches);
            }
        });
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

    @Override
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

    private void createNewAndConvertUntimed() {
	    PetriNetTab tab = duplicateTab(new TAPNLens(false, lens.isGame(), lens.isColored(), lens.isStochastic()), "-untimed");
        convertToUntimedTab(tab);
        guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
    }

    private void createNewAndConvertNonGame() {
        PetriNetTab tab = duplicateTab(new TAPNLens(lens.isTimed(), false, lens.isColored(), lens.isStochastic()), "-nongame");
        TabTransformer.removeGameInformation(tab);
        guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
    }

    private void createNewAndConvertNonColor(){
        PetriNetTab tab = duplicateTab(new TAPNLens(lens.isTimed(), lens.isGame(), false, lens.isStochastic()), "-noncolored");
        TabTransformer.removeColorInformation(tab);
        guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
    }

    private void createNewAndConvertColor(){
        PetriNetTab tab = duplicateTab(new TAPNLens(lens.isTimed(), lens.isGame(), true, lens.isStochastic()), "-colored");
        TabTransformer.addColorInformation(tab);
        guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
    }

    private void createNewAndConvertNonStochastic(){
        PetriNetTab tab = duplicateTab(new TAPNLens(lens.isTimed(), lens.isGame(), lens.isColored(), false), "-nonstochastic");
        TabTransformer.removeDistributionInformation(tab);
        TabTransformer.convertQueriesToOrFromSmc(tab.queries());
        guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
    }

    public void createNewAndUnfoldColor(boolean partition, boolean computeColorFixpoint, boolean useSymmetricVars){
        TabTransformer.unfoldTab(this, partition, computeColorFixpoint, useSymmetricVars);
    }

    @Override
    public void changeTimeFeature(boolean isTime) {
        if (isTime != lens.isTimed()) {
            if (!isTime){
                if (!network().isUntimed()){
                    String removeTimeWarning = "The net contains time information, which will be removed. Do you still wish to make the net untimed?";
                    int choice = JOptionPane.showOptionDialog(TAPAALGUI.getApp(), removeTimeWarning, "Remove time information",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 0);
                    if (choice == 0) {
                        createNewAndConvertUntimed();
                    }
                } else {
                    createNewAndConvertUntimed();
                }
            } else {
                PetriNetTab tab = duplicateTab(new TAPNLens(true, lens.isGame(), lens.isColored(), lens.isStochastic()), "-timed");
                guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
            }
            updateFeatureText();
        }
    }

    @Override
    public void changeGameFeature(boolean isGame) {
        if (isGame != lens.isGame()) {
            if (!isGame){
                if (network().hasUncontrollableTransitions()){
                    String removeTimeWarning = "The net contains game information, which will be removed. Do you still wish to make to remove the game semantics?";
                    int choice = JOptionPane.showOptionDialog(TAPAALGUI.getApp(), removeTimeWarning, "Remove game information",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 0);
                    if (choice == 0) {
                        createNewAndConvertNonGame();
                    }
                } else {
                    createNewAndConvertNonGame();
                }
            } else {
                PetriNetTab tab = duplicateTab(new TAPNLens(lens.isTimed(), true, lens.isColored(), lens.isStochastic()), "-game");
                guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
            }
            updateFeatureText();
        }
    }

    @Override
    public void changeColorFeature(boolean isColor) {
        if (isColor != lens.isColored()) {
            if (!isColor){
                String removeTimeWarning = "The net contains color information, which will be removed. Do you still wish to make to remove the color semantics?";
                int choice = JOptionPane.showOptionDialog(TAPAALGUI.getApp(), removeTimeWarning, "Remove color information",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 0);
                if (choice == 0) {
                    createNewAndConvertNonColor();
                }
            } else {
                createNewAndConvertColor();
            }
            updateFeatureText();
        }
    }

    @Override
    public void changeStochasticFeature(boolean isStochastic) {
        if(isStochastic != lens.isStochastic()) {
            if (!isStochastic) {
                String removeStochasticWarning = "The net contains distribution informations, which will be removed. Do you still wish to make to remove the stochastic semantics ?";
                int choice = JOptionPane.showOptionDialog(TAPAALGUI.getApp(), removeStochasticWarning, "Remove stochastic information",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 0);
                if (choice == 0) {
                    createNewAndConvertNonStochastic();
                }
            } else {
                PetriNetTab tab = duplicateTab(new TAPNLens(lens.isTimed(), lens.isGame(), lens.isColored(), true), "-stochastic");
                TabTransformer.convertQueriesToOrFromSmc(tab.queries());
                guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
            }
            updateFeatureText();
        }
    }

    @Override
    public Map<PetriNetObject, Boolean> showNames(boolean showNames, boolean placeNames, boolean selectedComponent) {
        Map<PetriNetObject, Boolean> map = new HashMap<>();
        List<PetriNetObject> components = new ArrayList<>();

	    if (selectedComponent) {
	        Template template = currentTemplate();
	        template.guiModel().getPetriNetObjects().forEach(components::add);
        } else {
            Iterable<Template> templates = allTemplates();
            for (Template template : templates) {
                template.guiModel().getPetriNetObjects().forEach(components::add);
            }
        }

        for (Component component : components) {
            if (placeNames && component instanceof TimedPlaceComponent) {
                TimedPlaceComponent place = (TimedPlaceComponent) component;
                map.put(place, place.getAttributesVisible());
                place.setAttributesVisible(showNames);
                place.update(true);
                repaint();
            } else if (!placeNames && component instanceof TimedTransitionComponent) {
                TimedTransitionComponent transition = (TimedTransitionComponent) component;
                map.put(transition, transition.getAttributesVisible());
                transition.setAttributesVisible(showNames);
                transition.update(true);
                repaint();
            }
        }
        return map;
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

    public void changeToTemplate(Template tapn) {
		Require.notNull(tapn, "Can't change to a Template that is null");

        nameGenerator.add(tapn.model());
        drawingSurface.setModel(tapn.guiModel(), tapn.zoomer());

        app.ifPresent(gfa -> gfa.updateZoomSlider(tapn.zoomer().getPercent()));

        //XXX: moved from drawingsurface, temp while refactoring, there is a better way
        drawingSurface.getSelectionObject().clearSelection();

    }


    //Animation mode stuff, moved from view
	//XXX: kyrke -2019-07-06, temp solution while refactoring there is properly a better place
	private boolean animationmode = false;
	public void setAnimationMode(boolean on) {
	    if (animationmode != on) {
	        toggleAnimationMode();
        }
    }

    @Override
    public void toggleAnimationMode() {
        toggleAnimationMode(false);
    }

	@Override
	public void toggleAnimationMode(boolean explicit) {
		if (!animationmode) {
			if (numberOfActiveTemplates() > 0) {
				app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.animation));
                switchToAnimationComponents();

				setManager(animationModeController);

				drawingSurface().repaintAll();

				rememberSelectedTemplate();
				if (currentTemplate().isActive()){
					setSelectedTemplateWasActive();
				}

				getAnimator().resetTraceBox();

				getAnimator().reset(false);
				if(animControllerBox != null) {
                    animControllerBox.resetPlacementOfAnimationToolBar();
                }

				getAnimator().storeModel();
                getAnimator().updateFireableTransitions();
                getAnimator().reportBlockingPlaces();
				getAnimator().setFiringmode("Random");

                if (explicit) {
                    getAnimator().initializeInteractiveEngine();
                }

				// Set a light blue backgound color for animation mode
				drawingSurface().setBackground(Constants.ANIMATION_BACKGROUND_COLOR);
				getAnimationController().requestFocusInWindow();

				if (templateWasActiveBeforeSimulationMode()) {
					restoreSelectedTemplate();
					resetSelectedTemplateWasActive();
				}
				else {
					selectFirstActiveTemplate();
				}
				drawingSurface().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				animationmode = true; //XXX: Must be called after setGuiMode as guiMode uses last state,
                app.ifPresent(o->o.setStatusBarText(textforAnimation));

                animator.updateAnimationButtonsEnabled(); //Update stepBack/Forward
            } else {
				JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
						"You need at least one active template to enter simulation mode",
						"Simulation Mode Error", JOptionPane.ERROR_MESSAGE);
				animationmode = false;
                app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.draw));
			}
		} else {
			drawingSurface().getSelectionObject().clearSelection();
            app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.draw));

            if (isInAnimationMode()) {
                getAnimator().restoreModel();
            }

            this.setLeftComponent(editorSplitPaneScroller);

            setManager(notingManager);

			drawingSurface().setBackground(Constants.ELEMENT_FILL_COLOUR);
			setMode(DrawTool.SELECT);

			restoreSelectedTemplate();

			// Undo/Redo is enabled based on undo/redo manager
			getUndoManager().setUndoRedoStatus();
			animationmode = false;
            app.ifPresent(o->o.setStatusBarText(textforDrawing));

            if (restoreWorkflowDialog()) {
                WorkflowDialog.showDialog(this);
            }
        }
	}

    //XXX temp while refactoring, kyrke - 2019-07-25
    @Override
    public void setMode(DrawTool mode) {
        changeStatusbarText(mode);

		//Disable selection and deselect current selection
		drawingSurface().getSelectionObject().clearSelection();
        updateMode(mode);

        switch (mode) {
            case ADD_TOKEN:
                setManager(new AbstractDrawingSurfaceManager() {
                    @Override
                    public void registerEvents() {
                        registerEvent(
                            e -> e.pno instanceof TimedPlaceComponent && e.a == MouseAction.pressed && !lens.isColored(),
                            e -> guiModelManager.addToken(getModel(), (TimedPlaceComponent) e.pno, 1)
                        );
                        registerEvent(
                            e -> e.pno instanceof TimedPlaceComponent && e.a == MouseAction.pressed && lens.isColored(),
                            e -> ((TimedPlaceComponent) e.pno).showEditor()
                        );
                    }
                });
                break;
            case REMOVE_TOKEN:
                setManager(new AbstractDrawingSurfaceManager() {
                    @Override
                    public void registerEvents() {
                        registerEvent(
                            e -> e.pno instanceof TimedPlaceComponent && e.a == MouseAction.pressed && !lens.isColored(),
                            e -> guiModelManager.removeToken(getModel(), (TimedPlaceComponent) e.pno, 1)
                        );
                        registerEvent(
                            e -> e.pno instanceof TimedPlaceComponent && e.a == MouseAction.pressed && lens.isColored(),
                            e -> ((TimedPlaceComponent) e.pno).showEditor()
                        );
                    }
                });
                break;
            case PLACE:
                setManager(new CanvasPlaceDrawController());
                break;
            case TRANSITION:
                setManager(new CanvasTransitionDrawController());
                break;
            case URGENT_TRANSITION:
                setManager(new CanvasUrgentTransitionDrawController());
                break;
            case UNCONTROLLABLE_TRANSITION:
                setManager(new CanvasUncontrollableTransitionDrawController());
                break;
            case URGENT_UNCONTROLLABLE_TRANSITION:
                setManager(new CanvasUncontrollableUrgentTransitionDrawController());
                break;
            case ANNOTATION:
                setManager(new CanvasAnnotationNoteDrawController());
                break;
            case ARC:
                setManager(new CanvasArcDrawController());
                break;
            case INHIBITOR_ARC:
                setManager(new CanvasInhibitorarcDrawController());
                break;
            case TRANSPORT_ARC:
                setManager(new CanvasTransportarcDrawController());
                break;
            case SELECT:
                setManager(new CanvasGeneralDrawController(lens));
                break;
            default:
                setManager(notingManager);
                break;
        }

		if (mode == DrawTool.SELECT) {
			drawingSurface().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if (mode == DrawTool.DRAG) {
			drawingSurface().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		} else {
			drawingSurface().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
	}

    @Override
    public boolean searchBarHasFocus() {
        if (app == null || app.get() == null) {
            return false;
        }

        return app.get().getSearchBar().isFocusOwner();
    }

	@Override
	public void showStatistics() {
        StatisticsPanel.showStatisticsPanel(currentTemplate().model().getStatistics(), this);
	}

    @Override
    public void showChangeNameVisibility() {
	    NameVisibilityPanel panel = new NameVisibilityPanel(this);
	    if (showNamesOption != null && isSelectedComponentOption != null && isPlaceOption != null && isTransitionOption != null) {
            panel.showNameVisibilityPanel(showNamesOption, isPlaceOption, isTransitionOption, isSelectedComponentOption);
        } else {
            panel.showNameVisibilityPanel();
        }

        showNamesOption = panel.isShowNamesOption();
        isPlaceOption = panel.isPlaceOption();
        isTransitionOption = panel.isTransitionOption();
        isSelectedComponentOption = panel.isSelectedComponentOption();
    }

    @Override
    public void showColorTypesVariables() {
        StringBuilder buffer = new StringBuilder();
        Context context = new Context(TAPAALGUI.getCurrentTab());

        List<ColorType> listColorTypes = context.network().colorTypes();
        List<Variable> variableList = context.network().variables();
        List<Constant> constantsList = new ArrayList<>(context.network().constants());

        getColorTypesFormattedString(listColorTypes, buffer);

        if(!variableList.isEmpty()) {
            buffer.append("<br>");
        }

        getVariablesFormattedString(variableList, buffer);

        if(!constantsList.isEmpty()) {
            buffer.append("<br><br>");
        }

        getConstantsFormattedString(constantsList, buffer);

        JLabel label = new JLabel("<html>" + buffer + "</html>");
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, label.getFont().getSize()));

        JOptionPane.showMessageDialog(null, label, "Global color types/variables", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getColorTypesFormattedString(List<ColorType> listColorTypes, StringBuilder buffer) {
        String stringColorList = "";
        for(int i = 0; i < listColorTypes.size(); i++) {
            if(i == 0) {
                buffer.append("Color Types:<br>");
            }
            buffer.append(listColorTypes.get(i).getName() + " <b>is</b> ");

            if(listColorTypes.get(i).isProductColorType()) {
                buffer.append("&lt;");
                for(int x = 0; x < listColorTypes.get(i).getProductColorTypes().size(); x++) {
                    stringColorList += listColorTypes.get(i).getProductColorTypes().get(x).getName();

                    if(x != listColorTypes.get(i).getProductColorTypes().size() - 1){
                        stringColorList += ", ";
                    }
                }
                buffer.append(stringColorList + "&gt;<br>");
                stringColorList = "";

            } else if(listColorTypes.get(i).isIntegerRange()) {
                if(listColorTypes.get(i).size() > 1) {
                    int listSize = listColorTypes.get(i).size();
                    buffer.append("[" + listColorTypes.get(i).getColors().get(0).getColorName() + "," + listColorTypes.get(i).getColors().get(listSize - 1).getColorName() + "]");
                } else {
                    buffer.append("[" + listColorTypes.get(i).getFirstColor().getColorName() + "]");
                }
                buffer.append("<br>");

            } else {
                buffer.append("[");
                for(int x = 0; x < listColorTypes.get(i).getColors().size(); x++) {
                    stringColorList += listColorTypes.get(i).getColors().get(x).getName();

                    if(x != listColorTypes.get(i).getColors().size() - 1){
                        stringColorList += ", ";
                    }
                }
                buffer.append(stringColorList + "]<br>");
                stringColorList = "";
            }
        }

        return buffer.toString();
    }

    private String getVariablesFormattedString(List<Variable> variableList, StringBuilder buffer) {
        for(int i = 0; i < variableList.size(); i++) {
            if (i == 0) {
                buffer.append("Variables:<br>");
            }
            buffer.append(variableList.get(i).getName() + " <b>in</b> " + variableList.get(i).getColorType().getName());
            if(i != variableList.size() - 1) {
                buffer.append("<br>");
            }
        }
        return buffer.toString();
    }

    private String getConstantsFormattedString(List<Constant> constantsList, StringBuilder buffer) {
        for (int i = 0; i < constantsList.size(); i++) {
            if (i == 0) {
                buffer.append("Constants:<br>");
            }
            buffer.append(constantsList.get(i).toString());
            if (i != constantsList.size() - 1) {
                buffer.append("<br>");
            }
        }
        return buffer.toString();
    }

    public void alignToGrid() {
        List<PetriNetObject> petriNetObjects = drawingSurface.getGuiModel().getPlaceTransitionObjects();
        undoManager.newEdit();

        Quadtree quadtree = new Quadtree();
        final int minimumDistance = 45;
        for (PetriNetObject object : petriNetObjects) {
            int x = Grid.align(object.getPositionX(), drawingSurface.getZoom());
            int y = Grid.align(object.getPositionY(), drawingSurface.getZoom());
            Point point = new Point(x, y);

            if (quadtree.containsWithin(point, minimumDistance)) return;
            quadtree.insert(point);
        }

        for (PetriNetObject object : petriNetObjects) {
            PlaceTransitionObject ptobject = (PlaceTransitionObject) object;
            int x = Grid.align(ptobject.getPositionX(), drawingSurface.getZoom());
            int y = Grid.align(ptobject.getPositionY(), drawingSurface.getZoom());
            Point point = new Point(x, y);
            Command command = new MovePetriNetObjectCommand(ptobject, point, drawingSurface);
            command.redo();
            undoManager.addEdit(command);

            if (object instanceof Transition) {
                for (Arc arc : ((PlaceTransitionObject) object).getPreset()) {
                    for (ArcPathPoint arcPathPoint : arc.getArcPath().getArcPathPoints()) {
                        x = Grid.align(arcPathPoint.getPositionX(), drawingSurface.getZoom());
                        y = Grid.align(arcPathPoint.getPositionY(), drawingSurface.getZoom());
                        point = new Point(x, y);
                        Command pathCommand = new MovePetriNetObjectCommand(arcPathPoint, point, drawingSurface);
                        pathCommand.redo();
                        undoManager.addEdit(pathCommand);
                    }
                }

                for (Arc arc : ((PlaceTransitionObject) object).getPostset()) {
                    for (ArcPathPoint arcPathPoint : arc.getArcPath().getArcPathPoints()) {
                        x = Grid.align(arcPathPoint.getPositionX(), drawingSurface.getZoom());
                        y = Grid.align(arcPathPoint.getPositionY(), drawingSurface.getZoom());
                        point = new Point(x, y);
                        Command pathCommand = new MovePetriNetObjectCommand(arcPathPoint, point, drawingSurface);
                        pathCommand.redo();
                        undoManager.addEdit(pathCommand);
                    }
                }
            }

            ptobject.updateOnMoveOrZoom();
        }
    }

    @Override
    public void copy() {
        String message = CopyPastImportExport.toXML(drawingSurface().getSelectionObject().getSelection());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(message), null);
    }

    @Override
    public void cut() {
        String message = CopyPastImportExport.toXML(drawingSurface().getSelectionObject().getSelection());
        deleteSelection();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(message), null);
    }

    @Override
    public void past() {
        String s = "";
        //odd: the Object param of getContents is not currently used
        Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                s = (String)contents.getTransferData(DataFlavor.stringFlavor);
            }
            catch (UnsupportedFlavorException | IOException ignored){}
        }
        CopyPastImportExport.past(s, this);
    }

    @Override
    public void print() {
        Export.exportGuiView(drawingSurface, Export.PRINTER, null, null, this);
    }

    @Override
	public void importSUMOQueries() {
		File[] files = FileBrowser.constructor("Import SUMO", "txt", FileBrowser.userPath).openFiles();
		for(File f : files){
			if(f.exists() && f.isFile() && f.canRead()){
				FileBrowser.userPath = f.getParent();
				SUMOQueryLoader.importQueries(f, network(), this);
			}
		}
	}

	@Override
	public void importXMLQueries() {
		File[] files = FileBrowser.constructor("Import XML queries", "xml", FileBrowser.userPath).openFiles();
		for(File f : files){
			if(f.exists() && f.isFile() && f.canRead()){
				FileBrowser.userPath = f.getParent();
				XMLQueryLoader.importQueries(f, network(), this);
			}
		}
	}

	public boolean isInAnimationMode() {
		return animationmode;
	}

	public Animator getAnimator() {
		return animator;
	}

    @Override
    public void mergeNetComponents() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();

        int openCTLDialog;
        boolean inlineConstants = false;

        if (!tapnNetwork.constants().isEmpty()) {
            Object[] options = {
                "Yes",
                "No"
            };

            String optionText = "Do you want to replace constants with values?";
            openCTLDialog = JOptionPane.showOptionDialog(TAPAALGUI.getApp(), optionText, "Merge Net Components Dialog", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (openCTLDialog == JOptionPane.YES_OPTION) {
                inlineConstants = true;
            } else if (openCTLDialog == JOptionPane.NO_OPTION) {
                network.setConstants(tapnNetwork.constants());
            }
        }
        network.setColorTypes(tapnNetwork.colorTypes());
        network.setVariables(tapnNetwork.variables());

        TAPNComposer composer = new TAPNComposer(new MessengerImpl(), guiModels, lens, true, inlineConstants);
        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(tapnNetwork);

        ArrayList<Template> templates = new ArrayList<>(1);

        templates.add(new Template(transformedModel.value1(), composer.getGuiModel(), new Zoomer()));
        network.add(transformedModel.value1());

        NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(network, templates, new ArrayList<>(0), network.constants(), lens);

        try {
            ByteArrayOutputStream outputStream = tapnWriter.savePNML();

            String composedName = "composed-" + getTabTitle();
            composedName = composedName.replace(".tapn", "");
            TAPAALGUI.openNewTabFromStream(new ByteArrayInputStream(outputStream.toByteArray()), composedName);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /* GUI Model / Actions */

	@Override
	public void setApp(GuiFrameActions newApp) {
		app.setReference(newApp);
		undoManager.setApp(app);

		updateFeatureText();
		updateZoom();

		//XXX
		if (isInAnimationMode()) {
			app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.animation));
			animator.updateAnimationButtonsEnabled(); //Update stepBack/Forward
		} else {
			app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.draw));
			app.ifPresent(o->setMode(DrawTool.SELECT));
		}
		app.ifPresent(o->o.registerDrawingActions(getAvailableDrawActions()));
        app.ifPresent(o->o.registerAnimationActions(getAvailableSimActions()));
        app.ifPresent(o->o.registerToolsActions(getAvailableToolActions()));

        //TODO: this is a temporary implementation untill actions can be moved
        app.ifPresent(o->o.registerViewActions(List.of()));

	}

	@Override
	public void setSafeGuiFrameActions(SafeGuiFrameActions ref) {
		safeApp.setReference(ref);
	}

	@Override
	public void zoomOut() {
		boolean didZoom = drawingSurface().getZoomController().zoomOut();
		if (didZoom) {
			app.ifPresent(e -> e.updateZoomSlider(drawingSurface().getZoomController().getPercent()));
		}
	}

	@Override
	public void zoomIn() {
		boolean didZoom = drawingSurface().getZoomController().zoomIn();
		if (didZoom) {
			app.ifPresent(e -> e.updateZoomSlider(drawingSurface().getZoomController().getPercent()));
		}
	}

    @Override
    public void setIsAlreadyFitToScreen(boolean alreadyFitToScreen) {
        this.alreadyFitToScreen = alreadyFitToScreen;
    }

    @Override
    public boolean isAlreadyFitToScreen() {
        return alreadyFitToScreen;
    }

    @Override
    public void fitToScreen() {
        final int margin = 50;

        // Loop until it converges
        Double prevZoom = null;
        while (true) {
            Iterable<PetriNetObject> petriNetObjects = currentTemplate().guiModel().getPetriNetObjects();
            if (!petriNetObjects.iterator().hasNext()) {
                alreadyFitToScreen = true;
                return;
            }

            int smallestX = Integer.MAX_VALUE;
            int smallestY = Integer.MAX_VALUE;
            int largestX = Integer.MIN_VALUE;
            int largestY = Integer.MIN_VALUE;

            JViewport viewport = (JViewport)drawingSurface().getParent();
            for (PetriNetObject pno : currentTemplate().guiModel().getPetriNetObjects()) {
                if (pno instanceof PlaceTransitionObject) {
                    if (pno.getOriginalX() < smallestX) {
                        smallestX = pno.getOriginalX();
                    }

                    if (pno.getOriginalY() < smallestY) {
                        smallestY = pno.getOriginalY();
                    }

                    if (pno.getOriginalX() + pno.getWidth() > largestX) {
                        largestX = pno.getOriginalX() + pno.getWidth();
                    }

                    if (pno.getOriginalY() + pno.getHeight() > largestY) {
                        largestY = pno.getOriginalY() + pno.getHeight();
                    }

                    if (pno instanceof Transition) {
                        Transition t = (Transition) pno;
                        for (Arc arc : t.getPreset()) {
                            for (ArcPathPoint point : arc.getArcPath().getArcPathPoints()) {
                                if (point.getOriginalX() < smallestX) {
                                    smallestX = point.getOriginalX();
                                }

                                if (point.getOriginalY() < smallestY) {
                                    smallestY = point.getOriginalY();
                                }

                                if (point.getOriginalX() > largestX) {
                                    largestX = point.getOriginalX();
                                }

                                if (point.getOriginalY() > largestY) {
                                    largestY = point.getOriginalY();
                                }
                            }
                        }

                        for (Arc arc : t.getPostset()) {
                            for (ArcPathPoint point : arc.getArcPath().getArcPathPoints()) {
                                if (point.getOriginalX() < smallestX) {
                                    smallestX = point.getOriginalX();
                                }

                                if (point.getOriginalY() < smallestY) {
                                    smallestY = point.getOriginalY();
                                }

                                if (point.getOriginalX() > largestX) {
                                    largestX = point.getOriginalX();
                                }

                                if (point.getOriginalY() > largestY) {
                                    largestY = point.getOriginalY();
                                }
                            }
                        }
                    }
                }
            }

            smallestX = Math.max(0, smallestX - margin);
            smallestY = Math.max(0, smallestY - margin);

            largestX += margin;
            largestY += margin;

            int width = largestX - smallestX;
            int height = largestY - smallestY;
            double xZoomFactor = (double) viewport.getWidth() / width;
            double yZoomFactor = (double) viewport.getHeight() / height;
            double zoomFactor = Math.min(xZoomFactor, yZoomFactor);
            double zoomPercent = zoomFactor * 100;

            double currentZoomPercent = drawingSurface().getZoomController().getPercent();

            final double zoomConvergence = 1;
            final double current = Math.abs(currentZoomPercent - zoomPercent);
            if (current < zoomConvergence || (prevZoom != null && current == prevZoom)) {
                int x = (int) (smallestX * zoomFactor) - margin;
                int y = (int) (smallestY * zoomFactor) - margin;

                x = Math.max(0, x);
                y = Math.max(0, y);

                viewport.setViewPosition(new Point(x, y));
                alreadyFitToScreen = true;
                return;
            }

            prevZoom = current;

            app.ifPresent(e -> e.updateZoomSlider((int)zoomPercent));
        }
    }

    @Override
    public void selectAll() {
        drawingSurface().getSelectionObject().selectAll();
    }

	@Override
	public void deleteSelection() {
		guiModelManager.deleteSelection();
	}

	@Override
	public void stepBackwards() {
		getAnimator().stepBack();
	}

	@Override
	public void stepForward() {
		getAnimator().stepForward();
	}

    @Override
    public void undo() {
        if (!isInAnimationMode()) {
            getUndoManager().undo();
            network().buildConstraints();
        }
    }

    @Override
    public void redo() {
        if (!isInAnimationMode()) {
            getUndoManager().redo();
            network().buildConstraints();
        }
    }

	//Writes a tapaal net to a file, with the posibility to overwrite the quires
	public void writeNetToFile(File outFile, List<TAPNQuery> queriesOverwrite, TAPNLens lens) {
		try {
			NetworkMarking currentMarking = null;
			if(isInAnimationMode()){
				currentMarking = network().marking();
				network().setMarking(getAnimator().getInitialMarking());
			}

			NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(
					network(),
					allTemplates(),
					queriesOverwrite,
					network().constants(),
                    lens
			);

			tapnWriter.savePNML(outFile);

			if(isInAnimationMode()){
				network().setMarking(currentMarking);
			}
		} catch (Exception e) {
			Logger.log(e);
			e.printStackTrace();
			JOptionPane.showMessageDialog(TAPAALGUI.getApp(), e.toString(),
					"File Output Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void writeNetToFile(File outFile) {
		writeNetToFile(outFile, (List<TAPNQuery>) queries(), lens);
	}

	@Override
	public void saveNet(File outFile) {
		try {
			writeNetToFile(outFile);

			setFile(outFile);

			setNetChanged(false);
			getUndoManager().clear();

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(TAPAALGUI.getApp(), e.toString(), "File Output Error", JOptionPane.ERROR_MESSAGE);
		}
	}

    @Override
    public void increaseSpacing() {
		double factor = 1.25;
		changeSpacing(factor);
		getUndoManager().addNewEdit(new ChangeSpacingEditCommand(factor, this));
    }

	@Override
	public void decreaseSpacing() {
		double factor = 0.8;
		changeSpacing(factor);
		getUndoManager().addNewEdit(new ChangeSpacingEditCommand(factor, this));
	}

	public void changeSpacing(double factor) {
        if (factor < 1) {
            Quadtree quadtree = new Quadtree();
            final int minimumDistance = 45;
            /* Precompute the distance between all objects after translation,
            and check if they are within the minimum distance */
            for (PetriNetObject obj : currentTemplate().guiModel().getPetriNetObjects()) {
                if (obj instanceof PlaceTransitionObject) {
                    int newX = (int)(((PlaceTransitionObject)obj).getCenter().getX() * factor);
                    int newY = (int)(((PlaceTransitionObject)obj).getCenter().getY() * factor);
                    Point newLocation = new Point(newX, newY);
                    if (quadtree.containsWithin(newLocation, minimumDistance)) return;
                    quadtree.insert(newLocation);
                }
            }
        }

        Map<PlaceTransitionObject, Point> locations = new HashMap<>();
		for (PetriNetObject obj : this.currentTemplate().guiModel().getPetriNetObjects()){
			if (obj instanceof PlaceTransitionObject){
                PlaceTransitionObject pno = (PlaceTransitionObject) obj;
                Point2D center = pno.getCenter();

                int newCenterX = (int)(center.getX() * factor);
                int newCenterY = (int)(center.getY() * factor);

				pno.setCenter(newCenterX, newCenterY);
                locations.put(pno, new Point(newCenterX, newCenterY));

				if (pno instanceof Transition) {
                    for (Arc arc : pno.getPreset()) {
						for (ArcPathPoint point : arc.getArcPath().getArcPathPoints()) {
                            int newX = (int)(point.getPoint().x * factor);
                            int newY = (int)(point.getPoint().y * factor);

                            int offsetX = point.getPoint().x - newX > 0 ? Grid.getGridSpacing() : -Grid.getGridSpacing();
                            int offsetY = point.getPoint().y - newY > 0 ? Grid.getGridSpacing() : -Grid.getGridSpacing();
                            while (locations.containsValue(new Point(newX, newY))) {
                                newX += offsetX;
                                newY += offsetY;
                            }

                            point.setPointLocation(newX, newY);
						}
					}

                    for (Arc arc : pno.getPostset()) {
						for (ArcPathPoint point : arc.getArcPath().getArcPathPoints()) {
                            int newX = (int)(point.getPoint().x * factor);
                            int newY = (int)(point.getPoint().y * factor);

							point.setPointLocation(newX, newY);
						}
					}
				}

				pno.update(true);
			} else {
                int newX = (int)(obj.getLocation().x * factor);
                int newY = (int)(obj.getLocation().y * factor);

				obj.setLocation(newX, newY);
			}
		}

		this.currentTemplate().guiModel().repaintAll(true);
		drawingSurface().updatePreferredSize();
	}

	public PetriNetTab duplicateTab(TAPNLens overwriteLens, String appendName) {
        NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(
            network(),
            allTemplates(),
            queries(),
            network().constants(),
            overwriteLens
        );

        try {
            ByteArrayOutputStream outputStream = tapnWriter.savePNML();
            String composedName = getTabTitle();
            composedName = composedName.replace(".tapn", "");
            composedName += appendName;
            return createNewTabFromInputStream(new ByteArrayInputStream(outputStream.toByteArray()), composedName);
        } catch (Exception e1) {
            Logger.log("Could not load model");
            e1.printStackTrace();
        }
        return null;
    }

	static final class CanvasPlaceDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedPlace(canvas.getGuiModel(), p);
        }

        @Override
        public void registerEvents() {}
    }

    static final class CanvasTransitionDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedTransitions(canvas.getGuiModel(), p, false, false);
        }

        @Override
        public void registerEvents() {}
    }

    static final class CanvasUrgentTransitionDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedTransitions(canvas.getGuiModel(), p, true, false);
        }

        @Override
        public void registerEvents() {}
    }

    static final class CanvasUncontrollableTransitionDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedTransitions(canvas.getGuiModel(), p, false, true);
        }

        @Override
        public void registerEvents() {}
    }

    static final class CanvasUncontrollableUrgentTransitionDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedTransitions(canvas.getGuiModel(), p, true, true);
        }

        @Override
        public void registerEvents() {}
    }

    static final class CanvasAnnotationNoteDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

           guiModelManager.addAnnotationNote(canvas.getGuiModel(), p);
        }

        @Override
        public void registerEvents() {}
    }

    static final class CanvasInhibitorarcDrawController extends AbstractCanvasArcDrawController {

        private TimedTransitionComponent transition;
        private TimedPlaceComponent place;

        protected void transitionClicked(TimedTransitionComponent pno, MouseEvent e) {
            if (place != null && transition == null) {
                transition = pno;
                canvas.clearAllPrototype();
                var result = guiModelManager.addInhibitorArc(canvas.getGuiModel(), place, transition, arc.getArcPath());
                showPopupIfFailed(result);
                clearPendingArc();
            }
        }

        protected void placeClicked(TimedPlaceComponent pno, MouseEvent e) {
            if (place == null && transition == null) {
                place = pno;
                connectsTo = 2;
                arc = new TimedInhibitorArcComponent(pno);
                //XXX calling zoomUpdate will set the endpoint to 0,0, drawing the arc from source to 0,0
                //to avoid this we change the endpoint to set the end point to the same as the end point
                //needs further refactorings //kyrke 2019-09-05
                arc.setEndPoint(pno.getPositionX(), pno.getPositionY(), false);
                canvas.addPrototype(arc);
                arc.requestFocusInWindow();
                arc.setSelectable(false);
                arc.enableDrawingKeyBindings(this::clearPendingArc);
            }
        }

        @Override
        protected void clearPendingArc() {
            super.clearPendingArc();
            canvas.clearAllPrototype();
            place = null;
            transition = null;
            arc = null;
        }

    }

    static abstract class AbstractCanvasArcDrawController extends AbstractDrawingSurfaceManager {
        protected Arc arc;
        protected int connectsTo = 1; // 0 if nothing, 1 if place, 2 if transition

        @Override
        public void registerEvents() {
            registerEvent(
                e->e.pno instanceof TimedPlaceComponent && e.a == MouseAction.pressed,
                e->placeClicked(((TimedPlaceComponent) e.pno), e.e)
            );
            registerEvent(
                e->e.pno instanceof TimedTransitionComponent && e.a == MouseAction.pressed,
                e->transitionClicked(((TimedTransitionComponent) e.pno), e.e)
            );
            registerEvent(
                e->e.pno instanceof PlaceTransitionObject && e.a == MouseAction.entered,
                e->placetranstionMouseOver(((PlaceTransitionObject) e.pno))
            );
            registerEvent(
                e->e.pno instanceof PlaceTransitionObject && e.a == MouseAction.exited,
                e->placetranstionMouseExited(((PlaceTransitionObject) e.pno))
            );
            registerEvent(
                e->e.pno instanceof PlaceTransitionObject && e.a == MouseAction.moved,
                e->placetransitionMouseMoved(((PlaceTransitionObject) e.pno), e.e)
            );
            registerEvent(
                e->e.pno instanceof Arc && e.a == MouseAction.pressed,
                e->arcClicked((Arc)e.pno, e.e)
            );
        }

        private void arcClicked(Arc pno, MouseEvent e){
            //Avoid clicking on the arrow itself while drawing (dispatch to parent to add arc path point)
            if ((pno).isPrototype()) {
                //Changes dispatches an event to the parent component, with the mouse location updated to the parent
                //MouseLocation is relative to the component
                e.translatePoint(pno.getX(), pno.getY());
                pno.getParent().dispatchEvent(e);
            }
        }

        protected abstract void transitionClicked(TimedTransitionComponent pno, MouseEvent e);
        protected abstract void placeClicked(TimedPlaceComponent pno, MouseEvent e);

        protected void clearPendingArc() {
            connectsTo = 0;
        }

        @Override
        public void setupManager() {
            TAPAALGUI.useExtendedBounds = true;
        }

        @Override
        public void teardownManager() {
            clearPendingArc();
            TAPAALGUI.useExtendedBounds = false;
        }

        @Override
        public void drawingSurfaceMouseMoved(MouseEvent e) {
            if(arc!=null) {
                arc.setEndPoint(e.getX(), e.getY(), e.isShiftDown());
            }
        }

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            if (arc!=null) {
                if (!e.isControlDown()) {
                    Point p = e.getPoint();
                    int x = Zoomer.getUnzoomedValue(p.x, canvas.getZoom());
                    int y = Zoomer.getUnzoomedValue(p.y, canvas.getZoom());

                    boolean shiftDown = e.isShiftDown();
                    //XXX: x,y is ignored is overwritten when mouse is moved, this just add a new point to the end of list
                    arc.getArcPath().addPoint(arc.getArcPath().getEndIndex(), x, y, shiftDown);
                } else if (connectsTo != 0) { // Quick draw
                    Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

                    if (connectsTo == 1) { // Place
                        var r = guiModelManager.addNewTimedPlace(canvas.getGuiModel(), p);
                        placeClicked(r.result, e);
                    } else { //Transition
                        var r = guiModelManager.addNewTimedTransitions(canvas.getGuiModel(), p, false, false);
                        transitionClicked(r.result, e);
                    }
                }
            } else if (e.isControlDown()){ // Quick draw
                Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());
                var r = guiModelManager.addNewTimedPlace(canvas.getGuiModel(), p);

                placeClicked(r.result, e);
            }
        }

        protected void showPopupIfFailed(Result<?, ModelViolation> result) {
            if (result.hasErrors) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("There was an error drawing the arc. Possible problems:");
                for (ModelViolation v : result.getErrors()) {
                    errorMessage.append("\n  - ").append(v.getErrorMessage());
                }

                JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    errorMessage,
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        }

        protected void placetransitionMouseMoved(PlaceTransitionObject pno, MouseEvent e) {
            if (arc != null) {
                if (arc.getSource() == pno || !arc.getSource().areNotSameType(pno)) {
                    //Dispatch event to parent (drawing surface)
                    e.translatePoint(pno.getX(),pno.getY());
                    pno.getParent().dispatchEvent(e);
                }
            }
        }

        protected void placetranstionMouseExited(PlaceTransitionObject pto) {
            if (arc != null) {
                arc.setTarget(null);
                //XXX this is bad, we have to clean up internal state manually, should be refactored //kyrke - 2019-11-14
                // Relates to bug #1849786
                if (pto instanceof Transition) {
                    ((Transition)pto).removeArcCompareObject(arc);
                    ((Transition)pto).updateEndPoints();
                }
                arc.updateArcPosition();
            }
        }

        protected void placetranstionMouseOver(PlaceTransitionObject pno) {
            if (arc != null) {
                if (arc.getSource() != pno && arc.getSource().areNotSameType(pno)) {
                    arc.setTarget(pno);
                    arc.updateArcPosition();
                }
            }
        }
    }

    static final class CanvasArcDrawController extends AbstractCanvasArcDrawController {
        private TimedTransitionComponent transition;
        private TimedPlaceComponent place;

        protected void transitionClicked(TimedTransitionComponent pno, MouseEvent e) {
            if (place == null && transition == null) {
                transition = pno;
                connectsTo = 1;
                arc = new TimedOutputArcComponent(pno);

                //XXX calling zoomUpdate will set the endpoint to 0,0, drawing the arc from source to 0,0
                //to avoid this we change the endpoint to set the end point to the same as the end point
                //needs further refactorings //kyrke 2019-09-05
                arc.setEndPoint(pno.getPositionX(), pno.getPositionY(), false);
                canvas.addPrototype(arc);
                arc.requestFocusInWindow();
                arc.setSelectable(false);
                arc.enableDrawingKeyBindings(this::clearPendingArc);
            } else if (place != null && transition == null) {
                transition = pno;
                canvas.clearAllPrototype();
                var result = guiModelManager.addTimedInputArc(canvas.getGuiModel(), place, transition, arc.getArcPath());
                showPopupIfFailed(result);
                clearPendingArc();

                if (e != null && e.isControlDown()) {
                    transition = pno;
                    connectsTo = 1;
                    arc = new TimedOutputArcComponent(pno);
                    //XXX calling zoomUpdate will set the endpoint to 0,0, drawing the arc from source to 0,0
                    //to avoid this we change the endpoint to set the end point to the same as the end point
                    //needs further refactorings //kyrke 2019-09-05
                    arc.setEndPoint(pno.getPositionX(), pno.getPositionY(), false);
                    canvas.addPrototype(arc);
                    arc.requestFocusInWindow();
                    arc.setSelectable(false);
                    arc.enableDrawingKeyBindings(this::clearPendingArc);
                }
            }
        }

        protected void placeClicked(TimedPlaceComponent pno, MouseEvent e) {
            if (place == null && transition == null) {
                place = pno;
                connectsTo = 2;
                arc = new TimedInputArcComponent(pno);
                //XXX calling zoomUpdate will set the endpoint to 0,0, drawing the arc from source to 0,0
                //to avoid this we change the endpoint to set the end point to the same as the end point
                //needs further refactorings //kyrke 2019-09-05
                arc.setEndPoint(pno.getPositionX(), pno.getPositionY(), false);
                canvas.addPrototype(arc);
                arc.requestFocusInWindow();
                arc.setSelectable(false);
                arc.enableDrawingKeyBindings(this::clearPendingArc);
            } else if (transition != null && place == null) {
                place = pno;
                canvas.clearAllPrototype();
                var result = guiModelManager.addTimedOutputArc(canvas.getGuiModel(), transition, place, arc.getArcPath());
                showPopupIfFailed(result);
                clearPendingArc();

                if (e!= null && e.isControlDown()) {
                    place = pno;
                    connectsTo = 2;
                    arc = new TimedInputArcComponent(pno);
                    //XXX calling zoomUpdate will set the endpoint to 0,0, drawing the arc from source to 0,0
                    //to avoid this we change the endpoint to set the end point to the same as the end point
                    //needs further refactorings //kyrke 2019-09-05
                    arc.setEndPoint(pno.getPositionX(), pno.getPositionY(), false);
                    canvas.addPrototype(arc);
                    arc.requestFocusInWindow();
                    arc.setSelectable(false);
                    arc.enableDrawingKeyBindings(this::clearPendingArc);
                }
            }
        }

        @Override
        protected void clearPendingArc() {
            super.clearPendingArc();
            canvas.clearAllPrototype();
            place = null;
            transition = null;
            arc = null;
        }

    }

	static final class CanvasAnimationController extends AbstractDrawingSurfaceManager {

		private final Animator animator;

        public CanvasAnimationController(Animator animator) {
			this.animator = animator;
        }

		@Override
		public void registerEvents() {
			registerEvent(
					e -> e.a == MouseAction.pressed && e.pno instanceof TimedTransitionComponent && SwingUtilities.isLeftMouseButton(e.e),
					e -> transitionLeftClicked((TimedTransitionComponent)e.pno)
			);
			registerEvent(
					e->e.a == MouseAction.entered && e.pno instanceof PlaceTransitionObject,
					e->mouseEnterPTO((PlaceTransitionObject)e.pno)
			);
			registerEvent(
					e->e.a == MouseAction.exited && e.pno instanceof PlaceTransitionObject,
					e->mouseExitPTO((PlaceTransitionObject)e.pno)
			);
            registerEvent(
                e -> e.a == MouseAction.wheel,
                e -> e.pno.getParent().dispatchEvent(e.e) // Forward mouse wheel events to canvas
            );
		}

		void transitionLeftClicked(TimedTransitionComponent t) {
			TimedTransition transition = t.underlyingTransition();
			if (transition.isDEnabled()) {
				animator.dFireTransition(transition);
			}
		}

		void mouseEnterPTO(PlaceTransitionObject pto) {
			if (pto instanceof TimedPlaceComponent) {
				((TimedPlaceComponent) pto).showAgeOfTokens(true);
			} else if (pto instanceof TimedTransitionComponent) {
				((TimedTransitionComponent) pto).showDInterval(true);
			}
		}
		void mouseExitPTO(PlaceTransitionObject pto) {
			if (pto instanceof TimedPlaceComponent) {
				((TimedPlaceComponent) pto).showAgeOfTokens(false);
			} else if (pto instanceof TimedTransitionComponent) {
				((TimedTransitionComponent) pto).showDInterval(false);
			}
		}

        @Override
        public void teardownManager() {
            //Remove all mouse-over menus if we exit animation mode
            ArrayList<PetriNetObject> pnObjects = canvas.getGuiModel().getPNObjects();

            for (PetriNetObject pn : pnObjects) {
                if (pn instanceof TimedPlaceComponent) {
                    TimedPlaceComponent place = (TimedPlaceComponent) pn;
                    place.showAgeOfTokens(false);
                } else if (pn instanceof TimedTransitionComponent) {
                    TimedTransitionComponent transition = (TimedTransitionComponent) pn;
                    transition.showDInterval(false);
                }
            }
        }
    }

    private void setManager(AbstractDrawingSurfaceManager newManager) {
        //De-register old manager
		managerRef.get().deregisterManager();
        managerRef.setReference(newManager);
		managerRef.get().registerManager(drawingSurface, guiModelManager);
    }

    public void updateZoom() {
	    int zoom = drawingSurface().getZoom();
	    zoomTo(zoom);
    }

    public void updateFeatureText() {
        boolean[] features = {lens.isTimed(), lens.isGame(), lens.isColored(), lens.isStochastic()};
        app.ifPresent(o->o.setFeatureInfoText(features));
    }

    public TAPNLens getLens() {
        return lens;
    }

    private void convertToUntimedTab(PetriNetTab tab) {
        TabTransformer.removeTimingInformation(tab);
        if(lens.isStochastic()) {
            TabTransformer.removeDistributionInformation(tab);
        }
    }

    static final class CanvasTransportarcDrawController extends AbstractCanvasArcDrawController {

        private TimedTransitionComponent transition;
        private TimedPlaceComponent place1;
        private TimedPlaceComponent place2;
        private Arc arc1;
        private Arc arc2;

        protected void placetranstionMouseExited(PlaceTransitionObject pto) {
            if (arc != null) {
                arc.setTarget(null);
                //XXX this is bad, we have to clean up internal state manually, should be refactored //kyrke - 2019-11-14
                // Relates to bug #1849786
                if (pto instanceof Transition) {
                    ((Transition)pto).removeArcCompareObject(arc);
                }
                arc.updateArcPosition();
            }
        }

        protected void placetranstionMouseOver(PlaceTransitionObject pno) {
            if (arc != null) {
                if (arc.getSource() != pno && arc.getSource().areNotSameType(pno)) {
                    arc.setTarget(pno);
                    arc.updateArcPosition();
                }
            }
        }

        protected void transitionClicked(TimedTransitionComponent pno, MouseEvent e) {
            if (place1 != null && transition == null) {
                transition = pno;
                connectsTo = 1;
                arc2 = arc = new TimedTransportArcComponent(pno, -1, false);

                //XXX calling zoomUpdate will set the endpoint to 0,0, drawing the arc from source to 0,0
                //to avoid this we change the endpoint to set the end point to the same as the end point
                //needs further refactorings //kyrke 2019-09-05
                arc.setEndPoint(pno.getPositionX(), pno.getPositionY(), false);
                canvas.addPrototype(arc);
                arc.requestFocusInWindow();
                arc.setSelectable(false);
                arc.enableDrawingKeyBindings(this::clearPendingArc);
            }
        }

        protected void placeClicked(TimedPlaceComponent pno, MouseEvent e) {
            if (place1 == null && transition == null) {
                place1 = pno;
                connectsTo = 2;
                arc1 = arc = new TimedTransportArcComponent(pno, -1, true);
                //XXX calling zoomUpdate will set the endpoint to 0,0, drawing the arc from source to 0,0
                //to avoid this we change the endpoint to set the end point to the same as the end point
                //needs further refactorings //kyrke 2019-09-05
                arc.setEndPoint(pno.getPositionX(), pno.getPositionY(), false);
                canvas.addPrototype(arc);
                arc.requestFocusInWindow();
                arc.setSelectable(false);
                arc.enableDrawingKeyBindings(this::clearPendingArc);
            } else if (transition != null && place2 == null) {
                place2 = pno;
                canvas.clearAllPrototype();
                var result = guiModelManager.addTimedTransportArc(canvas.getGuiModel(), place1, transition, place2, arc1.getArcPath(), arc2.getArcPath());
                showPopupIfFailed(result);
                clearPendingArc();

                if (e != null && e.isControlDown()) {
                    place1 = pno;
                    connectsTo = 2;
                    arc1 = arc = new TimedTransportArcComponent(pno, -1, true);
                    //XXX calling zoomUpdate will set the endpoint to 0,0, drawing the arc from source to 0,0
                    //to avoid this we change the endpoint to set the end point to the same as the end point
                    //needs further refactorings //kyrke 2019-09-05
                    arc.setEndPoint(pno.getPositionX(), pno.getPositionY(), false);
                    canvas.addPrototype(arc);
                    arc.requestFocusInWindow();
                    arc.setSelectable(false);
                    arc.enableDrawingKeyBindings(this::clearPendingArc);
                }
            }
        }

        @Override
        protected void clearPendingArc() {
            super.clearPendingArc();
            canvas.clearAllPrototype();
            place1 = place2 = null;
            transition = null;
            arc = arc1 = arc2 = null;
        }

    }

    static final class CanvasGeneralDrawController extends AbstractDrawingSurfaceManager {
        final TAPNLens lens;

        public CanvasGeneralDrawController(TAPNLens lens) {
            this.lens = lens;
        }

        @Override
        public void registerEvents() {

            //Drag events
            registerEvent(
                e->e.pno instanceof PetriNetObject && e.a == MouseAction.pressed,
                e-> pnoPressed((PetriNetObject)e.pno, e.e)
            );
            registerEvent(
                e->e.pno instanceof PetriNetObject && e.a == MouseAction.released,
                e-> pnoReleased((PetriNetObject)e.pno, e.e)
            );
            registerEvent(
                e->e.pno instanceof PetriNetObject && e.a == MouseAction.dragged,
                e-> pnoDragged((PetriNetObject)e.pno, e.e)
            );

            registerEvent(
                e->e.pno instanceof TimedTransitionComponent && e.a == MouseAction.doubleClicked,
                e-> ((TimedTransitionComponent) e.pno).showEditor()
            );
            registerEvent(
                e->e.pno instanceof TimedPlaceComponent && e.a == MouseAction.doubleClicked,
                e-> ((TimedPlaceComponent) e.pno).showEditor()
            );
            registerEvent(
                e->e.pno instanceof TimedTransitionComponent && e.a == MouseAction.rightClicked,
                e-> ((TimedTransitionComponent) e.pno).getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
            );
            registerEvent(
                e->e.pno instanceof TimedPlaceComponent && e.a == MouseAction.rightClicked,
                e-> ((TimedPlaceComponent) e.pno).getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
            );
            registerEvent(
                e->e.pno instanceof Arc && e.a == MouseAction.rightClicked,
                e-> ((Arc) e.pno).getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
            );
            registerEvent(
                e->e.pno instanceof ArcPathPoint && e.a == MouseAction.rightClicked,
                e-> ((ArcPathPoint) e.pno).getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
            );
            registerEvent(
                e->e.pno instanceof AnnotationNote && e.a == MouseAction.doubleClicked,
                e-> ((AnnotationNote) e.pno).enableEditMode()
            );
            registerEvent(
                e->e.pno instanceof AnnotationNote && e.a == MouseAction.rightClicked,
                e-> ((AnnotationNote) e.pno).getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
            );
            registerEvent(
                e->e.pno instanceof Arc && e.a == MouseAction.entered,
                e -> ((Arc)e.pno).getArcPath().showPoints()
            );
            registerEvent(
                e->e.pno instanceof Arc && e.a == MouseAction.exited,
                e -> ((Arc)e.pno).getArcPath().hidePoints()
            );
            registerEvent(
                e->e.pno instanceof TimedOutputArcComponent && e.a == MouseAction.doubleClicked && !e.e.isControlDown(),
                e -> ((TimedOutputArcComponent) e.pno).showTimeIntervalEditor()
            );
            registerEvent(
                e->e.pno instanceof Arc && e.a == MouseAction.doubleClicked && e.e.isControlDown(),
                e->arcDoubleClickedWithContrl(((Arc) e.pno), e.e)
            );
            registerEvent(
                e->e.pno instanceof TimedPlaceComponent && e.a == MouseAction.wheel,
                e->timedPlaceMouseWheelWithShift(((TimedPlaceComponent) e.pno), ((MouseWheelEvent) e.e))
            );
            registerEvent(
                e->e.pno instanceof TimedTransitionComponent && e.a == MouseAction.wheel,
                e->timedTranstionMouseWheelWithShift(((TimedTransitionComponent) e.pno), ((MouseWheelEvent) e.e))
            );
            registerEvent(
                e->e.pno instanceof ArcPathPoint && e.a == MouseAction.wheel,
                e->{
                    if (e.e.isShiftDown()) {
                        guiModelManager.toggleArcPathPointType((ArcPathPoint) e.pno);
                    }
                }
            );
            registerEvent(
                e->e.pno instanceof Arc && e.a == MouseAction.wheel,
                e->arcMouseWheel((PetriNetObject) e.pno, e.e)
            );
            registerEvent(
                e->e.pno instanceof ArcPathPoint && e.a == MouseAction.wheel,
                e->arcMouseWheel(((PetriNetObject) e.pno), e.e)
            );
        }

        boolean justSelected = false;

        boolean isDragging = false;
        Point dragInit = new Point();

        private int totalX = 0;
        private int totalY = 0;
        private void pnoPressed(PetriNetObject pno, MouseEvent e) {
            if (!pno.isSelected()) {
                if (!e.isShiftDown()) {
                    canvas.getSelectionObject().clearSelection();
                }
                pno.select();
                justSelected = true;
            }
            dragInit = e.getPoint();
        }
        private void pnoReleased(PetriNetObject pno, MouseEvent e) {

            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            if (isDragging) {
                isDragging = false;
                canvas.translateSelection(totalX, totalY);
                totalX = 0;
                totalY = 0;
            } else {
                if (!justSelected) {
                    if (e.isShiftDown()) {
                        pno.deselect();
                    } else {
                        canvas.getSelectionObject().clearSelection();
                        pno.select();
                    }
                }
            }
            justSelected = false;
        }
        private void pnoDragged(PetriNetObject pno, MouseEvent e) {

            //Disabled dragging endpoints or arcs as its broken (sometimes)
            if ( pno instanceof Arc ||
                (pno instanceof ArcPathPoint && ((ArcPathPoint) pno).isEndPoint())
            ) {
                return;
            }
            int previousX = pno.getX();
            int previousY = pno.getY();

            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            if (pno.isDraggable()) {
                if (!isDragging) {
                    isDragging = true;
                }
            }

            // Calculate translation in mouse
            int transX = Grid.align(e.getX() - dragInit.x, canvas.getZoom());
            int transY = Grid.align(e.getY() - dragInit.y, canvas.getZoom());
            canvas.getSelectionObject().translateSelection(transX, transY);

            //Only register the actual distance and direction moved (in case of dragging past edge)
            totalX += pno.getX() - previousX;
            totalY += pno.getY() - previousY;
        }


        private Point dragStartPoint;
        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point clickPoint = e.getPoint();

            if (SwingUtilities.isLeftMouseButton(e)) {
                canvas.getSelectionObject().dispatchEvent(e);
            } else {
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                dragStartPoint = new Point(clickPoint);
            }
            canvas.updatePreferredSize();
        }

        @Override
        public void drawingSurfaceMouseReleased(MouseEvent e) {
            if (dragStartPoint != null) {
                dragStartPoint = null;
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            canvas.getSelectionObject().dispatchEvent(e);
        }

        @Override
        public void drawingSurfaceMouseDragged(MouseEvent e) {
            if (dragStartPoint != null) {
                canvas.drag(dragStartPoint, e.getPoint());
            } else {
                canvas.getSelectionObject().dispatchEvent(e);
            }
        }

        private void arcMouseWheel(PetriNetObject pno, MouseEvent e) {
            pno.getParent().dispatchEvent(e);
        }

        private void timedTranstionMouseWheelWithShift(TimedTransitionComponent p, MouseWheelEvent e) {
            if (p.isSelected()) {
                int rotation;
                if (e.getWheelRotation() < 0) {
                    rotation = -e.getWheelRotation() * 135;
                } else {
                    rotation = e.getWheelRotation() * 45;
                }

                TAPAALGUI.getCurrentTab().getUndoManager().addNewEdit(((Transition) p).rotate(rotation));
            } else {
                p.getParent().dispatchEvent(e);
            }
        }

        private void timedPlaceMouseWheelWithShift(TimedPlaceComponent p, MouseWheelEvent e) {
            //If the net is colored we do nothing
            if(!lens.isColored()) {
                if (p.isSelected()) {
                    if (e.getWheelRotation() < 0) {
                        guiModelManager.addToken(canvas.getGuiModel(), p, 1);
                    } else {
                        guiModelManager.removeToken(canvas.getGuiModel(), p, 1);
                    }

                } else {
                    p.getParent().dispatchEvent(e);

                }
            }
        }

        private void arcDoubleClickedWithContrl(Arc arc, MouseEvent e) {
            TAPAALGUI.getCurrentTab().getUndoManager().addNewEdit(
                arc.getArcPath().insertPoint(
                    new Point2D.Double(
                        Zoomer.getUnzoomedValue(arc.getX() + e.getX(), arc.getZoom()),
                        Zoomer.getUnzoomedValue(arc.getY() + e.getY(), arc.getZoom())
                    ),
                    e.isAltDown()
                )
            );
        }
    }
    public List<GuiAction> getAvailableDrawActions(){
        List<GuiAction> actions;
        if (lens.isTimed() && !lens.isGame()) {
            actions = new ArrayList<>(Arrays.asList(selectAction, timedPlaceAction, transAction, urgentTransAction, timedArcAction, transportArcAction, inhibarcAction, tokenAction, deleteTokenAction, annotationAction, toggleUrgentAction));
        } else if (lens.isTimed()) {
            actions = new ArrayList<>(Arrays.asList(selectAction, timedPlaceAction, transAction, urgentTransAction, uncontrollableTransAction, uncontrollableUrgentTransAction, timedArcAction, transportArcAction, inhibarcAction, tokenAction, deleteTokenAction, annotationAction, toggleUrgentAction, toggleUncontrollableAction));
        } else if (lens.isGame()){
            actions = new ArrayList<>(Arrays.asList(selectAction, timedPlaceAction, transAction, uncontrollableTransAction, timedArcAction, inhibarcAction, tokenAction, deleteTokenAction, annotationAction, toggleUncontrollableAction));
        } else {
            actions = new ArrayList<>(Arrays.asList(selectAction, timedPlaceAction, transAction, timedArcAction, inhibarcAction, tokenAction, deleteTokenAction, annotationAction));
        }
        if (lens.isColored()) {
            actions.remove(tokenAction);
            actions.remove(deleteTokenAction);
        }

        return actions;
    }

    public List<GuiAction> getAvailableSimActions(){
        if(lens.isTimed()){
            return new ArrayList<>(Arrays.asList(timeAction, delayFireAction));
        } else{
            delayFireAction.setName("Fire");
            delayFireAction.setTooltip("Fire Selected Transition");
            return new ArrayList<>(List.of(delayFireAction));
        }
    }

    public List<GuiAction> getAvailableToolActions(){
        if(lens.isColored()){
            return new ArrayList<>(List.of(unfoldTabAction));
        } else {
            return new ArrayList<>(List.of());
        }
    }

    private final GuiAction selectAction = new GuiAction("Select", "Select components (S)", "S", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.SELECT);
        }
    };
    private final GuiAction annotationAction = new GuiAction("Annotation", "Add an annotation (N)", "N", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.ANNOTATION);
        }
    };
    private final GuiAction inhibarcAction = new GuiAction("Inhibitor arc", "Add an inhibitor arc (I)", "I", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.INHIBITOR_ARC);
        }
    };
    private final GuiAction transAction = new GuiAction("Transition", "Add a transition (T)", "T", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.TRANSITION);
        }
    };
    private final GuiAction urgentTransAction = new GuiAction("Urgent transition", "Add an urgent transition (Y)", "Y", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.URGENT_TRANSITION);
        }
    };
    private final GuiAction uncontrollableTransAction = new GuiAction("Uncontrollable transition", "Add an uncontrollable transition (L)", "L", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.UNCONTROLLABLE_TRANSITION);
        }
    };
    private final GuiAction uncontrollableUrgentTransAction = new GuiAction("Uncontrollable urgent transition", "Add an uncontrollable urgent transition (O)", "O", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.URGENT_UNCONTROLLABLE_TRANSITION);
        }
    };
    private final GuiAction tokenAction = new GuiAction("Add token", "Add a token (+)", "typed +", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.ADD_TOKEN);
        }
    };
    private final GuiAction deleteTokenAction = new GuiAction("Delete token", "Delete a token (-)", "typed -", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.REMOVE_TOKEN);
        }
    };
    private final GuiAction timedPlaceAction = new GuiAction("Place", "Add a place (P)", "P", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.PLACE);
        }
    };

    private final GuiAction timedArcAction = new GuiAction("Arc", "Add an arc (A)", "A", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.ARC);
        }
    };
    private final GuiAction transportArcAction = new GuiAction("Transport arc", "Add a transport arc (R)", "R", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(DrawTool.TRANSPORT_ARC);
        }
    };
    private final GuiAction toggleUncontrollableAction = new GuiAction("Toggle uncontrollable transition", "Toggle between control/environment transition", "E", true) {
        public void actionPerformed(ActionEvent e) {
            guiModelManager.toggleUncontrollableTrans();
        }
    };
    private final GuiAction toggleUrgentAction = new GuiAction("Toggle urgent transition", "Toggle between urgent/non-urgent transition", "U", true) {
        public void actionPerformed(ActionEvent e) {
            guiModelManager.toggleUrgentTrans();
        }
    };
    private final GuiAction timeAction = new GuiAction("Delay one time unit", "Let time pass one time unit", "W") {
        public void actionPerformed(ActionEvent e) {
            getAnimator().letTimePass(BigDecimal.ONE);
        }
    };
    private final GuiAction delayFireAction = new GuiAction("Delay and fire", "Delay and fire selected transition", "F") {
        public void actionPerformed(ActionEvent e) {
            getTransitionFiringComponent().fireSelectedTransition();
        }
    };

    private final GuiAction unfoldTabAction = new GuiAction("Unfold net", "Unfold the colors in the tab") {
        @Override
        public void actionPerformed(ActionEvent e) {
            ColoredSimulationDialog.showUnfoldDialog(PetriNetTab.this);
        }
    };

    public void updateMode(DrawTool mode) {
        // deselect other actions
        selectAction.setSelected(mode == DrawTool.SELECT);
        transAction.setSelected(mode == DrawTool.TRANSITION);
        urgentTransAction.setSelected(mode == DrawTool.URGENT_TRANSITION);
        uncontrollableTransAction.setSelected(mode == DrawTool.UNCONTROLLABLE_TRANSITION);
        uncontrollableUrgentTransAction.setSelected(mode == DrawTool.URGENT_UNCONTROLLABLE_TRANSITION);
        timedPlaceAction.setSelected(mode == DrawTool.PLACE);
        timedArcAction.setSelected(mode == DrawTool.ARC);
        transportArcAction.setSelected(mode == DrawTool.TRANSPORT_ARC);
        inhibarcAction.setSelected(mode == DrawTool.INHIBITOR_ARC);
        tokenAction.setSelected(mode == DrawTool.ADD_TOKEN);
        deleteTokenAction.setSelected(mode == DrawTool.REMOVE_TOKEN);
        annotationAction.setSelected(mode == DrawTool.ANNOTATION);
    }
    @Override
    public void updateEnabledActions(GuiFrame.GUIMode mode){
        switch(mode){
            case draw:
                selectAction.setEnabled(true);
                transAction.setEnabled(true);
                urgentTransAction.setEnabled(true);
                uncontrollableTransAction.setEnabled(true);
                toggleUncontrollableAction.setEnabled(true);
                uncontrollableUrgentTransAction.setEnabled(true);
                toggleUrgentAction.setEnabled(true);
                timedPlaceAction.setEnabled(true);
                timedArcAction.setEnabled(true);
                transportArcAction.setEnabled(true);
                inhibarcAction.setEnabled(true);
                tokenAction.setEnabled(true);
                deleteTokenAction.setEnabled(true);
                annotationAction.setEnabled(true);
                delayFireAction.setEnabled(false);
                timeAction.setEnabled(false);
                break;
            case noNet:
                selectAction.setEnabled(false);
                transAction.setEnabled(false);
                urgentTransAction.setEnabled(false);
                uncontrollableTransAction.setEnabled(false);
                toggleUncontrollableAction.setEnabled(false);
                uncontrollableUrgentTransAction.setEnabled(false);
                toggleUrgentAction.setEnabled(false);
                timedPlaceAction.setEnabled(false);
                timedArcAction.setEnabled(false);
                transportArcAction.setEnabled(false);
                inhibarcAction.setEnabled(false);
                tokenAction.setEnabled(false);
                deleteTokenAction.setEnabled(false);
                annotationAction.setEnabled(false);
                delayFireAction.setEnabled(false);
                timeAction.setEnabled(false);
            case animation:
                selectAction.setEnabled(false);
                transAction.setEnabled(false);
                urgentTransAction.setEnabled(false);
                uncontrollableTransAction.setEnabled(false);
                toggleUncontrollableAction.setEnabled(false);
                uncontrollableUrgentTransAction.setEnabled(false);
                toggleUrgentAction.setEnabled(false);
                timedPlaceAction.setEnabled(false);
                timedArcAction.setEnabled(false);
                transportArcAction.setEnabled(false);
                inhibarcAction.setEnabled(false);
                tokenAction.setEnabled(false);
                deleteTokenAction.setEnabled(false);
                annotationAction.setEnabled(false);
                delayFireAction.setEnabled(true);
                if(lens.isTimed())
                    timeAction.setEnabled(true);
                break;
        }
    }

    public void enableActionsForSearchBar(boolean enable) {
        selectAction.setEnabled(enable);
        transAction.setEnabled(enable);
        urgentTransAction.setEnabled(enable);
        uncontrollableTransAction.setEnabled(enable);
        uncontrollableUrgentTransAction.setEnabled(enable);
        timedPlaceAction.setEnabled(enable);
        timedArcAction.setEnabled(enable);
        transportArcAction.setEnabled(enable);
        inhibarcAction.setEnabled(enable);
        tokenAction.setEnabled(enable);
        deleteTokenAction.setEnabled(enable);
        annotationAction.setEnabled(enable);
    }


    public static final String textforDrawing = "Drawing Mode: Click on a button to start adding components to the Editor";
    public static final String textforTAPNPlace = "Place Mode: Right click on a place to see menu options ";
    public static final String textforTransition = "Transition Mode: Right click on a transition to see menu options [Mouse wheel -> rotate]";
    public static final String textforUncontrollableTrans = "Uncontrollable Transition Mode: Right click on a transition to see menu options [Mouse wheel -> rotate]";
    public static final String textforAddtoken = "Add Token Mode: Click on a place to add a token";
    public static final String textforDeltoken = "Delete Token Mode: Click on a place to delete a token ";
    public static final String textforAnimation = "Simulation Mode: Red transitions are enabled, click a transition to fire it";
    public static final String textforArc = "Arc Mode: Right click on an arc to see menu options ";
    public static final String textforTransportArc = "Transport Arc Mode: Right click on an arc to see menu options ";
    public static final String textforInhibArc = "Inhibitor Mode: Right click on an arc to see menu options ";
    public static final String textforMove = "Select Mode: Click/drag to select objects; drag to move them";
    public static final String textforAnnotation = "Annotation Mode: Right click on an annotation to see menu options; double click to edit";
    public static final String textforDrag = "Drag Mode";

    public void changeStatusbarText(DrawTool type) {
        switch (type) {
            case UNCONTROLLABLE_TRANSITION:
                app.ifPresent(o14 -> o14.setStatusBarText(textforUncontrollableTrans));

            case PLACE:
                app.ifPresent(o12 -> o12.setStatusBarText(textforTAPNPlace));
                break;

            case TRANSITION:
                app.ifPresent(o11 -> o11.setStatusBarText(textforTransition));
                break;

            case ARC:
                app.ifPresent(o9 -> o9.setStatusBarText(textforArc));
                break;

            case TRANSPORT_ARC:
                app.ifPresent(o8 -> o8.setStatusBarText(textforTransportArc));
                break;

            case INHIBITOR_ARC:
                app.ifPresent(o7 -> o7.setStatusBarText(textforInhibArc));
                break;

            case ADD_TOKEN:
                app.ifPresent(o6 -> o6.setStatusBarText(textforAddtoken));
                break;

            case REMOVE_TOKEN:
                app.ifPresent(o5 -> o5.setStatusBarText(textforDeltoken));
                break;

            case SELECT:
                app.ifPresent(o4 -> o4.setStatusBarText(textforMove));
                break;

            case DRAW:
                app.ifPresent(o3 -> o3.setStatusBarText(textforDrawing));
                break;

            case ANNOTATION:
                app.ifPresent(o2 -> o2.setStatusBarText(textforAnnotation));
                break;

            case DRAG:
                app.ifPresent(o1 -> o1.setStatusBarText(textforDrag));
                break;

            default:
                app.ifPresent(o->o.setStatusBarText("To-do (textfor" + type));
                break;
        }
    }


    private boolean canNetBeSavedAndShowMessage() {
        if (network().paintNet()) {
            return true;
        } else {
            String message = "The net is too big and cannot be saved or exported.";
            Object[] dialogContent = {message};
            JOptionPane.showMessageDialog(null, dialogContent, "Large net limitation", JOptionPane.WARNING_MESSAGE);
        }
        return false;
    }

    @Override
    public void exportPNG() {
        if (canNetBeSavedAndShowMessage()) {
            Export.exportGuiView(drawingSurface(), Export.PNG, null, null, this);
        }
    }

    @Override
    public void exportPS() {
        if (canNetBeSavedAndShowMessage()) {
            Export.exportGuiView(drawingSurface(), Export.POSTSCRIPT, null, null, this);
        }
    }

    @Override
    public void exportTIKZ() {
        if (canNetBeSavedAndShowMessage()) {
            Export.exportGuiView(drawingSurface(), Export.TIKZ, drawingSurface().getGuiModel(), null, this);
        }
    }

    @Override
    public void exportPNML() {
        if (canNetBeSavedAndShowMessage()) {
            if (Preferences.getInstance().getShowPNMLWarning() && lens.isTimed()) {
                JCheckBox showAgain = new JCheckBox("Do not show this warning.");
                String message = "In the saved PNML all timing information will be lost\n" +
                    "and the components in the net will be merged into one big net.";
                Object[] dialogContent = {message, showAgain};
                JOptionPane.showMessageDialog(null, dialogContent,
                    "PNML loss of information", JOptionPane.WARNING_MESSAGE);
                Preferences.getInstance().setShowPNMLWarning(!showAgain.isSelected());
            }
            Export.exportGuiView(drawingSurface(), Export.PNML, null, null, this);
        }
    }

    @Override
    public void exportQueryXML() {
        if (canNetBeSavedAndShowMessage()) {
            Export.exportGuiView(drawingSurface(), Export.QUERY, null, lens, this);
        }
    }


    @Override
    public void workflowAnalyse() {
        //XXX prop. should take this as argument, insted of using static accessors //kyrke 2019-11-05
        WorkflowDialog.showDialog(this);
    }

    // Workflow dialog
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
