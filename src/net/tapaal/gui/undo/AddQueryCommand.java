package net.tapaal.gui.undo;

import net.tapaal.gui.verification.TAPNQuery;
import pipe.gui.petrinet.PetriNetTab;

public class AddQueryCommand extends Command {
	private final TAPNQuery query;
	private final PetriNetTab tabContent;
	
	public AddQueryCommand(TAPNQuery query, PetriNetTab tabContent) {
		this.query = query;
		this.tabContent = tabContent;
	}
	
	@Override
	public void redo() {
		tabContent.addQuery(query);

	}

	@Override
	public void undo() {
		tabContent.removeQuery(query);
	}

}
