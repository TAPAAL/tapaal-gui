package pipe.gui.undo;

import java.util.List;

import pipe.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import net.tapaal.gui.verification.TAPNQuery;

public class RemoveQueriesCommand extends Command {
	
	private final List<TAPNQuery> queriesToRemove;
	private final TabContent tabContent;


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
