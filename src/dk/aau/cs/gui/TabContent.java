package dk.aau.cs.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.components.StatisticsPanel;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.DeleteQueriesCommand;
import dk.aau.cs.gui.undo.TimedPlaceMarkingEdit;
import dk.aau.cs.io.*;
import dk.aau.cs.io.queries.SUMOQueryLoader;
import dk.aau.cs.io.queries.XMLQueryLoader;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import net.tapaal.gui.DrawingSurfaceManager.AbstractDrawingSurfaceManager;
import net.tapaal.helpers.Reference.MutableReference;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Split;

import pipe.dataLayer.*;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.*;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.graphicElements.*;
import pipe.gui.graphicElements.tapn.*;
import pipe.gui.handler.PlaceTransitionObjectHandler;
import pipe.gui.undo.*;
import pipe.gui.widgets.ConstantsPane;
import pipe.gui.undo.ChangeSpacingEdit;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.*;

import net.tapaal.swinghelpers.JSplitPaneFix;
import dk.aau.cs.gui.components.BugHandledJXMultisplitPane;
import dk.aau.cs.gui.components.TransitionFireingComponent;
import dk.aau.cs.util.Require;
import pipe.gui.widgets.filebrowser.FileBrowser;

public class TabContent extends JSplitPane implements TabContentActions{

	//Model and state
	private final TimedArcPetriNetNetwork tapnNetwork;

	//XXX: Replace with bi-map
	private final HashMap<TimedArcPetriNet, DataLayer> guiModels = new HashMap<TimedArcPetriNet, DataLayer>();
	private final HashMap<DataLayer, TimedArcPetriNet> guiModelToModel = new HashMap<>();

	private final HashMap<TimedArcPetriNet, Zoomer> zoomLevels = new HashMap<TimedArcPetriNet, Zoomer>();


	private final UndoManager undoManager = new UndoManager();

	public final GuiModelManager guiModelManager = new GuiModelManager();
	public class GuiModelManager {
	    public GuiModelManager(){

        }

        public void addNewTimedPlace(DataLayer c, Point p){
	        Require.notNull(c, "datalyer can't be null");
            Require.notNull(p, "Point can't be null");

            dk.aau.cs.model.tapn.LocalTimedPlace tp = new dk.aau.cs.model.tapn.LocalTimedPlace(drawingSurface.getNameGenerator().getNewPlaceName(guiModelToModel.get(c)));
            TimedPlaceComponent pnObject = new TimedPlaceComponent(p.x, p.y, tp);
            guiModelToModel.get(c).add(tp);
            c.addPetriNetObject(pnObject);

            getUndoManager().addNewEdit(new AddTimedPlaceCommand(pnObject, guiModelToModel.get(c), c));

        }

        public void addNewTimedTransitions(DataLayer c, Point p) {
            dk.aau.cs.model.tapn.TimedTransition transition = new dk.aau.cs.model.tapn.TimedTransition(drawingSurface.getNameGenerator().getNewTransitionName(guiModelToModel.get(c)));

            TimedTransitionComponent pnObject = new TimedTransitionComponent(p.x, p.y, transition);

            guiModelToModel.get(c).add(transition);
            c.addPetriNetObject(pnObject);

            getUndoManager().addNewEdit(new AddTimedTransitionCommand(pnObject, guiModelToModel.get(c), c));
        }

        public void addAnnotationNote(DataLayer c, Point p) {
            AnnotationNote pnObject = new AnnotationNote(p.x, p.y);

            //enableEditMode open editor, retuns true of text added, else false
            //If no text is added,dont add it to model
            if (pnObject.enableEditMode(true)) {
                c.addPetriNetObject(pnObject);
                getUndoManager().addEdit(new AddAnnotationNoteCommand(pnObject, c));
            }
        }

        public void addTimedInputArc(DataLayer c, TimedPlaceComponent p, TimedTransitionComponent t, ArcPath path) {
            Require.notNull(c, "DataLayer can't be null");
            Require.notNull(p, "Place can't be null");
            Require.notNull(t, "Transitions can't be null");

            TimedArcPetriNet modelNet = guiModelToModel.get(c);

            if (!modelNet.hasArcFromPlaceToTransition(p.underlyingPlace(), t.underlyingTransition())) {

                TimedInputArc tia = new TimedInputArc(
                    p.underlyingPlace(),
                    t.underlyingTransition(),
                    TimeInterval.ZERO_INF
                );

                TimedInputArcComponent tiac = new TimedInputArcComponent(p,t,tia);

                if (path != null) {
                    tiac.setArcPath(new ArcPath(tiac, path));
                }

                Command edit = new AddTimedInputArcCommand(
                    tiac,
                    modelNet,
                    c
                );
                edit.redo();

                undoManager.addNewEdit(edit);

            }  else {
                //TODO: can't have two arcs between place and transition
                JOptionPane.showMessageDialog(
                    CreateGui.getApp(),
                    "There was an error drawing the arc. Possible problems:\n"
                        + " - There is already an arc between the selected place and transition\n"
                        + " - You are attempting to draw an arc between a shared transition and a shared place",
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        }

        public void deleteSelection() {
            // check if queries need to be removed
            ArrayList<PetriNetObject> selection = drawingSurface().getSelectionObject().getSelection();
            Iterable<TAPNQuery> queries = queries();
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
                } else if (pn instanceof TimedTransitionComponent){
                    TimedTransitionComponent transition = (TimedTransitionComponent)pn;
                    if(!transition.underlyingTransition().isShared()){
                        for (TAPNQuery q : queries) {
                            if (q.getProperty().containsAtomicPropositionWithSpecificTransitionInTemplate((transition.underlyingTransition()).model().name(),transition.underlyingTransition().name())) {
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
                getUndoManager().newEdit(); // new "transaction""
                if (queriesAffected) {
                    TabContent currentTab = TabContent.this;
                    for (TAPNQuery q : queriesToDelete) {
                        Command cmd = new DeleteQueriesCommand(currentTab, Arrays.asList(q));
                        cmd.redo();
                        getUndoManager().addEdit(cmd);
                    }
                }

                deleteSelection(selection);
                network().buildConstraints();
            }
        }

        //XXX: function moved from undoManager --kyrke - 2019-07-06
        private void deleteObject(PetriNetObject pnObject) {
            if (pnObject instanceof ArcPathPoint) {

                ArcPathPoint arcPathPoint = (ArcPathPoint)pnObject;

                //If the arc is marked for deletion, skip deleting individual arcpathpoint
                if (!(arcPathPoint.getArcPath().getArc().isSelected())) {

                    //Don't delete the two last arc path points
                    if (arcPathPoint.isDeleteable()) {
                        Command cmd = new DeleteArcPathPointEdit(
                            arcPathPoint.getArcPath().getArc(),
                            arcPathPoint,
                            arcPathPoint.getIndex(),
                            getModel()
                        );
                        cmd.redo();
                        getUndoManager().addEdit(cmd);
                    }
                }
            }else{
                //The list of selected objects is not updated when a element is deleted
                //We might delete the same object twice, which will give an error
                //Eg. a place with output arc is deleted (deleted also arc) while arc is also selected.
                //There is properly a better way to track this (check model?) but while refactoring we will keeps it close
                //to the orginal code -- kyrke 2019-06-27
                if (!pnObject.isDeleted()) {
                    Command cmd = null;
                    if(pnObject instanceof TimedPlaceComponent){
                        TimedPlaceComponent tp = (TimedPlaceComponent)pnObject;
                        cmd = new DeleteTimedPlaceCommand(tp, guiModelToModel.get(getModel()), getModel());
                    }else if(pnObject instanceof TimedTransitionComponent){
                        TimedTransitionComponent transition = (TimedTransitionComponent)pnObject;
                        cmd = new DeleteTimedTransitionCommand(transition, transition.underlyingTransition().model(), getModel());
                    }else if(pnObject instanceof TimedTransportArcComponent){
                        TimedTransportArcComponent transportArc = (TimedTransportArcComponent)pnObject;
                        cmd = new DeleteTransportArcCommand(transportArc, transportArc.underlyingTransportArc(), transportArc.underlyingTransportArc().model(), getModel());
                    }else if(pnObject instanceof TimedInhibitorArcComponent){
                        TimedInhibitorArcComponent tia = (TimedInhibitorArcComponent)pnObject;
                        cmd = new DeleteTimedInhibitorArcCommand(tia, tia.underlyingTimedInhibitorArc().model(), getModel());
                    }else if(pnObject instanceof TimedInputArcComponent){
                        TimedInputArcComponent tia = (TimedInputArcComponent)pnObject;
                        cmd = new DeleteTimedInputArcCommand(tia, tia.underlyingTimedInputArc().model(), getModel());
                    }else if(pnObject instanceof TimedOutputArcComponent){
                        TimedOutputArcComponent toa = (TimedOutputArcComponent)pnObject;
                        cmd = new DeleteTimedOutputArcCommand(toa, toa.underlyingArc().model(), getModel());
                    }else if(pnObject instanceof AnnotationNote){
                        cmd = new DeleteAnnotationNoteCommand((AnnotationNote)pnObject, getModel());
                    }else{
                        throw new RuntimeException("This should not be possible");
                    }
                    cmd.redo();
                    getUndoManager().addEdit(cmd);
                }
            }
        }


        private void deleteSelection(PetriNetObject pnObject) {
            if(pnObject instanceof PlaceTransitionObject){
                PlaceTransitionObject pto = (PlaceTransitionObject)pnObject;

                ArrayList<Arc> arcsToDelete = new ArrayList<>();

                //Notice since we delte elements from the collection we can't do this while iterating, we need to
                // capture the arcs and delete them later.
                for(Arc arc : pto.getPreset()){
                    arcsToDelete.add(arc);
                }

                for(Arc arc : pto.getPostset()){
                    arcsToDelete.add(arc);
                }

                arcsToDelete.forEach(this::deleteObject);
            }

            deleteObject(pnObject);
        }

        public void deleteSelection(ArrayList<PetriNetObject> selection) {
            for (PetriNetObject pnObject : selection) {
                deleteSelection(pnObject);
            }
        }
    }


    /**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 */
	public static TabContent createNewTabFromInputStream(InputStream file, String name) throws Exception {

	    try {
			ModelLoader loader = new ModelLoader();
			LoadedModel loadedModel = loader.load(file);

            TabContent tab = new TabContent(loadedModel.network(), loadedModel.templates(), loadedModel.queries());
            tab.setInitialName(name);

			tab.selectFirstElements();

			tab.setFile(null);
			return tab;
		} catch (Exception e) {
			throw new Exception("TAPAAL encountered an error while loading the file: " + name + "\n\nPossible explanations:\n  - " + e.toString());
		}

	}

	public static TabContent createNewEmptyTab(String name){
		TabContent tab = new TabContent();
		tab.setInitialName(name);

		//Set Default Template
		String templateName = tab.drawingSurface().getNameGenerator().getNewTemplateName();
		Template template = new Template(new TimedArcPetriNet(templateName), new DataLayer(), new Zoomer());
		tab.addTemplate(template);

		return tab;
	}

	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 */

	public static TabContent createNewTabFromPNMLFile(File file) throws Exception {

		if (file != null) {
			try {

				LoadedModel loadedModel;

				PNMLoader loader = new PNMLoader();
				loadedModel = loader.load(file);

                TabContent tab = new TabContent(loadedModel.network(), loadedModel.templates(), loadedModel.queries());

                String name = null;

                if (file != null) {
                    name = file.getName().replaceAll(".pnml", ".tapn");
                }
                tab.setInitialName(name);

				tab.selectFirstElements();

				tab.setMode(Pipe.ElementType.SELECT);

                //appView.updatePreferredSize(); //XXX 2018-05-23 kyrke seems not to be needed
                name = name.replace(".pnml",".tapn"); // rename .pnml input file to .tapn
                return tab;

			} catch (Exception e) {
				throw new Exception("TAPAAL encountered an error while loading the file: " + file.getName() + "\n\nPossible explanations:\n  - " + e.toString());
			}
		}
		return null;
	}

	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 */
	//XXX should properly be in controller?
	public static TabContent createNewTabFromFile(File file) throws Exception {
		try {
			String name = file.getName();
			boolean showFileEndingChangedMessage = false;

			if(name.toLowerCase().endsWith(".xml")){
				name = name.substring(0, name.lastIndexOf('.')) + ".tapn";
				showFileEndingChangedMessage = true;
			}

			InputStream stream = new FileInputStream(file);
			TabContent tab = createNewTabFromInputStream(stream, name);
			if (tab != null && !showFileEndingChangedMessage) tab.setFile(file);

			showFileEndingChangedMessage(showFileEndingChangedMessage);

			return tab;
		}catch (FileNotFoundException e) {
			throw new FileNotFoundException("TAPAAL encountered an error while loading the file: " + file.getName() + "\n\nFile not found:\n  - " + e.toString());
		}
	}

	private static void showFileEndingChangedMessage(boolean showMessage) {
		if(showMessage) {
			//We thread this so it does not block the EDT
			new Thread(new Runnable() {
				@Override
				public void run() {
					CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					new MessengerImpl().displayInfoMessage("We have changed the ending of TAPAAL files from .xml to .tapn and the opened file was automatically renamed to end with .tapn.\n"
							+ "Once you save the .tapn model, we recommend that you manually delete the .xml file.", "FILE CHANGED");
				}
			}).start();
		}
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	//GUI

	private HashMap<TimedArcPetriNet, Boolean> hasPositionalInfos = new HashMap<TimedArcPetriNet, Boolean>();

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
	private AnimationControlSidePanel animControlerBox;
    private AnimationHistorySidePanel animationHistorySidePanel;

	private JScrollPane animationControllerScrollPane;
	private AnimationHistoryList abstractAnimationPane = null;
	private JPanel animationControlsPanel;
	private TransitionFireingComponent transitionFireing;

	private static final String transitionFireingName = "enabledTransitions";
	private static final String animControlName = "animControl";

	private JSplitPane animationHistorySplitter;

	private BugHandledJXMultisplitPane animatorSplitPane;

	private Integer selectedTemplate = 0;
	private Boolean selectedTemplateWasActive = false;
	
	private WorkflowDialog workflowDialog = null;


	private TabContent() {
	    this(new TimedArcPetriNetNetwork(), new ArrayList<>());
    }

	private TabContent(TimedArcPetriNetNetwork network, Collection<Template> templates) {

        Require.that(network != null, "network cannot be null");
        tapnNetwork = network;

        guiModels.clear();
        for (Template template : templates) {
            addGuiModel(template.model(), template.guiModel());
            zoomLevels.put(template.model(), template.zoomer());
            hasPositionalInfos.put(template.model(), template.getHasPositionalInfo());
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

	private TabContent(TimedArcPetriNetNetwork network, Collection<Template> templates, Iterable<TAPNQuery> tapnqueries) {
        this(network, templates);

        setNetwork(network, templates);
        setQueries(tapnqueries);
        setConstants(network().constants());
	}
	
	public SharedPlacesAndTransitionsPanel getSharedPlacesAndTransitionsPanel(){
		return sharedPTPanel;
	}
	
	public TemplateExplorer getTemplateExplorer(){
		return templateExplorer;
	}
	
	public void createEditorLeftPane() {

		constantsPanel = new ConstantsPane(this);
		constantsPanel.setPreferredSize(
				new Dimension(
						constantsPanel.getPreferredSize().width,
						constantsPanel.getMinimumSize().height
				)
		);

		queries = new QueryPane(new ArrayList<TAPNQuery>(), this);
		queries.setPreferredSize(
				new Dimension(
						queries.getPreferredSize().width,
						queries.getMinimumSize().height
				)
		);

		templateExplorer = new TemplateExplorer(this);
		templateExplorer.setPreferredSize(
				new Dimension(
						templateExplorer.getPreferredSize().width,
						templateExplorer.getMinimumSize().height
				)
		);

		sharedPTPanel = new SharedPlacesAndTransitionsPanel(this);
		sharedPTPanel.setPreferredSize(
				new Dimension(
						sharedPTPanel.getPreferredSize().width,
						sharedPTPanel.getMinimumSize().height
				)
		);
		
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
				panel.getMinimumSize().width,
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

	public void setDrawingSurface(DrawingSurfaceImpl drawingSurface) {
		this.drawingSurface = drawingSurface;
	}


	//XXX this is a temp solution while refactoring
	// to keep the name of the net when the when a file is not set.
	String initialName;
	public void setInitialName(String name) {
		if (name == null || name.isEmpty()) {
			name = "New Petri net " + (CreateGui.getApp().getNameCounter()) + ".tapn";
			CreateGui.getApp().incrementNameCounter();
		} else if (!name.toLowerCase().endsWith(".tapn")){
			name = name + ".tapn";
		}
		this.initialName = name;

		safeApp.ifPresent(tab -> tab.updatedTabName(this));
	}
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

	/** Creates a new animationHistory text area, and returns a reference to it */
	private void createAnimationHistory() {
        animationHistorySidePanel = new AnimationHistorySidePanel();
	}

	private void createAnimatorSplitPane() {

	    createAnimationHistory();

		if (animControlerBox == null) {
            createAnimationControlSidePanel();
        }
		if (transitionFireing == null) {
            createTransitionFireing();
        }
		
		boolean floatingDividers = false;
		if(simulatorModelRoot == null){
			Leaf templateExplorerLeaf = new Leaf(templateExplorerName);
			Leaf enabledTransitionsListLeaf = new Leaf(transitionFireingName);
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
		gbc.weighty = 0.0;
		animationControlsPanel.add(animControlerBox, gbc);

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
		transitionFireing.setPreferredSize(
		    new Dimension(
				transitionFireing.getPreferredSize().width,
				transitionFireing.getMinimumSize().height
            )
        );

        JButton dummy = new JButton("AnimatorDummy");
        dummy.setMinimumSize(templateExplorer.getMinimumSize());
        dummy.setPreferredSize(templateExplorer.getPreferredSize());
        animatorSplitPane.add(new JPanel(), templateExplorerName);

		animatorSplitPane.add(animationControlsPanel, animControlName);
		animatorSplitPane.add(transitionFireing, transitionFireingName);
		
		animatorSplitPaneScroller = createLeftScrollPane(animatorSplitPane);
		animatorSplitPane.repaint();
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
			animatorSplitPane.add(dummy, templateExplorerName);
		}

		templateExplorer.switchToEditorMode();
		this.setLeftComponent(editorSplitPaneScroller);
		//drawingSurface.repaintAll();
	}

	public AnimationHistoryList getUntimedAnimationHistory() {
		return abstractAnimationPane;
	}

	public AnimationControlSidePanel getAnimationController() {
		return animControlerBox;
	}
	
	public DelayEnabledTransitionControl getDelayEnabledTransitionControl(){
		return transitionFireing.getDelayEnabledTransitionControl();
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

	private void createAnimationControlSidePanel() {
		animControlerBox = new AnimationControlSidePanel(animator);
	}

	public AnimationHistoryList getAnimationHistorySidePanel() {
		return animationHistorySidePanel.getAnimationHistoryList();
	}

	private void createTransitionFireing() {
		transitionFireing = new TransitionFireingComponent(CreateGui.getApp().isShowingDelayEnabledTransitions());
	}

	public TransitionFireingComponent getTransitionFireingComponent() {
		return transitionFireing;
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
			Template template = new Template(net, guiModels.get(net), zoomLevels.get(net));
			template.setHasPositionalInfo(hasPositionalInfos.get(net));
			list.add(template);
		}
		return list;
	}

	public Iterable<Template> activeTemplates() {
		ArrayList<Template> list = new ArrayList<Template>();
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

	public void addGuiModel(TimedArcPetriNet net, DataLayer guiModel) {
		guiModels.put(net, guiModel);
		guiModelToModel.put(guiModel, net);
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

	private void setQueries(Iterable<TAPNQuery> queries) {
		this.queries.setQueries(queries);
	}

	public void removeQuery(TAPNQuery queryToRemove) {
		queries.removeQuery(queryToRemove);
	}

	public void addQuery(TAPNQuery query) {
		queries.addQuery(query);
	}

	private void setConstants(Iterable<Constant> constants) {
		tapnNetwork.setConstants(constants);
	}

	private void setNetwork(TimedArcPetriNetNetwork network, Collection<Template> templates) {


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

			editorSplitPane.getMultiSplitLayout().displayNode(templateExplorerName, enable);

			if (animatorSplitPane != null) {
				animatorSplitPane.getMultiSplitLayout().displayNode(templateExplorerName, enable);
			}
			makeSureEditorPanelIsVisible(templateExplorer);
		}
	}

	public void showSharedPT(boolean enable) {
	    if (enable != sharedPTPanel.isVisible()) {
            editorSplitPane.getMultiSplitLayout().displayNode(sharedPTName, enable);
            makeSureEditorPanelIsVisible(sharedPTPanel);
        }
    }

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

	public void showEnabledTransitionsList(boolean enable) {
	    //displayNode fires and relayout, so we check of value is changed
        // else elements will be set to default size.
		if (transitionFireing.isVisible() != enable) {
			animatorSplitPane.getMultiSplitLayout().displayNode(transitionFireingName, enable);
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
		TraceImportExport.exportTrace();
	}

	@Override
	public void importTrace() {
		TraceImportExport.importTrace();
	}

	@Override
	public void zoomTo(int newZoomLevel) {
		boolean didZoom = drawingSurface().getZoomController().setZoom(newZoomLevel);
		if (didZoom) {
			app.ifPresent(GuiFrameActions::updateZoomCombo);
			drawingSurface().zoomToMidPoint(); //Do Zoom
		}
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

	private boolean netChanged = false;
	@Override
	public boolean getNetChanged() {
		return netChanged;
	}

	public void setNetChanged(boolean _netChanged) {
		netChanged = _netChanged;
	}

    public void changeToTemplate(Template tapn) {
		Require.notNull(tapn, "Can't change to a Template that is null");

		drawingSurface.setModel(tapn.guiModel(), tapn.model(), tapn.zoomer());

		//If the template is currently selected
		//XXX: kyrke - 2019-07-06, templ solution while refactoring, there is properly a better way
		if (CreateGui.getCurrentTab() == this) {

			app.ifPresent(GuiFrameActions::updateZoomCombo);

			//XXX: moved from drawingsurface, temp while refactoring, there is a better way
			drawingSurface.getSelectionObject().clearSelection();

		}
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

		if (!animationmode) {
			if (numberOfActiveTemplates() > 0) {
				CreateGui.getApp().setGUIMode(GuiFrame.GUIMode.animation);

				setManager(animationModeController);

				drawingSurface().repaintAll();

				rememberSelectedTemplate();
				if (currentTemplate().isActive()){
					setSelectedTemplateWasActive();
				}

				getAnimator().reset(false);
				getAnimator().storeModel();
				getAnimator().highlightEnabledTransitions();
				getAnimator().reportBlockingPlaces();
				getAnimator().setFiringmode("Random");

				// Set a light blue backgound color for animation mode
				drawingSurface().setBackground(Pipe.ANIMATION_BACKGROUND_COLOR);
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
			} else {
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"You need at least one active template to enter simulation mode",
						"Simulation Mode Error", JOptionPane.ERROR_MESSAGE);
				animationmode = false;
				CreateGui.getApp().setGUIMode(GuiFrame.GUIMode.draw);
			}
		} else {
			drawingSurface().getSelectionObject().clearSelection();
			CreateGui.getApp().setGUIMode(GuiFrame.GUIMode.draw);
			setManager(notingManager);

			drawingSurface().setBackground(Pipe.ELEMENT_FILL_COLOUR);
			setMode(Pipe.ElementType.SELECT);

			restoreSelectedTemplate();

			// Undo/Redo is enabled based on undo/redo manager
			getUndoManager().setUndoRedoStatus();
			animationmode = false;
		}
		animator.updateAnimationButtonsEnabled(); //Update stepBack/Forward
	}

	//XXX temp while refactoring, kyrke - 2019-07-25
	@Override
	public void setMode(Pipe.ElementType mode) {

		app.ifPresent(o->o.updateMode(mode));

		//Disable selection and deselect current selection
		drawingSurface().getSelectionObject().clearSelection();

		//If pending arc draw, remove it
		if (drawingSurface().createArc != null) {
			PlaceTransitionObjectHandler.cleanupArc(drawingSurface().createArc, drawingSurface());
		}

        switch (mode) {
            case ADDTOKEN:
                setManager(new AbstractDrawingSurfaceManager() {
                    @Override
                    public void registerEvents() {
                        registerEvent(
                            e -> e.pno instanceof TimedPlaceComponent && e.a == MouseAction.clicked,
                            e -> placeClicked((TimedPlaceComponent) e.pno)
                        );
                    }

                    public void placeClicked(TimedPlaceComponent pno) {
                        Command command = new TimedPlaceMarkingEdit(pno, 1);
                        command.redo();
                        undoManager.addNewEdit(command);
                    }
                });
                break;
            case DELTOKEN:
                setManager(new AbstractDrawingSurfaceManager() {
                    @Override
                    public void registerEvents() {
                        registerEvent(
                            e -> e.pno instanceof TimedPlaceComponent && e.a == MouseAction.clicked,
                            e -> placeClicked((TimedPlaceComponent) e.pno)
                        );
                    }

                    public void placeClicked(TimedPlaceComponent pno) {
                        Command command = new TimedPlaceMarkingEdit(pno, -1);
                        command.redo();
                        undoManager.addNewEdit(command);
                    }
                });
                break;
            case TAPNPLACE:
                setManager(new CanvasPlaceDrawController());
                break;
            case TAPNTRANS:
                setManager(new CanvasTransitionDrawController());
                break;
            case ANNOTATION:
                setManager(new CanvasAnnotationNoteDrawController());
                break;
            default:
                setManager(notingManager);
                break;
        }

		if (mode == Pipe.ElementType.SELECT) {
			drawingSurface().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if (mode == Pipe.ElementType.DRAG) {
			drawingSurface().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		} else {
			drawingSurface().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
	}

	@Override
	public void showStatistics() {
        StatisticsPanel.showStatisticsPanel(drawingSurface().getModel().getStatistics());
	}

	@Override
	public void importSUMOQueries() {
		File[] files = FileBrowser.constructor("Import SUMO", "txt", FileBrowser.userPath).openFiles();
		for(File f : files){
			if(f.exists() && f.isFile() && f.canRead()){
				FileBrowser.userPath = f.getParent();
				SUMOQueryLoader.importQueries(f, network());
			}
		}
	}

	@Override
	public void importXMLQueries() {
		File[] files = FileBrowser.constructor("Import XML queries", "xml", FileBrowser.userPath).openFiles();
		for(File f : files){
			if(f.exists() && f.isFile() && f.canRead()){
				FileBrowser.userPath = f.getParent();
				XMLQueryLoader.importQueries(f, network());
			}
		}
	}

	@Override
	public void workflowAnalyse() {
		//XXX prop. should take this as argument, insted of using static accessors //kyrke 2019-11-05
		WorkflowDialog.showDialog();
	}

	public boolean isInAnimationMode() {
		return animationmode;
	}

	public Animator getAnimator() {
		return animator;
	}

	private final Animator animator = new Animator(this);


    @Override
    public void mergeNetComponents() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();

        int openCTLDialog = JOptionPane.YES_OPTION;
        boolean inlineConstants = false;

        if(!tapnNetwork.constants().isEmpty()){
            Object[] options = {
                "Yes",
                "No"};

            String optionText = "Do you want to replace constants with values?";
            openCTLDialog = JOptionPane.showOptionDialog(CreateGui.getApp(), optionText, "Merge Net Components Dialog", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if(openCTLDialog == JOptionPane.YES_OPTION){
                inlineConstants = true;
            } else if(openCTLDialog == JOptionPane.NO_OPTION){
                network.setConstants(tapnNetwork.constants());
            }
        }

        TAPNComposer composer = new TAPNComposer(new MessengerImpl(), guiModels, true, inlineConstants);
        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(tapnNetwork);

        ArrayList<Template> templates = new ArrayList<Template>(1);

        templates.add(new Template(transformedModel.value1(), composer.getGuiModel(), new Zoomer()));



        network.add(transformedModel.value1());

        NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(network, templates, new ArrayList<pipe.dataLayer.TAPNQuery>(0), network.constants());

        try {
            ByteArrayOutputStream outputStream = tapnWriter.savePNML();
            String composedName = "composed-" + CreateGui.getApp().getCurrentTabName();
            composedName = composedName.replace(".tapn", "");
            CreateGui.openNewTabFromStream(new ByteArrayInputStream(outputStream.toByteArray()), composedName);
        } catch (Exception e1) {
            System.console().printf(e1.getMessage());
        }
    }

    /* GUI Model / Actions */

	Optional<GuiFrameActions>  app = Optional.empty();
	MutableReference<SafeGuiFrameActions> safeApp = new MutableReference<>();
	@Override
	public void setApp(GuiFrameActions newApp) {
		this.app = Optional.ofNullable(newApp);
		undoManager.setApp(newApp);

		//XXX
		if (isInAnimationMode()) {
			app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.animation));
			animator.updateAnimationButtonsEnabled(); //Update stepBack/Forward
		} else {
			app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.draw));
			app.ifPresent(o->setMode(Pipe.ElementType.SELECT));
		}

	}

	@Override
	public void setSafeGuiFrameActions(SafeGuiFrameActions ref) {
		safeApp.setReference(ref);
	}

	@Override
	public void zoomOut() {
		boolean didZoom = drawingSurface().getZoomController().zoomOut();
		if (didZoom) {
			app.ifPresent(GuiFrameActions::updateZoomCombo);
			drawingSurface().zoomToMidPoint(); //Do Zoom
		}
	}

	@Override
	public void zoomIn() {
		boolean didZoom = drawingSurface().getZoomController().zoomIn();
		if (didZoom) {
			app.ifPresent(GuiFrameActions::updateZoomCombo);
			drawingSurface().zoomToMidPoint(); //Do Zoom
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
	public void timeDelay() {
		getAnimator().letTimePass(BigDecimal.ONE);
	}

	@Override
	public void delayAndFire() {
		getTransitionFireingComponent().fireSelectedTransition();
	}

	@Override
	public void undo() {

		if (!isInAnimationMode()) {

			//If arc is being drawn delete it

			if (drawingSurface().createArc == null) {
				getUndoManager().undo();
				network().buildConstraints();

			} else {

				PlaceTransitionObjectHandler.cleanupArc(drawingSurface().createArc, drawingSurface());

			}
		}


	}

	@Override
	public void redo() {

			if (!isInAnimationMode()) {

				//If arc is being drawn delete it

				if (drawingSurface().createArc == null) {
					getUndoManager().redo();
					network().buildConstraints();

				} else {

					PlaceTransitionObjectHandler.cleanupArc(drawingSurface().createArc, drawingSurface());

				}
			}
	}

    final AbstractDrawingSurfaceManager notingManager = new AbstractDrawingSurfaceManager(){
        @Override
        public void registerEvents() {
            //No-thing manager
        }
    };
	final AbstractDrawingSurfaceManager animationModeController;

	//Writes a tapaal net to a file, with the posibility to overwrite the quires
	public void writeNetToFile(File outFile, List<TAPNQuery> queriesOverwrite) {
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
					network().constants()
			);

			tapnWriter.savePNML(outFile);

			if(isInAnimationMode()){
				network().setMarking(currentMarking);
			}
		} catch (Exception e) {
			Logger.log(e);
			e.printStackTrace();
			JOptionPane.showMessageDialog(CreateGui.getApp(), e.toString(),
					"File Output Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void writeNetToFile(File outFile) {
		writeNetToFile(outFile, (List<TAPNQuery>) queries());
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
			JOptionPane.showMessageDialog(CreateGui.getApp(), e.toString(), "File Output Error", JOptionPane.ERROR_MESSAGE);
		}
	}

    @Override
    public void increaseSpacing() {
		double factor = 1.25;
		changeSpacing(factor);
		getUndoManager().addNewEdit(new ChangeSpacingEdit(factor, this));
    }

	@Override
	public void decreaseSpacing() {
		double factor = 0.8;
		changeSpacing(factor);
		getUndoManager().addNewEdit(new ChangeSpacingEdit(factor, this));
	}

	public void changeSpacing(double factor){
		for(PetriNetObject obj : this.currentTemplate().guiModel().getPetriNetObjects()){
			if(obj instanceof PlaceTransitionObject){
				obj.translate((int) (obj.getLocation().x*factor-obj.getLocation().x), (int) (obj.getLocation().y*factor-obj.getLocation().y));

				if(obj instanceof Transition){
					for(Arc arc : ((PlaceTransitionObject) obj).getPreset()){
						for(ArcPathPoint point : arc.getArcPath().getArcPathPoints()){
							point.setPointLocation((int)Math.max(point.getPoint().x*factor, point.getWidth()), (int)Math.max(point.getPoint().y*factor, point.getHeight()));
						}
					}
					for(Arc arc : ((PlaceTransitionObject) obj).getPostset()){
						for(ArcPathPoint point : arc.getArcPath().getArcPathPoints()){
							point.setPointLocation((int)Math.max(point.getPoint().x*factor, point.getWidth()), (int)Math.max(point.getPoint().y*factor, point.getHeight()));
						}
					}
				}

				((PlaceTransitionObject) obj).update(true);
			}else{
				obj.setLocation((int) (obj.getLocation().x*factor), (int) (obj.getLocation().y*factor));
			}
		}

		this.currentTemplate().guiModel().repaintAll(true);
		drawingSurface().updatePreferredSize();
	}

	public TabContent duplicateTab() {
		NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(
				network(),
				allTemplates(),
				queries(),
				network().constants()
		);

		try {
			ByteArrayOutputStream outputStream = tapnWriter.savePNML();
			String composedName = getTabTitle();
			composedName = composedName.replace(".tapn", "");
			composedName += "-untimed";
			return createNewTabFromInputStream(new ByteArrayInputStream(outputStream.toByteArray()), composedName);
		} catch (Exception e1) {
			e1.printStackTrace();
			System.console().printf(e1.getMessage());
		}
		return null;
	}

	class CanvasPlaceDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMouseClicked(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedPlace(canvas.getGuiModel(), p);
        }

        @Override
        public void registerEvents() {

        }
    }

    class CanvasTransitionDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMouseClicked(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedTransitions(drawingSurface.getGuiModel(), p);
        }

        @Override
        public void registerEvents() {

        }
    }

    class CanvasAnnotationNoteDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMouseClicked(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

           guiModelManager.addAnnotationNote(drawingSurface.getGuiModel(), p);
        }

        @Override
        public void registerEvents() {

        }
    }

	static class CanvasAnimationController extends AbstractDrawingSurfaceManager {

		private final Animator animator;

        public CanvasAnimationController(Animator animator) {
			this.animator = animator;
        }

		@Override
		public void registerEvents() {
			registerEvent(
					e -> e.a == MouseAction.clicked && e.pno instanceof TimedTransitionComponent && SwingUtilities.isLeftMouseButton(e.e),
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
	}


    MutableReference<AbstractDrawingSurfaceManager> managerRef = new MutableReference<>(notingManager);
    private void setManager(AbstractDrawingSurfaceManager newManager) {
        //De-register old manager
		managerRef.get().deregisterManager();
        managerRef.setReference(newManager);
		managerRef.get().registerManager(drawingSurface);
    }

}
