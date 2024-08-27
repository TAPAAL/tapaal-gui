package net.tapaal.gui.petrinet.undo;

import java.util.Collection;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import pipe.gui.petrinet.PetriNetTab;

public class DeleteQueriesCommand implements Command {
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
