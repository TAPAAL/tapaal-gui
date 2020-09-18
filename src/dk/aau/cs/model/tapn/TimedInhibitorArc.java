package dk.aau.cs.model.tapn;

import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimedInhibitorArc extends TimedInputArc {

    private List<ColoredTimeInterval> colorTimeIntervals = new ArrayList<ColoredTimeInterval>();

    public TimedInhibitorArc(TimedPlace source, TimedTransition destination, TimeInterval interval, ArcExpression expression) {
		this(source, destination, interval, new IntWeight(1), expression);
	}
	
	public TimedInhibitorArc(TimedPlace source, TimedTransition destination, TimeInterval interval, Weight weight, ArcExpression expression) {
		super(source, destination, TimeInterval.ZERO_INF, weight, expression);
	}

    public TimedInhibitorArc(TimedPlace source, TimedTransition destination) {
        this(source, destination, TimeInterval.ZERO_INF, new IntWeight(1), null);
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
		return new TimedInhibitorArc(tapn.getPlaceByName(source().name()), tapn.getTransitionByName(destination().name()), interval().copy(), getWeight(), expression.copy());
	}
}
