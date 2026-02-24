package net.tapaal.gui.petrinet.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.ContainsSharedPlaceVisitor;
import dk.aau.cs.TCTL.visitors.ContainsSharedTransitionVisitor;
import dk.aau.cs.verification.observations.Observation;
import net.tapaal.gui.petrinet.NameGenerator;
import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.editor.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import net.tapaal.gui.petrinet.editor.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.gui.petrinet.undo.DeleteQueriesCommand;
import net.tapaal.gui.petrinet.undo.DeleteSharedPlaceCommand;
import net.tapaal.gui.petrinet.undo.DeleteSharedTransitionCommand;
import net.tapaal.gui.petrinet.undo.RenameTimedPlaceCommand;
import net.tapaal.gui.petrinet.undo.RenameTimedTransitionCommand;
import net.tapaal.gui.petrinet.undo.UnsharePlaceCommand;
import net.tapaal.gui.petrinet.undo.UnshareTransitionCommand;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.TAPAALGUI;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;
import net.tapaal.gui.petrinet.undo.DeleteTimedInhibitorArcCommand;
import net.tapaal.gui.petrinet.undo.DeleteTimedInputArcCommand;
import net.tapaal.gui.petrinet.undo.DeleteTimedOutputArcCommand;
import net.tapaal.gui.petrinet.undo.DeleteTimedPlaceCommand;
import net.tapaal.gui.petrinet.undo.DeleteTimedTransitionCommand;
import net.tapaal.gui.petrinet.undo.DeleteTransportArcCommand;
import pipe.gui.petrinet.undo.UndoManager;

public final class DeleteSharedPlaceOrTransitionAction implements ActionListener{
	
	private static final String TRANSITION_IS_USED_MESSAGE = "<html>The shared transition is used in one or more components.<br/>TAPAAL will unshare all transitions under this name,<br/>but leave the transitions in the components.</html>";
	private static final String PLACE_IS_USED_MESSAGE = "<html>The shared place is used in one or more components.<br/>TAPAAL will unshare all places under this name,<br/>but leave the places in the components.</html>";
	
	final JList list;
	final SharedPlacesAndTransitionsPanel sharedPlacesAndTransitionsPanel;
	final PetriNetTab tab;
	final UndoManager undoManager;
	final SharedPlacesListModel sharedPlacesListModel;
	final SharedTransitionsListModel sharedTransitionsListModel;
	final NameGenerator nameGenerator;
	boolean messageShown;
	
	public DeleteSharedPlaceOrTransitionAction(JList list, SharedPlacesAndTransitionsPanel sharedPlacesAndTransitionsPanel, PetriNetTab tab,
                                               SharedPlacesListModel sharedPlacesListModel, SharedTransitionsListModel sharedTransitionsListModel, NameGenerator nameGenerator) {
		this.list = list;
		this.sharedPlacesAndTransitionsPanel = sharedPlacesAndTransitionsPanel;
		this.tab = tab;
		undoManager = tab.getUndoManager();
		this.sharedPlacesListModel = sharedPlacesListModel;
		this.sharedTransitionsListModel = sharedTransitionsListModel;
		this.nameGenerator = nameGenerator;
	}
	
	public DeleteSharedResult showDeleteDialog(List<String> affectedComponents) {
		int result;
		JCheckBox checkBox = new JCheckBox("Delete from all components");

		JLabel label = new JLabel(sharedPlacesAndTransitionsPanel.isDisplayingTransitions() ? TRANSITION_IS_USED_MESSAGE : PLACE_IS_USED_MESSAGE);
		JList listOfComponents = new JList(affectedComponents.toArray());
		JScrollPane scrollPane = new JScrollPane(listOfComponents);
		Object[] params = {label, checkBox, new JLabel("Components affected:"), scrollPane};
		result = JOptionPane.showConfirmDialog(TAPAALGUI.getApp(), params, "Warning", JOptionPane.OK_CANCEL_OPTION);
		boolean deleteFromTemplates = checkBox.isSelected();
		return new DeleteSharedResult(result, deleteFromTemplates);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		messageShown = false;
		if(list.getSelectedValuesList() != null){
			ArrayList<String> affectedComponents = new ArrayList<String>();
			ArrayList<String> affectedComponentsWithDupes = new ArrayList<String>();
			if(sharedPlacesAndTransitionsPanel.isDisplayingTransitions()){
				for(Object transition : list.getSelectedValuesList()) {
					affectedComponentsWithDupes.addAll((getComponentsUsingThisTransition((SharedTransition)transition)));
				}
				for(String component : affectedComponentsWithDupes) {
					if(!(affectedComponents.contains(component))) {
						affectedComponents.add(component);
					}
				}
			} else {
				for(Object place : list.getSelectedValuesList()) {
					affectedComponentsWithDupes.addAll(((SharedPlace)place).getComponentsUsingThisPlace());
				}
				for(String component : affectedComponentsWithDupes) {
					if(!(affectedComponents.contains(component))) {
						affectedComponents.add(component);
					}
				}
			}
			
			DeleteSharedResult result = new DeleteSharedResult(JOptionPane.OK_OPTION, false);
			if(!affectedComponents.isEmpty()){
				result = showDeleteDialog(affectedComponents);
			}
				
			if(result.choice == JOptionPane.OK_OPTION){
				undoManager.newEdit();
				Collection<TAPNQuery> affectedQueries = new ArrayList<TAPNQuery>();
				
				if(sharedPlacesAndTransitionsPanel.isDisplayingTransitions()){
					affectedQueries = findAffectedTransitionQueries(list.getSelectedValuesList());
					for(Object transition : list.getSelectedValuesList()) {
						deleteSharedTransition(result.deleteFromTemplates, (SharedTransition) transition, affectedQueries);
					}
						
				}else{
					affectedQueries = findAffectedPlaceQueries(list.getSelectedValuesList());
					for(Object place : list.getSelectedValuesList()) {
						deleteSharedPlace(result.deleteFromTemplates, (SharedPlace) place, affectedQueries);
					}
				}
			}
		}
	}

	private void deleteSharedPlace(boolean deleteFromTemplates, SharedPlace placeToRemove, Collection<TAPNQuery> affectedQueries) {
		List<TAPNQuery> queriesToDelete = new ArrayList<TAPNQuery>();
		List<TAPNQuery> queriesWithObservationsToRemove = new ArrayList<TAPNQuery>();
		for (TAPNQuery q : affectedQueries) {
			ContainsSharedPlaceVisitor visitor = new ContainsSharedPlaceVisitor(placeToRemove.name());
			BooleanResult result = new BooleanResult();
			q.getProperty().accept(visitor, result);
			
			if (result.result()) {
				queriesToDelete.add(q);
			} else if (q.getSmcSettings() != null) {
				for (Observation obs : q.getSmcSettings().getObservations()) {
					if (obs.getExpression() != null && obs.getExpression().containsPlace(placeToRemove)) {
						queriesWithObservationsToRemove.add(q);
						break;
					}
				}
			}
		}

		if ((!queriesToDelete.isEmpty() || !queriesWithObservationsToRemove.isEmpty()) && !messageShown) {
			messageShown = true;
			StringBuilder buffer = new StringBuilder();

			if (!queriesToDelete.isEmpty()) {
				buffer.append("The following queries are associated with the shared place and will be deleted:\n\n");
				for (TAPNQuery query : queriesToDelete) {
					buffer.append(query.getName());
					buffer.append('\n');
				}

				buffer.append("\nAre you sure you want to remove the shared place and all associated queries?");
			} else if (!queriesWithObservationsToRemove.isEmpty()) {
				buffer.append("The following queries have observations associated with the shared place:\n\n");
				for (TAPNQuery query : queriesWithObservationsToRemove) {
					buffer.append(query.getName());
					buffer.append('\n');
				}
				
				buffer.append("\nObservations containing the removed place will be removed from these queries.");
				buffer.append("\n\nAre you sure you want to remove the shared place?");
			}

			int choice = JOptionPane.showConfirmDialog(TAPAALGUI.getApp(), buffer.toString(), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(choice == JOptionPane.NO_OPTION) return;
			
			if (!queriesToDelete.isEmpty()) {
				Command cmd = new DeleteQueriesCommand(tab, queriesToDelete);
				cmd.redo();
				undoManager.addEdit(cmd);
			}
		}
		if(deleteFromTemplates){
			for(Template template : tab.allTemplates()){ // TODO: Get rid of pipe references somehow
				TimedPlaceComponent place = (TimedPlaceComponent)template.guiModel().getPlaceByName(placeToRemove.name());
				if(place != null){
                    //XXX: we need to save arcs to delete, if we delete it while iterating pre/post set it can lead to errors
                    ArrayList<Arc> arcsToDelete = new ArrayList<>();

                    for(Arc arc : place.getPreset()){
                        arcsToDelete.add(arc);
                    }

                    for(Arc arc : place.getPostset()){
                        arcsToDelete.add(arc);
                    }
                    arcsToDelete.forEach(arc->deleteArc(arc, template));

					Command cmd = new DeleteTimedPlaceCommand(place, template.model(), template.guiModel());
					cmd.redo();
					undoManager.addEdit(cmd);
				}
			}
			tab.drawingSurface().repaint();
			sharedPlacesListModel.removeElement(placeToRemove);
			undoManager.addEdit(new DeleteSharedPlaceCommand(placeToRemove, sharedPlacesListModel));
		}else{
			Hashtable<LocalTimedPlace, String> createdPlaces = new Hashtable<LocalTimedPlace, String>();
			for(Template template : tab.allTemplates()){
				TimedPlace place = template.model().getPlaceByName(placeToRemove.name());
				TimedPlaceComponent component = (TimedPlaceComponent) template.guiModel().getPlaceByName(placeToRemove.name());
				if(place != null){
					String name = nameGenerator.getNewPlaceName(template.model());
					LocalTimedPlace localPlace = new LocalTimedPlace(name);
					createdPlaces.put(localPlace, name);
					Command cmd = new UnsharePlaceCommand(template.model(), placeToRemove, localPlace, component);
					cmd.redo();
					undoManager.addEdit(cmd);
				}
			}
			Command deleteCmd = new DeleteSharedPlaceCommand(placeToRemove, sharedPlacesListModel);
			deleteCmd.redo();
			undoManager.addEdit(deleteCmd);
			
			// We introduced temporary name before, to avoid exceptions, so we rename the places to the correct names here
			for(Entry<LocalTimedPlace, String> entry : createdPlaces.entrySet()){
				Command renameCmd = new RenameTimedPlaceCommand(tab, entry.getKey(), entry.getValue(), placeToRemove.name());
				renameCmd.redo();
				undoManager.addEdit(renameCmd);
			}
		}
	}

	private Collection<TAPNQuery> findAffectedPlaceQueries(List<Object> sharedPlaces) {
		ArrayList<TAPNQuery> queries = new ArrayList<TAPNQuery>();
		for(Object sharedPlace : sharedPlaces) {
			ContainsSharedPlaceVisitor visitor = new ContainsSharedPlaceVisitor(((SharedPlace)sharedPlace).name());
	
			for(TAPNQuery query : tab.queries()){
				BooleanResult result = new BooleanResult();
				query.getProperty().accept(visitor, result);

				boolean usedInObservation = false;
				if (query.getSmcSettings() != null) {
					for(Observation obs : query.getSmcSettings().getObservations()) {
						if (obs.getExpression() != null && obs.getExpression().containsPlace((SharedPlace)sharedPlace)) {
							usedInObservation = true;
							break;
						}
					}
				}

				if ((result.result() || usedInObservation) && !(queries.contains(query))) {
					queries.add(query);
				}
			}
		}
		return queries;
	}
        
	private Collection<TAPNQuery> findAffectedTransitionQueries(List<Object> sharedTransitions) {
		ArrayList<TAPNQuery> queries = new ArrayList<TAPNQuery>();
		for(Object sharedTransition : sharedTransitions) {
			ContainsSharedTransitionVisitor visitor = new ContainsSharedTransitionVisitor(((SharedTransition)sharedTransition).name());
			
			for(TAPNQuery query : tab.queries()){
				BooleanResult result = new BooleanResult();
				query.getProperty().accept(visitor, result);
				if(result.result() && !(queries.contains(query))){
					queries.add(query);
				}
			}
		}
		return queries;
	}
        

	private Command createDeleteArcCommand(Template template, Arc arc, DrawingSurfaceImpl drawingSurface) {
		if(arc instanceof TimedInhibitorArcComponent){
			return new DeleteTimedInhibitorArcCommand((TimedInhibitorArcComponent)arc, template.model(), template.guiModel());
		}else if(arc instanceof TimedTransportArcComponent){
			TimedTransportArcComponent component = (TimedTransportArcComponent)arc;
			return new DeleteTransportArcCommand(component, component.underlyingTransportArc(), template.model(), template.guiModel());
		}else if(arc instanceof TimedInputArcComponent){
			return new DeleteTimedInputArcCommand((TimedInputArcComponent)arc, template.model(), template.guiModel());
		}else{
			return new DeleteTimedOutputArcCommand((TimedOutputArcComponent)arc, template.model(), template.guiModel());
		}
	}
	
	private void deleteArc(Arc arc, Template template){
		Command cmd = createDeleteArcCommand(template, arc, tab.drawingSurface()); 
		cmd.redo();
		undoManager.addEdit(cmd);
	}
	
	private void deleteSharedTransition(boolean deleteFromTemplates, SharedTransition transitionToBeRemoved, Collection<TAPNQuery> affectedQueries) {
        if(affectedQueries.size() > 0 && !messageShown){
			messageShown = true;
	        StringBuilder buffer = new StringBuilder("The following queries contains the shared transition and will also be deleted:");
	        buffer.append(System.getProperty("line.separator"));
	        buffer.append(System.getProperty("line.separator"));
	
	        for(TAPNQuery query : affectedQueries){
	                buffer.append(query.getName());
	                buffer.append(System.getProperty("line.separator"));
	        }
	        buffer.append(System.getProperty("line.separator"));
	        buffer.append("Do you want to continue?");
	        int choice = JOptionPane.showConfirmDialog(TAPAALGUI.getApp(), buffer.toString(), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	        if(choice == JOptionPane.NO_OPTION) return;
	
	        Command cmd = new DeleteQueriesCommand(tab, affectedQueries);
	        cmd.redo();
	        undoManager.addEdit(cmd);
		}
		if(deleteFromTemplates){
			for(Template template : tab.allTemplates()){ // TODO: Get rid of pipe references somehow
				TimedTransitionComponent transition = (TimedTransitionComponent)template.guiModel().getTransitionByName(transitionToBeRemoved.name());
				if(transition != null){
				    //XXX: we need to save arcs to delete, if we delete it while iterating pre/post set it can lead to errors
				    ArrayList<Arc> arcsToDelete = new ArrayList<>();

					for(Arc arc : transition.getPreset()){
						arcsToDelete.add(arc);
					}

					for(Arc arc : transition.getPostset()){
						arcsToDelete.add(arc);
					}
					arcsToDelete.forEach(arc->deleteArc(arc, template));

					Command c = new DeleteTimedTransitionCommand(transition, transition.underlyingTransition().model(), template.guiModel());
					undoManager.addEdit(c);
					c.redo();
				}
			}
			tab.drawingSurface().repaint();
			sharedTransitionsListModel.removeElement(transitionToBeRemoved);
			undoManager.addEdit(new DeleteSharedTransitionCommand(transitionToBeRemoved, sharedTransitionsListModel));
		}else{
			
			ArrayList<TimedTransition> transitions = new ArrayList<TimedTransition> ();
			for(Template template : tab.allTemplates()) {
				TimedTransition timedTransition = template.model().getTransitionByName(transitionToBeRemoved.name());
				if(timedTransition != null)
					transitions.add(timedTransition);
			}
			for(TimedTransition transition : transitions){
				transition.unshare();
				undoManager.addEdit(new UnshareTransitionCommand(transitionToBeRemoved, transition));
			}
			sharedTransitionsListModel.removeElement(transitionToBeRemoved);
			undoManager.addEdit(new DeleteSharedTransitionCommand(transitionToBeRemoved, sharedTransitionsListModel));
			for(TimedTransition transition : transitions){
				String name = nameGenerator.getNewTransitionName(transition.model());
				// We add this invisible transition renaming to avoid problems with undo
				Command renameCommand = new RenameTimedTransitionCommand(tab, transition, name, transition.name());
				renameCommand.redo();
				undoManager.addEdit(renameCommand); 
			}
		}
	}

    public ArrayList<String> getComponentsUsingThisTransition(SharedTransition transition){
        ArrayList<String> components = new ArrayList<String>();
        for(Template t : tab.allTemplates()){
            TimedTransition tt = t.model().getTransitionByName(transition.name());
            if(tt != null){
                components.add(t.model().name());
            }
        }
        return components;
    }
	
	private static class DeleteSharedResult{
		public final int choice;
		public final boolean deleteFromTemplates;
		
		public DeleteSharedResult(int choice, boolean deleteFromTemplates) {
			this.choice = choice;
			this.deleteFromTemplates = deleteFromTemplates;
		}
	}
}
