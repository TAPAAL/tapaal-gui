package dk.aau.cs.gui.undo;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.TCTL.visitors.RenameTransitionTCTLVisitor;
import dk.aau.cs.gui.TabContent;

public class RenameTimedTransitionCommand extends Command {
	private final TimedTransition transition;
	private final String oldName;
	private final String newName;
	private final TabContent tabContent;

	public RenameTimedTransitionCommand(TabContent tabContent, TimedTransition transition, String oldName, String newName) {
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
