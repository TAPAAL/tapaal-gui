package pipe.gui.undo;

import pipe.dataLayer.Template;
import dk.aau.cs.gui.TemplateExplorer;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class RemoveTemplateCommand extends AddTemplateCommand {

	public RemoveTemplateCommand(TemplateExplorer templateExplorer,
			Template<TimedArcPetriNet> template, int listIndex) {
		super(templateExplorer, template, listIndex);
	}
	
	@Override
	public void redo() {
		super.undo(); // Just the opposite of adding a template
	}
	
	@Override
	public void undo() {
		super.redo(); // Just the opposite of adding a template
	}
}
