package pipe.gui.undo;

import java.util.Collection;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.TemplateExplorer;

public class RemoveTemplateCommand extends AddTemplateCommand {
	private Collection<TAPNQuery> queriesToDelete;
	private TabContent tabContent;
	
	public RemoveTemplateCommand(TabContent tabContent, TemplateExplorer templateExplorer,
			Template template, int listIndex, Collection<TAPNQuery> queriesToDelete) {
		super(templateExplorer, template, listIndex);
		this.tabContent = tabContent;
		this.queriesToDelete = queriesToDelete;
	}

	@Override
	public void redo() {
		super.undo(); // Just the opposite of adding a template
		for(TAPNQuery query : queriesToDelete){
			tabContent.removeQuery(query);
		}
	}

	@Override
	public void undo() {
		super.redo(); // Just the opposite of adding a template
		for(TAPNQuery query : queriesToDelete){
			tabContent.addQuery(query);
		}
	}
}
