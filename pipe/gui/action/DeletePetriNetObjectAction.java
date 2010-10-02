/*
 * Created on 04-Mar-2004
 * Author is Michael Camacho
 *
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TimedPlace;
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
		ArrayList<TAPNQuery> queries = CreateGui.getModel().getQueries();
		ArrayList<TAPNQuery> queriesToDelete = new ArrayList<TAPNQuery>();
		boolean queriesAffected = false;
		
		for (PetriNetObject p : selection) {
			if(p instanceof TimedPlace)
			{
				for (TAPNQuery q : queries) {
					if(q.getProperty().containsAtomicPropWithSpecificPlace(p.getName())){ 	// matches(".*" + p.getName() + "[^\\_a-zA-Z0-9].*")){
						queriesAffected = true;
						queriesToDelete.add(q);
					}
				}
			}
		}

		int choice = queriesAffected ? JOptionPane.showConfirmDialog(CreateGui.getApp(), "All queries associated with the currently selected objects will also be removed.\n\n Are you sure you want to remove the current selection and all associated queries?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) : JOptionPane.YES_OPTION;

		if(choice == JOptionPane.YES_OPTION)
		{
			CreateGui.getView().getUndoManager().newEdit(); // new "transaction""
			
			if(CreateGui.getView().getSelectionObject().getSelectionCount() <= 1)
			{
				if(queriesAffected){
					CreateGui.getModel().getQueries().removeAll(queriesToDelete);
					CreateGui.createLeftPane();
				}

				CreateGui.getView().getUndoManager().deleteSelection(selected);
				selected.delete();
			}
			else
			{
				if(queriesAffected){
					CreateGui.getModel().getQueries().removeAll(queriesToDelete);
					CreateGui.createLeftPane();
				}
				CreateGui.getView().getUndoManager().deleteSelection(CreateGui.getView().getSelectionObject().getSelection());
				CreateGui.getView().getSelectionObject().deleteSelection();
			}
		}
	}
}


