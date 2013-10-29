package dk.aau.cs.translations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import dk.aau.cs.model.tapn.IntWeight;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

// This class assumes that the transition is conservative
public class PairingCombi extends Pairing {
	
	private Hashtable<String, Boolean> placeNameToTimed;
	
	public PairingCombi(TimedTransition t, Hashtable<String, Boolean> placeNameToTimed) {
		super(t);
		this.placeNameToTimed = placeNameToTimed;
		
		//This forces the instantiation of inputArcToOutputArc, this is necessary since
		//the generateParings method in this class has side effects.
		//The first time the placeNameToTimed is read, is when iterating over the transitions preset,  
		//and as generatePairing alters this, the normal lazy instantiation done in Pairing will fail.
		getInputArcToOutputArc();
	}

	//TODO this method is way too long
	protected void generatePairing() {
		List<TimedInputArc> inputArcs = getTransition().getInputArcs();
		List<TimedOutputArc> outputArcs = getTransition().getOutputArcs();
		
		int presetSize = 0;
		int postsetSize = 0;
		
		for(TimedInputArc ia : inputArcs){
			if(placeNameToTimed.get(ia.source().name())){
				presetSize ++;
			}
		}
		
		for(TimedOutputArc oa : outputArcs){
			if(placeNameToTimed.get(oa.destination().name())){
				postsetSize++;
			}
		}	
		
		TimedInputArc[] timedInputArcs = new TimedInputArc[presetSize];
		TimedOutputArc[] timedOutputArcs = new TimedOutputArc[postsetSize];

		int in = 0;
		for(TimedInputArc ia : inputArcs){
			if(placeNameToTimed.get(ia.source().name())){
				timedInputArcs[in] = ia;
				in++;
			}
		}
		
		int out = 0;
		for(TimedOutputArc oa : outputArcs){
			if(placeNameToTimed.get(oa.destination().name())){
				timedOutputArcs[out] = oa;
				out++;
			}
		}
		
		Require.that(presetSize == postsetSize, "The provided model is not conservative");
		
		Map<String, ArrayList<Map<String, Integer>>> inputToOutputName = new HashMap<String, ArrayList<Map<String, Integer>>>();

		for(int i = 0; i < presetSize; i++)
		{
			boolean test = false;
			int j = 0;
			if(inputToOutputName.containsKey(timedInputArcs[i].source().name())){
				for(j=0 ; j < inputToOutputName.get(timedInputArcs[i].source().name()).size() ; j++ ){
					if(inputToOutputName.get(timedInputArcs[i].source().name()).get(j).containsKey(timedOutputArcs[i].destination().name())){
						test = true;
						break;
					}
				}
			}
			
			if(test){
				int prevArc = inputToOutputName.get(timedInputArcs[i].source().name()).get(j).get(timedOutputArcs[i].destination().name());
				IntWeight newInWeight = new IntWeight(timedInputArcs[prevArc].getWeight().value()+1);
				IntWeight newOutWeight = new IntWeight(timedOutputArcs[prevArc].getWeight().value()+1);
				
				getTransition().removeFromPreset(timedInputArcs[i]);
				getTransition().removeFromPostset(timedOutputArcs[i]);
				
				timedInputArcs[prevArc].setWeight(newInWeight);
				timedOutputArcs[prevArc].setWeight(newOutWeight);
			}else{
				add(timedInputArcs[i], timedOutputArcs[i]);
				Map<String,Integer> newOutMap = new HashMap<String,Integer>();
				
				newOutMap.put(timedOutputArcs[i].destination().name(),i);
				
				ArrayList<Map<String, Integer>> existing = new ArrayList<Map<String, Integer>>();
				
				if(inputToOutputName.containsKey(timedInputArcs[i].source().name())){
					existing = inputToOutputName.get(timedInputArcs[i].source().name());
				}
				
				existing.add(newOutMap);
				
				inputToOutputName.put(timedInputArcs[i].source().name(), existing);
			}
		}	
	}
}
