package dk.aau.cs.gui.components;

import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

//JComboBox which removes the keylistener on JComboboxes which make them searchable.
//It's used as eg. switching between the animation and edit mode using the hotkey M, could cause the token selection mode to change instead of switching between the different modes
//It removes the keylistener which is of type javax.swing.plaf.basic.BasicComboBoxUI.Handler - as this is the one causing the behaviour.
//As the javax.swing.plaf.basic.BasicComboBoxUI.Handler class is a private nested class, in the javax.swing.plaf.basic.BasicComboBoxUI class, 
//it's not possible to use "instanceof" to do the test.
//Therefore the toString of the keylistener is used.
public class NonsearchableJComboBox<E> extends JComboBox<E> {

	public NonsearchableJComboBox() {
		super();
		removeKeyListener();
	}

	public NonsearchableJComboBox(ComboBoxModel<E> aModel) {
		super(aModel);
		removeKeyListener();
	}

	public NonsearchableJComboBox(E[] items) {
		super(items);
		removeKeyListener();
	}

	public NonsearchableJComboBox(Vector<E> items) {
		super(items);
		removeKeyListener();
	}
	
	private void removeKeyListener() {
		for(KeyListener k : getKeyListeners()){
			if(k.toString().contains("javax.swing.plaf.basic.BasicComboBoxUI$Handler")){
				removeKeyListener(k);
			}
		}
	}

}
