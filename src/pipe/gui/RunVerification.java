/**
 * 
 */
package pipe.gui;

import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import pipe.gui.GuiFrame.GUIMode;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.verification.IconSelector;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunVerification extends RunVerificationBase {
	private IconSelector iconSelector;
	public RunVerification(ModelChecker modelChecker, IconSelector selector, Messenger messenger) {
		super(modelChecker, messenger);
		iconSelector = selector;
	}

	@Override
	protected void showResult(VerificationResult<TAPNNetworkTrace> result) {
		if (result != null && !result.error()) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), 
					createMessagePanel(result),
					"Verification Result", JOptionPane.INFORMATION_MESSAGE, iconSelector.getIconFor(result.getQueryResult()));

			if (result.getTrace() != null) {
				// DataLayer model = CreateGui.getModel();
				// TraceTransformer interpreter = model.isUsingColors() ? new
				// ColoredTraceTransformer(model) : new TraceTransformer(model);
				// TAPNTrace trace =
				// interpreter.interpretTrace(result.getTrace());
				CreateGui.getApp().setGUIMode(GUIMode.animation);

				CreateGui.getAnimator().SetTrace(result.getTrace());

			}

		}else{
			
			//Check if the is something like 
			//verifyta: relocation_error:
			///usr/lib32/libnss_msdn4_minimal.so.2 symbol strlen, 
			//version GLIB_2.0 not defined in file libc.so.6 with
			//link time reference
			//is the error as this (often) means the possibility for a uppaal licence key error
			
			String extraInformation = "";
			
			if (result != null && (result.errorMessage().contains("relocation") || result.errorMessage().toLowerCase().contains("internet connection is required for activation"))){
				
				extraInformation = "We detected an error that often arises when UPPAAL is missing a valid Licence file.\n" +
						"Open the UPPAAL GUI while connected to the internet to correct this problem.";
				
			}
			
			String message = "An error occured during the verification." +
			System.getProperty("line.separator") + 	
			System.getProperty("line.separator");
			
			if (!extraInformation.equals("")){
				message += extraInformation +			
				System.getProperty("line.separator") + 	
				System.getProperty("line.separator");
			}
			
			message += "Model checker output:\n" + result.errorMessage();
			
			messenger.displayWrappedErrorMessage(message,"Error during verification");

		}
	}
	
	@Override
	protected void showHumanTrace(VerificationResult<TAPNNetworkTrace> result) {
		if(result.getHumanTrace() == null) return;
		//JOptionPane.showMessageDialog(new JFrame(), result.getHumanTrace(), "Trace", JOptionPane.PLAIN_MESSAGE);
		
		JOptionPane pane = new JOptionPane(result.getHumanTrace(), JOptionPane.PLAIN_MESSAGE);
		JDialog d = pane.createDialog("Trace");
		d.setModalityType(ModalityType.MODELESS);
		d.setAlwaysOnTop(true);
		d.pack();
		d.setVisible(true);
		
	}

	private String toHTML(String string){
		StringBuffer buffer = new StringBuffer("<html>");
		buffer.append(string.replace(System.getProperty("line.separator"), "<br/>"));
		buffer.append("</html>");
		return buffer.toString();
	}
	
	private JPanel createMessagePanel(VerificationResult<TAPNNetworkTrace> result) {
		final JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0,0,15,0);
		gbc.anchor = GridBagConstraints.WEST;		
		panel.add(new JLabel(toHTML(result.getResultString())), gbc);
		
		if(modelChecker.supportsStats()){
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(0,0,15,0);
			gbc.anchor = GridBagConstraints.WEST;
			panel.add(new JLabel(toHTML(result.getStatsAsString())), gbc);
			
			JButton infoButton = new JButton("Explanation");
			infoButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					JOptionPane.showMessageDialog(panel, modelChecker.getStatsExplanation(), "Stats Explanation", JOptionPane.INFORMATION_MESSAGE);
				}
			});
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.insets = new Insets(0,10,15,0);
			gbc.anchor = GridBagConstraints.EAST;
			panel.add(infoButton, gbc);
		}
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel(result.getVerificationTimeString()), gbc);
		
		return panel;
	}
	
	
}