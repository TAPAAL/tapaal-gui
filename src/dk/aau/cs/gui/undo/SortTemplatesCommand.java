package dk.aau.cs.gui.undo;

import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import dk.aau.cs.gui.TabComponent;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.TemplateExplorer;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class SortTemplatesCommand extends Command{
	TimedArcPetriNet[] oldOrder;
	TabContent tabContent; 
	TemplateExplorer templateExplorer;
	JList templateList;
	DefaultListModel listModel;
	
	public SortTemplatesCommand(TabContent tabContent, TemplateExplorer templateExplorer, JList templateList, DefaultListModel listModel) {
		this.templateList = templateList;
		this.tabContent = tabContent;
		this.templateExplorer = templateExplorer;
		this.listModel = listModel;
	}
	
	@Override
	public void undo() {
		Object selectedValue = templateList.getSelectedValue();
		tabContent.undoSort(oldOrder);
		templateExplorer.updateTemplateList();
		templateList.setSelectedValue(selectedValue, true);
	}

	@Override
	public void redo() {
		Object selectedValue = templateList.getSelectedValue();
		oldOrder = tabContent.sortTemplates();
		templateExplorer.updateTemplateList();
		templateList.setSelectedValue(selectedValue, true);
	}
}
