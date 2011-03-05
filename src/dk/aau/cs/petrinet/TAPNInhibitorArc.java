package dk.aau.cs.petrinet;

/**
 * @Deprecated use dk.aau.cs.model.tapn.TimedInhibitorArc instead
 */
@Deprecated
public class TAPNInhibitorArc extends TAPNArc {

	public TAPNInhibitorArc(PlaceTransitionObject source,
			PlaceTransitionObject target, String guard) {
		super(source, target, guard);

	}

	public TAPNInhibitorArc(String guard) {
		super(guard);
	}
}
