package pipe.gui.undo;

import java.util.Collection;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.TemplateExplorer;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public class RemoveTemplateCommand extends AddTemplateCommand {
	private final Collection<TAPNQuery> queriesToDelete;
	private final TabContent tabContent;
	private final Collection<Tuple<LocalTimedPlace, SharedPlace>> placesToUnshare;
	private final Collection<Tuple<TimedTransition, SharedTransition>> transitionsToUnshare;

	public RemoveTemplateCommand(
			TabContent tabContent, 
			TemplateExplorer templateExplorer, 
			Template template, 
			int listIndex, 
			Collection<TAPNQuery> queriesToDelete, 
			Collection<Tuple<LocalTimedPlace, SharedPlace>> placesToUnshare, 
			Collection<Tuple<TimedTransition, SharedTransition>> transitionsToUnshare
	) {
		super(templateExplorer, template, listIndex);
		Require.notImplemented();
		this.tabContent = tabContent;
		this.queriesToDelete = queriesToDelete;
		this.placesToUnshare = placesToUnshare;
		this.transitionsToUnshare = transitionsToUnshare;
	}

	@Override
	public void redo() {
		Require.notImplemented();
		super.undo(); // Just the opposite of adding a template
		for(TAPNQuery query : queriesToDelete) { tabContent.removeQuery(query);	}
//		for(Tuple<TimedPlace, SharedPlace> tuple : placesToUnshare){ tuple.value1().unshare(); }
//		for(Tuple<TimedTransition, SharedTransition> tuple : transitionsToUnshare){ tuple.value1().unshare(); }
	}

	@Override
	public void undo() {
		Require.notImplemented();
		super.redo(); // Just the opposite of adding a template
		for(TAPNQuery query : queriesToDelete){ tabContent.addQuery(query); }
//		for(Tuple<TimedPlace, SharedPlace> tuple : placesToUnshare){ tuple.value2().makeShared(tuple.value1()); }
//		for(Tuple<TimedTransition, SharedTransition> tuple : transitionsToUnshare){ tuple.value2().makeShared(tuple.value1()); }
	}
}
