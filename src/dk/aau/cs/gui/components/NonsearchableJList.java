package dk.aau.cs.gui.components;

import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListModel;

public class NonsearchableJList extends JList{
	public NonsearchableJList() {
		super();
		removeKeyListener(getKeyListeners()[1]);
	}
	
	public NonsearchableJList(ListModel dataModel){
		super (dataModel);
		removeKeyListener(getKeyListeners()[1]);
	}
	
	public NonsearchableJList(Object[]  listData){
		super (listData);
		removeKeyListener(getKeyListeners()[1]);
	}
	
	public NonsearchableJList(Vector<?> listData){
		super (listData);
		removeKeyListener(getKeyListeners()[1]);
	}
}
