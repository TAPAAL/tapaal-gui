package pipe.gui.undo;

import net.tapaal.gui.verification.TAPNQuery;
import pipe.gui.TabContent;
import net.tapaal.gui.undo.Command;

public class AddQueryCommand extends Command {
	private final TAPNQuery query;
	private final TabContent tabContent;
	
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
