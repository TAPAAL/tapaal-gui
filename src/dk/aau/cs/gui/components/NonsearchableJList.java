package dk.aau.cs.gui.components;

import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListModel;

//List which removes the keylistener on lists which make them searchable.
//It's used as eg. switching to the "placetool" using the "p" hotkey could change the selected component (To the next in the list starting with p).
//It removes the keylistener which is of type javax.swing.plaf.basic.BasicListUI.Handler - as this is the one causing the behavior.
//As the javax.swing.plaf.basic.BasicListUI.Handler class is a private nested class, in the javax.swing.plaf.basic.BasicListUI class, 
//it's not possible to use "instanceof" to do the test.
//Therefore the toString of the keylistener is used.
public class NonsearchableJList extends JList{
	private static final long serialVersionUID = 2706313217985326989L;

	public NonsearchableJList() {
		super();
		removeKeyListener();
	}

	public NonsearchableJList(ListModel dataModel){
		super (dataModel);
		removeKeyListener();
	}

	public NonsearchableJList(Object[]  listData){
		super (listData);
		removeKeyListener();
	}

	public NonsearchableJList(Vector<?> listData){
		super (listData);
		removeKeyListener();
	}
	
	private void removeKeyListener(){
		for(KeyListener k : getKeyListeners()){
			if(k.toString().contains("javax.swing.plaf.basic.BasicListUI$Handler") ){
				removeKeyListener(k);
			}
		}
	}
}
