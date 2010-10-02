package pipe.gui.action;

import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import pipe.dataLayer.Arc;
import pipe.dataLayer.TimedArc;
import pipe.gui.CreateGui;

public class EditTimeIntervalAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5730320425921717153L;
	private Container contentPane;
	private Arc myArc;

	public EditTimeIntervalAction(Container contentPane, Arc a) {
		this.contentPane = contentPane;
		myArc = a;
	}

	public void actionPerformed(ActionEvent e) {
		String currentInterval = ((TimedArc)myArc).getGuard();
		
		String input = JOptionPane.showInputDialog( "Time Interval:", currentInterval);

		if ( input == null ) {
			return;		// do nothing if the user clicks "Cancel"
		}

		if ( ! TimedArc.validateTimeInterval(input) ) {
			JOptionPane.showMessageDialog(
					contentPane, "Please enter a valid interval starting from 0 - for syntax see Help Menu");
		} else if ( ! input.equals(currentInterval) ){
			CreateGui.getView().getUndoManager().addNewEdit(((TimedArc)myArc).setGuard(input));
		}      
	}
}
