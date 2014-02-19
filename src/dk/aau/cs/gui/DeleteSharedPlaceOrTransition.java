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
		if(list.getSelectedValue() != null){
			ArrayList<String> affectedComponents = new ArrayList<String>(); 
			if(sharedPlacesAndTransitionsPanel.isDisplayingTransitions()){
				for(TimedTransition t : ((SharedTransition)list.getSelectedValue()).transitions()){
					affectedComponents.add(t.model().name());
				}
			} else {
				affectedComponents = ((SharedPlace)list.getSelectedValue()).getComponentsUsingThisPlace();
			}
			
			DeleteSharedResult result = new DeleteSharedResult(JOptionPane.OK_OPTION, false);
			if(!affectedComponents.isEmpty()){
				result = showDeleteDialog(affectedComponents);
			}
				
			if(result.choice == JOptionPane.OK_OPTION){
				undoManager.newEdit();
				if(sharedPlacesAndTransitionsPanel.isDisplayingTransitions()){
					deleteSharedTransition(result.deleteFromTemplates);
				}else{
					deleteSharedPlace(result.deleteFromTemplates);
				}
			}
		}
	}

	private void deleteSharedPlace(boolean deleteFromTemplates) {
		SharedPlace sharedPlace = (SharedPlace)list.getSelectedValue();
		Collection<TAPNQuery> affectedQueries = findAffectedQueries(sharedPlace);
		if(affectedQueries.size() > 0){
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
						Command cmd = createDeleteArcCommand(template, arc, tab.drawingSurface()); 
						cmd.redo();
						undoManager.addEdit(cmd);
					}

					for(Arc arc : place.getPostset()){
						Command cmd = createDeleteArcCommand(template, arc, tab.drawingSurface());
						cmd.redo();
						undoManager.addEdit(cmd);
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

	private Collection<TAPNQuery> findAffectedQueries(SharedPlace sharedPlace) {
		ArrayList<TAPNQuery> queries = new ArrayList<TAPNQuery>();
		ContainsSharedPlaceVisitor visitor = new ContainsSharedPlaceVisitor(sharedPlace.name());

		for(TAPNQuery query : tab.queries()){
			BooleanResult result = new BooleanResult();
			query.getProperty().accept(visitor, result);
			if(result.result()){
				queries.add(query);
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

	private void deleteSharedTransition(boolean deleteFromTemplates) {
		SharedTransition sharedTransition = (SharedTransition)list.getSelectedValue();
		if(deleteFromTemplates){
			for(Template template : tab.allTemplates()){ // TODO: Get rid of pipe references somehow
				TimedTransitionComponent transition = (TimedTransitionComponent)template.guiModel().getTransitionByName(sharedTransition.name());
				if(transition != null){
					undoManager.addEdit(new DeleteTimedTransitionCommand(transition, transition.underlyingTransition().model(), template.guiModel(), tab.drawingSurface()));
					transition.delete();
				}
			}
			tab.drawingSurface().repaint();
			sharedTransitionsListModel.removeElement(sharedTransition);
			undoManager.addEdit(new DeleteSharedTransitionCommand(sharedTransition, sharedTransitionsListModel));
		}else{
			Collection<TimedTransition> copy = sharedTransition.transitions();
			for(TimedTransition transition : copy){
				transition.unshare();
				undoManager.addEdit(new UnshareTransitionCommand(sharedTransition, transition));
			}
			sharedTransitionsListModel.removeElement(sharedTransition);
			undoManager.addEdit(new DeleteSharedTransitionCommand(sharedTransition, sharedTransitionsListModel));
			for(TimedTransition transition : copy){
				String name = nameGenerator.getNewTransitionName(transition.model());
				// We add this invisible transition renaming to avoid problems with undo
				undoManager.addEdit(new RenameTimedTransitionCommand(transition, name, transition.name())); 
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
