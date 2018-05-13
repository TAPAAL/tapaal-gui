package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pipe.gui.Pipe;

import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TransportArc extends TAPNElement {
	private Weight weight;
	private TimedPlace source;
	private TimedTransition transition;
	private TimedPlace destination;

	private TimeInterval interval;
	
	public TransportArc(TimedPlace source, TimedTransition transition, TimedPlace destination, TimeInterval interval){
		this(source, transition, destination, interval, new IntWeight(1));
	}

	public TransportArc(TimedPlace source, TimedTransition transition, TimedPlace destination, TimeInterval interval, Weight weight) {
		Require.that(source != null, "The source place cannot be null");
		Require.that(transition != null, "The associated transition cannot be null");
		Require.that(destination != null, "The destination place cannot be null");
		Require.that(!source.isShared() || !transition.isShared(), "You cannot draw an arc between a shared transition and shared place.");
		Require.that(!transition.isShared() || !destination.isShared(), "You cannot draw an arc between a shared transition and shared place.");
		
		this.source = source;
		this.transition = transition;
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

	public TimedTransition transition() {
		return transition;
	}

	public TimedPlace destination() {
		return destination;
	}

	public TimeInterval interval() {
		return interval;
	}

	public void setTimeInterval(TimeInterval interval) {
		Require.that(interval != null, "A transport arc must have an associated interval");

		this.interval = interval;
	}

	public boolean isEnabled() {
		return getElligibleTokens().size() >= weight.value();
	}

	public boolean isEnabledBy(TimedToken token) {
		Require.that(source.equals(token.place()), "Token must be in the correct place");

		return interval.isIncluded(token.age()) && destination.invariant().isSatisfied(token.age());
	}
	
	public List<TimedToken> getElligibleTokens(){
		List<TimedToken> elligibleTokens = new ArrayList<TimedToken>();
		Iterable<TimedToken> tokens = source.tokens();
		for (TimedToken token : tokens) {
			if (isEnabledBy(token)) elligibleTokens.add(token);
		}
		return elligibleTokens;
	}
	
	

	@Override
	public void delete() {
		if (model() != null)
			model().remove(this);
	}

	public TransportArc copy(TimedArcPetriNet tapn) {
		return new TransportArc(tapn.getPlaceByName(source.name()), 
								tapn.getTransitionByName(transition.name()), 
								tapn.getPlaceByName(destination.name()), 
								interval.copy(), weight);
	}

	// Should ONLY be called in relation to sharing/unsharing places
	public void setSource(TimedPlace place) {
		Require.that(place != null, "place cannot be null");
		source = place;		
	}
	
	// Should ONLY be called in relation to sharing/unsharing places
	public void setDestination(TimedPlace place) {
		Require.that(place != null, "place cannot be null");
		destination = place;		
	}
	
	public ArrayList<TimeInterval> getDEnabledInterval(){
		ArrayList<TimeInterval> result = new ArrayList<TimeInterval>();
		//We need to take the invariant on the destination into account
		TimeInterval interval = IntervalOperations.intersection(this.interval, destination().invariant().asIterval());
		
		//The arc can never be enabled 
		if(interval == null) return result;
		
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
					
					if (newUpper.compareTo(newLower) == 0 && interval.IsLowerBoundNonStrict() && interval.IsUpperBoundNonStrict()){
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
	
	@Override
	public String toString() {
		return "From " + source.name() + " to " + destination.name() + " through " + transition.name() + " with interval " + interval().toString();
	}
}
