package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.colors.ColoredPipeTapnToColoredAauTapnTransformer;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;

/**
 * Implementes af class for handling integrated Uppaal Verification
 * 
 * Copyright 2009
 * Author Kenneth Yrke Joergensen <kenneth@yrke.dk>
 * 
 * Licensed under the Open Software License version 3.0
 */


public class Verifier {
	private static Verifyta getVerifyta() {
		Verifyta verifyta = new Verifyta(new FileFinderImpl(), new MessengerImpl());
		verifyta.setup();
		return verifyta;
	}

	public static void analyzeAndOptimizeKBound(TimedArcPetriNetNetwork tapnNetwork, int k, JSpinner tokensControl)
	{
		Verifyta verifyta = getVerifyta();
		
		if (!verifyta.isCorrectVersion()){
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n" +
			"Update to the latest development version.");
			return;
		}
		KBoundOptimizer optimizer = new KBoundOptimizer(tapnNetwork, k, verifyta, new MessengerImpl(), tokensControl);
		optimizer.analyze();
	}

	public static void analyseKBounded(TimedArcPetriNetNetwork tapnNetwork, int k){
		Verifyta verifyta = getVerifyta();
		
		if (!verifyta.isCorrectVersion()){
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n" +
			"Update to the latest development version.");
			return;
		}
		KBoundAnalyzer analyzer = new KBoundAnalyzer(tapnNetwork, k, verifyta, new MessengerImpl());
		analyzer.analyze();
	}

	public static void runUppaalVerification(TimedArcPetriNetNetwork timedArcPetriNetNetwork, TAPNQuery input) {
		runUppaalVerification(timedArcPetriNetNetwork, input, false);
	}
	
	private static void runUppaalVerification(TimedArcPetriNetNetwork timedArcPetriNetNetwork, TAPNQuery input, boolean untimedTrace) {
		Verifyta verifyta = getVerifyta();
		if (!verifyta.isCorrectVersion()){
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n" +
			"Update to the latest development version.");
			return;
		}

//		TCTLAbstractProperty inputQuery = input.getProperty();
//
//		VerifytaOptions verifytaOptions = new VerifytaOptions(input.getTraceOption(), input.getSearchOption(), untimedTrace, input.getReductionOption());
//
//		if (inputQuery == null) {return;}


//		//Handle problems with liveness checking 
//		// Bit of a hack because we know the first node of the query AST is always the path quantifier,
//		// i.e. we cant have nested path quantifiers
//		if (inputQuery instanceof TCTLEGNode || inputQuery instanceof TCTLAFNode ) {
//
//			//If selected wrong method for checking
//			if (input.getReductionOption() == ReductionOption.STANDARD || input.getReductionOption() == ReductionOption.STANDARDSYMMETRY){
//				//Error
//				JOptionPane.showMessageDialog(CreateGui.getApp(),
//						"Verification of liveness properties (EG,AF) is not possible with the selected reduction option.",
//						"Verification Error",
//						JOptionPane.ERROR_MESSAGE);
//				// XXX - Srba
//				return;
//			}
//
//			//Check if degree-2 or give an error
//			boolean isModelDegree2 = timedArcPetriNetNetwork.isDegree2();
//			if (!isModelDegree2 && !(input.getReductionOption() == ReductionOption.BROADCAST 
//					|| input.getReductionOption() == ReductionOption.BROADCASTSYMMETRY
//					|| input.getReductionOption() == ReductionOption.DEGREE2BROADCAST
//					|| input.getReductionOption() == ReductionOption.DEGREE2BROADCASTSYMMETRY)){
//				//Error
//				JOptionPane.showMessageDialog(CreateGui.getApp(),
//						"The net cannot be verified for liveness properties (EG,AF) because there is\n"+
//						"a transition with either more that two input places or more than two output places.\n"+
//						"You may try to modify the model so that the net does not contain any such transition\n"+
//						"and then run the verification process again.",
//						"Liveness Verification Error",
//						JOptionPane.ERROR_MESSAGE);
//				return;
//			}
//
//		}
//
//		TAPN model = convertModelToAAUTAPN(timedArcPetriNetNetwork);

//		if(model != null){
//			RunVerificationBase thread = new RunVerification(verifyta, new MessengerImpl());
//			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
//			dialog.setupListeners(thread);
//			thread.execute(verifytaOptions, model, new dk.aau.cs.petrinet.TAPNQuery(input.getProperty(), input.getCapacity() + model.getNumberOfTokens()));
//			dialog.setVisible(true);
//		}else{
//			JOptionPane.showMessageDialog(CreateGui.getApp(),
//					"There was an error converting the model.",
//					"Conversion error",
//					JOptionPane.ERROR_MESSAGE);
//		}

		return;
	}

	private static TAPN convertModelToAAUTAPN(DataLayer appModel) {
		PipeTapnToAauTapnTransformer transformer = appModel.isUsingColors() ? 
				new ColoredPipeTapnToColoredAauTapnTransformer() 
		: new PipeTapnToAauTapnTransformer();

				TAPN model=null;
				try {
					model = transformer.getAAUTAPN(appModel, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return model;
	}
}
