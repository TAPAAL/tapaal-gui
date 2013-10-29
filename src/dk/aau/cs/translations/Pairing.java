package dk.aau.cs.translations;

import java.util.Hashtable;
import java.util.List;

import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

// This class assumes that the transition is conservative
public class Pairing {
	private TimedTransition transition;
	
	private Hashtable<TimedInputArc,TimedOutputArc> inputArcToOutputArc = null;
	
	public Pairing(TimedTransition t) {
		transition = t;
	}

	protected void generatePairing() {
		List<TimedInputArc> inputArcs = transition.getInputArcs();
		List<TimedOutputArc> outputArcs = transition.getOutputArcs();
		
		int presetSize = inputArcs.size();
		int postsetSize = outputArcs.size();
		
		Require.that(presetSize == postsetSize, "The provided model is not conservative");

		for(int i = 0; i < presetSize; i++)
		{			
			add(inputArcs.get(i), outputArcs.get(i));
		}
	}

	protected void add(TimedInputArc inputArc, TimedOutputArc outputArc) {
		getInputArcToOutputArc().put(inputArc, outputArc);
	}
	
	public TimedOutputArc getOutputArcFor(TimedInputArc inputArc) {
		Require.that(getInputArcToOutputArc().containsKey(inputArc), "The given input arc is not in the preset of the transition");
		
		return getInputArcToOutputArc().get(inputArc);
	}
	
	protected TimedTransition getTransition(){
		return transition;
	}
	
	protected Hashtable<TimedInputArc,TimedOutputArc> getInputArcToOutputArc(){
		if(inputArcToOutputArc == null){
			inputArcToOutputArc = new Hashtable<TimedInputArc, TimedOutputArc>();
			generatePairing();
		}
		return inputArcToOutputArc;
	}
	
}
