package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.Template;
import net.tapaal.gui.petrinet.editor.TemplateExplorer;

public class AddTemplateCommand extends Command {
	protected final Template template;
	protected final TemplateExplorer templateExplorer;
	private final int listIndex;

	public AddTemplateCommand(TemplateExplorer templateExplorer, Template template, int listIndex) {
		this.templateExplorer = templateExplorer;
		this.template = template;
		this.listIndex = listIndex;
	}

	@Override
	public void redo() {
		templateExplorer.addTemplate(listIndex, template);
	}

	@Override
	public void undo() {
		templateExplorer.removeTemplate(listIndex, template);
	}
}
