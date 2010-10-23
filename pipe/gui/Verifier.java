package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLEGNode;
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
		return new Verifyta(new FileFinderImpl(), new MessengerImpl());
	}

	public static void analyzeAndOptimizeKBound(DataLayer appModel, int k, JSpinner tokensControl)
	{
		KBoundOptimizer optimizer = new KBoundOptimizer(appModel, k, getVerifyta(), tokensControl);
		optimizer.analyze();
	}

	public static void analyseKBounded(DataLayer appModel, int k){
		KBoundAnalyzer analyzer = new KBoundAnalyzer(appModel, k, getVerifyta());
		analyzer.analyze();
	}

	public static void runUppaalVerification(DataLayer appModel, TAPNQuery input) {
		runUppaalVerification(appModel, input, false);
	}
	public static void runUppaalVerification(DataLayer appModel, TAPNQuery input, boolean untimedTrace) {
		getVerifyta().setup();
		if (!getVerifyta().isCorrectVersion()){
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n" +
			"Update to the latest development version.");
			return;
		}

		TCTLAbstractProperty inputQuery = input.getProperty();

		VerifytaOptions verifytaOptions = new VerifytaOptions(input.getTraceOption(), input.getSearchOption(), untimedTrace, input.getReductionOption());

		if (inputQuery == null) {return;}


		//Handle problems with liveness checking 
		// Bit of a hack because we know the first node of the query AST is always the path quantifier,
		// i.e. we cant have nested path quantifiers
		if (inputQuery instanceof TCTLEGNode || inputQuery instanceof TCTLAFNode ) {

			//If selected wrong method for checking
			if (input.getReductionOption() == ReductionOption.STANDARD || input.getReductionOption() == ReductionOption.STANDARDSYMMETRY){
				//Error
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"Verification of liveness properties (EG,AF) is not possible with the selected reduction option.",
						"Verification Error",
						JOptionPane.ERROR_MESSAGE);
				// XXX - Srba
				return;
			}

			//Check if degree-2 or give an error
			boolean isModelDegree2 = appModel.isDegree2();
			if (!isModelDegree2 && !(input.getReductionOption() == ReductionOption.BROADCAST 
					|| input.getReductionOption() == ReductionOption.BROADCASTSYMMETRY
					|| input.getReductionOption() == ReductionOption.DEGREE2BROADCAST
					|| input.getReductionOption() == ReductionOption.DEGREE2BROADCASTSYMMETRY)){
				//Error
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"The net cannot be verified for liveness properties (EG,AF) because there is\n"+
						"a transition with either more that two input places or more than two output places.\n"+
						"You may try to modify the model so that the net does not contain any such transition\n"+
						"and then run the verification process again.",
						"Liveness Verification Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

		}

		TAPN model = convertModelToAAUTAPN(appModel);

		if(model != null){
			RunVerificationBase thread = new RunVerification(getVerifyta());
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
			dialog.setupListeners(thread);
			thread.execute(verifytaOptions, model, new dk.aau.cs.petrinet.TAPNQuery(input.getProperty(), input.getCapacity() + model.getNumberOfTokens()));
			dialog.setVisible(true);
		}else{
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error",
					JOptionPane.ERROR_MESSAGE);
		}

		//		// Show simulator is selected and reduction is the right method
		//		if ((input.traceOption != TAPNQuery.TraceOption.NONE && (input.reductionOption == TAPNQuery.ReductionOption.NAIVE || input.reductionOption == ReductionOption.ADV_NOSYM) )&&
		//				//and Only view the trace, if a trace is generated based on property
		//				((inputQuery.contains("E<>") && property) || (inputQuery.contains("A[]") && !property) ||
		//						(inputQuery.contains("E[]") && property) || (inputQuery.contains("A<>") && !property))){
		//			
		//			
		//			
		//
		//			
		//			//Select to display concreet trace
		//			if ((inputQuery.contains("E<>") || inputQuery.contains("A[]")) && !untimedTrace){
		//				
		//			
		//
		//		    //Set to animation mode   
		//			CreateGui.getApp().setAnimationMode(true);
		//			CreateGui.getApp().setMode(Pipe.START);
		//            PetriNetObject.ignoreSelection(true);
		//			CreateGui.getView().getSelectionObject().clearSelection();
		//			
		//			CreateGui.getAnimator().resethistory();
		//			
		//			
		//			try {
		//				ArrayList<FiringAction> tmp2 = null;
		//				tmp2 = UppaalTrace.parseUppaalTraceAdvanced(bufferedReaderStderr);
		//
		//				// Handeling of the UPPAAL verifyta error in generating traces
		//				if (tmp2 == null){
		//					JOptionPane.showMessageDialog(CreateGui.getApp(),
		//							"Generation of a concrete trace in UPPAAL failed.\n\n" +
		//							"TAPAAL will re-run the verification process\n" +
		//							"in order to obtain at least an untimed trace.",				
		//							"Concrete Trace Generation Error",
		//							JOptionPane.INFORMATION_MESSAGE);
		//					//XXX - Srba
		//					
		//					runUppaalVerification(appModel, input, true);
		//					return;
		//					
		//				}
		//				
		//				CreateGui.getAnimator().resethistory();
		//				
		//				for (FiringAction f : tmp2){
		//
		//					if (f instanceof TimeDelayFiringAction){
		//
		//						BigDecimal time = new BigDecimal(""+((TimeDelayFiringAction)f).getDealy());
		//						CreateGui.getAnimator().manipulatehistory(time);
		//
		//					} else if (f instanceof DiscreetFiringAction){
		//						DiscreetFiringAction stringdfa = (DiscreetFiringAction) f;
		//
		//						Transition trans = CreateGui.currentPNMLData().getTransitionByName(stringdfa.getTransition());
		//						pipe.dataLayer.DiscreetFiringAction realdfa = new pipe.dataLayer.DiscreetFiringAction(trans);
		//
		//						for (String s : stringdfa.getConsumedTokensList().keySet()){
		//
		//							Place p = CreateGui.currentPNMLData().getPlaceByName(s);
		//							BigDecimal token = new BigDecimal(""+stringdfa.getConsumedTokensList().get(s).get(0)); // XXX - just getting the first, guess that we dont support more tokens from smae place any wway :( (for now)
		//
		//							realdfa.addConsumedToken(p, token);
		//						}
		//						CreateGui.getAnimator().manipulatehistory(realdfa);
		//					}
		//					
		//				}
		//				
		//			} catch (Exception e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//			
		//			} else {
		//				// Display abstract trace
		//				try {
		//					ArrayList<AbstractMarking> tmp2 = null;
		//					tmp2 = SymbolicUppaalTrace.parseUppaalAbstractTrace(bufferedReaderStderr);
		//
		//					JOptionPane.showMessageDialog(CreateGui.getApp(),
		//							"The verification process returned an untimed trace.\n\n"+
		//							"This means that with appropriate time delays the displayed\n"+
		//							"sequence of discrete transitions can become a concrete trace.\n"+
		//							"In case of liveness properties (EG, AF) the untimed trace\n"+
		//							"either ends in a deadlock, or time divergent computation without\n" +
		//							"any discrete transitions, or it loops back to some earlier configuration.\n"+
		//							"The user may experiment in the simulator with different time delays\n"+
		//							"in order to realize the suggested untimed trace in the model.",
		//							"Verification Information",
		//							JOptionPane.INFORMATION_MESSAGE);
		//					
		//					//Set to animation mode   
		//					CreateGui.getApp().setAnimationMode(true);
		//					CreateGui.getApp().setMode(Pipe.START);
		//		            PetriNetObject.ignoreSelection(true);
		//					CreateGui.getView().getSelectionObject().clearSelection();
		//					
		//			
		//					CreateGui.getAnimator().resethistory();
		//					
		//					CreateGui.addAbstractAnimationPane();
		//					
		//					AnimationHistory untimedAnimationHistory = CreateGui.getAbstractAnimationPane();
		//
		//					
		//					for (AbstractMarking am : tmp2){
		//						untimedAnimationHistory.addHistoryItemDontChange(am.getFiredTranstiion().trim());
		//					}
		//						
		//				} catch (Exception e){
		////					 TODO Auto-generated catch block
		//					e.printStackTrace();
		//				}	
		//			}			
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
