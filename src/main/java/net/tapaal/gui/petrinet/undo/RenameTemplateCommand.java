package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import dk.aau.cs.TCTL.visitors.ITCTLVisitor;
import dk.aau.cs.TCTL.visitors.RenameTemplateVisitor;
import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.editor.TemplateExplorer;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class RenameTemplateCommand implements Command {
	private final TemplateExplorer templateExplorer;
	private final TimedArcPetriNet tapn;
	private final String oldName;
	private final String newName;
	private final PetriNetTab tab;

	public RenameTemplateCommand(TemplateExplorer templateExplorer, PetriNetTab tab,
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
