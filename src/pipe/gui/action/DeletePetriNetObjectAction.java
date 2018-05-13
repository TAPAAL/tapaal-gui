/*
 * Created on 04-Mar-2004
 * Author is Michael Camacho
 *
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import pipe.dataLayer.TAPNQuery;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.DeleteQueriesCommand;
import dk.aau.cs.model.tapn.LocalTimedPlace;

public class DeletePetriNetObjectAction extends AbstractAction {

	private static final long serialVersionUID = 5945117611937154440L;
	private PetriNetObject selected;

	public DeletePetriNetObjectAction(PetriNetObject component) {
		selected = component;
	}

	public void actionPerformed(ActionEvent e) {
		// check if queries need to be removed
		ArrayList<PetriNetObject> selection = CreateGui.getDrawingSurface().getSelectionObject().getSelection();
		Iterable<TAPNQuery> queries = ((TabContent) CreateGui.getTabs().getSelectedComponent()).queries();
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
			CreateGui.getDrawingSurface().getUndoManager().newEdit(); // new "transaction""
			if (queriesAffected) {
				TabContent currentTab = ((TabContent) CreateGui.getTabs().getSelectedComponent());
				for (TAPNQuery q : queriesToDelete) {
					Command cmd = new DeleteQueriesCommand(currentTab, Arrays.asList(q));
					cmd.redo();
					CreateGui.getDrawingSurface().getUndoManager().addEdit(cmd);
				}
			}
			
			CreateGui.getDrawingSurface().getUndoManager().deleteSelection(CreateGui.getDrawingSurface().getSelectionObject().getSelection());
			CreateGui.getDrawingSurface().getSelectionObject().deleteSelection();
			CreateGui.getDrawingSurface().repaint();
			CreateGui.getCurrentTab().network().buildConstraints();
		}
	}
}
