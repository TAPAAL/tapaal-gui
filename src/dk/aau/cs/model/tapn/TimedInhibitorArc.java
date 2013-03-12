package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimedInhibitorArc extends TimedInputArc {

	public TimedInhibitorArc(TimedPlace source, TimedTransition destination, TimeInterval interval) {
		this(source, destination, interval, new IntWeight(1));
	}
	
	public TimedInhibitorArc(TimedPlace source, TimedTransition destination, TimeInterval interval, Weight weight) {
		super(source, destination, TimeInterval.ZERO_INF, weight);
	}
	
	public List<TimeInterval> getDEnabledInterval(){
		if(source().tokens().size() < getWeight().value()){
			return Arrays.asList(new TimeInterval(true, new RatBound(BigDecimal.ZERO), Bound.Infinity, false));
		} else {
			return new ArrayList<TimeInterval>();
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
		return new TimedInhibitorArc(tapn.getPlaceByName(source().name()), tapn.getTransitionByName(destination().name()), interval().copy(), getWeight());
	}
}
