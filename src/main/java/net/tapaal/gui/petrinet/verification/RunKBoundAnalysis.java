package net.tapaal.gui.petrinet.verification;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import net.tapaal.resourcemanager.ResourceManager;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.verification.Boundedness;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.TAPAALGUI;

import java.util.HashMap;

public class RunKBoundAnalysis extends RunVerificationBase {

	private final JSpinner spinner;
    private final boolean resultShown;

	public RunKBoundAnalysis(ModelChecker modelChecker, Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels, JSpinner spinner, boolean resultShown) {
		super(modelChecker, messenger, null, "", false, spinner);
		this.spinner = spinner;
		this.resultShown = resultShown;
		this.guiModels = guiModels;
	}

	private void updateSpinner(VerificationResult<TAPNNetworkTrace> result) {
		spinner.setValue(result.getQueryResult().boundednessAnalysis().usedTokens() - result.getQueryResult().boundednessAnalysis().tokensInNet());
	}

	@Override
	protected boolean showResult(VerificationResult<TAPNNetworkTrace> result) {
		if (result != null && !result.error()) {
		    if ((result.getBound() != -1 && result.getQueryResult().boundednessAnalysis().usedTokens() > result.getBound()) ||
                !result.getQueryResult().boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) {
				JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
						getAnswerNotBoundedString(), "Analysis Result",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
			    if (modelChecker instanceof VerifyPN && !resultShown) {
                    Object[] options = {"Ok", "Minimize extra tokens"};
                    int answer = JOptionPane.showOptionDialog(TAPAALGUI.getApp(),
                        getPNAnswerBoundedString(), "Analysis Result,",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        ResourceManager.satisfiedIcon(), options, JOptionPane.OK_OPTION);

                    if (answer != JOptionPane.OK_OPTION && result.getRawOutput().contains("max tokens:")) {
                        updateSpinner(result);
                    } else {
                        return answer != JOptionPane.OK_OPTION;
                    }
                } else if (modelChecker instanceof VerifyPN) {
                    updateSpinner(result);
                } else {
                    updateSpinner(result);
                    JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                        getAnswerBoundedString(), "Analysis Result",
                        JOptionPane.INFORMATION_MESSAGE, ResourceManager.satisfiedIcon());
                }
			}
		} else {						
			String message = "An error occurred during the verification." +
			System.getProperty("line.separator") + 	
			System.getProperty("line.separator");
			message += "Model checker output:\n" + result.errorMessage();
			
			messenger.displayWrappedErrorMessage(message,"Error during verification");
		}
		return false;
	}

	private String formatAnswer(String status, String explanation, String tokenDetail) {
		return "The net with the specified extra number of tokens is " + status + ".\n\n"
				+ "This means that the analysis " + explanation + "\n\n"
				+ (tokenDetail.isEmpty() ? "" : tokenDetail + "\n\n")
				+ (isColored() ? "The boundedness check is performed using the unfolding approach.\n"
				+ "The number of required extra tokens can be lower when using explicit engine." : "");
	}

	protected String getAnswerNotBoundedString() {
		return formatAnswer(
				"either unbounded or\nmore extra tokens have to be added in order to achieve an exact analysis",
				"using the currently selected number \nof extra tokens provides only an under-approximation of the net behaviour.\nIf you think that the net is bounded, try to add more extra tokens in order\nto achieve exact verification analysis.",
				""
		);
	}

	protected String getAnswerBoundedString() {
		return formatAnswer(
				"bounded",
				"will be exact and always give \nthe correct answer.",
				"The number of extra tokens was automatically lowered to the\nminimum number of tokens needed for an exact analysis."
		);
	}

	protected String getPNAnswerBoundedString() {
		return formatAnswer(
				"bounded",
				"will be exact and always give \nthe correct answer.",
				"The number of extra tokens can be lowered to the minimum number\nof tokens needed for an exact analysis."
		);
	}

    private boolean isColored() {
        return (lens != null && lens.isColored()) || model.isColored();
    }
}
