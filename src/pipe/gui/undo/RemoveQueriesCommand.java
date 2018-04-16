package pipe.gui.undo;

import java.util.List;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.TAPNQuery;

public class RemoveQueriesCommand extends Command {
	
	private List<TAPNQuery> queriesToRemove;
	private TabContent tabContent;


	public RemoveQueriesCommand(List<TAPNQuery> QueriesToRemove, TabContent TabContent) {
		this.queriesToRemove = QueriesToRemove;
		this.tabContent = TabContent;
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
