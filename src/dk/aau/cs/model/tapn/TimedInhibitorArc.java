package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

public class TimedInhibitorArc extends TimedInputArc {
	public TimedInhibitorArc(TimedPlace source, TimedTransition destination, TimeInterval interval) {
		super(source, destination, TimeInterval.ZERO_INF);
	}
	
	public TimeInterval getDEnabledInterval(){
		if(source().tokens().size() == 0){
			return new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
		} else {
			return new TimeInterval(false, new IntBound(0), new IntBound(0), false);
		}
	}
	
	@Override
	public void setTimeInterval(TimeInterval interval) {
		super.setTimeInterval(TimeInterval.ZERO_INF);
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
	public List<TimedToken> getElligibleTokens() {
		ArrayList<TimedToken> elligbleTokens = new ArrayList<TimedToken>();
		List<TimedToken> tokens = source().tokens();

		for (TimedToken t : tokens) {
			if (!isEnabledBy(t)) elligbleTokens.add(t);
		}

		return elligbleTokens;
	}

	@Override
	public void delete() {
		model().remove(this);
	}
	
	public TimedInhibitorArc copy(TimedArcPetriNet tapn) {
		return new TimedInhibitorArc(tapn.getPlaceByName(source().name()), tapn.getTransitionByName(destination().name()), interval().copy());
	}
}
