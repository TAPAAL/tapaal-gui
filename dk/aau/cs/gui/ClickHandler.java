package dk.aau.cs.gui;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

public class ClickHandler extends MouseInputAdapter {
	private EditableControl control;
	
	public ClickHandler(EditableControl control){
		this.control = control;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(SwingUtilities.isLeftMouseButton(e)){
			if(e.getClickCount() == 2){
				
			}
		}else if(SwingUtilities.isRightMouseButton(e)){
			
		}
	}
}
