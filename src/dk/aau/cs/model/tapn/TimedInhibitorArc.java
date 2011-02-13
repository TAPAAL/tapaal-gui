package dk.aau.cs.model.tapn;

public class TimedInhibitorArc extends TimedInputArc {
	public TimedInhibitorArc(TimedPlace source, TimedTransition destination,
			TimeInterval interval) {
		super(source, destination, interval);
	}

	@Override
	public boolean isEnabled() {
		return !super.isEnabled();
	}

	@Override
	public boolean isEnabledBy(TimedToken token) {
		return !super.isEnabledBy(token);
	}

	@Override
	public void delete() {
		model().remove(this);
	}
	
	public TimedInhibitorArc copy(TimedArcPetriNet tapn) {
		return new TimedInhibitorArc(tapn.getPlaceByName(source().name()), tapn.getTransitionByName(destination().name()), interval().copy());
	}
}
