package net.tapaal.gui.petrinet.undo;

import java.util.Arrays;

import javax.swing.DefaultListModel;

import dk.aau.cs.util.StringComparator;

public class SortQueriesCommand implements Command {
	
	final DefaultListModel listModel;
	Object[] oldOrder;
	
	public SortQueriesCommand(DefaultListModel listModel) {
		this.listModel = listModel;
	}
	
	@Override
	public void undo() {
		listModel.clear();
		for(Object o : oldOrder){
			listModel.addElement(o);
		}
	}

	@Override
	public void redo() {
		Object[] sortedListModel = listModel.toArray();
		oldOrder = Arrays.copyOf(sortedListModel, sortedListModel.length);
		Arrays.sort(sortedListModel, new StringComparator());
		listModel.clear();
		for(Object o : sortedListModel){
			listModel.addElement(o);
		}	
	}
}
