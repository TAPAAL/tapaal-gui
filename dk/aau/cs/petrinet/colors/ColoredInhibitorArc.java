package dk.aau.cs.petrinet.colors;

import dk.aau.cs.petrinet.TAPNInhibitorArc;
import dk.aau.cs.petrinet.TAPNTransition;

public class ColoredInhibitorArc extends TAPNInhibitorArc {

	private ColoredInterval timeGuard;
	private ColorSet colorGuard;

	public ColoredInhibitorArc(ColoredPlace source, TAPNTransition target, 
			ColoredInterval timeGuard, ColorSet colorGuard) {
		super(source, target,"");
		this.timeGuard = timeGuard;
		this.colorGuard = colorGuard;
	}
}
