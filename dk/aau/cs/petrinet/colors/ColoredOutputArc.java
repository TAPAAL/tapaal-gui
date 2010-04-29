package dk.aau.cs.petrinet.colors;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPNTransition;

public class ColoredOutputArc extends Arc {
	private int outputValue;
	
	public ColoredOutputArc(TAPNTransition source, ColoredPlace target, int outputValue){
		super(source, target);
		this.outputValue = outputValue;
	}
	
	public void setOutputValue(int value){
		outputValue = value;
	}
	
	public int getOutputValue() {
		return outputValue;
	}

}
