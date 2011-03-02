package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import pipe.dataLayer.TAPNQuery;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;

/**
 * Implementes af class for handling integrated Uppaal Verification
 * 
 * Copyright 2009 Author Kenneth Yrke Joergensen <kenneth@yrke.dk>
 * 
 * Licensed under the Open Software License version 3.0
 */

public class Verifier {
	private static Verifyta getVerifyta() {
		Verifyta verifyta = new Verifyta(new FileFinderImpl(), new MessengerImpl());
		verifyta.setup();
		return verifyta;
	}
	
	private static VerifyTAPN getVerifyTAPN() {
		VerifyTAPN verifytapn = new VerifyTAPN(new FileFinderImpl(), new MessengerImpl());
		verifytapn.setup();
		return verifytapn;
	}

	public static void analyzeAndOptimizeKBound(
			TimedArcPetriNetNetwork tapnNetwork, int k, JSpinner tokensControl) {
		Verifyta verifyta = getVerifyta();

		if (!verifyta.isCorrectVersion()) {
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n"
							+ "Update to the latest development version.");
			return;
		}
		KBoundOptimizer optimizer = new KBoundOptimizer(tapnNetwork, k,
				verifyta, new MessengerImpl(), tokensControl);
		optimizer.analyze();
	}

	public static void analyseKBounded(TimedArcPetriNetNetwork tapnNetwork,
			int k) {
		Verifyta verifyta = getVerifyta();

		if (!verifyta.isCorrectVersion()) {
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n"
							+ "Update to the latest development version.");
			return;
		}
		KBoundAnalyzer analyzer = new KBoundAnalyzer(tapnNetwork, k, verifyta, new MessengerImpl());
		analyzer.analyze();
	}

	public static void runUppaalVerification(TimedArcPetriNetNetwork timedArcPetriNetNetwork, TAPNQuery input) {
		runUppaalVerification(timedArcPetriNetNetwork, input, false);
	}

	private static void runUppaalVerification(TimedArcPetriNetNetwork timedArcPetriNetNetwork, TAPNQuery input,	boolean untimedTrace) {
		Verifyta verifyta = getVerifyta();
		if (!verifyta.isCorrectVersion()) {
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n"
							+ "Update to the latest development version.");
			return;
		}

		TCTLAbstractProperty inputQuery = input.getProperty();

		VerifytaOptions verifytaOptions = new VerifytaOptions(input.getTraceOption(), input.getSearchOption(), untimedTrace, input.getReductionOption());

		if (inputQuery == null) {
			return;
		}

		if (timedArcPetriNetNetwork != null) {
			RunVerificationBase thread = new RunVerification(verifyta, new MessengerImpl());
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
			dialog.setupListeners(thread);
			thread.execute(verifytaOptions, timedArcPetriNetNetwork, new dk.aau.cs.petrinet.TAPNQuery(input.getProperty(), input.getCapacity() + timedArcPetriNetNetwork.marking().size()));
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

		return;
	}

	public static void runVerifyTAPNVerification(TimedArcPetriNetNetwork tapnNetwork, TAPNQuery query) {
		VerifyTAPN verifytapn = getVerifyTAPN();

		TCTLAbstractProperty inputQuery = query.getProperty();

		int bound = tapnNetwork.marking().size() + query.getCapacity();
		VerifyTAPNOptions verifytapnOptions = new VerifyTAPNOptions(bound, query.getTraceOption(), query.getSearchOption());

		if (inputQuery == null) {
			return;
		}

		if (tapnNetwork != null) {
			RunVerificationBase thread = new RunVerification(verifytapn, new MessengerImpl());
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
			dialog.setupListeners(thread);
			thread.execute(verifytapnOptions, tapnNetwork, new dk.aau.cs.petrinet.TAPNQuery(query.getProperty(), bound));
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

		return;
		
	}
}
