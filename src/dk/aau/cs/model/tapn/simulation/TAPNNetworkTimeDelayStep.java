package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import pipe.gui.Pipe;
import dk.aau.cs.model.tapn.NetworkMarking;

public class TAPNNetworkTimeDelayStep extends TAPNNetworkTraceStep {
	private BigDecimal delay;

	public TAPNNetworkTimeDelayStep(BigDecimal delay) {
		this.delay = delay;
	}

	public NetworkMarking performStepFrom(NetworkMarking marking) {
		return marking.delay(delay);
	}
	
	public BigDecimal getDelay(){
		return delay;
	}
	
	public void setDelay(BigDecimal delay){
		this.delay = delay;
	}

	@Override
	public String toString() {
		String number = delay.toString();
		int index = number.indexOf(".");
		index = index < 0 ? number.length() : index;
		String padding = "";
		
		if(index < 4){
			//We multiply by two as in the used font a space only takes up half the space of a number
			int numberOfSpacesToPad = (4-index)*2;
			padding = new String(new char[numberOfSpacesToPad]).replace("\0", " ");
		}
		
		String toReturn = "TimeDelay: " + padding + number;
		return isLoopStep() ? formatAsLoopStep(toReturn) : toReturn;
	}
}
