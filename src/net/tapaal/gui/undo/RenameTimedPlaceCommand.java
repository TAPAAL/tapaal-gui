package net.tapaal.gui.undo;

import net.tapaal.gui.verification.TAPNQuery;
import dk.aau.cs.TCTL.visitors.RenamePlaceTCTLVisitor;
import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.model.tapn.LocalTimedPlace;

public class RenameTimedPlaceCommand extends Command {
	private final LocalTimedPlace place;
	private final String oldName;
	private final String newName;
	private final PetriNetTab tabContent;

	public RenameTimedPlaceCommand(PetriNetTab tabContent, LocalTimedPlace place, String oldName, String newName) {
		this.tabContent = tabContent;
		this.place = place;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public void redo() {
		place.setName(newName);
		updateQueries(oldName, newName);
    }

	@Override
	public void undo() {
		place.setName(oldName);
		updateQueries(newName,oldName);
    }
	
	private void updateQueries(String nameToFind, String nameToInsert){
		RenamePlaceTCTLVisitor renameVisitor = new RenamePlaceTCTLVisitor(nameToFind, nameToInsert);
		for (TAPNQuery q : tabContent.queries()) {
			q.getProperty().accept(renameVisitor, null);
		}
	}
}
