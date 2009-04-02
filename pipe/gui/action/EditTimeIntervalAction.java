package pipe.gui.action;

import java.awt.Checkbox;
import java.awt.Container;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.plaf.ComboBoxUI;

import pipe.dataLayer.Arc;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;

public class EditTimeIntervalAction extends AbstractAction {

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
