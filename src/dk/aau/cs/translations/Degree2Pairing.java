package dk.aau.cs.translations;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

// This class assumes that the transition is conservative and
// moreover that it is also Degree 2.
public class Degree2Pairing extends Pairing {

	public Degree2Pairing(TimedTransition t) {
		super(t);
	}
	
	@Override
	protected void generatePairing() {
		List<TimedInputArc> inputArcs = getTransition().getInputArcs();
		List<TimedOutputArc> outputArcs = getTransition().getOutputArcs();
		
		int presetSize = inputArcs.size();
		int postsetSize = outputArcs.size();
		
		Require.that(presetSize == postsetSize, "The provided model is not conservative");
		Require.that(presetSize <= 2, "The provided model is more than degree 2");

		if(getTransition().presetSize() == 0)
			return;
		else if(getTransition().getInputArcs().size() == 1)
			add(getTransition().getInputArcs().get(0), getTransition().getOutputArcs().get(0));
		else {
			for(TimedInputArc inputArc : inputArcs) {
				if(isPartOfLockTemplate(inputArc.source().name())) {
					for(TimedOutputArc outputArc : outputArcs) {
						if(isPartOfLockTemplate(outputArc.destination().name()) && !getInputArcToOutputArc().containsValue(outputArc)) {		
							add(inputArc,outputArc);
							break;
						}
					}
				} else {
					for(TimedOutputArc outputArc : outputArcs) {
						if(!isPartOfLockTemplate(outputArc.destination().name()) && !getInputArcToOutputArc().containsValue(outputArc)) {
							add(inputArc,outputArc);
							break;
						}
					}
				}
					
			}
		}
	}
	
	private boolean isPartOfLockTemplate(String name) {
		Pattern pattern = Pattern.compile("^(P_(?:[a-zA-Z][a-zA-Z0-9_]*)_(?:(?:[0-9]*_(?:in|out)|check))|P_lock|P_deadlock)$");

		Matcher matcher = pattern.matcher(name);
		return matcher.find();
	}

}
