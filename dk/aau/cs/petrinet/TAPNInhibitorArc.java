package dk.aau.cs.petrinet;

public class TAPNInhibitorArc extends TAPNArc {

	public TAPNInhibitorArc(PlaceTransitionObject source,
			PlaceTransitionObject target, String guard) {
		super(source, target, guard);

	}

	public TAPNInhibitorArc(String guard) {
		super(guard);
	}
}
