package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.TCTL.visitors.RenameTransitionTCTLVisitor;
import pipe.gui.petrinet.PetriNetTab;

public class RenameTimedTransitionCommand extends Command {
	private final TimedTransition transition;
	private final String oldName;
	private final String newName;
	private final PetriNetTab tabContent;

	public RenameTimedTransitionCommand(PetriNetTab tabContent, TimedTransition transition, String oldName, String newName) {
		this.tabContent = tabContent;
		this.transition = transition;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public void redo() {
		transition.setName(newName);
		updateQueries(oldName, newName);
	}

	@Override
	public void undo() {
		transition.setName(oldName);
		updateQueries(newName,oldName);
	}

	private void updateQueries(String nameToFind, String nameToInsert){
		RenameTransitionTCTLVisitor renameVisitor = new RenameTransitionTCTLVisitor(nameToFind, nameToInsert);
		for (TAPNQuery q : tabContent.queries()) {
			q.getProperty().accept(renameVisitor, null);
		}
	}
}
