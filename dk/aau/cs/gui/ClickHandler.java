package dk.aau.cs.gui;

import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

public class ClickHandler extends MouseInputAdapter {
	private PetriNetElementControl control;
	
	public ClickHandler(PetriNetElementControl control){
		this.control = control;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(SwingUtilities.isLeftMouseButton(e)){
			if(e.getClickCount() == 2){
				JOptionPane.showMessageDialog(null, "editor");
			}else{
				control.select();
				//JOptionPane.showMessageDialog(null, "select");
				// if not animation mode
				// select control
			}
		}else if(SwingUtilities.isRightMouseButton(e)){
			//if not animation mode
			control.showPopupMenu(e.getX(), e.getY());
		}
	}
}
