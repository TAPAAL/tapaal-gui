package pipe.gui;

import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import dk.aau.cs.verification.VerifyTAPN.*;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.UPPAAL.UppaalIconSelector;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;

/**
 * Implementes af class for handling integrated Uppaal Verification
 * 
 * Copyright 2009 Author Kenneth Yrke Joergensen <kenneth@yrke.dk>
 * 
 * Licensed under the Open Software License version 3.0
 */

public class Verifier {
	private static Verifyta getVerifyta() {
		Verifyta verifyta = new Verifyta(new FileFinder(), new MessengerImpl());
		verifyta.setup();
		return verifyta;
	}
	
	private static VerifyTAPN getVerifyTAPN() {
		VerifyTAPN verifytapn = new VerifyTAPN(new FileFinder(), new MessengerImpl());
		verifytapn.setup();
		return verifytapn;
	}

	private static VerifyTAPNDiscreteVerification getVerifydTAPN() {
		VerifyTAPNDiscreteVerification verifydtapn = new VerifyTAPNDiscreteVerification(new FileFinder(), new MessengerImpl());
		verifydtapn.setup();
		return verifydtapn;
	}

	private static VerifyPN getVerifyPN() {
		VerifyPN verifypn = new VerifyPN(new FileFinder(), new MessengerImpl());
		verifypn.setup();
		return verifypn;
	}

	private static ModelChecker getModelChecker(TAPNQuery query) {
		if(query.getReductionOption() == ReductionOption.VerifyTAPN){
			return getVerifyTAPN();
		} else if(query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification){
			return getVerifydTAPN();
		} else if(query.getReductionOption() == ReductionOption.VerifyPN){
			return getVerifyPN();
		}
		else{
			throw new RuntimeException("Verification method: " + query.getReductionOption() + ", should not be send here");
		}
	}

	public static void analyzeKBound(
			TimedArcPetriNetNetwork tapnNetwork, int k, JSpinner tokensControl) {
		ModelChecker modelChecker;
		
		if(tapnNetwork.isUntimed()){
			modelChecker = getVerifyPN();
		} else if(tapnNetwork.hasWeights() || tapnNetwork.hasUrgentTransitions()){
			modelChecker = getVerifydTAPN();
		} else {
			modelChecker = getVerifyTAPN();
		}

		if (!modelChecker.isCorrectVersion()) {
			System.err.println("The model checker not found, or you are running an old version of it.\n"
							+ "Update to the latest development version.");
			return;
		}
		KBoundAnalyzer optimizer = new KBoundAnalyzer(tapnNetwork, k, modelChecker, new MessengerImpl(), tokensControl);
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

		VerifytaOptions verifytaOptions = new VerifytaOptions(
				input.getTraceOption(),
				input.getSearchOption(),
				untimedTrace,
				input.getReductionOption(),
				input.useSymmetry(),
				input.useOverApproximation(),
				input.isOverApproximationEnabled(),
				input.isUnderApproximationEnabled(),
				input.approximationDenominator()
		);

		if (inputQuery == null) {
			return;
		}

		if (timedArcPetriNetNetwork != null) {
			RunVerificationBase thread = new RunVerification(verifyta, new UppaalIconSelector(), new MessengerImpl());
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
			dialog.setupListeners(thread);
			thread.execute(
					verifytaOptions,
					timedArcPetriNetNetwork,
					new dk.aau.cs.model.tapn.TAPNQuery(input.getProperty(), input.getCapacity()),
					null
			);
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

		return;
	}
	
	public static void runVerifyTAPNVerification(TimedArcPetriNetNetwork tapnNetwork, TAPNQuery query) {
		runVerifyTAPNVerification(tapnNetwork, query, null);		
	}
	
	public static void runVerifyTAPNVerification(TimedArcPetriNetNetwork tapnNetwork, TAPNQuery query, VerificationCallback callback) {
		runVerifyTAPNVerification(tapnNetwork, query, callback, null);
	}

	public static void runVerifyTAPNVerification(
			TimedArcPetriNetNetwork tapnNetwork,
			TAPNQuery query,
			VerificationCallback callback,
			HashMap<TimedArcPetriNet, DataLayer> guiModels) {
		ModelChecker verifytapn = getModelChecker(query);

		if (!verifytapn.isCorrectVersion()) {
			new MessengerImpl().displayErrorMessage(
					"No "+verifytapn+" specified: The verification is cancelled",
					"Verification Error");
			return;
		}
		
		TCTLAbstractProperty inputQuery = query.getProperty();

		int bound = query.getCapacity();
		
		VerifyTAPNOptions verifytapnOptions;
		if(query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification){
			verifytapnOptions = new VerifyDTAPNOptions(
					bound,
					query.getTraceOption(),
					query.getSearchOption(),
					query.useSymmetry(),
					query.useGCD(),
					query.useTimeDarts(),
					query.usePTrie(),
					query.useOverApproximation(),
					query.discreteInclusion(),
					query.inclusionPlaces(),
					query.getWorkflowMode(),
					query.getStrongSoundnessBound(),
					query.isOverApproximationEnabled(),
					query.isUnderApproximationEnabled(),
					query.approximationDenominator(),
					query.isStubbornReductionEnabled()
			);
		} else if(query.getReductionOption() == ReductionOption.VerifyPN){
			verifytapnOptions = new VerifyPNOptions(
					bound,
					query.getTraceOption(),
					query.getSearchOption(),
					query.useOverApproximation(),
					query.useReduction()? ModelReduction.AGGRESSIVE:ModelReduction.NO_REDUCTION,
					query.isOverApproximationEnabled(),
					query.isUnderApproximationEnabled(),
					query.approximationDenominator(),
					query.getCategory(),
					query.getAlgorithmOption(),
					query.isSiphontrapEnabled(),
					query.isQueryReductionEnabled(),
					query.isStubbornReductionEnabled()
			);
		} else {
			verifytapnOptions = new VerifyTAPNOptions(
					bound,
					query.getTraceOption(),
					query.getSearchOption(),
					query.useSymmetry(),
					query.useOverApproximation(),
					query.discreteInclusion(),
					query.inclusionPlaces(),
					query.isOverApproximationEnabled(),
					query.isUnderApproximationEnabled(),
					query.approximationDenominator()
			);
		}
		
		if (inputQuery == null) {
			return;
		}
		
		if (tapnNetwork != null) {
			RunVerificationBase thread = new RunVerification(verifytapn, new VerifyTAPNIconSelector(), new MessengerImpl(), callback, guiModels);
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
			dialog.setupListeners(thread);
			thread.execute(verifytapnOptions, tapnNetwork, new dk.aau.cs.model.tapn.TAPNQuery(query.getProperty(), bound), query);
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

		return;
		
	}
}
