package net.tapaal.gui.undo;

import java.util.Collection;

import net.tapaal.gui.verification.TAPNQuery;
import pipe.gui.petrinet.PetriNetTab;

public class DeleteQueriesCommand extends Command {
	private final Collection<TAPNQuery> queries;
	private final PetriNetTab tab;
	
	public DeleteQueriesCommand(PetriNetTab tab, Collection<TAPNQuery> queries){
		this.tab = tab;
		this.queries = queries;
	}

	@Override
	public void redo() {
		for(TAPNQuery query : queries){
			tab.removeQuery(query);
		}
	}

	@Override
	public void undo() {
		for(TAPNQuery query : queries){
			tab.addQuery(query);
		}
	}
	
	
}
