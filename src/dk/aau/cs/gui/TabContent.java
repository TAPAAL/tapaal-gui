package dk.aau.cs.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.TCTL.Parsing.ParseException;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.components.BugHandledJXMultisplitPane;
import dk.aau.cs.gui.components.NameVisibilityPanel;
import dk.aau.cs.gui.components.StatisticsPanel;
import dk.aau.cs.gui.components.TransitionFiringComponent;
import dk.aau.cs.gui.undo.*;
import dk.aau.cs.io.*;
import dk.aau.cs.io.queries.SUMOQueryLoader;
import dk.aau.cs.io.queries.XMLQueryLoader;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import net.tapaal.gui.DrawingSurfaceManager.AbstractDrawingSurfaceManager;
import net.tapaal.helpers.Reference.MutableReference;
import net.tapaal.swinghelpers.JSplitPaneFix;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Split;
import org.jetbrains.annotations.NotNull;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetWriter;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.*;
import pipe.gui.action.GuiAction;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.graphicElements.*;
import pipe.gui.graphicElements.tapn.*;
import pipe.gui.undo.*;
import pipe.gui.widgets.ConstantsPane;
import pipe.gui.widgets.QueryPane;
import pipe.gui.widgets.WorkflowDialog;
import pipe.gui.widgets.filebrowser.FileBrowser;

import java.awt.event.MouseWheelEvent;

public class TabContent extends JSplitPane implements TabContentActions{

    private MutableReference<GuiFrameControllerActions> guiFrameControllerActions = new MutableReference<>();

    public void setGuiFrameControllerActions(GuiFrameControllerActions guiFrameControllerActions) {
        this.guiFrameControllerActions.setReference(guiFrameControllerActions);
    }

    public static final class TAPNLens {
        public static final TAPNLens Default = new TAPNLens(true, true);
        public boolean isTimed() {
            return timed;
        }

        public boolean isGame() {
            return game;
        }

        private final boolean timed;
        private final boolean game;

        public TAPNLens(boolean timed, boolean game) {
            this.timed = timed;
            this.game = game;
        }
    }
    private final TAPNLens lens;

	//Model and state
	private final TimedArcPetriNetNetwork tapnNetwork;

	//XXX: Replace with bi-map
	private final HashMap<TimedArcPetriNet, DataLayer> guiModels = new HashMap<TimedArcPetriNet, DataLayer>();
	private final HashMap<DataLayer, TimedArcPetriNet> guiModelToModel = new HashMap<>();

	//XXX: should be replaced iwth DataLayer->Zoomer, TimedArcPetriNet has nothing to do with zooming
	private final HashMap<TimedArcPetriNet, Zoomer> zoomLevels = new HashMap<TimedArcPetriNet, Zoomer>();


	private final UndoManager undoManager = new UndoManager();

	private enum FeatureOption { TIME, GAME, COLOR };

    public final static class Result<T,R> {
        private final T result;
        private final boolean hasErrors;
        private final List<R> errors;

        public Result(T result) {
            hasErrors = false;
            this.result = result;
            errors = new ArrayList<>(0);
        }
        public Result(Collection<R> errors) {
            hasErrors = true;
            this.errors = new ArrayList<>(errors);
            result = null;
        }
    }
    public final static class RequirementChecker<R> {
        public final List<R> errors = new LinkedList<R>();

        public final void Not(boolean b, R s) {
            if (b) {
                errors.add(s);
            }
        }

        public final void notNull(Object c, R s) {
            if (c == null) {
                errors.add(s);
            }
        }

        public final boolean failed() {
            return errors.size() != 0;
        }
        public final List<R> getErrors() {
            return Collections.unmodifiableList(errors);
        }
    }
    private enum ModelViolation {
        
        //PlaceNotNull("Place can't be null"),
        //TransitionNotNull("Transion can't be null"),
        //ModelNotNull("Model can't be null"),
        MaxOneArcBetweenPlaceAndTransition("There is already an arc between the selected place and transition"),
        MaxOneArcBetweenTransitionAndPlace("There is already an arc between the selected transition and place"),
        CantHaveArcBetweenSharedPlaceAndTransition("You are attempting to draw an arc between a shared transition and a shared place");

        private final String errorMessage;

        ModelViolation(String s) {
            this.errorMessage = s;
        }

        public String getErrorMessage() { return this.errorMessage;}
    }
	public final GuiModelManager guiModelManager = new GuiModelManager();
	public class GuiModelManager {
	    public GuiModelManager(){

        }

        public Result<TimedPlaceComponent, ModelViolation> addNewTimedPlace(DataLayer c, Point p){
	        Require.notNull(c, "datalyer can't be null");
            Require.notNull(p, "Point can't be null");

            dk.aau.cs.model.tapn.LocalTimedPlace tp = new dk.aau.cs.model.tapn.LocalTimedPlace(drawingSurface.getNameGenerator().getNewPlaceName(guiModelToModel.get(c)));
            TimedPlaceComponent pnObject = new TimedPlaceComponent(p.x, p.y, tp, lens);
            guiModelToModel.get(c).add(tp);
            c.addPetriNetObject(pnObject);

            getUndoManager().addNewEdit(new AddTimedPlaceCommand(pnObject, guiModelToModel.get(c), c));
            return new Result<>(pnObject);
        }

        public Result<TimedTransitionComponent, ModelViolation> addNewTimedTransitions(DataLayer c, Point p, boolean isUncontrollable, boolean isUrgent) {
            dk.aau.cs.model.tapn.TimedTransition transition = new dk.aau.cs.model.tapn.TimedTransition(drawingSurface.getNameGenerator().getNewTransitionName(guiModelToModel.get(c)));

            transition.setUncontrollable(isUncontrollable);
            transition.setUrgent(isUrgent);
            TimedTransitionComponent pnObject = new TimedTransitionComponent(p.x, p.y, transition, lens);

            guiModelToModel.get(c).add(transition);
            c.addPetriNetObject(pnObject);

            getUndoManager().addNewEdit(new AddTimedTransitionCommand(pnObject, guiModelToModel.get(c), c));
            return new Result<>(pnObject);
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

        public Result<TimedInputArcComponent, ModelViolation> addTimedInputArc(@NotNull DataLayer c, @NotNull TimedPlaceComponent p, @NotNull TimedTransitionComponent t, ArcPath path) {
            Require.notNull(c, "DataLayer can't be null");
            Require.notNull(p, "Place can't be null");
            Require.notNull(t, "Transitions can't be null");

	        var require = new RequirementChecker<ModelViolation>();
            require.Not(guiModelToModel.get(c).hasArcFromPlaceToTransition(p.underlyingPlace(), t.underlyingTransition()), ModelViolation.MaxOneArcBetweenPlaceAndTransition);
            require.Not( (p.underlyingPlace().isShared() && t.underlyingTransition().isShared()), ModelViolation.CantHaveArcBetweenSharedPlaceAndTransition);

            if (require.failed()) {
                return new Result<>(require.getErrors());
            }

            TimedArcPetriNet modelNet = guiModelToModel.get(c);
            TimedInputArc tia = new TimedInputArc(
                p.underlyingPlace(),
                t.underlyingTransition(),
                TimeInterval.ZERO_INF
            );

            TimedInputArcComponent tiac = new TimedInputArcComponent(p, t, tia, lens);

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

            return new Result<>(tiac);
        }

        public Result<TimedOutputArcComponent, ModelViolation> addTimedOutputArc(DataLayer c, TimedTransitionComponent t, TimedPlaceComponent p, ArcPath path) {
            Require.notNull(c, "DataLayer can't be null");
            Require.notNull(p, "Place can't be null");
            Require.notNull(t, "Transitions can't be null");

            var require = new RequirementChecker<ModelViolation>();
            require.Not(guiModelToModel.get(c).hasArcFromTransitionToPlace(t.underlyingTransition(), p.underlyingPlace()), ModelViolation.MaxOneArcBetweenTransitionAndPlace);
            require.Not((p.underlyingPlace().isShared() && t.underlyingTransition().isShared()), ModelViolation.CantHaveArcBetweenSharedPlaceAndTransition);

            if (require.failed()) {
                return new Result<>(require.getErrors());
            }

            TimedArcPetriNet modelNet = guiModelToModel.get(c);

            TimedOutputArc toa = new TimedOutputArc(
                t.underlyingTransition(),
                p.underlyingPlace()
            );

            TimedOutputArcComponent toac = new TimedOutputArcComponent(t, p, toa);

            if (path != null) {
                toac.setArcPath(new ArcPath(toac, path));
            }

            Command edit = new AddTimedOutputArcCommand(
                toac,
                modelNet,
                c
            );
            edit.redo();
            undoManager.addNewEdit(edit);

            return new Result<>(toac);
        }

        public Result<TimedInhibitorArcComponent, ModelViolation> addInhibitorArc(DataLayer c, TimedPlaceComponent p, TimedTransitionComponent t, ArcPath path) {
            Require.notNull(c, "DataLayer can't be null");
            Require.notNull(p, "Place can't be null");
            Require.notNull(t, "Transitions can't be null");

            TimedArcPetriNet modelNet = guiModelToModel.get(c);

            var require = new RequirementChecker<ModelViolation>();
            require.Not(modelNet.hasArcFromPlaceToTransition(p.underlyingPlace(), t.underlyingTransition()), ModelViolation.MaxOneArcBetweenPlaceAndTransition);
            require.Not( (p.underlyingPlace().isShared() && t.underlyingTransition().isShared()), ModelViolation.CantHaveArcBetweenSharedPlaceAndTransition);

            if (require.failed()) {
                return new Result<>(require.getErrors());
            }

            TimedInhibitorArc tiha = new TimedInhibitorArc(
                p.underlyingPlace(),
                t.underlyingTransition()
            );

            TimedInhibitorArcComponent tihac = new TimedInhibitorArcComponent(p, t, tiha);

            if (path != null) {
                tihac.setArcPath(new ArcPath(tihac, path));
            }

            Command edit = new AddTimedInhibitorArcCommand(
                tihac,
                modelNet,
                c
            );
            edit.redo();
            undoManager.addNewEdit(edit);

            return new Result<>(tihac);
        }

        public Result<TimedTransportArcComponent, ModelViolation> addTimedTransportArc(DataLayer c, TimedPlaceComponent p1, TimedTransitionComponent t, TimedPlaceComponent p2, ArcPath path1, ArcPath path2) {
            Require.notNull(c, "DataLayer can't be null");
            Require.notNull(p1, "Place1 can't be null");
            Require.notNull(t, "Transitions can't be null");
            Require.notNull(p2, "Place2 can't be null");

            TimedArcPetriNet modelNet = guiModelToModel.get(c);

            var require = new RequirementChecker<ModelViolation>();
            require.Not(modelNet.hasArcFromPlaceToTransition(p1.underlyingPlace(), t.underlyingTransition()), ModelViolation.MaxOneArcBetweenPlaceAndTransition);
            require.Not(modelNet.hasArcFromTransitionToPlace(t.underlyingTransition(), p2.underlyingPlace()), ModelViolation.MaxOneArcBetweenTransitionAndPlace);
            require.Not((p1.underlyingPlace().isShared() && t.underlyingTransition().isShared()), ModelViolation.CantHaveArcBetweenSharedPlaceAndTransition);
            require.Not((p2.underlyingPlace().isShared() && t.underlyingTransition().isShared()), ModelViolation.CantHaveArcBetweenSharedPlaceAndTransition);

            if (require.failed()) {
                return new Result<>(require.getErrors());
            }


            int groupNr = getNextTransportArcMaxGroupNumber(p1, t);

            TransportArc tta = new TransportArc(p1.underlyingPlace(), t.underlyingTransition(), p2.underlyingPlace());

            TimedTransportArcComponent ttac1 = new TimedTransportArcComponent(p1, t, tta, groupNr);
            TimedTransportArcComponent ttac2 = new TimedTransportArcComponent(t, p2, tta, groupNr);

            ttac1.setConnectedTo(ttac2);
            ttac2.setConnectedTo(ttac1);

            if (path1 != null) {
                ttac1.setArcPath(new ArcPath(ttac1, path1));
            }
            if (path2 != null) {
                ttac2.setArcPath(new ArcPath(ttac2, path2));
            }

            //XXX: the Command should take both arcs
            Command edit = new AddTransportArcCommand(
                ttac2,
                tta,
                modelNet,
                c
            );
            edit.redo();
            undoManager.addNewEdit(edit);

            return new Result<>(ttac1);

        }

        private int getNextTransportArcMaxGroupNumber(TimedPlaceComponent p, TimedTransitionComponent t) {
            int groupMaxCounter = 0;

            for (Arc a : t.getPreset()) {
                if (a instanceof TimedTransportArcComponent && a.getTarget().equals(t)) {
                    if (((TimedTransportArcComponent) a).getGroupNr() > groupMaxCounter) {
                        groupMaxCounter = ((TimedTransportArcComponent) a).getGroupNr();
                    }
                }
            }

            return groupMaxCounter+1;
        }

        public void addToken(DataLayer d, TimedPlaceComponent p, int numberOfTokens) {
	        Require.notNull(d, "Datalayer can't be null");
	        Require.notNull(p, "TimedPlaceComponent can't be null");
	        Require.that(numberOfTokens > 0, "Number of tokens to add must be strictly greater than 0");

            Command command = new TimedPlaceMarkingEdit(p, numberOfTokens);
            command.redo();
            undoManager.addNewEdit(command);
        }

        public void removeToken(DataLayer d, TimedPlaceComponent p, int numberOfTokens) {
            Require.notNull(d, "Datalayer can't be null");
            Require.notNull(p, "TimedPlaceComponent can't be null");
            Require.that(numberOfTokens > 0, "Number of tokens to remove must be strictly greater than 0");

            //Can't remove more than the number of tokens
            int tokensToRemove = Math.min(numberOfTokens, p.getNumberOfTokens());

            //Ignore if number of tokens to remove is 0
            if (tokensToRemove > 0) {
                Command command = new TimedPlaceMarkingEdit(p, -tokensToRemove);
                command.redo();
                undoManager.addNewEdit(command);
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

        public void toggleUncontrollableTrans() {
            ArrayList<PetriNetObject> selection = drawingSurface().getSelectionObject().getSelection();
            TabContent currentTab = TabContent.this;
            getUndoManager().newEdit();

            for (PetriNetObject o : selection) {
                if (o instanceof TimedTransitionComponent) {
                    TimedTransitionComponent transition = (TimedTransitionComponent) o;
                    Command cmd = new ToggleTransitionUncontrollable(transition.underlyingTransition(), currentTab);

                    cmd.redo();
                    getUndoManager().addEdit(cmd);
                }
            }
            repaint();
        }

        public void toggleUrgentTrans() {
            ArrayList<PetriNetObject> selection = drawingSurface().getSelectionObject().getSelection();
            TabContent currentTab = TabContent.this;
            getUndoManager().newEdit();

            for (PetriNetObject o : selection) {
                if (o instanceof TimedTransitionComponent) {
                    TimedTransitionComponent transition = (TimedTransitionComponent) o;
                    if(!transition.underlyingTransition().hasUntimedPreset()) {
                        JOptionPane.showMessageDialog(null,"Incoming arcs to urgent transitions must have the interval [0,\u221e).", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Command cmd = new ToggleTransitionUrgent(transition.underlyingTransition(), currentTab);
                    cmd.redo();
                    getUndoManager().addEdit(cmd);
                }
            }
            repaint();
        }


    }


    /**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 */
	public static TabContent createNewTabFromInputStream(InputStream file, String name) throws Exception {

	    try {
			ModelLoader loader = new ModelLoader();
			LoadedModel loadedModel = loader.load(file);

			if (loadedModel == null) {
                throw new Exception("Could not open the selected file, as it does not have the correct format.");
			}

			if (loadedModel.getMessages().size() != 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        String message = "While loading the net we found one or more warnings: \n\n";
                        for (String s : loadedModel.getMessages()) {
                            message += s + "\n\n";
                        }

                        new MessengerImpl().displayInfoMessage(message, "Warning");
                    }
                }).start();
            }

            TabContent tab = new TabContent(loadedModel.network(), loadedModel.templates(), loadedModel.queries(), loadedModel.getLens());

            checkQueries(tab);

            tab.setInitialName(name);

			tab.selectFirstElements();

			tab.setFile(null);

            return tab;
		} catch (ParseException e) {
            throw new ParseException("TAPAAL encountered an error while loading the file: " + name + "\n\nPossible explanations:\n  - " + e.getMessage());
        } catch (Exception e) {
            throw e;
		}

	}

    public static TAPNLens getFileLens(InputStream file) throws Exception {
        try {
            ModelLoader loader = new ModelLoader();
            return loader.loadLens(file);
        } catch (Exception e) {
            throw e;
        }

    }

	private static void checkQueries(TabContent tab) {
        List<TAPNQuery> queriesToRemove = new ArrayList<TAPNQuery>();
        EngineSupportOptions verifyTAPNOptions= new VerifyTAPNEngineOptions();
        boolean gameChanged = false;

        EngineSupportOptions UPPAALCombiOptions= new UPPAALCombiOptions();
        EngineSupportOptions UPPAALOptimizedStandardOptions = new UPPAALOptimizedStandardOptions();
        EngineSupportOptions UPPAAALStandardOptions = new UPPAAALStandardOptions();
        EngineSupportOptions UPPAALBroadcastOptions = new UPPAALBroadcastOptions();
        EngineSupportOptions UPPAALBroadcastDegree2Options = new UPPAALBroadcastDegree2Options();
        EngineSupportOptions verifyDTAPNOptions= new VerifyDTAPNEngineOptions();
        EngineSupportOptions verifyPNOptions = new VerifyPNEngineOptions();

        EngineSupportOptions[] engineSupportOptions = new EngineSupportOptions[]{verifyDTAPNOptions,verifyTAPNOptions,UPPAALCombiOptions,UPPAALOptimizedStandardOptions,UPPAAALStandardOptions,UPPAALBroadcastOptions,UPPAALBroadcastDegree2Options,verifyPNOptions};
        TimedArcPetriNetNetwork net = tab.network();
        for (TAPNQuery q : tab.queries()) {
            boolean hasEngine = false;
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
                q.hasUntimedOnlyProperties()
            };
            for(EngineSupportOptions engine : engineSupportOptions){
                if(engine.areOptionsSupported(queryOptions)){
                    hasEngine = true;
                    break;
                }
            }
            if (!hasEngine) {
                queriesToRemove.add(q);
                tab.removeQuery(q);
            } else if (tab.lens.isGame()) {
                if (q.getProperty() instanceof TCTLEFNode || q.getProperty() instanceof TCTLEGNode) {
                    queriesToRemove.add(q);
                    tab.removeQuery(q);
                } if (q.getSearchOption().equals(TAPNQuery.SearchOption.HEURISTIC)) {
                    q.setSearchOption(TAPNQuery.SearchOption.DFS);
                    gameChanged = true;
                }
                if (q.useGCD() || q.useTimeDarts() || q.getTraceOption().equals(TAPNQuery.TraceOption.FASTEST) ||
                    !q.getReductionOption().equals(ReductionOption.VerifyTAPNdiscreteVerification) ||
                    q.isOverApproximationEnabled() || q.isUnderApproximationEnabled()) gameChanged = true;
                q.setUseGCD(false);
                q.setUseTimeDarts(false);
                q.setTraceOption(TAPNQuery.TraceOption.NONE);
                q.setReductionOption(ReductionOption.VerifyTAPNdiscreteVerification);
                q.setUseOverApproximationEnabled(false);
                q.setUseUnderApproximationEnabled(false);
            } else if (!tab.lens.isTimed()) {
                q.setReductionOption(ReductionOption.VerifyPN);
                q.setUseOverApproximationEnabled(false);
                q.setUseUnderApproximationEnabled(false);
            } else {
                if (q.getCategory() == TAPNQuery.QueryCategory.LTL) {
                    queriesToRemove.add(q);
                    tab.removeQuery(q);
                }
            }
        }
        String message = "";
        if (!queriesToRemove.isEmpty()) {
            message = "The following queries will be removed in the conversion:";
            for (TAPNQuery q : queriesToRemove) {
                message += "\n" + q.getName();
            }
        }
        if (gameChanged) {
            message += (message.length() == 0 ? "" : "\n\n");
            message += "Some options may have been changed to make the query compatible with the net features.";
        }
        if(message.length() > 0){
            new MessengerImpl().displayInfoMessage(message, "Information");
        }
	}

    public static TabContent createNewEmptyTab(String name, boolean isTimed, boolean isGame){
		TabContent tab = new TabContent(isTimed, isGame);
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

                TabContent tab = new TabContent(loadedModel.network(), loadedModel.templates(), loadedModel.queries(), loadedModel.getLens());

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

	private final HashMap<TimedArcPetriNet, Boolean> hasPositionalInfos = new HashMap<TimedArcPetriNet, Boolean>();

	private final JScrollPane drawingSurfaceScroller;
	private JScrollPane editorSplitPaneScroller;
	private JScrollPane animatorSplitPaneScroller;
	private DrawingSurfaceImpl drawingSurface;
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
	private AnimationControlSidePanel animControlerBox;
    private AnimationHistorySidePanel animationHistorySidePanel;

	private JScrollPane animationControllerScrollPane;
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

	private NameVisibilityPanel nameVisibilityPanel = null;

    private Boolean showNamesOption = null;
    private Boolean isSelectedComponentOption = null;
    private Boolean isPlaceOption = null;
    private Boolean isTransitionOption = null;

    private TabContent(boolean isTimed, boolean isGame) {
	    this(new TimedArcPetriNetNetwork(), new ArrayList<>(), new TAPNLens(isTimed,isGame));
    }

	private TabContent(TimedArcPetriNetNetwork network, Collection<Template> templates, TAPNLens lens) {

        Require.that(network != null, "network cannot be null");
        Require.notNull(lens, "Lens can't be null");

        tapnNetwork = network;
        this.lens = lens;

        guiModels.clear();
        for (Template template : templates) {
            addGuiModel(template.model(), template.guiModel());
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

        nameVisibilityPanel = new NameVisibilityPanel(this);
    }

	public TabContent(TimedArcPetriNetNetwork network, Collection<Template> templates, Iterable<TAPNQuery> tapnqueries, TAPNLens lens) {
        this(network, templates, lens);

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
	String initialName = "";
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
		if (transitionFiring == null) {
            createTransitionFiring();
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
		transitionFiring.setPreferredSize(
		    new Dimension(
				transitionFiring.getPreferredSize().width,
				transitionFiring.getMinimumSize().height
            )
        );

        JButton dummy = new JButton("AnimatorDummy");
        dummy.setMinimumSize(templateExplorer.getMinimumSize());
        dummy.setPreferredSize(templateExplorer.getPreferredSize());
        animatorSplitPane.add(new JPanel(), templateExplorerName);

		animatorSplitPane.add(animationControlsPanel, animControlName);
		animatorSplitPane.add(transitionFiring, transitionFiringName);
		
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

	private void hideTimedInformation(){
	    if(!lens.isTimed()){
            animControlerBox.setVisible(false);
        }

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

	private void createAnimationControlSidePanel() {
		animControlerBox = new AnimationControlSidePanel(animator, lens);
	}

	public AnimationHistoryList getAnimationHistorySidePanel() {
		return animationHistorySidePanel.getAnimationHistoryList();
	}

	private void createTransitionFiring() {
		transitionFiring = new TransitionFiringComponent(CreateGui.getApp().isShowingDelayEnabledTransitions(), lens);
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
		if (transitionFiring.isVisible() != enable) {
			animatorSplitPane.getMultiSplitLayout().displayNode(transitionFiringName, enable);
		}
	}
	
	public void showDelayEnabledTransitions(boolean enable){
		transitionFiring.showDelayEnabledTransitions(enable);
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

    private void createNewAndConvertUntimed() {
	    TabContent tab = duplicateTab(new TAPNLens(false, lens.isGame()), "-untimed");
        convertToUntimedTab(tab);
        guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
    }

    private void createNewAndConvertNonGame() {
        TabContent tab = duplicateTab(new TAPNLens(lens.isTimed(), false), "-nongame");
        TabTransformer.removeGameInformation(tab);
        guiFrameControllerActions.ifPresent(o -> o.openTab(tab));
    }

    @Override
    public void changeTimeFeature(boolean isTime) {
        if (isTime != lens.isTimed()) {
            if (!isTime){
                if (!network().isUntimed()){
                    String removeTimeWarning = "The net contains time information, which will be removed. Do you still wish to make the net untimed?";
                    int choice = JOptionPane.showOptionDialog(CreateGui.getApp(), removeTimeWarning, "Remove time information",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 0);
                    if (choice == 0) {
                        createNewAndConvertUntimed();
                    }
                } else {
                    createNewAndConvertUntimed();
                }
            } else {
                TabContent tab = duplicateTab(new TAPNLens(true, lens.isGame()), "-timed");
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
                    int choice = JOptionPane.showOptionDialog(CreateGui.getApp(), removeTimeWarning, "Remove game information",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 0);
                    if (choice == 0) {
                        createNewAndConvertNonGame();
                    }
                } else {
                    createNewAndConvertNonGame();
                }
            } else {
                TabContent tab = duplicateTab(new TAPNLens(lens.isTimed(), true), "-game");
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
				app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.animation));
                switchToAnimationComponents(true);

				setManager(animationModeController);

				drawingSurface().repaintAll();

				rememberSelectedTemplate();
				if (currentTemplate().isActive()){
					setSelectedTemplateWasActive();
				}

				getAnimator().reset(false);
				getAnimator().storeModel();
                getAnimator().updateFireableTransitions();
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
                app.ifPresent(o->o.setStatusBarText(textforAnimation));

                animator.updateAnimationButtonsEnabled(); //Update stepBack/Forward
            } else {
				JOptionPane.showMessageDialog(CreateGui.getApp(),
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

            switchToEditorComponents();

			setManager(notingManager);

			drawingSurface().setBackground(Pipe.ELEMENT_FILL_COLOUR);
			setMode(Pipe.ElementType.SELECT);

			restoreSelectedTemplate();

			// Undo/Redo is enabled based on undo/redo manager
			getUndoManager().setUndoRedoStatus();
			animationmode = false;
            app.ifPresent(o->o.setStatusBarText(textforDrawing));

            if (restoreWorkflowDialog()) {
                WorkflowDialog.showDialog();
            }
        }
	}

	private Pipe.ElementType editorMode = Pipe.ElementType.SELECT;

	//XXX temp while refactoring, kyrke - 2019-07-25
	@Override
	public void setMode(Pipe.ElementType mode) {

        CreateGui.guiMode = mode;
        changeStatusbarText(mode);

		//Disable selection and deselect current selection
		drawingSurface().getSelectionObject().clearSelection();
        editorMode = mode;
        updateMode();
        switch (mode) {
            case ADDTOKEN:
                setManager(new AbstractDrawingSurfaceManager() {
                    @Override
                    public void registerEvents() {
                        registerEvent(
                            e -> e.pno instanceof TimedPlaceComponent && e.a == MouseAction.pressed,
                            e -> guiModelManager.addToken(getModel(), (TimedPlaceComponent) e.pno, 1)
                        );
                    }
                });
                break;
            case DELTOKEN:
                setManager(new AbstractDrawingSurfaceManager() {
                    @Override
                    public void registerEvents() {
                        registerEvent(
                            e -> e.pno instanceof TimedPlaceComponent && e.a == MouseAction.pressed,
                            e -> guiModelManager.removeToken(getModel(), (TimedPlaceComponent) e.pno, 1)
                        );
                    }
                });
                break;
            case TAPNPLACE:
                setManager(new CanvasPlaceDrawController());
                break;
            case TAPNTRANS:
                setManager(new CanvasTransitionDrawController());
                break;
            case TAPNURGENTTRANS:
                setManager(new CanvasUrgentTransitionDrawController());
                break;
            case UNCONTROLLABLETRANS:
                setManager(new CanvasUncontrollableTransitionDrawController());
                break;
            case TAPNURGENTUNCONTROLLABLETRANS:
                setManager(new CanvasUncontrollableUrgentTransitionDrawController());
                break;
            case ANNOTATION:
                setManager(new CanvasAnnotationNoteDrawController());
                break;
            case TAPNARC:
                setManager(new CanvasArcDrawController());
                break;
            case TAPNINHIBITOR_ARC:
                setManager(new CanvasInhibitorarcDrawController());
                break;
            case TRANSPORTARC:
                setManager(new CanvasTransportarcDrawController());
                break;
            case SELECT:
                setManager(new CanvasGeneralDrawController());
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

        NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(network, templates, new ArrayList<pipe.dataLayer.TAPNQuery>(0), network.constants(), lens);

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

	private final MutableReference<GuiFrameActions> app = new MutableReference<>();
	private final MutableReference<SafeGuiFrameActions> safeApp = new MutableReference<>();
	@Override
	public void setApp(GuiFrameActions newApp) {
		app.setReference(newApp);
		undoManager.setApp(app);

		updateFeatureText();

		//XXX
		if (isInAnimationMode()) {
			app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.animation));
			animator.updateAnimationButtonsEnabled(); //Update stepBack/Forward
		} else {
			app.ifPresent(o->o.setGUIMode(GuiFrame.GUIMode.draw));
			app.ifPresent(o->setMode(Pipe.ElementType.SELECT));
		}
		app.ifPresent(o->o.registerDrawingActions(getAvailableDrawActions()));
        app.ifPresent(o->o.registerAnimationActions(getAvailableSimActions()));

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
		getTransitionFiringComponent().fireSelectedTransition();
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

    final AbstractDrawingSurfaceManager notingManager = new AbstractDrawingSurfaceManager(){
        @Override
        public void registerEvents() {
            //No-thing manager
        }
    };
	final AbstractDrawingSurfaceManager animationModeController;

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
			JOptionPane.showMessageDialog(CreateGui.getApp(), e.toString(),
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

	public TabContent duplicateTab(TAPNLens overwriteLens, String appendName) {
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

	class CanvasPlaceDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedPlace(canvas.getGuiModel(), p);
        }

        @Override
        public void registerEvents() {

        }
    }

    class CanvasTransitionDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedTransitions(drawingSurface.getGuiModel(), p, false, false);
        }

        @Override
        public void registerEvents() {

        }
    }

    class CanvasUrgentTransitionDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedTransitions(drawingSurface.getGuiModel(), p, false, true);
        }

        @Override
        public void registerEvents() {

        }
    }

    class CanvasUncontrollableTransitionDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedTransitions(drawingSurface.getGuiModel(), p, true, false);
        }

        @Override
        public void registerEvents() {

        }
    }

    class CanvasUncontrollableUrgentTransitionDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

            guiModelManager.addNewTimedTransitions(drawingSurface.getGuiModel(), p, true, true);
        }

        @Override
        public void registerEvents() {

        }
    }

    class CanvasAnnotationNoteDrawController extends AbstractDrawingSurfaceManager {

        @Override
        public void drawingSurfaceMousePressed(MouseEvent e) {
            Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

           guiModelManager.addAnnotationNote(drawingSurface.getGuiModel(), p);
        }

        @Override
        public void registerEvents() {

        }
    }

    final class CanvasInhibitorarcDrawController extends AbstractCanvasArcDrawController {

        private TimedTransitionComponent transition;
        private TimedPlaceComponent place;

        protected void transitionClicked(TimedTransitionComponent pno, MouseEvent e) {
            if (place != null && transition == null) {
                transition = pno;
                CreateGui.getDrawingSurface().clearAllPrototype();
                var result = guiModelManager.addInhibitorArc(getModel(), place, transition, arc.getArcPath());
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
                CreateGui.getDrawingSurface().addPrototype(arc);
                arc.requestFocusInWindow();
                arc.setSelectable(false);
                arc.enableDrawingKeyBindings(this::clearPendingArc);
            }
        }

        @Override
        protected void clearPendingArc() {
            super.clearPendingArc();
            CreateGui.getDrawingSurface().clearAllPrototype();
            place = null;
            transition = null;
            arc = null;
        }

    }

    abstract class AbstractCanvasArcDrawController extends AbstractDrawingSurfaceManager {
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
        }

        protected abstract void transitionClicked(TimedTransitionComponent pno, MouseEvent e);
        protected abstract void placeClicked(TimedPlaceComponent pno, MouseEvent e);

        protected void clearPendingArc() {
            connectsTo = 0;
        };

        @Override
        public void setupManager() {
            CreateGui.useExtendedBounds = true;
        }

        @Override
        public void teardownManager() {
            clearPendingArc();
            CreateGui.useExtendedBounds = false;
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
                    int x = Zoomer.getUnzoomedValue(p.x, CreateGui.getDrawingSurface().getZoom());
                    int y = Zoomer.getUnzoomedValue(p.y, CreateGui.getDrawingSurface().getZoom());

                    boolean shiftDown = e.isShiftDown();
                    //XXX: x,y is ignored is overwritten when mouse is moved, this just add a new point to the end of list
                    arc.getArcPath().addPoint(arc.getArcPath().getEndIndex(), x, y, shiftDown);
                } else if (connectsTo != 0) { // Quick draw
                    Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());

                    if (connectsTo == 1) { // Place
                        var r = guiModelManager.addNewTimedPlace(getModel(), p);
                        placeClicked(r.result, e);
                    } else { //Transition
                        var r = guiModelManager.addNewTimedTransitions(getModel(), p, false, false);
                        transitionClicked(r.result, e);
                    }
                }
            } else if (e.isControlDown()){ // Quick draw
                Point p = canvas.adjustPointToGridAndZoom(e.getPoint(), canvas.getZoom());
                var r = guiModelManager.addNewTimedPlace(getModel(), p);

                placeClicked(r.result, e);
            }
        }

        protected void showPopupIfFailed(Result<?, ModelViolation> result) {
            if (result.hasErrors) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("There was an error drawing the arc. Possible problems:");
                for (ModelViolation v : result.errors) {
                    errorMessage.append("\n  - ").append(v.getErrorMessage());
                }

                JOptionPane.showMessageDialog(
                    CreateGui.getApp(),
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

    final class CanvasArcDrawController extends AbstractCanvasArcDrawController {
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
                CreateGui.getDrawingSurface().addPrototype(arc);
                arc.requestFocusInWindow();
                arc.setSelectable(false);
                arc.enableDrawingKeyBindings(this::clearPendingArc);
            } else if (place != null && transition == null) {
                transition = pno;
                CreateGui.getDrawingSurface().clearAllPrototype();
                var result = guiModelManager.addTimedInputArc(getModel(), place, transition, arc.getArcPath());
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
                    CreateGui.getDrawingSurface().addPrototype(arc);
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
                CreateGui.getDrawingSurface().addPrototype(arc);
                arc.requestFocusInWindow();
                arc.setSelectable(false);
                arc.enableDrawingKeyBindings(this::clearPendingArc);
            } else if (transition != null && place == null) {
                place = pno;
                CreateGui.getDrawingSurface().clearAllPrototype();
                var result = guiModelManager.addTimedOutputArc(getModel(), transition, place, arc.getArcPath());
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
                    CreateGui.getDrawingSurface().addPrototype(arc);
                    arc.requestFocusInWindow();
                    arc.setSelectable(false);
                    arc.enableDrawingKeyBindings(this::clearPendingArc);
                }
            }
        }

        @Override
        protected void clearPendingArc() {
            super.clearPendingArc();
            CreateGui.getDrawingSurface().clearAllPrototype();
            place = null;
            transition = null;
            arc = null;
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
            ArrayList<PetriNetObject> selection = CreateGui.getCurrentTab().drawingSurface().getGuiModel().getPNObjects();

            for (PetriNetObject pn : selection) {
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


    MutableReference<AbstractDrawingSurfaceManager> managerRef = new MutableReference<>(notingManager);
    private void setManager(AbstractDrawingSurfaceManager newManager) {
        //De-register old manager
		managerRef.get().deregisterManager();
        managerRef.setReference(newManager);
		managerRef.get().registerManager(drawingSurface);
    }

    public void updateFeatureText() {
        boolean[] features = {lens.isTimed(), lens.isGame()};
        app.ifPresent(o->o.setFeatureInfoText(features));
    }

    public TAPNLens getLens() {
        return lens;
    }

    private void convertToUntimedTab(TabContent tab) {
        TabTransformer.removeTimingInformation(tab);
    }

    private final class CanvasTransportarcDrawController extends AbstractCanvasArcDrawController {

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
                CreateGui.getDrawingSurface().addPrototype(arc);
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
                CreateGui.getDrawingSurface().addPrototype(arc);
                arc.requestFocusInWindow();
                arc.setSelectable(false);
                arc.enableDrawingKeyBindings(this::clearPendingArc);
            } else if (transition != null && place2 == null) {
                place2 = pno;
                CreateGui.getDrawingSurface().clearAllPrototype();
                var result = guiModelManager.addTimedTransportArc(getModel(), place1, transition, place2, arc1.getArcPath(), arc2.getArcPath());
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
                    CreateGui.getDrawingSurface().addPrototype(arc);
                    arc.requestFocusInWindow();
                    arc.setSelectable(false);
                    arc.enableDrawingKeyBindings(this::clearPendingArc);
                }
            }
        }

        @Override
        protected void clearPendingArc() {
            super.clearPendingArc();
            CreateGui.getDrawingSurface().clearAllPrototype();
            place1 = place2 = null;
            transition = null;
            arc = arc1 = arc2 = null;
        }

    }

    private class CanvasGeneralDrawController extends AbstractDrawingSurfaceManager {
        @Override
        public void registerEvents() {
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
                e-> ((TimedTransitionComponent) e.pno).getMouseHandler().getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
            );
            registerEvent(
                e->e.pno instanceof TimedPlaceComponent && e.a == MouseAction.rightClicked,
                e-> ((TimedPlaceComponent) e.pno).getMouseHandler().getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
            );
            registerEvent(
                e->e.pno instanceof Arc && e.a == MouseAction.rightClicked,
                e-> ((Arc) e.pno).getMouseHandler().getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
            );
            registerEvent(
                e->e.pno instanceof ArcPathPoint && e.a == MouseAction.rightClicked,
                e-> ((ArcPathPoint) e.pno).getMouseHandler().getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
            );
            registerEvent(
                e->e.pno instanceof AnnotationNote && e.a == MouseAction.doubleClicked,
                e-> ((AnnotationNote) e.pno).enableEditMode()
            );
            registerEvent(
                e->e.pno instanceof AnnotationNote && e.a == MouseAction.rightClicked,
                e-> ((AnnotationNote) e.pno).getMouseHandler().getPopup(e.e).show(e.pno, e.e.getX(), e.e.getY())
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
                e->e.pno instanceof Arc && e.a == MouseAction.wheel,
                e->arcMouseWheel((PetriNetObject) e.pno, e.e)
            );
            registerEvent(
                e->e.pno instanceof ArcPathPoint && e.a == MouseAction.wheel,
                e->arcMouseWheel(((PetriNetObject) e.pno), e.e)
            );
        }

        private void arcMouseWheel(PetriNetObject pno, MouseEvent e) {
            pno.getParent().dispatchEvent(e);
        }

        private void timedTranstionMouseWheelWithShift(TimedTransitionComponent p, MouseWheelEvent e) {
            if (p.isSelected()) {
                int rotation = 0;
                if (e.getWheelRotation() < 0) {
                    rotation = -e.getWheelRotation() * 135;
                } else {
                    rotation = e.getWheelRotation() * 45;
                }

                CreateGui.getCurrentTab().getUndoManager().addNewEdit(((Transition) p).rotate(rotation));
            } else {
                p.getParent().dispatchEvent(e);
            }
        }

        private void timedPlaceMouseWheelWithShift(TimedPlaceComponent p, MouseWheelEvent e) {
            if (p.isSelected()) {
                if (e.getWheelRotation() < 0) {
                    guiModelManager.addToken(getModel(), p, 1);
                } else {
                    guiModelManager.removeToken(getModel(), p, 1);
                }
            } else {
                p.getParent().dispatchEvent(e);
            }
        }

        private void arcDoubleClickedWithContrl(Arc arc, MouseEvent e) {
            CreateGui.getCurrentTab().getUndoManager().addNewEdit(
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
        if (lens.isTimed() && lens.isGame()) {
            return new ArrayList<>(Arrays.asList(selectAction, timedPlaceAction, transAction, urgentTransAction, uncontrollableTransAction, uncontrollableUrgentTransAction, timedArcAction, transportArcAction, inhibarcAction, tokenAction, deleteTokenAction, annotationAction, toggleUrgentAction, toggleUncontrollableAction));
        } else if (lens.isTimed() && !lens.isGame()) {
            return new ArrayList<>(Arrays.asList(selectAction, timedPlaceAction, transAction, urgentTransAction, timedArcAction, transportArcAction, inhibarcAction, tokenAction, deleteTokenAction, annotationAction, toggleUrgentAction));
        } else if (!lens.isTimed() && lens.isGame()){
            return new ArrayList<>(Arrays.asList(selectAction, timedPlaceAction, transAction, uncontrollableTransAction, timedArcAction, inhibarcAction, tokenAction, deleteTokenAction, annotationAction, toggleUncontrollableAction));
        } else {
            return new ArrayList<>(Arrays.asList(selectAction, timedPlaceAction, transAction, timedArcAction, inhibarcAction, tokenAction, deleteTokenAction, annotationAction));
        }
    }

    public List<GuiAction> getAvailableSimActions(){
        if(lens.isTimed()){
            return new ArrayList<>(Arrays.asList(timeAction, delayFireAction));
        } else{
            delayFireAction.setName("Fire");
            delayFireAction.setTooltip("Fire Selected Transition");
            return new ArrayList<>(Arrays.asList(delayFireAction));
        }
    }

    private final GuiAction selectAction = new GuiAction("Select", "Select components (S)", "S", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.SELECT);
        }
    };
    private final GuiAction annotationAction = new GuiAction("Annotation", "Add an annotation (N)", "N", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.ANNOTATION);
        }
    };
    private final GuiAction inhibarcAction = new GuiAction("Inhibitor arc", "Add an inhibitor arc (I)", "I", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.TAPNINHIBITOR_ARC);
        }
    };
    private final GuiAction transAction = new GuiAction("Transition", "Add a transition (T)", "T", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.TAPNTRANS);
        }
    };
    private final GuiAction urgentTransAction = new GuiAction("Urgent transition", "Add an urgent transition (Y)", "Y", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.TAPNURGENTTRANS);
        }
    };
    private final GuiAction uncontrollableTransAction = new GuiAction("Uncontrollable transition", "Add an uncontrollable transition (L)", "L", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.UNCONTROLLABLETRANS);
        }
    };
    private final GuiAction uncontrollableUrgentTransAction = new GuiAction("Uncontrollable urgent transition", "Add an uncontrollable urgent transition (O)", "O", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.TAPNURGENTUNCONTROLLABLETRANS);
        }
    };
    private final GuiAction tokenAction = new GuiAction("Add token", "Add a token (+)", "typed +", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.ADDTOKEN);
        }
    };

    private final GuiAction deleteTokenAction = new GuiAction("Delete token", "Delete a token (-)", "typed -", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.DELTOKEN);
        }
    };
    private final GuiAction timedPlaceAction = new GuiAction("Place", "Add a place (P)", "P", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.TAPNPLACE);
        }
    };

    private final GuiAction timedArcAction = new GuiAction("Arc", "Add an arc (A)", "A", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.TAPNARC);
        }
    };
    private final GuiAction transportArcAction = new GuiAction("Transport arc", "Add a transport arc (R)", "R", true) {
        public void actionPerformed(ActionEvent e) {
            setMode(Pipe.ElementType.TRANSPORTARC);
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
            timeDelay();
        }
    };
    private final GuiAction delayFireAction = new GuiAction("Delay and fire", "Delay and fire selected transition", "F") {
        public void actionPerformed(ActionEvent e) {
            delayAndFire();
        }
    };

    public void updateMode() {
        // deselect other actions
        selectAction.setSelected(CreateGui.guiMode == Pipe.ElementType.SELECT);
        transAction.setSelected(editorMode == Pipe.ElementType.TAPNTRANS);
        urgentTransAction.setSelected(editorMode == Pipe.ElementType.TAPNURGENTTRANS);
        uncontrollableTransAction.setSelected(editorMode == Pipe.ElementType.UNCONTROLLABLETRANS);
        uncontrollableUrgentTransAction.setSelected(editorMode == Pipe.ElementType.TAPNURGENTUNCONTROLLABLETRANS);
        timedPlaceAction.setSelected(editorMode == Pipe.ElementType.TAPNPLACE);
        timedArcAction.setSelected(editorMode == Pipe.ElementType.TAPNARC);
        transportArcAction.setSelected(editorMode == Pipe.ElementType.TRANSPORTARC);
        inhibarcAction.setSelected(editorMode == Pipe.ElementType.TAPNINHIBITOR_ARC);
        tokenAction.setSelected(editorMode == Pipe.ElementType.ADDTOKEN);
        deleteTokenAction.setSelected(editorMode == Pipe.ElementType.DELTOKEN);
        annotationAction.setSelected(editorMode == Pipe.ElementType.ANNOTATION);
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


    public static final String textforDrawing = "Drawing Mode: Click on a button to start adding components to the Editor";
    public static final String textforPlace = "Place Mode: Right click on a place to see menu options ";
    public static final String textforTAPNPlace = "Place Mode: Right click on a place to see menu options ";
    public static final String textforTrans = "Transition Mode: Right click on a transition to see menu options [Mouse wheel -> rotate]";
    public static final String textforTimedTrans = "Timed Transition Mode: Right click on a transition to see menu options [Mouse wheel -> rotate]";
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

    public void changeStatusbarText(Pipe.ElementType type) {
        switch (type) {
            case UNCONTROLLABLETRANS:
                app.ifPresent(o14 -> o14.setStatusBarText(textforUncontrollableTrans));

            case PLACE:
                app.ifPresent(o13 -> o13.setStatusBarText(textforPlace));
                break;

            case TAPNPLACE:
                app.ifPresent(o12 -> o12.setStatusBarText(textforTAPNPlace));
                break;

            case IMMTRANS:
            case TAPNTRANS:
                app.ifPresent(o11 -> o11.setStatusBarText(textforTrans));
                break;

            case TIMEDTRANS:
                app.ifPresent(o10 -> o10.setStatusBarText(textforTimedTrans));
                break;

            case ARC:
            case TAPNARC:
                app.ifPresent(o9 -> o9.setStatusBarText(textforArc));
                break;

            case TRANSPORTARC:
                app.ifPresent(o8 -> o8.setStatusBarText(textforTransportArc));
                break;

            case TAPNINHIBITOR_ARC:
            case INHIBARC:
                app.ifPresent(o7 -> o7.setStatusBarText(textforInhibArc));
                break;

            case ADDTOKEN:
                app.ifPresent(o6 -> o6.setStatusBarText(textforAddtoken));
                break;

            case DELTOKEN:
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

}
