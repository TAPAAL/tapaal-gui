package dk.aau.cs.petrinet.colors;

import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNTransition;

public class ColoredInputArc extends TAPNArc {

	private ColoredInterval timeGuard;
	private ColorSet colorGuard;

	public ColoredInputArc(ColoredPlace source, TAPNTransition target,
			ColoredInterval timeGuard, ColorSet colorGuard) {
		super(source, target, "");
		this.timeGuard = timeGuard;
		this.colorGuard = colorGuard;
	}

	public ColoredInputArc(ColoredPlace source, TAPNTransition target) {
		this(source, target, new ColoredInterval(), new ColorSet());
	}

	public ColoredInterval getTimeGuard() {
		return timeGuard;
	}

	public ColorSet getColorGuard() {
		return colorGuard;
	}

}
