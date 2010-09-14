/**
 * 
 */
package pipe.gui.widgets;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JRootPane;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;

public class RunningVerificationPanel extends javax.swing.JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7324730866458670919L;
	private JRootPane myRootPane;
	Thread verification=null;
	boolean finished=false;
	private boolean interrupted=false;
	
	public RunningVerificationPanel() {
		setLayout(new GridLayout(2,1));
		add(new Label("Verification is running ...\n" +
				"Please wait!")
		);
		
		JButton okButton = new JButton("Interupt Verification");
		

		okButton.addActionListener(	
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						interrupted=true;
						close();
					}
				}
		);
		
		add(okButton);
	    
		
	}

	public void close() {
		myRootPane.getParent().setVisible(false);
	}
	
	public void show() {
		if (!finished){
			myRootPane.getParent().setVisible(true);
		}
	}
	
	public void createDialog(){
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), "Verification running : " + Pipe.getProgramName(), true);

		myRootPane = guiDialog.getRootPane();
		Container contentPane = guiDialog.getContentPane();
		
		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add query editor
		contentPane.add(this);
		
		guiDialog.setResizable(true);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(false);
		
		return;
	}

	public void finished() {
		finished = true;
		this.close();
	}

	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

	public boolean isInterrupted() {
		return interrupted;
	}
	
	
}