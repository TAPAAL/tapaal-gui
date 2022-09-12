package dk.aau.cs.approximation;

import java.math.BigDecimal;

import net.tapaal.gui.petrinet.TAPNLens;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import pipe.gui.TAPAALGUI;
import net.tapaal.gui.petrinet.verification.RunVerificationBase;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;
import dk.aau.cs.Messenger;
import dk.aau.cs.io.batchProcessing.LoadedBatchProcessingModel;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.TAPNTraceDecomposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
import dk.aau.cs.verification.batchProcessing.BatchProcessingWorker;

public class ApproximationWorker {
	public VerificationResult<TAPNNetworkTrace> normalWorker(
        VerificationOptions options,
        ModelChecker modelChecker,
        Tuple<TimedArcPetriNet, NameMapping> transformedModel,
        ITAPNComposer composer,
        TAPNQuery clonedQuery,
        RunVerificationBase verificationBase,
        TimedArcPetriNetNetwork model,
        DataLayer guiModel,
        net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery,
        TAPNLens lens
    ) throws Exception {
		
		// If options is of an instance of VerifyTAPNOptions then save the inclusion places before verify alters them
		InclusionPlaces oldInclusionPlaces = null;
		if (options instanceof VerifyTAPNOptions)
			oldInclusionPlaces = ((VerifyTAPNOptions) options).inclusionPlaces();
		
		// Enable SOME_TRACE if not already
		TraceOption oldTraceOption = options.traceOption();
		if ((options.enabledOverApproximation() || options.enabledUnderApproximation())) {
			options.setTraceOption(TraceOption.SOME);
		}
		
		VerificationResult<TAPNNetworkTrace> toReturn = null;
		VerificationResult<TimedArcPetriNetTrace> result;

		result = modelChecker.verify(options, transformedModel, clonedQuery, guiModel, dataLayerQuery, lens);

		if (result == null) return null;
        if (result.error()) {
			options.setTraceOption(oldTraceOption);
			return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
		}
		else if (options.enabledOverApproximation()) {
			// Over-approximation
			//ApproximationDenominator should not be able to be 1, if its one its the same as an exact analyses. --kyrke 2020-03-25
			if (options.approximationDenominator() == 1) {
				// If r = 1
				// No matter what it answered -> return that answer
				QueryResult queryResult = result.getQueryResult();
                NameMapping nameMapping = model.isColored()? result.getUnfoldedModel().value2(): transformedModel.value2();
                TimedArcPetriNetNetwork netNetwork = model.isColored()? result.getUnfoldedModel().value1().parentNetwork(): model;
                toReturn =  new VerificationResult<TAPNNetworkTrace>(
                    queryResult,
                    decomposeTrace(result.getTrace(),  nameMapping, netNetwork),
                    decomposeTrace(result.getSecondaryTrace(), nameMapping, netNetwork),
                    result.verificationTime(),
                    result.stats(),
                    false,
                    result.getUnfoldedModel());
                toReturn.setNameMapping(transformedModel.value2());


			} else {
				// If r > 1
				if (result.getTrace() != null && (((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && result.getQueryResult().isQuerySatisfied())
					|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && !result.getQueryResult().isQuerySatisfied()))) {
						// If we have a trace AND ((EF OR EG) AND satisfied) OR ((AG OR AF) AND not satisfied)
						// The results are inconclusive, but we get a trace and can use trace TAPN for verification.
					
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
                        NameMapping nameMapping = model.isColored()? result.getUnfoldedModel().value2(): transformedModel.value2();
                        TimedArcPetriNetNetwork netNetwork = model.isColored()? result.getUnfoldedModel().value1().parentNetwork(): model;
                        toReturn = new VerificationResult<TAPNNetworkTrace>(
                            approxResult.getQueryResult(),
                            decomposeTrace(approxResult.getTrace(), nameMapping, netNetwork),
                            decomposeTrace(approxResult.getSecondaryTrace(), nameMapping, netNetwork),
                            approxResult.verificationTime(),
                            approxResult.stats(),
                            false,
                            approxResult.getUnfoldedModel());
                        toReturn.setNameMapping(nameMapping);
						
						OverApproximation overaprx = new OverApproximation();
			
						//Create trace TAPN from the trace
						Tuple<TimedArcPetriNet, NameMapping> transformedOriginalModel = composer.transformModel(model);
						overaprx.makeTraceTAPN(transformedOriginalModel, toReturn, clonedQuery);
						
						// Reset the inclusion places in order to avoid NullPointerExceptions
						if (options instanceof VerifyTAPNOptions && oldInclusionPlaces != null)
							((VerifyTAPNOptions) options).setInclusionPlaces(oldInclusionPlaces);
			
						// run model checker again for trace TAPN
						MemoryMonitor.cumulateMemory();
						result = modelChecker.verify(options, transformedOriginalModel, clonedQuery, guiModel, dataLayerQuery, null);

						if (result.error()) {
							options.setTraceOption(oldTraceOption);
							// if the old trace option was none, we need to set the results traces to null so GUI doesn't try to display the traces later
							if (oldTraceOption == TraceOption.NONE && toReturn != null){
								toReturn.setTrace(null);
								toReturn.setSecondaryTrace(null);
							}
							return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), approxResult.verificationTime() + result.verificationTime());
						}
						//Create the result from trace TAPN
						renameTraceTransitions(result.getTrace());
						renameTraceTransitions(result.getSecondaryTrace());
						QueryResult queryResult = result.getQueryResult();
						
						// If ((EG OR EG) AND not satisfied trace) OR ((AG OR AF) AND satisfied trace) -> inconclusive
						if (((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && !queryResult.isQuerySatisfied()) 
								|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && queryResult.isQuerySatisfied())){
							queryResult.setApproximationInconclusive(true);
						}
						
						// If satisfied trace -> Return result
						// This is satisfied for EF and EG and not satisfied for AG and AF
                        toReturn = new VerificationResult<TAPNNetworkTrace>(
                            queryResult,
                            decomposeTrace(result.getTrace(), nameMapping, netNetwork),
                            decomposeTrace(result.getSecondaryTrace(), nameMapping, netNetwork),
                            approxResult.verificationTime() + result.verificationTime(),
                            approxResult.stats(),
                            false,
                            approxResult.getUnfoldedModel());
                        toReturn.setNameMapping(nameMapping);


					} 
					else if (((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && !result.getQueryResult().isQuerySatisfied())
							|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && result.getQueryResult().isQuerySatisfied())) {
						// If ((EF OR EG) AND not satisfied) OR ((AG OR AF) AND satisfied)
						
						QueryResult queryResult = result.getQueryResult();
						
						if(queryResult.hasDeadlock() || result.getQueryResult().queryType() == QueryType.EG || result.getQueryResult().queryType() == QueryType.AF){
							queryResult.setApproximationInconclusive(true);
						}
						
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
                    NameMapping nameMapping = model.isColored()? result.getUnfoldedModel().value2(): transformedModel.value2();
                    TimedArcPetriNetNetwork netNetwork = model.isColored()? result.getUnfoldedModel().value1().parentNetwork(): model;
                        toReturn = new VerificationResult<TAPNNetworkTrace>(
                            approxResult.getQueryResult(),
                            decomposeTrace(approxResult.getTrace(), nameMapping, netNetwork),
                            decomposeTrace(approxResult.getSecondaryTrace(), nameMapping, netNetwork),
                            approxResult.verificationTime(),
                            approxResult.stats(),
                            false,
                            approxResult.getUnfoldedModel());
                        toReturn.setNameMapping(nameMapping);


					} else {
						// We cannot use the result directly, and did not get a trace.
						
						QueryResult queryResult = result.getQueryResult();
						queryResult.setApproximationInconclusive(true);
						
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
                    NameMapping nameMapping = model.isColored()? result.getUnfoldedModel().value2(): transformedModel.value2();
                    TimedArcPetriNetNetwork netNetwork = model.isColored()? result.getUnfoldedModel().value1().parentNetwork(): model;
                    toReturn = new VerificationResult<TAPNNetworkTrace>(
                        approxResult.getQueryResult(),
                        decomposeTrace(approxResult.getTrace(), nameMapping, netNetwork),
                        decomposeTrace(approxResult.getSecondaryTrace(), nameMapping, netNetwork),
                        approxResult.verificationTime(),
                        approxResult.stats(),
                        false,
                        approxResult.getUnfoldedModel());
                    toReturn.setNameMapping(nameMapping);

					}
			}
		} 
		else if (options.enabledUnderApproximation()) {
			// Under-approximation
			
			if (result.getTrace() != null) {
				for (TimedArcPetriNetStep k : result.getTrace()) {
					if (k instanceof TimeDelayStep){
						((TimeDelayStep) k).setDelay(((TimeDelayStep) k).delay().multiply(new BigDecimal(options.approximationDenominator())));
					}
					else if (k instanceof TimedTransitionStep) {
						for (TimedToken a : ((TimedTransitionStep) k).consumedTokens()){
							a.setAge(a.age().multiply(new BigDecimal(options.approximationDenominator())));
						}
					}
				}
			}
			//ApproximationDenominator should not be able to be 1, if its one its the same as an exact analyses. --kyrke 2020-03-25
			if (options.approximationDenominator() == 1) { 
				// If r = 1
				// No matter it answered -> return that answer
				QueryResult queryResult= result.getQueryResult();
                NameMapping nameMapping = model.isColored()? result.getUnfoldedModel().value2(): transformedModel.value2();
                TimedArcPetriNetNetwork netNetwork = model.isColored()? result.getUnfoldedModel().value1().parentNetwork(): model;
                toReturn =  new VerificationResult<TAPNNetworkTrace>(
                    queryResult,
                    decomposeTrace(result.getTrace(), nameMapping, netNetwork),
                    decomposeTrace(result.getSecondaryTrace(), nameMapping, netNetwork),
                    result.verificationTime(),
                    result.stats(),
                    false,
                    result.getUnfoldedModel());
                toReturn.setNameMapping(nameMapping);


			}
			else {
				// If r > 1
				if ((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && ! result.getQueryResult().isQuerySatisfied()
				|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && result.getQueryResult().isQuerySatisfied())) {
					// If ((EF OR EG) AND not satisfied) OR ((AG OR AF) and satisfied) -> Inconclusive
					QueryResult queryResult= result.getQueryResult();
					queryResult.setApproximationInconclusive(true);
                    NameMapping nameMapping = model.isColored()? result.getUnfoldedModel().value2(): transformedModel.value2();
                    TimedArcPetriNetNetwork netNetwork = model.isColored()? result.getUnfoldedModel().value1().parentNetwork(): model;
                    toReturn =  new VerificationResult<TAPNNetworkTrace>(
                        queryResult,
                        decomposeTrace(result.getTrace(), nameMapping, netNetwork),
                        decomposeTrace(result.getSecondaryTrace(), nameMapping, netNetwork),
                        result.verificationTime(),
                        result.stats(),
                        false,
                        result.getUnfoldedModel());
                    toReturn.setNameMapping(nameMapping);

				} else if ((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && result.getQueryResult().isQuerySatisfied()
						|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && ! result.getQueryResult().isQuerySatisfied())) {
										
					if (result.getTrace() != null){
						// If query does have deadlock or EG or AF a trace -> create trace TAPN
						//Create the verification satisfied result for the approximation
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
                        NameMapping nameMapping = model.isColored()? result.getUnfoldedModel().value2(): transformedModel.value2();
                        TimedArcPetriNetNetwork netNetwork = model.isColored()? result.getUnfoldedModel().value1().parentNetwork(): model;
                        toReturn = new VerificationResult<TAPNNetworkTrace>(
                            approxResult.getQueryResult(),
                            decomposeTrace(approxResult.getTrace(), nameMapping, netNetwork),
                            decomposeTrace(approxResult.getSecondaryTrace(), nameMapping, netNetwork),
                            approxResult.verificationTime(),
                            approxResult.stats(),
                            false,
                            result.getUnfoldedModel());
                        toReturn.setNameMapping(nameMapping);


						
						OverApproximation overaprx = new OverApproximation();
						
			
						//Create trace TAPN from the trace
						Tuple<TimedArcPetriNet, NameMapping> transformedOriginalModel = composer.transformModel(model);
						overaprx.makeTraceTAPN(transformedOriginalModel, toReturn, clonedQuery);
						
						// Reset the inclusion places in order to avoid NullPointerExceptions
						if (options instanceof VerifyTAPNOptions && oldInclusionPlaces != null)
							((VerifyTAPNOptions) options).setInclusionPlaces(oldInclusionPlaces);
			
						//run model checker again for trace TAPN
						MemoryMonitor.cumulateMemory();
						result = modelChecker.verify(options, transformedOriginalModel, clonedQuery, guiModel, dataLayerQuery, null);

						if (result.error()) {
							options.setTraceOption(oldTraceOption);
							// if the old trace option was none, we need to set the results traces to null so GUI doesn't try to display the traces later
							if (oldTraceOption == TraceOption.NONE && toReturn != null){
								toReturn.setTrace(null);
								toReturn.setSecondaryTrace(null);
							}
							return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime() + approxResult.verificationTime());
						}
						//Create the result from trace TAPN
						renameTraceTransitions(result.getTrace());
						renameTraceTransitions(result.getSecondaryTrace());
						QueryResult queryResult = result.getQueryResult();
						
						
						// If (EF or EG AND not satisfied trace) OR (AG or AF AND satisfied trace) -> inconclusive
						if ((result.getQueryResult().queryType() == QueryType.EF && !queryResult.isQuerySatisfied())
							|| (result.getQueryResult().queryType() == QueryType.AG && queryResult.isQuerySatisfied())
							|| (result.getQueryResult().queryType() == QueryType.EG && !queryResult.isQuerySatisfied())
							|| (result.getQueryResult().queryType() == QueryType.AF && queryResult.isQuerySatisfied())) {
							queryResult.setApproximationInconclusive(true);
						}

						// If satisfied trace) -> Return result
						// This is satisfied for EF and EG and not satisfied for AG and AF
						toReturn = new VerificationResult<TAPNNetworkTrace>(
								queryResult,
								decomposeTrace(result.getTrace(), nameMapping, netNetwork),
								decomposeTrace(result.getSecondaryTrace(), nameMapping, netNetwork),
								approxResult.verificationTime() + result.verificationTime(),
								approxResult.stats(),
								false,
                                approxResult.getUnfoldedModel());
						toReturn.setNameMapping(transformedModel.value2());
					}
					else {
						// the query contains deadlock, but we do not have a trace.
						QueryResult queryResult = result.getQueryResult();
						queryResult.setApproximationInconclusive(true);
						
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
                        NameMapping nameMapping = model.isColored()? result.getUnfoldedModel().value2(): transformedModel.value2();
                        TimedArcPetriNetNetwork netNetwork = model.isColored()? result.getUnfoldedModel().value1().parentNetwork(): model;

						toReturn = new VerificationResult<TAPNNetworkTrace>(
								approxResult.getQueryResult(),
								decomposeTrace(approxResult.getTrace(), nameMapping, netNetwork),
								decomposeTrace(approxResult.getSecondaryTrace(), nameMapping, netNetwork),
								approxResult.verificationTime(),
								approxResult.stats(),
								false,
                                approxResult.getUnfoldedModel());
						toReturn.setNameMapping(nameMapping);
					}
				}
			}
		} 
		else {
            boolean isColored = (lens != null && lens.isColored() || model.isColored());
            NameMapping nameMapping = isColored? result.getUnfoldedModel().value2(): transformedModel.value2();
            TimedArcPetriNetNetwork netNetwork = isColored? result.getUnfoldedModel().value1().parentNetwork(): model;
			toReturn =  new VerificationResult<TAPNNetworkTrace>(
			    result.getQueryResult(),
                decomposeTrace(result.getTrace(), nameMapping, netNetwork),
                decomposeTrace(result.getSecondaryTrace(), nameMapping, netNetwork),
                result.verificationTime(),
                result.stats(),
                false,
                result.getRawOutput(),
                result.getUnfoldedModel(),
                result.getUnfoldedTab());
			toReturn.setNameMapping(nameMapping);
		}
		
		options.setTraceOption(oldTraceOption);
		// if the old traceoption was none, we need to set the results traces to null so GUI doesn't try to display the traces later
		if (oldTraceOption == TraceOption.NONE){
			toReturn.setTrace(null);
			toReturn.setSecondaryTrace(null);
		}
		
		return toReturn;
	}

	public VerificationResult<TimedArcPetriNetTrace> batchWorker(
			Tuple<TimedArcPetriNet, NameMapping> composedModel,
			VerificationOptions options,
			net.tapaal.gui.petrinet.verification.TAPNQuery query,
			LoadedBatchProcessingModel model,
			ModelChecker modelChecker,
			TAPNQuery queryToVerify,
			TAPNQuery clonedQuery,
			BatchProcessingWorker verificationBase
	) throws Exception {
		InclusionPlaces oldInclusionPlaces = null;
		if (options instanceof VerifyTAPNOptions)
			oldInclusionPlaces = ((VerifyTAPNOptions) options).inclusionPlaces();
		
		Tuple<TimedArcPetriNet, NameMapping> transformedOriginalModel = new Tuple<TimedArcPetriNet, NameMapping>(composedModel.value1().copy(), composedModel.value2());
		
		TraceOption oldTraceOption = options.traceOption();
		if (query != null && query.isOverApproximationEnabled()) {
			// Create a fresh model
			composedModel = composeModel(model);
			OverApproximation overaprx = new OverApproximation();
			overaprx.modifyTAPN(composedModel.value1(), query.approximationDenominator());
			options.setTraceOption(TraceOption.SOME);
		} else if (query != null && query.isUnderApproximationEnabled()) {
			// Create a fresh model
			composedModel = composeModel(model);
			UnderApproximation underaprx = new UnderApproximation();
			underaprx.modifyTAPN(composedModel.value1(), query.approximationDenominator());
			options.setTraceOption(TraceOption.SOME);
		}
		
		VerificationResult<TimedArcPetriNetTrace> verificationResult = modelChecker.verify(options, composedModel, queryToVerify, null, query, null);
		
		VerificationResult<TAPNNetworkTrace> valueNetwork = null;	//The final result is meant to be a PetriNetTrace but to make traceTAPN we make a networktrace
		VerificationResult<TimedArcPetriNetTrace> value = null;
		if (verificationResult.error()) {
			options.setTraceOption(oldTraceOption);
			return new VerificationResult<TimedArcPetriNetTrace>(verificationResult.errorMessage(), verificationResult.verificationTime());
		}
		else if (query != null && query.isOverApproximationEnabled()) {		
			//Create the verification satisfied result for the approximation
            
            // Over-approximation
			if (query.approximationDenominator() == 1) {
                // If r = 1
                // No matter what EF and AG answered -> return that answer
               QueryResult queryResult = verificationResult.getQueryResult();
               value =  new VerificationResult<TimedArcPetriNetTrace>(
					queryResult,
					verificationResult.getTrace(),
					verificationResult.getSecondaryTrace(),
					verificationResult.verificationTime(),
					verificationResult.stats(),
					false,
                   verificationResult.getUnfoldedModel());
				value.setNameMapping(composedModel.value2());
	        } else {
	            // If r > 1
				if (verificationResult.getTrace() != null && (((verificationResult.getQueryResult().queryType() == QueryType.EF || verificationResult.getQueryResult().queryType() == QueryType.EG ) && verificationResult.getQueryResult().isQuerySatisfied())
						|| ((verificationResult.getQueryResult().queryType() == QueryType.AG || verificationResult.getQueryResult().queryType() == QueryType.AF) && !verificationResult.getQueryResult().isQuerySatisfied()))) {
					// If ((EF OR EG) AND satisfied) OR ((AG OR AF) AND not satisfied)
					//Create the verification satisfied result for the approximation
	                VerificationResult<TimedArcPetriNetTrace> approxResult = verificationResult;
                    NameMapping nameMapping = model.network().isColored()? verificationResult.getUnfoldedModel().value2(): composedModel.value2();
                    TimedArcPetriNetNetwork netNetwork = model.network().isColored()? verificationResult.getUnfoldedModel().value1().parentNetwork(): model.network();
	                valueNetwork = new VerificationResult<TAPNNetworkTrace>(
	                            approxResult.getQueryResult(),
	                            decomposeTrace(approxResult.getTrace(), nameMapping, netNetwork),
	                            decomposeTrace(approxResult.getSecondaryTrace(), nameMapping, netNetwork),
	                            approxResult.verificationTime(),
	                            approxResult.stats(),
	        					false,
                                verificationResult.getUnfoldedModel());
	                valueNetwork.setNameMapping(nameMapping);
	                
	                OverApproximation overaprx = new OverApproximation();
	                
	                //Create trace TAPN from the network trace
	                overaprx.makeTraceTAPN(transformedOriginalModel, valueNetwork, clonedQuery);
	                
	                // Reset the inclusion places in order to avoid NullPointerExceptions
	                if (options instanceof VerifyTAPNOptions && oldInclusionPlaces != null){
	                    ((VerifyTAPNOptions) options).setInclusionPlaces(oldInclusionPlaces);
	                }

	                //run model checker again for trace TAPN
	                MemoryMonitor.cumulateMemory();
	                verificationResult = modelChecker.verify(options, transformedOriginalModel, clonedQuery, null, query, null);

	                if (verificationResult.error()) {
	                	options.setTraceOption(oldTraceOption);
	                    return new VerificationResult<TimedArcPetriNetTrace>(
	                    		verificationResult.errorMessage(),
	                    		verificationResult.verificationTime() + approxResult.verificationTime());
	                }
	                //Create the result from trace TAPN
	                renameTraceTransitions(verificationResult.getTrace());
	                renameTraceTransitions(verificationResult.getSecondaryTrace());
	                QueryResult queryResult = verificationResult.getQueryResult();
	                
	                // If ((EF OR EG) AND not satisfied trace) OR ((AG OR AF) AND satisfied trace) -> inconclusive
					if (((verificationResult.getQueryResult().queryType() == QueryType.EF || verificationResult.getQueryResult().queryType() == QueryType.EG) && !queryResult.isQuerySatisfied()) 
							|| ((verificationResult.getQueryResult().queryType() == QueryType.AG || verificationResult.getQueryResult().queryType() == QueryType.AF) && queryResult.isQuerySatisfied())){
						queryResult.setApproximationInconclusive(true);
					}
	                // If satisfied trace -> Return result
	                // This is satisfied for EF and EG and not satisfied for AG and AF
	                value = new VerificationResult<TimedArcPetriNetTrace>(
	                        queryResult,
	                        approxResult.getTrace(),
	                        approxResult.getSecondaryTrace(),
	                        approxResult.verificationTime() + verificationResult.verificationTime(),
	                        approxResult.stats(),
	    					false,
                            verificationResult.getUnfoldedModel());
	                value.setNameMapping(composedModel.value2());
	            }
				else if (((verificationResult.getQueryResult().queryType() == QueryType.EF || verificationResult.getQueryResult().queryType() == QueryType.EG) && !verificationResult.getQueryResult().isQuerySatisfied())
						|| ((verificationResult.getQueryResult().queryType() == QueryType.AG || verificationResult.getQueryResult().queryType() == QueryType.AF) && verificationResult.getQueryResult().isQuerySatisfied())) {
	                // If (EF AND EG not satisfied) OR (AG AND AF satisfied)
	               
	                QueryResult queryResult = verificationResult.getQueryResult();
	              
	                if(queryResult.hasDeadlock() || queryResult.queryType() == QueryType.EG || queryResult.queryType() == QueryType.AF){
						queryResult.setApproximationInconclusive(true);
					}
	                
	                value =  new VerificationResult<TimedArcPetriNetTrace>(
						queryResult,
						verificationResult.getTrace(),
						verificationResult.getSecondaryTrace(),
						verificationResult.verificationTime(),
						verificationResult.stats(),
						false,
                        verificationResult.getUnfoldedModel());
				    value.setNameMapping(composedModel.value2());
	            }
				else {
					// We cannot use the result directly, and did not get a trace.
					
					QueryResult queryResult = verificationResult.getQueryResult();
					
					queryResult.setApproximationInconclusive(true);
	                
	                value =  new VerificationResult<TimedArcPetriNetTrace>(
						queryResult,
						verificationResult.getTrace(),
						verificationResult.getSecondaryTrace(),
						verificationResult.verificationTime(),
						verificationResult.stats(),
						false,
                        verificationResult.getUnfoldedModel());
				    value.setNameMapping(composedModel.value2());
				}
	        }
	    } 
	    else if (query != null && query.isUnderApproximationEnabled()) {
	        // Under-approximation
			
	        if (query.approximationDenominator() == 1) { 
	            // If r = 1
	            // No matter what EF and AG answered -> return that answer
	            QueryResult queryResult= verificationResult.getQueryResult();
	            value =  new VerificationResult<TimedArcPetriNetTrace>(
                    queryResult,
                    verificationResult.getTrace(),
                    verificationResult.getSecondaryTrace(),
                    verificationResult.verificationTime(),
                    verificationResult.stats(),
					false,
                    verificationResult.getUnfoldedModel());
                value.setNameMapping(composedModel.value2());
	        }
	        else {
	            // If r > 1
				if ((verificationResult.getQueryResult().queryType() == QueryType.EF || verificationResult.getQueryResult().queryType() == QueryType.EG) && ! verificationResult.getQueryResult().isQuerySatisfied()
				|| ((verificationResult.getQueryResult().queryType() == QueryType.AG || verificationResult.getQueryResult().queryType() == QueryType.AF) && verificationResult.getQueryResult().isQuerySatisfied())) {
					// If ((EF OR EG) AND not satisfied) OR ((AG OR AF) and satisfied) -> Inconclusive
                    
                    QueryResult queryResult = verificationResult.getQueryResult();
                    queryResult.setApproximationInconclusive(true);
                    value =  new VerificationResult<TimedArcPetriNetTrace>(
                            queryResult,
                            verificationResult.getTrace(),
                            verificationResult.getSecondaryTrace(),
                            verificationResult.verificationTime(),
                            verificationResult.stats(),
        					false,
                            verificationResult.getUnfoldedModel());
                    value.setNameMapping(composedModel.value2());
	                    
				}
				else if ((verificationResult.getQueryResult().queryType() == QueryType.EF || verificationResult.getQueryResult().queryType() == QueryType.EG) && verificationResult.getQueryResult().isQuerySatisfied()
						|| ((verificationResult.getQueryResult().queryType() == QueryType.AG || verificationResult.getQueryResult().queryType() == QueryType.AF) && ! verificationResult.getQueryResult().isQuerySatisfied())) {
					// ((EF OR EG) AND satisfied) OR ((AG OR AF) and not satisfied) -> Check for deadlock
	                    
					if(verificationResult.getTrace() != null) {
	                    // If query does have deadlock -> create trace TAPN
	                    //Create the verification satisfied result for the approximation
						VerificationResult<TimedArcPetriNetTrace> approxResult = verificationResult;

                        NameMapping nameMapping = model.network().isColored()? verificationResult.getUnfoldedModel().value2(): composedModel.value2();
                        TimedArcPetriNetNetwork netNetwork = model.network().isColored()? verificationResult.getUnfoldedModel().value1().parentNetwork(): model.network();
                        valueNetwork = new VerificationResult<TAPNNetworkTrace>(
                            approxResult.getQueryResult(),
                            decomposeTrace(approxResult.getTrace(), nameMapping, netNetwork),
                            decomposeTrace(approxResult.getSecondaryTrace(), nameMapping, netNetwork),
                            approxResult.verificationTime(),
                            approxResult.stats(),
                            false,
                            verificationResult.getUnfoldedModel());
                        valueNetwork.setNameMapping(nameMapping);


	                    
	                    OverApproximation overaprx = new OverApproximation();
	        
	                    //Create trace TAPN from the trace
	                    overaprx.makeTraceTAPN(transformedOriginalModel, valueNetwork, clonedQuery);
	                    
	                    // Reset the inclusion places in order to avoid NullPointerExceptions
	                    if (options instanceof VerifyTAPNOptions && oldInclusionPlaces != null)
	                        ((VerifyTAPNOptions) options).setInclusionPlaces(oldInclusionPlaces);
	        
	                    //run model checker again for trace TAPN
	                    MemoryMonitor.cumulateMemory();
	                    verificationResult = modelChecker.verify(options, transformedOriginalModel, clonedQuery, null, query, null);

	                    if (verificationResult.error()) {
	                    	options.setTraceOption(oldTraceOption);
	        				return new VerificationResult<TimedArcPetriNetTrace>(
	        						verificationResult.errorMessage(),
	        						verificationResult.verificationTime() + approxResult.verificationTime());
	        			}
	                    
	                    //Create the result from trace TAPN
	                    renameTraceTransitions(verificationResult.getTrace());
	                    renameTraceTransitions(verificationResult.getSecondaryTrace());
	                    QueryResult queryResult = verificationResult.getQueryResult();
	                    
	                    // If (EF or EG AND not satisfied trace) OR (AG or AF AND satisfied trace) -> inconclusive
	                    if ((verificationResult.getQueryResult().queryType() == QueryType.EF && !queryResult.isQuerySatisfied())
	                        || (verificationResult.getQueryResult().queryType() == QueryType.AG && queryResult.isQuerySatisfied())
	                        || (verificationResult.getQueryResult().queryType() == QueryType.EG && !queryResult.isQuerySatisfied())
							|| (verificationResult.getQueryResult().queryType() == QueryType.AF && queryResult.isQuerySatisfied())) {
	                        queryResult.setApproximationInconclusive(true);
	                    }
	                    
	                    // If satisfied trace -> Return result
	                    // This is satisfied for EF and EG and not satisfied for AG and AF
	                   value =  new VerificationResult<TimedArcPetriNetTrace>(
	                		    queryResult,
	                            verificationResult.getTrace(),
	                            verificationResult.getSecondaryTrace(),
	                            verificationResult.verificationTime() + approxResult.verificationTime(),
	                            verificationResult.stats(),
	        					false,
                                verificationResult.getUnfoldedModel());
	                    value.setNameMapping(composedModel.value2());
	                }
	                else {
	                	QueryResult queryResult = verificationResult.getQueryResult();
						
						queryResult.setApproximationInconclusive(true);
		                
		                value =  new VerificationResult<TimedArcPetriNetTrace>(
							queryResult,
							verificationResult.getTrace(),
							verificationResult.getSecondaryTrace(),
							verificationResult.verificationTime(),
							verificationResult.stats(),
							false,
                            verificationResult.getUnfoldedModel());
					    value.setNameMapping(composedModel.value2());
	                }
	            }
	        }
	    }
	    else {
	        value =  new VerificationResult<TimedArcPetriNetTrace>(
	                verificationResult.getQueryResult(),
	                verificationResult.getTrace(),
	                verificationResult.getSecondaryTrace(),
	                verificationResult.verificationTime(),
	                verificationResult.stats(),
					false,
                    verificationResult.getUnfoldedModel());
	        value.setNameMapping(composedModel.value2());
	    }
		
		options.setTraceOption(oldTraceOption);
		return value;
	}
	
	private TAPNNetworkTrace decomposeTrace(TimedArcPetriNetTrace trace, NameMapping mapping, TimedArcPetriNetNetwork model) {
		if (trace == null)
			return null;

		TAPNTraceDecomposer decomposer = new TAPNTraceDecomposer(trace, model, mapping);
		return decomposer.decompose();
	}
	
	private void renameTraceTransitions(TimedArcPetriNetTrace trace) {
		if (trace != null){
			trace.reduceTraceForOriginalNet("_traceNet_", "PTRACE");
			trace.removeTokens("PBLOCK");
		}
	}
	
	private Tuple<TimedArcPetriNet, NameMapping> composeModel(LoadedBatchProcessingModel model) {
		ITAPNComposer composer = new TAPNComposer(new Messenger(){
			public void displayInfoMessage(String message) { }
			public void displayInfoMessage(String message, String title) {}
			public void displayErrorMessage(String message) {}
			public void displayErrorMessage(String message, String title) {}
			public void displayWrappedErrorMessage(String message, String title) {}
			
		}, false);
		Tuple<TimedArcPetriNet, NameMapping> composedModel = composer.transformModel(model.network());
		return composedModel;
	}
}
