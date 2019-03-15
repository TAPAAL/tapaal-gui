package dk.aau.cs.gui;

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
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.DeleteQueriesCommand;
import dk.aau.cs.gui.undo.DeleteSharedPlaceCommand;
import dk.aau.cs.gui.undo.DeleteSharedTransitionCommand;
import dk.aau.cs.gui.undo.RenameTimedPlaceCommand;
import dk.aau.cs.gui.undo.RenameTimedTransitionCommand;
import dk.aau.cs.gui.undo.UnsharePlaceCommand;
import dk.aau.cs.gui.undo.UnshareTransitionCommand;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import pipe.gui.undo.DeleteTimedInhibitorArcCommand;
import pipe.gui.undo.DeleteTimedInputArcCommand;
import pipe.gui.undo.DeleteTimedOutputArcCommand;
import pipe.gui.undo.DeleteTimedPlaceCommand;
import pipe.gui.undo.DeleteTimedTransitionCommand;
import pipe.gui.undo.DeleteTransportArcCommand;
import pipe.gui.undo.UndoManager;

public class DeleteSharedPlaceOrTransition implements ActionListener{
	
	private static final String TRANSITION_IS_USED_MESSAGE = "<html>The shared transition is used in one or more components.<br/>TAPAAL will unshare all transitions under this name,<br/>but leave the transitions in the components.</html>";
	private static final String PLACE_IS_USED_MESSAGE = "<html>The shared place is used in one or more components.<br/>TAPAAL will unshare all places under this name,<br/>but leave the places in the components.</html>";
	
	JList list;
	SharedPlacesAndTransitionsPanel sharedPlacesAndTransitionsPanel;
	TabContent tab;
	UndoManager undoManager;
	SharedPlacesListModel sharedPlacesListModel;
	SharedTransitionsListModel sharedTransitionsListModel;
	NameGenerator nameGenerator;
	boolean messageShown;
	
	public DeleteSharedPlaceOrTransition(JList list, SharedPlacesAndTransitionsPanel sharedPlacesAndTransitionsPanel, TabContent tab, 
			SharedPlacesListModel sharedPlacesListModel, SharedTransitionsListModel sharedTransitionsListModel, NameGenerator nameGenerator) {
		this.list = list;
		this.sharedPlacesAndTransitionsPanel = sharedPlacesAndTransitionsPanel;
		this.tab = tab;
		undoManager = tab.drawingSurface().getUndoManager();
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
		result = JOptionPane.showConfirmDialog(CreateGui.getApp(), params, "Warning", JOptionPane.WARNING_MESSAGE);
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
					affectedComponentsWithDupes.addAll(((SharedTransition)transition).getComponentsUsingThisTransition());
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
		SharedPlace sharedPlace = placeToRemove;
		if(affectedQueries.size() > 0 && messageShown == false){
			messageShown = true;
			StringBuffer buffer = new StringBuffer("The following queries contains the shared place and will also be deleted:");
			buffer.append(System.getProperty("line.separator"));
			buffer.append(System.getProperty("line.separator"));
			
			for(TAPNQuery query : affectedQueries){
				buffer.append(query.getName());
				buffer.append(System.getProperty("line.separator"));
			}
			buffer.append(System.getProperty("line.separator"));
			buffer.append("Do you want to continue?");
			int choice = JOptionPane.showConfirmDialog(CreateGui.getApp(), buffer.toString(), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(choice == JOptionPane.NO_OPTION) return;
			
			Command cmd = new DeleteQueriesCommand(tab, affectedQueries);
			cmd.redo();
			undoManager.addEdit(cmd);
		}
		if(deleteFromTemplates){
			for(Template template : tab.allTemplates()){ // TODO: Get rid of pipe references somehow
				TimedPlaceComponent place = (TimedPlaceComponent)template.guiModel().getPlaceByName(sharedPlace.name());
				if(place != null){
					for(Arc arc : place.getPreset()){
						deleteArc(arc, template);
					}

					for(Arc arc : place.getPostset()){
						deleteArc(arc, template);
					}

					Command cmd = new DeleteTimedPlaceCommand(place, template.model(), template.guiModel(), tab.drawingSurface());
					cmd.redo();
					undoManager.addEdit(cmd);
				}
			}
			tab.drawingSurface().repaint();
			sharedPlacesListModel.removeElement(sharedPlace);
			undoManager.addEdit(new DeleteSharedPlaceCommand(sharedPlace, sharedPlacesListModel));
		}else{
			Hashtable<LocalTimedPlace, String> createdPlaces = new Hashtable<LocalTimedPlace, String>();
			for(Template template : tab.allTemplates()){
				TimedPlace place = template.model().getPlaceByName(sharedPlace.name());
				TimedPlaceComponent component = (TimedPlaceComponent) template.guiModel().getPlaceByName(sharedPlace.name());
				if(place != null){
					String name = nameGenerator.getNewPlaceName(template.model());
					LocalTimedPlace localPlace = new LocalTimedPlace(name);
					createdPlaces.put(localPlace, name);
					Command cmd = new UnsharePlaceCommand(template.model(), sharedPlace, localPlace, component);
					cmd.redo();
					undoManager.addEdit(cmd);
				}
			}
			Command deleteCmd = new DeleteSharedPlaceCommand(sharedPlace, sharedPlacesListModel);
			deleteCmd.redo();
			undoManager.addEdit(deleteCmd);
			
			// We introduced temporary name before, to avoid exceptions, so we rename the places to the correct names here
			for(Entry<LocalTimedPlace, String> entry : createdPlaces.entrySet()){
				Command renameCmd = new RenameTimedPlaceCommand(tab, entry.getKey(), entry.getValue(), sharedPlace.name());
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
				if(result.result() && !(queries.contains(query))){
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
			return new DeleteTimedInhibitorArcCommand((TimedInhibitorArcComponent)arc, template.model(), template.guiModel(), drawingSurface);
		}else if(arc instanceof TimedTransportArcComponent){
			TimedTransportArcComponent component = (TimedTransportArcComponent)arc;
			return new DeleteTransportArcCommand(component, component.underlyingTransportArc(), template.model(), template.guiModel(), drawingSurface);
		}else if(arc instanceof TimedInputArcComponent){
			return new DeleteTimedInputArcCommand((TimedInputArcComponent)arc, template.model(), template.guiModel(), drawingSurface);
		}else{
			return new DeleteTimedOutputArcCommand((TimedOutputArcComponent)arc, template.model(), template.guiModel(), drawingSurface);
		}
	}
	
	private void deleteArc(Arc arc, Template template){
		Command cmd = createDeleteArcCommand(template, arc, tab.drawingSurface()); 
		cmd.redo();
		undoManager.addEdit(cmd);
	}
	
	private void deleteSharedTransition(boolean deleteFromTemplates, SharedTransition transitionToBeRemoved, Collection<TAPNQuery> affectedQueries) {
		SharedTransition sharedTransition = transitionToBeRemoved;
		if(affectedQueries.size() > 0 && !messageShown){
			messageShown = true;
	        StringBuffer buffer = new StringBuffer("The following queries contains the shared transition and will also be deleted:");
	        buffer.append(System.getProperty("line.separator"));
	        buffer.append(System.getProperty("line.separator"));
	
	        for(TAPNQuery query : affectedQueries){
	                buffer.append(query.getName());
	                buffer.append(System.getProperty("line.separator"));
	        }
	        buffer.append(System.getProperty("line.separator"));
	        buffer.append("Do you want to continue?");
	        int choice = JOptionPane.showConfirmDialog(CreateGui.getApp(), buffer.toString(), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	        if(choice == JOptionPane.NO_OPTION) return;
	
	        Command cmd = new DeleteQueriesCommand(tab, affectedQueries);
	        cmd.redo();
	        undoManager.addEdit(cmd);
		}
		if(deleteFromTemplates){
			for(Template template : tab.allTemplates()){ // TODO: Get rid of pipe references somehow
				TimedTransitionComponent transition = (TimedTransitionComponent)template.guiModel().getTransitionByName(sharedTransition.name());
				if(transition != null){
					for(Arc arc : transition.getPreset()){
						deleteArc(arc, template);
					}

					for(Arc arc : transition.getPostset()){
						deleteArc(arc, template);
					}

					undoManager.addEdit(new DeleteTimedTransitionCommand(transition, transition.underlyingTransition().model(), template.guiModel(), tab.drawingSurface()));
					transition.delete();
				}
			}
			tab.drawingSurface().repaint();
			sharedTransitionsListModel.removeElement(sharedTransition);
			undoManager.addEdit(new DeleteSharedTransitionCommand(sharedTransition, sharedTransitionsListModel));
		}else{
			
			ArrayList<TimedTransition> transitions = new ArrayList<TimedTransition> ();
			for(Template template : tab.allTemplates()) {
				TimedTransition timedTransition = template.model().getTransitionByName(sharedTransition.name());
				if(timedTransition != null)
					transitions.add(timedTransition);
			}
			for(TimedTransition transition : transitions){
				transition.unshare();
				undoManager.addEdit(new UnshareTransitionCommand(sharedTransition, transition));
			}
			sharedTransitionsListModel.removeElement(sharedTransition);
			undoManager.addEdit(new DeleteSharedTransitionCommand(sharedTransition, sharedTransitionsListModel));
			for(TimedTransition transition : transitions){
				String name = nameGenerator.getNewTransitionName(transition.model());
				// We add this invisible transition renaming to avoid problems with undo
				Command renameCommand = new RenameTimedTransitionCommand(tab, transition, name, transition.name());
				renameCommand.redo();
				undoManager.addEdit(renameCommand); 
			}
		}
	}
	
	private class DeleteSharedResult{
		public int choice;
		public boolean deleteFromTemplates;
		
		public DeleteSharedResult(int choice, boolean deleteFromTemplates) {
			this.choice = choice;
			this.deleteFromTemplates = deleteFromTemplates;
		}
	}
}
