package dk.aau.cs.petrinet.colors;

import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;

public class ColoredTransportArc extends TAPNTransportArc {

	private ColorSet colorGuard;
	private ColoredInterval timeGuard;
	private Preservation preserves;
	private int outputValue;
	
	public ColoredTransportArc(ColoredPlace source,
			TAPNTransition intermediateTransition, ColoredPlace target,
			ColoredInterval timeGuard, ColorSet colorGuard,
			Preservation preserves, int outputValue) {
		super(source, intermediateTransition, target);
		
		this.timeGuard = timeGuard;
		this.colorGuard = colorGuard;
		this.preserves = preserves;
		this.outputValue = outputValue;
	}


	public ColoredInterval getTimeGuard() {
		return timeGuard;
	}

	public ColorSet getColorGuard() {
		return colorGuard;
	}

	public Preservation getPreservation() {
		return preserves;
	}

	public int getOutputValue(){
		return outputValue;
	}
}
