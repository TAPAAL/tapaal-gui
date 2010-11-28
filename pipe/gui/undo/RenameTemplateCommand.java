package pipe.gui.undo;

import dk.aau.cs.gui.TemplateExplorer;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class RenameTemplateCommand extends Command {
	private TemplateExplorer templateExplorer;
	private TimedArcPetriNet tapn;
	private String oldName;
	private String newName;
	
	public RenameTemplateCommand(TemplateExplorer templateExplorer, TimedArcPetriNet tapn, String oldName, String newName) {
		this.templateExplorer = templateExplorer;
		this.tapn = tapn;
		this.oldName = oldName;
		this.newName = newName;
	}
	
	@Override
	public void redo() {
		tapn.setName(newName);
		templateExplorer.repaint();
	}

	@Override
	public void undo() {
		tapn.setName(oldName);
		templateExplorer.repaint();
	}

}
