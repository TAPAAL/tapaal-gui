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

    public RunKBoundAnalysis(
        ModelChecker modelChecker,
        Messenger messenger,
        HashMap<TimedArcPetriNet, DataLayer> guiModels,
        JSpinner spinner,
        boolean resultShown
    ) {
        super(modelChecker, messenger, null, "", false, spinner);
        this.spinner = spinner;
        this.resultShown = resultShown;
        this.guiModels = guiModels;
    }

    @Override
    protected boolean showResult(VerificationResult<TAPNNetworkTrace> result) {
        if (result != null && !result.error()) {
            if ((result.getBound() != -1 && result.getQueryResult().boundednessAnalysis().usedTokens() > result.getBound()) ||
                !result.getQueryResult().boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) {
                JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    getAnswerNotBoundedString(),
                    "Analysis Result",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                if (modelChecker instanceof VerifyPN && !resultShown) {
                    Object[] options = {"Ok", "Minimize extra tokens"};
                    int answer = JOptionPane.showOptionDialog(
                        TAPAALGUI.getApp(),
                        getPNAnswerBoundedString(),
                        "Analysis Result,",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        ResourceManager.satisfiedIcon(),
                        options,
                        JOptionPane.OK_OPTION
                    );

                    if (answer != JOptionPane.OK_OPTION && result.getRawOutput().contains("max tokens:")) {
                        spinner.setValue(result.getQueryResult().boundednessAnalysis().usedTokens() - result.getQueryResult().boundednessAnalysis().tokensInNet());
                    } else {
                        return answer != JOptionPane.OK_OPTION;
                    }
                } else if (modelChecker instanceof VerifyPN) {
                    spinner.setValue(result.getQueryResult().boundednessAnalysis().usedTokens() - result.getQueryResult().boundednessAnalysis().tokensInNet());
                } else {
                    spinner.setValue(result.getQueryResult().boundednessAnalysis().usedTokens() - result.getQueryResult().boundednessAnalysis().tokensInNet());
                    JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        getAnswerBoundedString(),
                        "Analysis Result",
                        JOptionPane.INFORMATION_MESSAGE,
                        ResourceManager.satisfiedIcon()
                    );
                }
            }
        } else {
            String message = "An error occurred during the verification." +
                System.getProperty("line.separator") +
                System.getProperty("line.separator");
            message += "Model checker output:\n" + result.errorMessage();

            messenger.displayWrappedErrorMessage(message, "Error during verification");
        }
        return false;
    }

    protected String getAnswerNotBoundedString() {
        return "The net with the specified extra number of tokens is either unbounded or\n"
            + "more extra tokens have to be added in order to achieve an exact analysis.\n\n"
            + "This means that the analysis using the currently selected number \n"
            + "of extra tokens provides only an under-approximation of the net behaviour.\n"
            + "If you think that the net is bounded, try to add more extra tokens in order\n"
            + "to achieve exact verification analysis.\n";
    }

    protected String getAnswerBoundedString() {
        return "The net with the specified extra number of tokens is bounded.\n\n"
            + "This means that the analysis will be exact and always give \n"
            + "the correct answer.\n\n"
            + "The number of extra tokens was automatically lowered to the\n"
            + "minimum number of tokens needed for an exact analysis.";
    }

    protected String getPNAnswerBoundedString() {
        return "The net with the specified extra number of tokens is bounded.\n\n"
            + "This means that the analysis will be exact and always give \n"
            + "the correct answer.\n\n"
            + "The number of extra tokens can be lowered to the minimum number\n"
            + "of tokens needed for an exact analysis.";
    }
}
