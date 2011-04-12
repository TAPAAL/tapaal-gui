package pipe.gui.undo;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.TCTL.visitors.ITCTLVisitor;
import dk.aau.cs.TCTL.visitors.RenameTemplateVisitor;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.TemplateExplorer;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class RenameTemplateCommand extends Command {
	private TemplateExplorer templateExplorer;
	private TimedArcPetriNet tapn;
	private String oldName;
	private String newName;
	private final TabContent tab;

	public RenameTemplateCommand(TemplateExplorer templateExplorer, TabContent tab,
			TimedArcPetriNet tapn, String oldName, String newName) {
		this.templateExplorer = templateExplorer;
		this.tab = tab;
		this.tapn = tapn;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public void redo() {
		tapn.setName(newName);
		templateExplorer.repaint();
		updateQueries(oldName, newName);
	}

	private void updateQueries(String nameToFind, String nameToReplaceWith) {
		ITCTLVisitor visitor = new RenameTemplateVisitor(nameToFind, nameToReplaceWith);
		for(TAPNQuery query : tab.queries()){
			query.getProperty().accept(visitor, null);
		}
	}

	@Override
	public void undo() {
		tapn.setName(oldName);
		templateExplorer.repaint();
		updateQueries(newName, oldName);
	}

}
