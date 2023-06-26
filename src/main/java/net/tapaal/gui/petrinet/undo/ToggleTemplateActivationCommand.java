package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.Template;
import net.tapaal.gui.petrinet.editor.TemplateExplorer;

public class ToggleTemplateActivationCommand  extends Command{

	private final TemplateExplorer templateExplorer;
	private final Template template;
	private final boolean newStatus;
	
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
