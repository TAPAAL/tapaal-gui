package dk.aau.cs.petrinet.colors;

import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;

public class ColoredTransportArc extends TAPNTransportArc {

	private ColorSet colorGuard;
	private ColoredInterval timeGuard;
	
	public ColoredTransportArc(ColoredPlace source,
			TAPNTransition intermediateTransition, ColoredPlace target,
			ColoredInterval timeGuard, ColorSet colorGuard) {
		super(source, intermediateTransition, target);
		
		this.timeGuard = timeGuard;
		this.colorGuard = colorGuard;
	}

}
