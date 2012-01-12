package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import pipe.gui.Pipe;

import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TimedInputArc extends TAPNElement {
	private TimedPlace source;
	private TimeInterval interval;
	private TimedTransition destination;

	public TimedInputArc(TimedPlace source, TimedTransition destination, TimeInterval interval) {
		Require.that(source != null, "A timed input arc cannot have a null source place");
		Require.that(destination != null, "A timed input arc cannot have a null destination transition");
		Require.that(!source.isShared() || !destination.isShared(), "You cannot draw an arc between a shared transition and shared place.");
		
		this.source = source;
		this.destination = destination;
		setTimeInterval(interval);
	}

	public TimedPlace source() {
		return source;
	}

	public TimeInterval interval() {
		return interval;
	}

	public void setTimeInterval(TimeInterval interval) {
		Require.that(interval != null, "A timed input arc cannot have a null interval");

		this.interval = interval;
	}

	public TimedTransition destination() {
		return destination;
	}

	public boolean isEnabled() {
		return getElligibleTokens().size() > 0;
	}

	public boolean isEnabledBy(TimedToken token) {
		Require.that(source.equals(token.place()), "This token is from a different place");
		return interval.isIncluded(token.age());
	}
	
	public List<TimedToken> getElligibleTokens(){
		ArrayList<TimedToken> elligbleTokens = new ArrayList<TimedToken>();
		List<TimedToken> tokens = source.tokens();

		for (TimedToken t : tokens) {
			if (isEnabledBy(t)) elligbleTokens.add(t);
		}

		return elligbleTokens;
	}

	public void delete() {
		model().remove(this);
	}

	public TimedInputArc copy(TimedArcPetriNet tapn) {
		return new TimedInputArc(tapn.getPlaceByName(source.name()), tapn.getTransitionByName(destination.name()), interval.copy());
	}

	// This method should ONLY be called in relation to sharing/unsharing a place
	public void setSource(TimedPlace place) {
		Require.that(place != null, "place cannot be null");
		source = place;		
	}
	
	@Override
	public String toString() {
		return "From " + source.name() + " to " + destination.name() + " with interval " + interval().toString();
	}
	
	public TimeInterval getDEnabledInterval(){
		TimeInterval result = null;
		BigDecimal iLow = new BigDecimal(interval.lowerBound().value(), new MathContext(Pipe.AGE_PRECISION));
		BigDecimal iHeigh = new BigDecimal(interval.upperBound().value(), new MathContext(Pipe.AGE_PRECISION));
		
		for(TimedToken token : source.tokens()){
			TimeInterval temp = null;
			if( token.age().compareTo(iHeigh) <= 0 || interval().upperBound().value() < 0){//token's age is smaller than the upper bound of the interval (or the intervals upperbound is infinite)
				BigDecimal newLower = iLow.subtract(token.age(), new MathContext(Pipe.AGE_PRECISION));
				if(newLower.compareTo(BigDecimal.ZERO) < 0){
					newLower = BigDecimal.ZERO;
				}
				
				if(interval.upperBound().value() >= 0){//not infinite
					BigDecimal newUpper = iHeigh.subtract(token.age(), new MathContext(Pipe.AGE_PRECISION));
					if(newUpper.compareTo(BigDecimal.ZERO) < 0){
						newUpper = BigDecimal.ZERO;
					}
					
					if(newUpper.compareTo(BigDecimal.ZERO) == 0 && interval.IsUpperBoundNonStrict()){
						temp = new TimeInterval(true, new IntBound(newLower.intValue()), new IntBound(newUpper.intValue()), true);
					} else if (newUpper.compareTo(newLower) == 0 && interval.IsLowerBoundNonStrict() && interval.IsUpperBoundNonStrict()){
						temp = new TimeInterval(true, new IntBound(newLower.intValue()), new IntBound(newUpper.intValue()), true);
					} else if (newLower.compareTo(newUpper) < 0){
						temp = new  TimeInterval(interval.IsLowerBoundNonStrict(), new IntBound(newLower.intValue()), new IntBound(newUpper.intValue()), interval.IsUpperBoundNonStrict());
					} else { //new bounds are wrong
						temp = null;
					}
				} else { //upper bound is inf
					temp = new TimeInterval(interval.IsLowerBoundNonStrict(), new IntBound(newLower.intValue()), interval.upperBound(), false);
				}
			}
			
			
			
			result = IntervalOperations.union(temp, result);
		}
		
		//TODO Consider invariants on the source
		return result;
	}
}
