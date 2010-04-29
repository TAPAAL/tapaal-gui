package dk.aau.cs.petrinet.colors;

import dk.aau.cs.petrinet.PlaceTransitionObject;
import dk.aau.cs.petrinet.TAPNArc;

public class ColoredInputArc extends TAPNArc {

	private ColoredInterval timeGuard;
	private ColorSet colorGuard;

	public ColoredInputArc(PlaceTransitionObject source,
			PlaceTransitionObject target, ColoredInterval timeGuard, ColorSet colorGuard) {
		super(source, target, "");
		this.timeGuard = timeGuard;
		this.colorGuard = colorGuard;
	}

}
