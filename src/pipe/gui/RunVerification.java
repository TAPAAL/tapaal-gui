/**
 * 
 */
package pipe.gui;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import pipe.gui.GuiFrame.GUIMode;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunVerification extends RunVerificationBase {
	private static ImageIcon satisfiedIcon = loadIcon("satisfied");
	private static ImageIcon notSatisfiedIcon = loadIcon("notsatisfied");
	private static ImageIcon inconclusiveIcon = loadIcon("maybe");
	
	public RunVerification(ModelChecker modelChecker, Messenger messenger) {
		super(modelChecker, messenger);
	}

	@Override
	protected void showResult(VerificationResult<TAPNNetworkTrace> result) {
		if (result != null && !result.error()) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), 
					result.getSummaryString(),
					"Verification Result", JOptionPane.INFORMATION_MESSAGE, getIcon(result));

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
				
				extraInformation = "We have detected an error that often arises when UPPAAL is missing a valid Licence File.\n" +
						"Please open the UPPAAL GUI while connected to the internet, to correct this problem.";
				
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
	
	private static ImageIcon loadIcon(String name){
		try {
			return new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + name + ".png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private <T> ImageIcon getIcon(VerificationResult<T> result){
		if(result.getQueryResult().isQuerySatisfied() && result.getQueryResult().isConclusive()) return satisfiedIcon;
		else if(!result.getQueryResult().isQuerySatisfied() && result.getQueryResult().isConclusive()) return notSatisfiedIcon;
		else return inconclusiveIcon;
	}
}