package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.Expressions.*;
import pipe.gui.Constants;

import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TransportArc extends TAPNElement {
	private Weight weight;
	private TimedPlace source;
	private final TimedTransition transition;
	private TimedPlace destination;
    private ArcExpression inputExpression;
    private ArcExpression outputExpression;
    private List<ColoredTimeInterval> ctiList = new ArrayList<ColoredTimeInterval>();

	private TimeInterval interval;
	
	public TransportArc(TimedPlace source, TimedTransition transition, TimedPlace destination, TimeInterval interval){
		this(source, transition, destination, interval, new IntWeight(1));
	}

    public TransportArc(TimedPlace source, TimedTransition transition, TimedPlace destination, TimeInterval interval, Weight weight) {
        this(source,transition,destination,interval,weight, null, null);
    }
	public TransportArc(TimedPlace source, TimedTransition transition, TimedPlace destination, TimeInterval interval, Weight weight,ArcExpression inputExpression, ArcExpression outputExpression) {
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

        if(inputExpression == null){
            createNewInputArcExpression();
        } else{
            this.inputExpression = inputExpression;
        }
        if(outputExpression == null){
            createNewOutputArcExpression();
        } else{
            this.outputExpression = outputExpression;
        }
	}

    public TransportArc(TimedPlace source, TimedTransition transitions, TimedPlace destination) {
        this(source, transitions, destination, TimeInterval.ZERO_INF);
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
	    TransportArc ta = new TransportArc(tapn.getPlaceByName(source.name()),
            tapn.getTransitionByName(transition.name()),
            tapn.getPlaceByName(destination.name()),
            interval.copy(), weight,inputExpression.deepCopy(),outputExpression.deepCopy());
	    ta.setColorTimeIntervals(ctiList);
		return ta;
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
				BigDecimal newLower = iLow.subtract(youngestToken.age(), new MathContext(Constants.AGE_PRECISION));
				if(newLower.compareTo(BigDecimal.ZERO) < 0){
					overrideLowerInclusion = true;
					newLower = BigDecimal.ZERO;
				}
				
				if(iHeigh.compareTo(BigDecimal.ZERO) >= 0){//not infinite
					BigDecimal newUpper = iHeigh.subtract(oldestToken.age(), new MathContext(Constants.AGE_PRECISION));
					if(newUpper.compareTo(BigDecimal.ZERO) < 0){
						newUpper = BigDecimal.ZERO;
					}
					
					if (newUpper.compareTo(newLower) == 0 && interval.isLowerBoundNonStrict() && interval.isUpperBoundNonStrict()){
						temp = new TimeInterval(true, new RatBound(newLower), new RatBound(newUpper), true);
					} else if (newLower.compareTo(newUpper) < 0){
						temp = new  TimeInterval(interval.isLowerBoundNonStrict() || overrideLowerInclusion, new RatBound(newLower), new RatBound(newUpper), interval.isUpperBoundNonStrict());
					} else { //new bounds are empty
						temp = null;
					}
				} else { //upper bound is inf
					temp = new TimeInterval(interval.isLowerBoundNonStrict(), new RatBound(newLower), interval.upperBound(), false);
				}
			}
			
			if(temp != null){
				result = IntervalOperations.unionIntervalSequences(List.of(temp), result);
			}
		}

		return result;
	}

    public void createNewInputArcExpression() {
        UserOperatorExpression userOperatorExpression = new UserOperatorExpression(source().getColorType().getFirstColor());
        Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
        vecColorExpr.add(userOperatorExpression);
        NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
        setInputExpression(numbExpr);
        setWeight(new IntWeight(1));
    }

    public void createNewOutputArcExpression() {
        UserOperatorExpression userOperatorExpression = new UserOperatorExpression(destination().getColorType().getFirstColor());
        Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
        vecColorExpr.add(userOperatorExpression);
        NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
        setOutputExpression(numbExpr);
        setWeight(new IntWeight(1));
    }
	
	@Override
	public String toString() {
		return "from " + source.name() + " to " + destination.name() + " through " + transition.name() + " with interval " + interval().toString();
	}

	public String fromTo(){
        return "from " + source.name() + " to " + destination.name() + " through " + transition.name();
    }
    public ArcExpression getOutputExpression() {
        return outputExpression;
    }

    public void setOutputExpression(ArcExpression outputExpression) {
        this.outputExpression = outputExpression;
    }

    public ArcExpression getInputExpression() {
        return inputExpression;
    }

    public void setInputExpression(ArcExpression inputExpression) {
        this.inputExpression = inputExpression;
    }

    public List<ColoredTimeInterval> getColorTimeIntervals() {return ctiList;}
    public void setColorTimeIntervals(List<ColoredTimeInterval> ctiList) {this.ctiList = ctiList;}
}
