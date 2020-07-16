package dk.aau.cs.gui.undo;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.TemplateExplorer;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.dataLayer.Template;

public class SortTemplatesCommand extends Command{
	TimedArcPetriNet[] oldOrder;
	final TabContent tabContent;
	final TemplateExplorer templateExplorer;
	final JList<Template> templateList;
	final DefaultListModel<Template> listModel;
	
	public SortTemplatesCommand(TabContent tabContent, TemplateExplorer templateExplorer, JList<Template> templateList, DefaultListModel<Template> listModel) {
		this.templateList = templateList;
		this.tabContent = tabContent;
		this.templateExplorer = templateExplorer;
		this.listModel = listModel;
	}
	
	@Override
	public void undo() {
		Template selectedValue = templateList.getSelectedValue();
		tabContent.undoSort(oldOrder);
		templateExplorer.updateTemplateList();
		templateList.setSelectedValue(selectedValue, true);
	}

	@Override
	public void redo() {
		Template selectedValue = templateList.getSelectedValue();
		oldOrder = tabContent.sortTemplates();
		templateExplorer.updateTemplateList();
		templateList.setSelectedValue(selectedValue, true);
	}
}
