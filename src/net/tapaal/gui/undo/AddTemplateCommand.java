package net.tapaal.gui.undo;

import pipe.dataLayer.Template;
import net.tapaal.gui.editor.TemplateExplorer;
import net.tapaal.gui.undo.Command;

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
