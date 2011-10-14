package pipe.gui.undo;

import pipe.dataLayer.Template;
import dk.aau.cs.gui.TemplateExplorer;
import dk.aau.cs.gui.undo.Command;

public class ToggleTemplateActivationCommand  extends Command{

	private TemplateExplorer templateExplorer;
	private Template template;
	private boolean newStatus;
	
	public ToggleTemplateActivationCommand(TemplateExplorer templateExplorer, Template template, boolean newStatus) {
		this.templateExplorer = templateExplorer;
		this.template = template;
		this.newStatus = newStatus;
	}
	
	
	@Override
	public void redo() {
		template.setActive(newStatus);
		templateExplorer.updateTemplateList();
	}

	@Override
	public void undo() {
		template.setActive(!newStatus);
		templateExplorer.updateTemplateList();
	}

}
