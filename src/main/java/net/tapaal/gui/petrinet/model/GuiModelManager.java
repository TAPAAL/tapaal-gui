package net.tapaal.gui.petrinet.model;

import net.tapaal.gui.petrinet.Template;
import net.tapaal.gui.petrinet.undo.*;
import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Expressions.AllExpression;
import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.Expressions.NumberOfExpression;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.Require;
import org.jetbrains.annotations.NotNull;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.*;
import pipe.gui.petrinet.graphicElements.tapn.*;
import pipe.gui.petrinet.undo.AddAnnotationNoteCommand;
import pipe.gui.petrinet.undo.CompoundCommand;
import pipe.gui.petrinet.undo.DeleteAnnotationNoteCommand;
import pipe.gui.petrinet.undo.DeleteArcPathPointEditCommand;

import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

public class GuiModelManager {
    //XXX: Instead of having access to the whole tab, it should properly take some sort of model
    private final PetriNetTab tabContent;

    private List<Command> pendingEdits = null;

    public void startTransaction() {
        if (pendingEdits == null) {
            pendingEdits = new ArrayList<>();
        } else {
            throw new RuntimeException("Transaction already in progress");
        }
    }
    public void commit() {
        if (pendingEdits != null && pendingEdits.size() > 0) {
            tabContent.getUndoManager().addNewEdit(new CompoundCommand(pendingEdits));
            pendingEdits = null;
        }
    }
    public void abort() {
        pendingEdits = null;
    }

    private void addCommand(Command c) {
        if (pendingEdits == null) {
            tabContent.getUndoManager().addNewEdit(c);
        } else {
            pendingEdits.add(c);
        }
    }

    public GuiModelManager(PetriNetTab tabContent) {
        this.tabContent = tabContent;
    }

    public Result<TimedPlaceComponent, ModelViolation> addNewTimedPlace(DataLayer c, Point p) {
        Require.notNull(c, "datalyer can't be null");
        Require.notNull(p, "Point can't be null");

        dk.aau.cs.model.tapn.LocalTimedPlace tp = new dk.aau.cs.model.tapn.LocalTimedPlace(tabContent.getNameGenerator().getNewPlaceName(tabContent.guiModelToModel.get(c)), ColorType.COLORTYPE_DOT);
        TimedPlaceComponent pnObject = new TimedPlaceComponent(p.x, p.y, tp, tabContent.lens);
        tabContent.guiModelToModel.get(c).add(tp);
        c.addPetriNetObject(pnObject);

        addCommand(new AddTimedPlaceCommand(pnObject, tabContent.guiModelToModel.get(c), c));
        return new Result<>(pnObject);
    }

    public Result<TimedTransitionComponent, ModelViolation> addNewTimedTransitions(DataLayer c, Point p, boolean isUrgent, boolean isUncontrollable) {
        dk.aau.cs.model.tapn.TimedTransition transition = new dk.aau.cs.model.tapn.TimedTransition(tabContent.getNameGenerator().getNewTransitionName(tabContent.guiModelToModel.get(c)));

        transition.setUrgent(isUrgent);
        transition.setUncontrollable(isUncontrollable);
        transition.setUrgent(isUrgent);
        TimedTransitionComponent pnObject = new TimedTransitionComponent(p.x, p.y, transition, tabContent.lens);

        tabContent.guiModelToModel.get(c).add(transition);
        c.addPetriNetObject(pnObject);

        addCommand(new AddTimedTransitionCommand(pnObject, tabContent.guiModelToModel.get(c), c));
        return new Result<>(pnObject);
    }

    public void addAnnotationNote(DataLayer c, Point p) {
        AnnotationNote pnObject = new AnnotationNote(p.x, p.y);

        //enableEditMode open editor, retuns true of text added, else false
        //If no text is added,dont add it to model
        if (pnObject.enableEditMode(true)) {
            c.addPetriNetObject(pnObject);
            addCommand(new AddAnnotationNoteCommand(pnObject, c));
        }
    }

    public Result<TimedInputArcComponent, ModelViolation> addTimedInputArc(@NotNull DataLayer c, @NotNull TimedPlaceComponent p, @NotNull TimedTransitionComponent t, ArcPath path) {
        Require.notNull(c, "DataLayer can't be null");
        Require.notNull(p, "Place can't be null");
        Require.notNull(t, "Transitions can't be null");

        var require = new RequirementChecker<ModelViolation>();
        require.Not(tabContent.guiModelToModel.get(c).hasArcFromPlaceToTransition(p.underlyingPlace(), t.underlyingTransition()), ModelViolation.MaxOneArcBetweenPlaceAndTransition);

        if (require.failed()) {
            return new Result<>(require.getErrors());
        }

        TimedArcPetriNet modelNet = tabContent.guiModelToModel.get(c);
        ColorType ct = p.underlyingPlace().getColorType();
        Vector<ColorExpression> vecColorExpr = new Vector<>();
        vecColorExpr.add(ct.createColorExpressionForFirstColor());
        NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
        TimedInputArc tia = new TimedInputArc(
            p.underlyingPlace(),
            t.underlyingTransition(),
            TimeInterval.ZERO_INF,
            numbExpr
        );

        TimedInputArcComponent tiac = new TimedInputArcComponent(p, t, tia, tabContent.lens);

        if (path != null) {
            tiac.setArcPath(new ArcPath(tiac, path));
        }

        Command edit = new AddTimedInputArcCommand(
            tiac,
            modelNet,
            c
        );
        edit.redo();

        addCommand(edit);

        return new Result<>(tiac);
    }

    public Result<TimedOutputArcComponent, ModelViolation> addTimedOutputArc(DataLayer c, TimedTransitionComponent t, TimedPlaceComponent p, ArcPath path) {
        Require.notNull(c, "DataLayer can't be null");
        Require.notNull(p, "Place can't be null");
        Require.notNull(t, "Transitions can't be null");

        var require = new RequirementChecker<ModelViolation>();
        require.Not(tabContent.guiModelToModel.get(c).hasArcFromTransitionToPlace(t.underlyingTransition(), p.underlyingPlace()), ModelViolation.MaxOneArcBetweenTransitionAndPlace);

        if (require.failed()) {
            return new Result<>(require.getErrors());
        }

        TimedArcPetriNet modelNet = tabContent.guiModelToModel.get(c);
        ColorType ct = p.underlyingPlace().getColorType();
        Vector<ColorExpression> vecColorExpr = new Vector<>();
        vecColorExpr.add(ct.createColorExpressionForFirstColor());
        NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);

        TimedOutputArc toa = new TimedOutputArc(
            t.underlyingTransition(),
            p.underlyingPlace(),
            numbExpr
        );

        TimedOutputArcComponent toac = new TimedOutputArcComponent(t, p, toa, tabContent.lens);

        if (path != null) {
            toac.setArcPath(new ArcPath(toac, path));
        }

        Command edit = new AddTimedOutputArcCommand(
            toac,
            modelNet,
            c
        );
        edit.redo();
        addCommand(edit);

        return new Result<>(toac);
    }

    public Result<TimedInhibitorArcComponent, ModelViolation> addInhibitorArc(DataLayer c, TimedPlaceComponent p, TimedTransitionComponent t, ArcPath path) {
        Require.notNull(c, "DataLayer can't be null");
        Require.notNull(p, "Place can't be null");
        Require.notNull(t, "Transitions can't be null");

        TimedArcPetriNet modelNet = tabContent.guiModelToModel.get(c);

        var require = new RequirementChecker<ModelViolation>();
        require.Not(modelNet.hasArcFromPlaceToTransition(p.underlyingPlace(), t.underlyingTransition()), ModelViolation.MaxOneArcBetweenPlaceAndTransition);

        if (require.failed()) {
            return new Result<>(require.getErrors());
        }

        TimedInhibitorArc tiha = new TimedInhibitorArc(
            p.underlyingPlace(),
            t.underlyingTransition()
        );

        ColorType ct = tiha.source().getColorType();
        AllExpression allExpression = new AllExpression(ct);
        Vector<ColorExpression> vecColorExpr = new Vector<>();
        vecColorExpr.add(allExpression);
        NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
        tiha.setExpression(numbExpr);

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
        addCommand(edit);

        return new Result<>(tihac);
    }

    public Result<TimedTransportArcComponent, ModelViolation> addTimedTransportArc(DataLayer c, TimedPlaceComponent p1, TimedTransitionComponent t, TimedPlaceComponent p2, ArcPath path1, ArcPath path2) {
        Require.notNull(c, "DataLayer can't be null");
        Require.notNull(p1, "Place1 can't be null");
        Require.notNull(t, "Transitions can't be null");
        Require.notNull(p2, "Place2 can't be null");

        TimedArcPetriNet modelNet = tabContent.guiModelToModel.get(c);

        var require = new RequirementChecker<ModelViolation>();
        require.Not(modelNet.hasArcFromPlaceToTransition(p1.underlyingPlace(), t.underlyingTransition()), ModelViolation.MaxOneArcBetweenPlaceAndTransition);
        require.Not(modelNet.hasArcFromTransitionToPlace(t.underlyingTransition(), p2.underlyingPlace()), ModelViolation.MaxOneArcBetweenTransitionAndPlace);

        if (require.failed()) {
            return new Result<>(require.getErrors());
        }

        int groupNr = getNextTransportArcMaxGroupNumber(p1, t);

        TransportArc tta = new TransportArc(p1.underlyingPlace(), t.underlyingTransition(), p2.underlyingPlace());
        instantiateArcExpressions(p1, t, p2, tta);

        TimedTransportArcComponent ttac1 = new TimedTransportArcComponent(p1, t, tta, groupNr, tabContent.lens);
        TimedTransportArcComponent ttac2 = new TimedTransportArcComponent(t, p2, tta, groupNr, tabContent.lens);

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
        addCommand(edit);

        return new Result<>(ttac1);
    }

    private void instantiateArcExpressions(TimedPlaceComponent p1, Transition t, TimedPlaceComponent p2, TransportArc tta) {
        //make for input
        ColorType ctin = p1.underlyingPlace().getColorType();
        Vector<ColorExpression> vecColorExpr = new Vector<>();
        vecColorExpr.add(ctin.createColorExpressionForFirstColor());
        NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
        tta.setInputExpression(numbExpr);

        //make for output
        ColorType ctout = p2.underlyingPlace().getColorType();
        vecColorExpr = new Vector<>();
        vecColorExpr.add(ctout.createColorExpressionForFirstColor());
        numbExpr = new NumberOfExpression(1, vecColorExpr);
        tta.setOutputExpression(numbExpr);
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

        return groupMaxCounter + 1;
    }

    public void addToken(DataLayer d, TimedPlaceComponent p, int numberOfTokens) {
        Require.notNull(d, "Datalayer can't be null");
        Require.notNull(p, "TimedPlaceComponent can't be null");
        Require.that(numberOfTokens > 0, "Number of tokens to add must be strictly greater than 0");

        Command command = new TimedPlaceMarkingEditCommand(p, numberOfTokens);
        command.redo();
        addCommand(command);
    }

    public void removeToken(DataLayer d, TimedPlaceComponent p, int numberOfTokens) {
        Require.notNull(d, "Datalayer can't be null");
        Require.notNull(p, "TimedPlaceComponent can't be null");
        Require.that(numberOfTokens > 0, "Number of tokens to remove must be strictly greater than 0");

        //Can't remove more than the number of tokens
        int tokensToRemove = Math.min(numberOfTokens, p.getNumberOfTokens());

        //Ignore if number of tokens to remove is 0
        if (tokensToRemove > 0) {
            Command command = new TimedPlaceMarkingEditCommand(p, -tokensToRemove);
            command.redo();
            addCommand(command);
        }
    }

    public void deleteSelection() {
        // check if queries need to be removed
        ArrayList<PetriNetObject> selection = tabContent.drawingSurface().getSelectionObject().getSelection();
        Iterable<TAPNQuery> queries = tabContent.queries();
        HashSet<TAPNQuery> queriesToDelete = new HashSet<>();

        boolean queriesAffected = false;
        for (PetriNetObject pn : selection) {
            if (pn instanceof TimedPlaceComponent) {
                TimedPlaceComponent place = (TimedPlaceComponent) pn;
                if (!place.underlyingPlace().isShared()) {
                    for (TAPNQuery q : queries) {
                        if (q.getProperty().containsAtomicPropositionWithSpecificPlaceInTemplate(((LocalTimedPlace) place.underlyingPlace()).model().name(), place.underlyingPlace().name())) {
                            queriesAffected = true;
                            queriesToDelete.add(q);
                        }
                    }
                }
            } else if (pn instanceof TimedTransitionComponent) {
                TimedTransitionComponent transition = (TimedTransitionComponent) pn;
                if (!transition.underlyingTransition().isShared()) {
                    for (TAPNQuery q : queries) {
                        if (q.getProperty().containsAtomicPropositionWithSpecificTransitionInTemplate((transition.underlyingTransition()).model().name(), transition.underlyingTransition().name())) {
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
            TAPAALGUI.getApp(), s.toString(), "Warning",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
            : JOptionPane.YES_OPTION;

        if (choice == JOptionPane.YES_OPTION) {
            tabContent.getUndoManager().newEdit(); // new "transaction"
            if (queriesAffected) {
                for (TAPNQuery q : queriesToDelete) {
                    Command cmd = new DeleteQueriesCommand(tabContent, List.of(q));
                    cmd.redo();
                    tabContent.getUndoManager().addEdit(cmd);
                }
            }

            deleteSelection(selection);
            tabContent.network().buildConstraints();
        }
    }

    private void deleteObject(PetriNetObject pnObject) {
        if (pnObject instanceof ArcPathPoint) {

            ArcPathPoint arcPathPoint = (ArcPathPoint) pnObject;

            //If the arc is marked for deletion, skip deleting individual ArcPathPoint
            if (!(arcPathPoint.getArcPath().getArc().isSelected())) {

                //Don't delete the two last arc path points
                if (arcPathPoint.isDeleteable()) {
                    Command cmd = new DeleteArcPathPointEditCommand(
                        arcPathPoint.getArcPath().getArc(),
                        arcPathPoint,
                        arcPathPoint.getIndex(),
                        tabContent.getModel()
                    );
                    cmd.redo();
                    tabContent.getUndoManager().addEdit(cmd);
                }
            }
        } else {
            //The list of selected objects is not updated when a element is deleted
            //We might delete the same object twice, which will give an error
            //Eg. a place with output arc is deleted (deleted also arc) while arc is also selected.
            //There is properly a better way to track this (check model?) but while refactoring we will keeps it close
            //to the original code -- kyrke 2019-06-27
            if (!pnObject.isDeleted()) {
                Command cmd = null;
                if (pnObject instanceof TimedPlaceComponent) {
                    TimedPlaceComponent tp = (TimedPlaceComponent) pnObject;
                    cmd = new DeleteTimedPlaceCommand(tp, tabContent.guiModelToModel.get(tabContent.getModel()), tabContent.getModel());
                } else if (pnObject instanceof TimedTransitionComponent) {
                    TimedTransitionComponent transition = (TimedTransitionComponent) pnObject;
                    cmd = new DeleteTimedTransitionCommand(transition, transition.underlyingTransition().model(), tabContent.getModel());
                } else if (pnObject instanceof TimedTransportArcComponent) {
                    TimedTransportArcComponent transportArc = (TimedTransportArcComponent) pnObject;
                    cmd = deleteTransportArc(transportArc);
                } else if (pnObject instanceof TimedInhibitorArcComponent) {
                    TimedInhibitorArcComponent tia = (TimedInhibitorArcComponent) pnObject;
                    cmd = deleteArc(tia, true);
                } else if (pnObject instanceof TimedInputArcComponent) {
                    TimedInputArcComponent tia = (TimedInputArcComponent) pnObject;
                    cmd = deleteArc(tia, true);
                } else if (pnObject instanceof TimedOutputArcComponent) {
                    TimedOutputArcComponent toa = (TimedOutputArcComponent) pnObject;
                    cmd = deleteArc(toa, false);
                } else if (pnObject instanceof AnnotationNote) {
                    cmd = new DeleteAnnotationNoteCommand((AnnotationNote) pnObject, tabContent.getModel());
                } else {
                    throw new RuntimeException("This should not be possible");
                }
                cmd.redo();
                tabContent.getUndoManager().addEdit(cmd);
            }
        }
    }

    /**
     * Creates delete command for a given transport arc. 
     * If src and dst of arc is shared it will delete it across all templates where the connection exists
     * @param transportArc transport arc
     * @return delete command for the arc(s)
     */
    private Command deleteTransportArc(TimedTransportArcComponent transportArc) {
        TimedPlace sourcePlace = transportArc.underlyingTransportArc().source();
        TimedTransition transition = transportArc.underlyingTransportArc().transition();
        TimedPlace targetPlace = transportArc.underlyingTransportArc().destination();
        
        boolean isSourceShared = sourcePlace.isShared();
        boolean isTransitionShared = transition.isShared();
        boolean isTargetShared = targetPlace.isShared();
        
        if (!(isSourceShared && isTransitionShared && isTargetShared)) {
            return new DeleteTransportArcCommand(
                transportArc, 
                transportArc.underlyingTransportArc(), 
                transportArc.underlyingTransportArc().model(), 
                tabContent.getModel()
            );
        }
        
        String transitionName = transition.name();
        String inputPlaceName = sourcePlace.name();
        String outputPlaceName = targetPlace.name();
        int groupNr = transportArc.getGroupNr();
        
        List<Command> deleteCommands = new ArrayList<>();
        deleteCommands.add(new DeleteTransportArcCommand(
            transportArc, 
            transportArc.underlyingTransportArc(), 
            transportArc.underlyingTransportArc().model(), 
            tabContent.getModel()
        ));
        
        for (Template template : tabContent.allTemplates()) {
            if (!template.guiModel().equals(tabContent.getModel())) {
                findAndAddTransportArcDeleteCommand(
                    template, inputPlaceName, transitionName, outputPlaceName, 
                    groupNr, deleteCommands
                );
            }
        }
        
        return new CompoundCommand(deleteCommands);
    }

    /**
     * Creates delete command for a given arc. 
     * If src and dst of arc is shared it will delete it across all templates where the connection exists
     * @param arc input, output or inhibitor arc
     * @param isInputArc true if arc is input arc, false if it is output arc
     * @return delete command for the arc(s)
     */
    private Command deleteArc(Arc arc, boolean isInputArc) {
        boolean isInhibitorArc = arc instanceof TimedInhibitorArcComponent;

        PlaceTransitionObject source = arc.getSource();
        PlaceTransitionObject target = arc.getTarget();
        
        boolean isSourceShared, isTargetShared;
        String sourceName = "", targetName = "";
        
        if (isInputArc || isInhibitorArc) {
            TimedPlaceComponent place = (TimedPlaceComponent)source;
            TimedTransitionComponent transition = (TimedTransitionComponent)target;
            
            isSourceShared = place.underlyingPlace().isShared();
            isTargetShared = transition.underlyingTransition().isShared();
            
            if (isSourceShared) sourceName = place.getName();
            if (isTargetShared) targetName = transition.getName();
        } else {
            TimedTransitionComponent transition = (TimedTransitionComponent)source;
            TimedPlaceComponent place = (TimedPlaceComponent)target;
            
            isSourceShared = transition.underlyingTransition().isShared();
            isTargetShared = place.underlyingPlace().isShared();
            
            if (isSourceShared) sourceName = transition.getName();
            if (isTargetShared) targetName = place.getName();
        }

        if (!(isSourceShared && isTargetShared)) {
            return createSingleArcDeleteCommand(arc, isInhibitorArc, isInputArc, tabContent.getModel());
        }
        
        List<Command> deleteCommands = new ArrayList<>();
        for (Template template : tabContent.allTemplates()) {
            if (template.guiModel().equals(tabContent.getModel())) {
                deleteCommands.add(createSingleArcDeleteCommand(arc, isInhibitorArc, isInputArc, tabContent.getModel()));
            } else {
                PlaceTransitionObject templateSource, templateTarget;
                if (isInputArc || isInhibitorArc) {
                    templateSource = template.guiModel().getPlaceByName(sourceName);
                    templateTarget = template.guiModel().getTransitionByName(targetName);
                } else {
                    templateSource = template.guiModel().getTransitionByName(sourceName);
                    templateTarget = template.guiModel().getPlaceByName(targetName);
                }
                
                if (templateSource != null && templateTarget != null) {
                    findAndAddDeleteCommand(templateSource, templateTarget, isInhibitorArc, isInputArc, deleteCommands, template.guiModel());
                }
            }
        }
        
        return new CompoundCommand(deleteCommands);
    }
    
    private void findAndAddTransportArcDeleteCommand(
        Template template, String sourcePlaceName, String transitionName, 
        String targetPlaceName, int groupNr, List<Command> commands
    ) {
        TimedPlaceComponent templateSourcePlace = (TimedPlaceComponent)template.guiModel().getPlaceByName(sourcePlaceName);
        TimedTransitionComponent templateTransition = (TimedTransitionComponent)template.guiModel().getTransitionByName(transitionName);
        TimedPlaceComponent templateTargetPlace = (TimedPlaceComponent)template.guiModel().getPlaceByName(targetPlaceName);
        
        if (templateSourcePlace != null && templateTransition != null && templateTargetPlace != null) {
            for (Arc templateArc : templateTransition.getPreset()) {
                if (templateArc instanceof TimedTransportArcComponent && 
                    templateArc.getSource().equals(templateSourcePlace) &&
                    ((TimedTransportArcComponent)templateArc).getGroupNr() == groupNr) {
                    
                    commands.add(new DeleteTransportArcCommand(
                        (TimedTransportArcComponent)templateArc, 
                        ((TimedTransportArcComponent)templateArc).underlyingTransportArc(), 
                        ((TimedTransportArcComponent)templateArc).underlyingTransportArc().model(), 
                        template.guiModel()
                    ));
                    
                    break;
                }
            }
        }
    }
    
    private void findAndAddDeleteCommand(
        PlaceTransitionObject source, PlaceTransitionObject target, 
        boolean isInhibitorArc, boolean isInputArc,
        List<Command> commands, DataLayer model
    ) {
        for (Arc templateArc : source.getPostset()) {
            if (templateArc.getTarget().equals(target) &&
                ((isInhibitorArc && templateArc instanceof TimedInhibitorArcComponent) ||
                 (isInputArc && templateArc instanceof TimedInputArcComponent) ||
                 (!isInputArc && !isInhibitorArc && templateArc instanceof TimedOutputArcComponent))) {
                
                commands.add(createSingleArcDeleteCommand(templateArc, isInhibitorArc, isInputArc, model));
                break;
            }
        }
    }
    
    private Command createSingleArcDeleteCommand(Arc arc, boolean isInhibitorArc, boolean isInputArc, DataLayer model) {
        if (isInhibitorArc) {
            return new DeleteTimedInhibitorArcCommand(
                (TimedInhibitorArcComponent)arc, 
                ((TimedInhibitorArcComponent)arc).underlyingTimedInhibitorArc().model(), 
                model
            );
        } else if (isInputArc) {
            return new DeleteTimedInputArcCommand(
                (TimedInputArcComponent)arc, 
                ((TimedInputArcComponent)arc).underlyingTimedInputArc().model(), 
                model
            );
        } else {
            return new DeleteTimedOutputArcCommand(
                (TimedOutputArcComponent)arc, 
                ((TimedOutputArcComponent)arc).underlyingArc().model(), 
                model
            );
        }
    }

    private void deleteSelection(PetriNetObject pnObject) {
        if (pnObject instanceof PlaceTransitionObject) {
            PlaceTransitionObject pto = (PlaceTransitionObject) pnObject;

            ArrayList<Arc> arcsToDelete = new ArrayList<>();

            //Notice since we delete elements from the collection we can't do this while iterating, we need to
            // capture the arcs and delete them later.
            for (Arc arc : pto.getPreset()) {
                arcsToDelete.add(arc);
            }

            for (Arc arc : pto.getPostset()) {
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
        ArrayList<PetriNetObject> selection = tabContent.drawingSurface().getSelectionObject().getSelection();
        tabContent.getUndoManager().newEdit();

        for (PetriNetObject o : selection) {
            if (o instanceof TimedTransitionComponent) {
                TimedTransitionComponent transition = (TimedTransitionComponent) o;
                Command cmd = new ToggleTransitionUncontrollableCommand(transition.underlyingTransition(), tabContent);

                cmd.redo();
                tabContent.getUndoManager().addEdit(cmd);
            }
        }
        tabContent.repaint();
    }

    public void toggleUrgentTrans() {
        ArrayList<PetriNetObject> selection = tabContent.drawingSurface().getSelectionObject().getSelection();
        tabContent.getUndoManager().newEdit();

        for (PetriNetObject o : selection) {
            if (o instanceof TimedTransitionComponent) {
                TimedTransitionComponent transition = (TimedTransitionComponent) o;
                if (!transition.underlyingTransition().hasUntimedPreset()) {
                    JOptionPane.showMessageDialog(null, "Incoming arcs to urgent transitions must have the interval [0,\u221e).", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Command cmd = new ToggleTransitionUrgentCommand(transition.underlyingTransition(), tabContent);
                cmd.redo();
                tabContent.getUndoManager().addEdit(cmd);
            }
        }
        tabContent.repaint();
    }


    public void toggleArcPathPointType(ArcPathPoint pno) {
        addCommand(
            pno.togglePointType()
        );
    }
}
