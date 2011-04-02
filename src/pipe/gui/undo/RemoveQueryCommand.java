package pipe.gui.undo;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.gui.TabContent;

public class RemoveQueryCommand extends AddQueryCommand {
	
	public RemoveQueryCommand(TAPNQuery query, TabContent tabContent) {
		super(query, tabContent);
	}
	
	@Override
	public void redo() {
		super.undo();
	}

	@Override
	public void undo() {
		super.redo();
	}

}
