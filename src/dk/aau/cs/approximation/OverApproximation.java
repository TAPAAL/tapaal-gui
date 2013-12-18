package dk.aau.cs.approximation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractPathProperty;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.model.tapn.simulation.*;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.VerificationResult;
import dk.aau.cs.verification.UPPAAL.UppaalExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNExporter;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.CreateGui;
import pipe.gui.MessengerImpl;
import pipe.gui.widgets.QueryDialog;

public class OverApproximation implements ITAPNApproximation {
	@Override
	public void modifyTAPN(TimedArcPetriNet net, TAPNQuery query) {
		// Fix input arcs
		for (TimedInputArc arc : net.inputArcs()) {
			TimeInterval oldInterval = arc.interval();
			TimeInterval newInterval = modifyIntervals(oldInterval, query.approximationDenominator());
			
			arc.setTimeInterval(newInterval);
		}
		 
		// Fix transport arcs
		for (TransportArc arc : net.transportArcs()) {
			TimeInterval oldInterval = arc.interval();
			TimeInterval newInterval = modifyIntervals(oldInterval, query.approximationDenominator());
			
			arc.setTimeInterval(newInterval);
		}
		 
		// Fix invariants in places
		for (TimedPlace place : net.places()) {
			if ( ! (place.invariant().upperBound() instanceof Bound.InfBound) && place.invariant().upperBound().value() > 0) {					
				TimeInvariant oldInvariant = place.invariant();
				place.setInvariant(new TimeInvariant(oldInvariant.isUpperNonstrict(), new IntBound((int) Math.ceil(oldInvariant.upperBound().value() / (double)query.approximationDenominator()))));
			}
		}
	}
	
	//Returns a copy of an approximated interval
	private TimeInterval modifyIntervals(TimeInterval oldInterval, int denominator){
		Bound newUpperBound;
		// Do not calculate upper bound for infinite
		if ( ! (oldInterval.upperBound() instanceof Bound.InfBound)) {
			 // Calculate the new upper bound value. If the value is fx. 22 the new value needs to be 3  
			int oldUpperBoundValue = oldInterval.upperBound().value();
			newUpperBound = new IntBound((int) Math.ceil((double)oldUpperBoundValue /  denominator));
		} else {
			newUpperBound = Bound.Infinity;
		}
		 
		// Calculate the new lower bound
		IntBound newLowerBound = new IntBound((int) Math.floor(oldInterval.lowerBound().value() / denominator));

		return new TimeInterval(
			 oldInterval.IsLowerBoundNonStrict(),
			 newLowerBound,
			 newUpperBound,
			 oldInterval.IsUpperBoundNonStrict()
			 );
	}
	
	public void makeTraceTAPN(Tuple<TimedArcPetriNet, NameMapping> transformedModel, VerificationResult<TAPNNetworkTrace> result, dk.aau.cs.model.tapn.TAPNQuery query) {
		TimedArcPetriNet net = transformedModel.value1();
                
		LocalTimedPlace currentPlace = new LocalTimedPlace("PTRACE0");
		TimedToken currentToken = new TimedToken(currentPlace);
		net.add(currentPlace);
		currentPlace.addToken(currentToken);
		
		// Block place, which secures the net makes at most one transition not in the trace.
		LocalTimedPlace blockPlace = new LocalTimedPlace("PBLOCK", TimeInvariant.LESS_THAN_INFINITY);
		TimedToken blockToken = new TimedToken(blockPlace);
		net.add(blockPlace);
		blockPlace.addToken(blockToken);
		
		ArrayList<TimedTransition> originalTransitions = new ArrayList<TimedTransition>();
		for (TimedTransition transition : net.transitions()) {
			originalTransitions.add(transition);
		}
		
		ArrayList<TimedInputArc> originalInput = new ArrayList<TimedInputArc>();
		for (TimedInputArc inputarc : net.inputArcs()) {
			originalInput.add(inputarc);
		}
		
		ArrayList<TimedOutputArc> originalOutput = new ArrayList<TimedOutputArc>();
		for (TimedOutputArc outputarc : net.outputArcs()) {
			originalOutput.add(outputarc);
		}
		
		ArrayList<TimedInhibitorArc> originalInhibitor = new ArrayList<TimedInhibitorArc>();
		for (TimedInhibitorArc inhibitor : net.inhibitorArcs()) {
			originalInhibitor.add(inhibitor);
		}
		
		ArrayList<TransportArc> originalTransport = new ArrayList<TransportArc>();
		for (TransportArc transport : net.transportArcs()) {
			originalTransport.add(transport);
		}

		int placeInteger = 0;
		int transitionInteger = 0;
		
		TAPNNetworkTrace trace = result.getTrace();
		HashMap<String,String> reversedNameMap = reverseNameMapping(transformedModel.value2().getMappedToOrg());
		
		for(TAPNNetworkTraceStep step : trace) {
			if (step instanceof TAPNNetworkTimedTransitionStep) {
				TimedTransition firedTransition = net.getTransitionByName(reversedNameMap.get(((TAPNNetworkTimedTransitionStep) step).getTransition().name()));
				TimedTransition copyTransition = new TimedTransition(firedTransition.name() + "_traceNet_" + Integer.toString(++transitionInteger), firedTransition.isUrgent());
				net.add(copyTransition);
				net.add(new TimedInputArc(currentPlace, copyTransition, TimeInterval.ZERO_INF));
				
				currentPlace = new LocalTimedPlace("PTRACE" + Integer.toString(++placeInteger));
				net.add(currentPlace);
				
				net.add(new TimedInputArc(blockPlace, copyTransition, TimeInterval.ZERO_INF));
				net.add(new TimedOutputArc(copyTransition, blockPlace));
				
				net.add(new TimedOutputArc(copyTransition, currentPlace));
				
				for (TimedInputArc arc : originalInput) {
					if (arc.destination() == firedTransition) {
						net.add(new TimedInputArc(arc.source(), copyTransition, arc.interval(), arc.getWeight()));
					}
				}
				for (TimedOutputArc arc : originalOutput) {
					if (arc.source() == firedTransition) {
						net.add(new TimedOutputArc(copyTransition, arc.destination(), arc.getWeight()));
					}
				}
				for (TimedInhibitorArc arc : originalInhibitor) {
					if (arc.destination() == firedTransition) {
						net.add(new TimedInhibitorArc(arc.source(), copyTransition, arc.interval(), arc.getWeight()));
					}
				}
				for (TransportArc arc : originalTransport) {
					if (arc.transition() == firedTransition) {
						net.add(new TransportArc(arc.source(), copyTransition, arc.destination(), arc.interval(), arc.getWeight()));
					}
				}
			}
		}
		
		TCTLAbstractProperty topNode = query.getProperty();
		TCTLAndListNode andList;
		TCTLAtomicPropositionNode pBlock = new TCTLAtomicPropositionNode(blockPlace.name(), "=", 1);
		
		if(topNode instanceof TCTLEFNode)
		{
			andList = new TCTLAndListNode((((TCTLEFNode) topNode).getProperty()), pBlock);
			((TCTLEFNode) topNode).setProperty(andList);
		}
		else if(topNode instanceof TCTLAGNode) // Beware: if the function is called with a AG query - the caller needs to flip the result, because the topNode cannot be a NotNode!
		{
			TCTLNotNode notNode = new TCTLNotNode(((TCTLAGNode) topNode).getProperty());
			andList = new TCTLAndListNode(notNode, pBlock);
			TCTLEFNode newTopNode = new TCTLEFNode(andList);
			query.setProperty(newTopNode);
		}
		
		
		for (TimedTransition transition : originalTransitions) {
			net.add(new TimedInputArc(blockPlace, transition, TimeInterval.ZERO_INF));	
		}
				
		/*
		PrintStream modelStream;
		try {
			modelStream = new PrintStream(new File("C:\\Users\\Sine\\Documents\\Universitet\\test.xml"));
			outputModel(net, modelStream);  
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
            
	}
	
	public static HashMap<String,String> reverseNameMapping(HashMap<String,Tuple<String,String>> map) {
		HashMap<String,String> newMap = new HashMap<String,String>();
		for ( Entry<String, Tuple<String,String>> entry : map.entrySet() ) {
		    newMap.put(entry.getValue().value2(), entry.getKey());
		}
		return newMap;
	}
	
	/*
	private void outputModel(TimedArcPetriNet model, PrintStream modelStream) {
		modelStream.append("<pnml>\n");
		modelStream.append("<net id=\"" + model.name() + "\" type=\"P/T net\">\n");
		
		for(TimedPlace p : model.places())
			outputPlace(p, modelStream);
		
		for(TimedTransition t : model.transitions())
			outputTransition(t,modelStream);
		
		for(TimedInputArc inputArc : model.inputArcs())
			outputInputArc(inputArc, modelStream);
		
		for(TimedOutputArc outputArc : model.outputArcs())
			outputOutputArc(outputArc, modelStream);
		
		for(TransportArc transArc : model.transportArcs())
			outputTransportArc(transArc, modelStream);
		
		for(TimedInhibitorArc inhibArc : model.inhibitorArcs())
			outputInhibitorArc(inhibArc, modelStream);
		
		modelStream.append("</net>\n");
		modelStream.append("</pnml>");
	}
	
	private void outputPlace(TimedPlace p, PrintStream modelStream) {
		modelStream.append("<place ");
		
		modelStream.append("id=\"" + p.name() + "\" ");
		modelStream.append("name=\"" + p.name() + "\" ");
		modelStream.append("invariant=\"" + p.invariant().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("initialMarking=\"" + p.numberOfTokens() + "\" ");
		
		modelStream.append("/>\n");
	}

	private void outputTransition(TimedTransition t, PrintStream modelStream) {
		modelStream.append("<transition ");
		
		modelStream.append("id=\"" + t.name() + "\" ");
		modelStream.append("name=\"" + t.name() + "\" ");
		modelStream.append("urgent=\"" + (t.isUrgent()? "true":"false") + "\"");
		
		modelStream.append("/>");
	}

	protected void outputInputArc(TimedInputArc inputArc, PrintStream modelStream) {
		modelStream.append("<inputArc ");
		
		modelStream.append("inscription=\"" + inputArc.interval().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("source=\"" + inputArc.source().name() + "\" ");
		modelStream.append("target=\"" + inputArc.destination().name() + "\" ");
		if(inputArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + inputArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}

	protected void outputOutputArc(TimedOutputArc outputArc, PrintStream modelStream) {
		modelStream.append("<outputArc ");
		
		modelStream.append("inscription=\"1\" " );
		modelStream.append("source=\"" + outputArc.source().name() + "\" ");
		modelStream.append("target=\"" + outputArc.destination().name() + "\" ");
		if(outputArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + outputArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}

	protected void outputTransportArc(TransportArc transArc, PrintStream modelStream) {
		modelStream.append("<transportArc ");
		
		modelStream.append("inscription=\"" + transArc.interval().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("source=\"" + transArc.source().name() + "\" ");
		modelStream.append("transition=\"" + transArc.transition().name() + "\" ");
		modelStream.append("target=\"" + transArc.destination().name() + "\" ");
		if(transArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + transArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}

	protected void outputInhibitorArc(TimedInhibitorArc inhibArc,	PrintStream modelStream) {
		modelStream.append("<inhibitorArc ");
		
		modelStream.append("inscription=\"" + inhibArc.interval().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("source=\"" + inhibArc.source().name() + "\" ");
		modelStream.append("target=\"" + inhibArc.destination().name() + "\" ");
		if(inhibArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + inhibArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}*/
}
