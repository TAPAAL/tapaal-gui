/**
 * 
 */
package pipe.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
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
		this.iconSelector = selector;
	}

	@Override
	protected void showResult(VerificationResult<TAPNNetworkTrace> result) {
		if (result != null && !result.error()) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), 
					createMessagePanel(result.getSummaryString()),
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
			
			if (extraInformation != ""){
				message += extraInformation +			
				System.getProperty("line.separator") + 	
				System.getProperty("line.separator");
			}
			
			message += "Model checker output:\n" + result.errorMessage();
			
			messenger.displayWrappedErrorMessage(message,"Error during verification");

		}
	}

	private JPanel createMessagePanel(String summaryString) {
		StringBuffer buffer = new StringBuffer("<html>");
		buffer.append(summaryString.replace("\n", "<br/>"));
		buffer.append("</html>");
		
		final JPanel panel = new JPanel();
		panel.add(new JLabel(buffer.toString()));
		if(this.modelChecker.supportsStats()){
			JButton infoButton = new JButton("Info");
			infoButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					JOptionPane.showMessageDialog(panel, modelChecker.getStatsExplanation(), "Stats Explanation", JOptionPane.INFORMATION_MESSAGE);
				}
			});
			panel.add(infoButton);
		}
		
		return panel;
	}
	
	
}