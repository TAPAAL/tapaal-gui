package net.tapaal.gui.petrinet.undo;

import java.util.List;

import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

public class RemoveQueriesCommand extends Command {
	
	private final List<TAPNQuery> queriesToRemove;
	private final PetriNetTab tabContent;


	public RemoveQueriesCommand(List<TAPNQuery> QueriesToRemove, PetriNetTab PetriNetTab) {
		this.queriesToRemove = QueriesToRemove;
		this.tabContent = PetriNetTab;
	}
	
	@Override
	public void redo() {
		for(TAPNQuery queryToRemove : queriesToRemove) {
			tabContent.removeQuery(queryToRemove);
		}
	}

	@Override
	public void undo() {
		for(TAPNQuery query : queriesToRemove)
			tabContent.addQuery(query);
	}
}
