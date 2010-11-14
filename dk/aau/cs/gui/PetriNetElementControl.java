package dk.aau.cs.gui;

public interface PetriNetElementControl {
	void showPopupMenu();
	
	void select();
	void deselect();
	
	void zoom(int percentage);
}
