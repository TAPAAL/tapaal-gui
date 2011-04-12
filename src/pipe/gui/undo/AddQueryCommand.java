package pipe.gui.undo;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;

public class AddQueryCommand extends Command {
	private TAPNQuery query;
	private TabContent tabContent;
	
	public AddQueryCommand(TAPNQuery query, TabContent tabContent) {
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
