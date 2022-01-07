package dk.aau.cs.gui.undo;

import java.util.Collection;

import net.tapaal.gui.verification.TAPNQuery;
import pipe.gui.TabContent;

public class DeleteQueriesCommand extends Command {
	private final Collection<TAPNQuery> queries;
	private final TabContent tab;
	
	public DeleteQueriesCommand(TabContent tab, Collection<TAPNQuery> queries){
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
