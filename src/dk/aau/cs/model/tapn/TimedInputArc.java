package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pipe.gui.Pipe;

import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TimedInputArc extends TAPNElement {
	private Weight weight = new IntWeight(1);
	private TimedPlace source;
	private TimeInterval interval;
	private TimedTransition destination;

	public TimedInputArc(TimedPlace source, TimedTransition destination, TimeInterval interval){
		this(source, destination, interval, new IntWeight(1));
	}
	
	public TimedInputArc(TimedPlace source, TimedTransition destination, TimeInterval interval, Weight weight) {
		Require.that(source != null, "A timed input arc cannot have a null source place");
		Require.that(destination != null, "A timed input arc cannot have a null destination transition");
		Require.that(!source.isShared() || !destination.isShared(), "You cannot draw an arc between a shared transition and shared place.");
		
		this.source = source;
		this.destination = destination;
		setTimeInterval(interval);
		this.weight = weight;
	}
	
	public Weight getWeight(){
		return weight;
	}
	
        public Weight getWeightValue(){
		return new IntWeight(weight.value());
	}
        
	public void setWeight(Weight weight){
		this.weight = weight;
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
		return getElligibleTokens().size() >= weight.value();
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
		return new TimedInputArc(tapn.getPlaceByName(source.name()), tapn.getTransitionByName(destination.name()), interval.copy(), weight);
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
	
	public List<TimeInterval> getDEnabledInterval(){
		ArrayList<TimeInterval> result = new ArrayList<TimeInterval>();
		BigDecimal iLow = IntervalOperations.getRatBound(interval.lowerBound()).getBound();
		BigDecimal iHeigh = IntervalOperations.getRatBound(interval.upperBound()).getBound();
		
		List<TimedToken> sortedTokens = source.sortedTokens();
		boolean overrideLowerInclusion = false;
		
		for(int i = 0; i + getWeight().value() -1 < sortedTokens.size(); i++){
			int j = i + getWeight().value() -1;
			TimedToken oldestToken = sortedTokens.get(i);
			TimedToken youngestToken = sortedTokens.get(j);
			TimeInterval temp = null;
			if( oldestToken.age().compareTo(iHeigh) <= 0 || iHeigh.compareTo(BigDecimal.ZERO) < 0){//token's age is smaller than the upper bound of the interval (or the intervals upperbound is infinite)
				BigDecimal newLower = iLow.subtract(youngestToken.age(), new MathContext(Pipe.AGE_PRECISION));
				if(newLower.compareTo(BigDecimal.ZERO) < 0){
					overrideLowerInclusion = true;
					newLower = BigDecimal.ZERO;
				}
				
				if(iHeigh.compareTo(BigDecimal.ZERO) >= 0){//not infinite
					BigDecimal newUpper = iHeigh.subtract(oldestToken.age(), new MathContext(Pipe.AGE_PRECISION));
					if(newUpper.compareTo(BigDecimal.ZERO) < 0){
						newUpper = BigDecimal.ZERO;
					}
					
					if (newUpper.compareTo(newLower) == 0 && (interval.IsLowerBoundNonStrict() || overrideLowerInclusion) && interval.IsUpperBoundNonStrict()){
						temp = new TimeInterval(true, new RatBound(newLower), new RatBound(newUpper), true);
					} else if (newLower.compareTo(newUpper) < 0){
						temp = new  TimeInterval(interval.IsLowerBoundNonStrict() || overrideLowerInclusion, new RatBound(newLower), new RatBound(newUpper), interval.IsUpperBoundNonStrict());
					} else { //new bounds are empty
						temp = null;
					}
				} else { //upper bound is inf
					temp = new TimeInterval(interval.IsLowerBoundNonStrict(), new RatBound(newLower), interval.upperBound(), false);
				}
			}
			
			if(temp != null){
				result = IntervalOperations.unionIntervalSequences(Arrays.asList(temp), result);
			}
		}
		
		return result;
	}
}
