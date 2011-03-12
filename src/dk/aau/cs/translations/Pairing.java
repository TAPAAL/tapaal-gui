package dk.aau.cs.translations;

import java.util.Hashtable;
import java.util.List;

import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.Require;

// This class assumes that the net is conservative and
// that a place name "_BOTTOM_" exist in the model
public class Pairing {
	private TimedTransition transition;
	
	private Hashtable<TimedInputArc,TimedOutputArc> inputArcToOutputArc = new Hashtable<TimedInputArc, TimedOutputArc>();
	
	public Pairing(TimedTransition t) {
		this.transition = t;
		generatePairing();
	}

	private void generatePairing() {
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

	private void add(TimedInputArc inputArc, TimedOutputArc outputArc) {
		inputArcToOutputArc.put(inputArc, outputArc);
	}
	
	public TimedOutputArc getOutputArcFor(TimedInputArc inputArc) {
		Require.that(inputArcToOutputArc.containsKey(inputArc), "The given input arc is not in the preset of the transition");
		
		return inputArcToOutputArc.get(inputArc);
	}
	
}
