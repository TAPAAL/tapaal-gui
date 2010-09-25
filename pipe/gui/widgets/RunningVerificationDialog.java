/**
 * 
 */
package pipe.gui.widgets;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

public class RunningVerificationDialog extends JDialog {
	private static final long serialVersionUID = -1943743974346875737L;
	private JButton okButton;

	public RunningVerificationDialog(JFrame owner) {
		super(owner, "Verification in progress", true);
		setLocationRelativeTo(null);
		setLayout(new GridLayout(2,1));
		
		okButton = new JButton("Interupt Verification");
		
		Container content = getContentPane();
		content.add(new Label("Verification is running ...\nPlease wait!"));
		content.add(okButton);		
			
		pack();
	}

	public void setupListeners(final SwingWorker<?,?> worker) {
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				worker.cancel(true);
			}
		});
		
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				worker.cancel(true);
			}
		});	

		worker.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent event) {
				if(event.getPropertyName().equals("state")){
					StateValue stateValue = (StateValue)event.getNewValue();
//					if(stateValue.equals(StateValue.STARTED)){
//						setVisible(true);
//					}else 
						if(stateValue.equals(StateValue.DONE)){
						setVisible(false);
						dispose();
					}
				}
			}
			
		});
	}
}