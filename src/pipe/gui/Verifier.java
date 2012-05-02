package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.UPPAAL.UppaalIconSelector;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerificationWA;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNIconSelector;
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
	
	private static VerifyTAPNDiscreteVerificationWA getVerifydTAPN() {
		VerifyTAPNDiscreteVerificationWA verifytapn = new VerifyTAPNDiscreteVerificationWA(new FileFinderImpl(), new MessengerImpl());
		verifytapn.setup();
		return verifytapn;
	}
	
	private static ModelChecker getModelChecker(TAPNQuery query) {
		if(query.getReductionOption() == ReductionOption.VerifyTAPN){
			return getVerifyTAPN();
		} else if(query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerificationWA){
			return getVerifydTAPN();
		} else{
			throw new RuntimeException("Verification method: " + query.getReductionOption() + ", should not be send here");
		}
	}

	public static void analyzeKBound(
			TimedArcPetriNetNetwork tapnNetwork, int k, JSpinner tokensControl) {
		VerifyTAPN verifytapn = getVerifyTAPN();

		if (!verifytapn.isCorrectVersion()) {
			System.err.println("Verifytapn not found, or you are running an old version of verifytapn.\n"
							+ "Update to the latest development version.");
			return;
		}
		KBoundAnalyzer optimizer = new KBoundAnalyzer(tapnNetwork, k, verifytapn, new MessengerImpl(), tokensControl);
		optimizer.analyze();
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

		VerifytaOptions verifytaOptions = new VerifytaOptions(input.getTraceOption(), input.getSearchOption(), untimedTrace, input.getReductionOption(), input.useSymmetry());

		if (inputQuery == null) {
			return;
		}

		if (timedArcPetriNetNetwork != null) {
			RunVerificationBase thread = new RunVerification(verifyta, new UppaalIconSelector(), new MessengerImpl());
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
			dialog.setupListeners(thread);
			thread.execute(verifytaOptions, timedArcPetriNetNetwork, new dk.aau.cs.model.tapn.TAPNQuery(input.getProperty(), input.getCapacity()));
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

		return;
	}
	
	

	public static void runVerifyTAPNVerification(TimedArcPetriNetNetwork tapnNetwork, TAPNQuery query) {
		ModelChecker verifytapn = getModelChecker(query);

		if (!verifytapn.isCorrectVersion()) {
			return;
		}
		
		TCTLAbstractProperty inputQuery = query.getProperty();

		int bound = query.getCapacity();
		
		//TODO fix the simulator such that this is not necessary
		if(query.getTraceOption() == TraceOption.SOME){
			if(query.queryType() == QueryType.EG || query.queryType() == QueryType.AF || tapnNetwork.hasWeights()){
				query.setTraceOption(TraceOption.HUMAN);
			}
		}
		
		VerifyTAPNOptions verifytapnOptions = new VerifyTAPNOptions(bound, query.getTraceOption(), query.getSearchOption(), query.useSymmetry(), query.discreteInclusion(), query.inclusionPlaces());

		if (inputQuery == null) {
			return;
		}

		if (tapnNetwork != null) {
			RunVerificationBase thread = new RunVerification(verifytapn, new VerifyTAPNIconSelector(), new MessengerImpl());
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
			dialog.setupListeners(thread);
			thread.execute(verifytapnOptions, tapnNetwork, new dk.aau.cs.model.tapn.TAPNQuery(query.getProperty(), bound));
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

		return;
		
	}
}
