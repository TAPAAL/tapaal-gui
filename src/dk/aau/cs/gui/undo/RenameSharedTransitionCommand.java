package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.TCTL.visitors.RenameSharedTransitionVisitor;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.SharedTransition;

public class RenameSharedTransitionCommand extends Command {
	private final SharedTransition transition;
	private final String oldName;
	private final String newName;
        private final TabContent tab;
	private SharedTransitionsListModel listModel;

	public RenameSharedTransitionCommand(SharedTransition transition, TabContent tab, String oldName, String name, SharedTransitionsListModel listModel) {
		this.transition = transition;
		this.oldName = oldName;
		this.newName = name;
                this.tab = tab;
		this.listModel = listModel;
	}

	@Override
	public void redo() {
		transition.setName(newName);
		listModel.updatedName();
		updateQueries(oldName, newName);
	}
        
        private void updateQueries(String nameToFind, String nameToReplaceWith) {
		for(TAPNQuery query : tab.queries()){
			query.getProperty().accept(new RenameSharedTransitionVisitor(nameToFind, nameToReplaceWith), null);
		}
	}

	@Override
	public void undo() {
		transition.setName(oldName);
		listModel.updatedName();
                updateQueries(newName, oldName);
	}

}
