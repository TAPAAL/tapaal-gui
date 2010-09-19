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
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class RunningVerificationDialog extends JDialog {
	private static final long serialVersionUID = -1943743974346875737L;
	private boolean hasFinished = false;

	public RunningVerificationDialog(JFrame owner, final Future<?> worker) {
		super(owner, "Verification in progress", true);
		setLocationRelativeTo(null);
		setLayout(new GridLayout(2,1));
		
		JButton okButton = new JButton("Interupt Verification");
		okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						worker.cancel(true);
					}
		});
		Container content = getContentPane();
		content.add(new Label("Verification is running ...\nPlease wait!"));
		content.add(okButton);		
		
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				worker.cancel(true);
			}
		});	
		pack();
	}

	public synchronized void finished() {
		hasFinished = true;
		this.setVisible(false);
	}
	
	@Override
	public void setVisible(boolean shouldShow) {
		if(hasFinished && shouldShow){
			return;
		}
		
		super.setVisible(shouldShow);
	}
}