/*
 * Created on 04-Mar-2004
 * Author is Michael Camacho
 *
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import dk.aau.cs.gui.TabContent;

import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.gui.CreateGui;


public class DeletePetriNetObjectAction 
extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5945117611937154440L;
	private PetriNetObject selected;


	public DeletePetriNetObjectAction(PetriNetObject component) {
		selected = component;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		// check if queries need to be removed
		ArrayList<PetriNetObject> selection = CreateGui.getView().getSelectionObject().getSelection();
		Iterable<TAPNQuery> queries = ((TabContent)CreateGui.getTab().getSelectedComponent()).queries();
		HashSet<TAPNQuery> queriesToDelete = new HashSet<TAPNQuery>();
		boolean queriesAffected = false;
		
		for (PetriNetObject p : selection) {
			if(p instanceof TimedPlaceComponent)
			{
				for (TAPNQuery q : queries) {
					if(q.getProperty().containsAtomicPropWithSpecificPlace(p.getName())){ 	// matches(".*" + p.getName() + "[^\\_a-zA-Z0-9].*")){
						queriesAffected = true;
						queriesToDelete.add(q);
					}
				}
			}
		}
		StringBuilder s = new StringBuilder();
		s.append("The following queries are associated with the currently selected objects:\n\n");
		for (TAPNQuery q : queriesToDelete) {
			s.append(q.getName());
			s.append("\n");
		}
		s.append("\nAre you sure you want to remove the current selection and all associated queries?");
		
		int choice = queriesAffected ? JOptionPane.showConfirmDialog(CreateGui.getApp(), s.toString(), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) : JOptionPane.YES_OPTION;

		if(choice == JOptionPane.YES_OPTION)
		{
			CreateGui.getView().getUndoManager().newEdit(); // new "transaction""
			
			if(CreateGui.getView().getSelectionObject().getSelectionCount() <= 1)
			{
				if(queriesAffected){
					TabContent currentTab = ((TabContent)CreateGui.getTab().getSelectedComponent());
					for(TAPNQuery q : queriesToDelete) {
						currentTab.removeQuery(q);
					}
				}

				CreateGui.getView().getUndoManager().deleteSelection(selected);
				selected.delete();
			}
			else
			{
				if(queriesAffected){
					TabContent currentTab = ((TabContent)CreateGui.getTab().getSelectedComponent());
					for(TAPNQuery q : queriesToDelete) {
						currentTab.removeQuery(q);
					}
				}
				CreateGui.getView().getUndoManager().deleteSelection(CreateGui.getView().getSelectionObject().getSelection());
				CreateGui.getView().getSelectionObject().deleteSelection();
			}
		}
	}
}


