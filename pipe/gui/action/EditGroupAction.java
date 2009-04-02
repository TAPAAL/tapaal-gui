package pipe.gui.action;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import pipe.dataLayer.Arc;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TransportArc;
import pipe.gui.CreateGui;

public class EditGroupAction extends AbstractAction {
	
	private Container contentPane;
	private Arc myArc;
	
	public EditGroupAction(Container contentPane, Arc a) {
		this.contentPane = contentPane;
		myArc = a;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		String currentInterval = ""+((TransportArc)myArc).getGroupNr();
		String input = JOptionPane.showInputDialog(
				"Group:", currentInterval);

		if ( input == null ) {
			return;		// do nothing if the user clicks "Cancel"
		}

		CreateGui.getView().getUndoManager().addNewEdit( ( (TransportArc)myArc ).setGroupNr( Integer.parseInt(input) ) );

	}
}