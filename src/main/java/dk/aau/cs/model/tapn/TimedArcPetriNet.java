package dk.aau.cs.model.tapn;

import java.util.*;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TimedArcPetriNet {
	private String name;
	private TimedArcPetriNetNetwork parentNetwork;
    private boolean isActive;

	//Should the names be checked to see if the name is already used 
	//This is used when loading big nets as the checking of names is slow.
	private boolean checkNames = true; 

	private final List<TimedPlace> places = new ArrayList<TimedPlace>();
	private final List<TimedTransition> transitions = new ArrayList<TimedTransition>();
	private final List<TimedInputArc> inputArcs = new ArrayList<TimedInputArc>();
	private final List<TimedOutputArc> outputArcs = new ArrayList<TimedOutputArc>();
	private final List<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();
	private final List<TransportArc> transportArcs = new ArrayList<TransportArc>();

	private TimedMarking currentMarking = new LocalTimedMarking();

	public TimedArcPetriNet(String name) {
        setName(name);
        isActive = true;
    }

	public TimedMarking marking(){
		return currentMarking;
	}

	public void setParentNetwork(TimedArcPetriNetNetwork network){
		parentNetwork = network;
	}

	public TimedArcPetriNetNetwork parentNetwork(){
		return parentNetwork;
	}

	public void add(TimedPlace place) {
		add(place, false);
	}
	public void add(TimedPlace place, boolean multiRemove) {
		Require.that(place != null, "Argument must be a non-null place");
		if (!multiRemove)
			Require.that(!isNameUsed(place.name()) || (place.isShared() && !places.contains(place)), "A place or transition with the specified name, "+place.name()+", already exists in the petri net.");
		if (!place.isShared()) ((LocalTimedPlace)place).setModel(this);
		places.add(place);
		place.setCurrentMarking(currentMarking);
	}

	public boolean isColored(){
        ExprValues values = new ExprValues();
	    for (TimedTransition transition : transitions) {
	        if(transition.getGuard() != null){
                transition.getGuard().getValues(values);
            }
        }
	    for (TimedInputArc arc : inputArcs) {
	        if (arc.getArcExpression() != null) {
                arc.getArcExpression().getValues(values);
            }
	        if (!arc.getColorTimeIntervals().isEmpty()) {
	            return true;
            }
        }
        for (TimedOutputArc arc : outputArcs) {
            if (arc.getExpression() != null) {
                arc.getExpression().getValues(values);
            }
        }
        for (TimedInhibitorArc arc : inhibitorArcs) {
            if (arc.getArcExpression() != null) {
                arc.getArcExpression().getValues(values);
            }
        }
        for (TransportArc arc : transportArcs) {
            if (!arc.getColorTimeIntervals().isEmpty()) {
                return true;
            }
            if (arc.getInputExpression() != null) {
                arc.getInputExpression().getValues(values);
            }
            if (arc.getOutputExpression() != null) {
                arc.getOutputExpression().getValues(values);
            }
        }
        boolean hasColors = false;
        for (Color color : values.getColors()) {
            if (color.getColorType() != ColorType.COLORTYPE_DOT && !color.getColorType().getId().equals("dot")) {
                hasColors = true;
                break;
            }
        }
        if (hasColors || values.getColorTypes().stream().distinct().count()  > 1 || !values.getVariables().isEmpty()) {
            return true;
        }

	    return false;
    }

    public boolean isStochastic() {
        for(TimedTransition t : transitions) {
            if(t.hasCustomDistribution()) {
                return true;
            }
        }
        return false;
    }

	public void add(TimedTransition transition) {
		Require.that(transition != null, "Argument must be a non-null transition");
		Require.that(!isNameUsed(transition.name()) || transition.isShared(), "A place or transition with the specified name, "+transition.name()+", already exists in the petri net.");
		transition.setModel(this);
		transitions.add(transition);
	}

	public void add(TimedInputArc arc) {
		Require.that(arc != null, "Argument must be a non-null input arc.");
		Require.that(!checkNames || places.contains(arc.source()),	"The source place must be part of the petri net.");
		Require.that(!checkNames || transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!checkNames || !inputArcs.contains(arc), "The specified arc is already a part of the petri net.");

        //This should be a check, but we uses this in the uppaal converter while generating model
		//Require.that(!checkNames || !hasArcFromPlaceToTransition(arc.source(), arc.destination()), "Cannot have two arcs between the same place and transition");
		
		arc.setModel(this);
		inputArcs.add(arc);
		arc.source().addInputArc(arc);
		arc.destination().addToPreset(arc);
	}

	public void add(TimedOutputArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(!checkNames || places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(!checkNames || transitions.contains(arc.source()), "The source transition must be part of the petri net");
		Require.that(!checkNames || !outputArcs.contains(arc),	"The specified arc is already a part of the petri net.");

        //This should be a check, but we uses this in the uppaal converter while generating model
		//Require.that(!checkNames || !hasArcFromTransitionToPlace(arc.source(), arc.destination()), "Cannot have two arcs between the same transition and place");
	
		arc.setModel(this);
		outputArcs.add(arc);
		arc.source().addToPostset(arc);
        arc.destination().addOutputArc(arc);
    }

	public void add(TimedInhibitorArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(!checkNames || places.contains(arc.source()),	"The source place must be part of the petri net.");
		Require.that(!checkNames || transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!checkNames || !inhibitorArcs.contains(arc), "The specified arc is already a part of the petri net.");
        Require.that(!checkNames || !hasArcFromPlaceToTransition(arc.source(), arc.destination()), "Cannot have two arcs between the same place and transition");

		arc.setModel(this);
		inhibitorArcs.add(arc);
		arc.destination().addInhibitorArc(arc);
		arc.source().addInhibitorArc(arc);
	}

	public void add(TransportArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(!checkNames || places.contains(arc.source()), "The source place must be part of the petri net.");
		Require.that(!checkNames || transitions.contains(arc.transition()), "The transition must be part of the petri net");
		Require.that(!checkNames || places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(!checkNames || !inhibitorArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!checkNames || !hasArcFromPlaceToTransition(arc.source(), arc.transition()), "Cannot have two arcs between the same place and transition");
		Require.that(!checkNames || !hasArcFromTransitionToPlace(arc.transition(), arc.destination()),	"Cannot have two arcs between the same transition and place");

		arc.setModel(this);
		transportArcs.add(arc);
		arc.transition().addTransportArcGoingThrough(arc);
        arc.source().addTransportArc(arc);
        arc.destination().addTransportArc(arc);
	}

	public void addToken(TimedToken token) {
		currentMarking.add(token);
	}

    public void remove(TimedPlace place) {
		boolean removed = places.remove(place);
		if (removed && !place.isShared()){
			currentMarking.removePlaceFromMarking(place);
			((LocalTimedPlace)place).setModel(null);
		}
	}

	public void remove(TimedTransition transition) { // TODO: These methods must clean up arcs also
		boolean removed = transitions.remove(transition);

		// TODO: Removed to fix bug #891944 
		//if (removed)
		//	transition.setModel(null);
	}

	public void remove(TimedInputArc arc) {
		boolean removed = inputArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
            arc.source().removeInputArc(arc);
            arc.destination().removeFromPreset(arc);
        }
	}

	public void remove(TransportArc arc) {
		boolean removed = transportArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
            arc.destination().removeTransportArc(arc);
            arc.transition().removeTransportArcGoingThrough(arc);
		}
	}

	public void remove(TimedOutputArc arc) {
		boolean removed = outputArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
            arc.source().removeFromPostset(arc);
            arc.destination().removeOutputArc(arc);
		}
	}

	public void remove(TimedInhibitorArc arc) {
		boolean removed = inhibitorArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
            arc.destination().removeInhibitorArc(arc);
            arc.source().removeInhibitorArc(arc);
		}
	}

	public boolean hasArcFromPlaceToTransition(TimedPlace source, TimedTransition destination) {
		for (TimedInputArc arc : inputArcs)
			if (arc.source().equals(source) && arc.destination().equals(destination))
				return true;
		for (TimedInhibitorArc arc : inhibitorArcs)
			if (arc.source().equals(source) && arc.destination().equals(destination))
				return true;
		for (TransportArc arc : transportArcs)
			if (arc.source().equals(source) && arc.transition().equals(destination))
				return true;

		return false;
	}

	public boolean hasArcFromTransitionToPlace(TimedTransition source, TimedPlace destination) {
		for (TimedOutputArc arc : outputArcs){
			if (arc.source().equals(source) && arc.destination().equals(destination))
				return true;
		}
		for (TransportArc arc : transportArcs){
			if (arc.transition().equals(source) && arc.destination().equals(destination))
				return true;
		}
		return false;
	}

	public boolean isNameUsed(String name) {
		if(!isCheckNames()) return false;
		if(parentNetwork != null && parentNetwork.isNameUsedForShared(name)) return true;

		for (TimedPlace place : places){
			if (place.name().equals(name)){
				return true;
			}
		}
		for (TimedTransition transition : transitions){
			if (transition.name().equals(name)){
				return true;
			}
		}
		return false;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "name cannot be null or empty");
		name = newName;
	}

	public TimedPlace getPlaceByName(String placeName) {
		for (TimedPlace p : places) {
			if (p.name().equals(placeName)) {
				return p;
			}
		}
		return null;
	}

	public TimedTransition getTransitionByName(String transitionName) {
		for (TimedTransition t : transitions) {
			if (t.name().equals(transitionName)) {
				return t;
			}
		}
		return null;
	}

	public void setMarking(TimedMarking marking) {
		Require.that(marking != null, "marking must not be null");
		currentMarking = marking;

		for (TimedPlace p : places) {
			p.setCurrentMarking(marking);
		}
	}

	public List<TimedPlace> places() {
		return places;
	}

	public List<TimedTransition> transitions() {
		return transitions;
	}

	public Iterable<TimedInputArc> inputArcs() {
		return inputArcs;
	}

	public Iterable<TimedOutputArc> outputArcs() {
		return outputArcs;
	}

	public Iterable<TransportArc> transportArcs() {
		return transportArcs;
	}

	public Iterable<TimedInhibitorArc> inhibitorArcs() {
		return inhibitorArcs;
	}

	public TimedArcPetriNet copy() {
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);

		for(TimedPlace p : places) {

			if(!p.isShared()) {
				TimedPlace copy = p.copy();
				tapn.add(copy);
				for(TimedToken token : p.tokens()){
					tapn.addToken(new TimedToken(copy, token.getColor()));
				}
			} else {
				tapn.add(p);
			}
		}

		for(TimedTransition t : transitions){
			TimedTransition copy = t.copy();
			tapn.add(copy);
			if(t.isShared()){
				t.sharedTransition().makeShared(copy);
			}
		}

		for(TimedInputArc inputArc : inputArcs)
			tapn.add(inputArc.copy(tapn));

		for(TimedOutputArc outputArc : outputArcs)
			tapn.add(outputArc.copy(tapn));

		for(TransportArc transArc : transportArcs)
			tapn.add(transArc.copy(tapn));

		for(TimedInhibitorArc inhibArc : inhibitorArcs)
			tapn.add(inhibArc.copy(tapn));

		tapn.setActive(isActive());
		
		return tapn;
	}

	public TimedInputArc getInputArcFromPlaceToTransition(TimedPlace place, TimedTransition transition) {
		for(TimedInputArc inputArc : inputArcs) {
			if(inputArc.source().equals(place) && inputArc.destination().equals(transition))
				return inputArc;
		}
		return null;
	}

	public TimedOutputArc getOutputArcFromTransitionAndPlace(TimedTransition transition, TimedPlace place) {
		for(TimedOutputArc outputArc : outputArcs) {
			if(outputArc.source().equals(transition) && outputArc.destination().equals(place))
				return outputArc;
		}
		return null;
	}

	public TransportArc getTransportArcFromPlaceTransitionAndPlace(TimedPlace sourcePlace, TimedTransition transition, TimedPlace destinationPlace) {
		for(TransportArc transArc : transportArcs) {
			if(transArc.source().equals(sourcePlace) && transArc.transition().equals(transition) && transArc.destination().equals(destinationPlace))
				return transArc;
		}
		return null;
	}

	public TimedInhibitorArc getInhibitorArcFromPlaceAndTransition(TimedPlace place, TimedTransition transition) {
		for(TimedInhibitorArc inhibArc : inhibitorArcs) {
			if(inhibArc.source().equals(place) && inhibArc.destination().equals(transition))
				return inhibArc;
		}

		return null;
	}

	public boolean hasInhibitorArcs() {
		return inhibitorArcs.size() > 0;
	}
	
	public boolean isDegree2(){
		for(TimedTransition t : this.transitions()) {
			if(t.presetSizeWithoutInhibitorArcs() > 2 || t.postsetSize() > 2)
				return false;
		}
		return true;
	}

    public int getHighestNetDegree(){
	    int currentHighestNetDegree = 0;
        for (TimedTransition t : this.transitions()) {
            currentHighestNetDegree = Collections.max(Arrays.asList(currentHighestNetDegree, t.presetSize(), t.postsetSize()));
        }
        return currentHighestNetDegree;
    }
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	private void fillStatistics(Iterable<TimedArcPetriNet> nets, Object[][] array, int columnNumber){
		int numberOfComponents = 0;
		int numberOfPlaces = 0;
		int numberOfTransitions = 0;
		int numberOfInputArcs = 0;
		int numberOfOutputArcs = 0;
		int numberOfInhibitorArcs = 0;
		int numberOfTransportArcs = 0;
		int numberOfTotalNumberOfArcs = 0;
		int numberOfTokens = 0;
        int numberOfOrphanTransitions = 0;
        int numberOfOrphanPlaces = 0;
		boolean networkUntimed = true;
		boolean networkWeighted = false; 
        boolean isGame = hasUncontrollableTransitions();
        boolean isColored = isColored();
        boolean isStochastic = isStochastic();
		int numberOfUntimedInputArcs = 0;
		int numberOfUntimedTransportArcs = 0;
                
                HashSet<TimedPlace> sharedPlaces = new HashSet<TimedPlace>();
                HashSet<TimedTransition> sharedTransitions = new HashSet<TimedTransition>();
		
		//For comparing to 
		TimeInterval infInterval = new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
		TimeInterval infIntervalConst = new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
		
                
                
		for(TimedArcPetriNet t : nets){
			numberOfComponents += 1;
                        // here we count only the non-shared places; shared places info is added after the for-loop
                        for (TimedPlace p: t.places()) {
                            if (!p.isShared()) {
                                numberOfPlaces++;
                                numberOfTokens += p.numberOfTokens();
                            } else {
                               sharedPlaces.add(p);
                            }
                        }
                       
                        // here we count only the non-shared transitions; shared transitions are added after the for-loop
                        int sharedTransitionCount = 0;
                        for (TimedTransition trans : t.transitions()) {
                            if (!trans.isShared()) {
                                numberOfTransitions++;
                            } else {
                                sharedTransitions.add(trans);
                            }
                        }
                       
			numberOfInputArcs += t.inputArcs.size();
			numberOfOutputArcs += t.outputArcs.size();
			numberOfInhibitorArcs += t.inhibitorArcs.size();
			numberOfTransportArcs += t.transportArcs.size();
            numberOfOrphanTransitions += t.getOrphanTransitions().size();
            numberOfOrphanPlaces += t.getOrphanPlaces().size();
			//Test if all input arcs is untimed and get the number of untimed input arcs
			for(TimedInputArc in : t.inputArcs()){
				if(!(in.interval().lowerBound().value() == 0 && in.interval().isLowerBoundNonStrict() && in.interval().upperBound().equals(Bound.Infinity))){
					networkUntimed = false;
				} else {
					numberOfUntimedInputArcs++;
				}
				if(!networkWeighted && in.getWeight().value() > 1){
					networkWeighted = true;
				}
			}
			//Test if all transport arcs is untimed and get the number of untimed transport arcs
			for(TransportArc in : t.transportArcs()){
				if(!(in.interval().lowerBound().value() == 0 && in.interval().isLowerBoundNonStrict() && in.interval().upperBound().equals(Bound.Infinity))){
					networkUntimed = false;
				} else {
					numberOfUntimedTransportArcs++;
				}
				if(!networkWeighted && in.getWeight().value() > 1){
					networkWeighted = true;
				}
			}

			for (TimedPlace p : t.places) {
			    if (!p.invariant().upperBound().equals(Bound.Infinity)) {
			        networkUntimed = false;
                }
            }

			for (TimedTransition transition : t.transitions) {
                if (transition.isUrgent()) {
                    networkUntimed = false;
                    break;
                }
            }
			
			// Test all output arcs for weights
			if(!networkWeighted){
				for(TimedOutputArc in : t.outputArcs()){
					if(in.getWeight().value() > 1){
						networkWeighted = true;
						break;
					}
				}
			}
			
			// Test all inhibitor arcs for weights
			if(!networkWeighted){
				for(TimedInhibitorArc in : t.inhibitorArcs()){
					if(in.getWeight().value() > 1){
						networkWeighted = true;
						break;
					}
				}
			}
		}
                
                numberOfPlaces += sharedPlaces.size();
                numberOfTransitions += sharedTransitions.size();
                for (TimedPlace p: sharedPlaces) {
                    numberOfTokens += p.numberOfTokens();
                }
		numberOfTotalNumberOfArcs = numberOfInputArcs + numberOfOutputArcs + numberOfInhibitorArcs + numberOfTransportArcs;
		
		int rowNumber = 0;
		array[rowNumber++][columnNumber] = numberOfComponents;
		array[rowNumber++][columnNumber] = numberOfPlaces;
		array[rowNumber++][columnNumber] = numberOfTransitions;
		array[rowNumber++][columnNumber] = numberOfInputArcs;
		array[rowNumber++][columnNumber] = numberOfOutputArcs;
		array[rowNumber++][columnNumber] = numberOfInhibitorArcs;
		array[rowNumber++][columnNumber] = numberOfTransportArcs;
		array[rowNumber++][columnNumber] = numberOfTotalNumberOfArcs;
		array[rowNumber++][columnNumber] = numberOfTokens;
		array[rowNumber++][columnNumber] = numberOfUntimedInputArcs;
		array[rowNumber++][columnNumber] = numberOfUntimedTransportArcs;
		//Make space for number of shared transitions and places
		rowNumber += 2;
		array[rowNumber++][columnNumber] = networkUntimed ? "yes" : "no";
		array[rowNumber++][columnNumber] = networkWeighted ? "yes" : "no";
        array[rowNumber++][columnNumber] = isGame ? "yes" : "no";
        array[rowNumber++][columnNumber] = isColored ? "yes" : "no";
        array[rowNumber++][columnNumber] = isStochastic ? "yes" : "no";
        array[rowNumber++][columnNumber] = numberOfOrphanTransitions;
        array[rowNumber++][columnNumber] = numberOfOrphanPlaces;
	}
	
	public Object[][] getStatistics(){
		
		Object[][] result = new Object[20][4];
		int rowNumber = 0;
		int columnNumber = 0;
		result[rowNumber++][columnNumber] = "Number of components considered: ";
		result[rowNumber++][columnNumber] = "Number of places: ";
		result[rowNumber++][columnNumber] = "Number of transitions: ";
		result[rowNumber++][columnNumber] = "Number of input arcs: ";
		result[rowNumber++][columnNumber] = "Number of output arcs: ";
		result[rowNumber++][columnNumber] = "Number of inhibitor arcs: ";
		result[rowNumber++][columnNumber] = "Number of pairs of transport arcs: ";
		result[rowNumber++][columnNumber] = "Total number of arcs: ";
		result[rowNumber++][columnNumber] = "Number of tokens: ";
		result[rowNumber++][columnNumber] = "Number of untimed input arcs: ";
		result[rowNumber++][columnNumber] = "Number of untimed transport arcs: ";
		result[rowNumber++][columnNumber] = "Number of shared places: ";
		result[rowNumber++][columnNumber] = "Number of shared transitions: ";
		result[rowNumber++][columnNumber] = "The network is untimed: ";
		result[rowNumber++][columnNumber] = "The network is weighted: ";
        result[rowNumber++][columnNumber] = "The network is game: ";
        result[rowNumber++][columnNumber] = "The network is colored: ";
        result[rowNumber++][columnNumber] = "The network is stochastic: ";
		result[rowNumber++][columnNumber] = "Number of orphan transitions: ";
        result[rowNumber++][columnNumber] = "Number of orphan places: ";

        fillStatistics(Arrays.asList(new TimedArcPetriNet[] {this}), result, 1);
		fillStatistics(this.parentNetwork().activeTemplates(), result, 2);
		fillStatistics(this.parentNetwork().allTemplates(), result, 3);
		
		//Add the number of shared places and transitions
		result[11][3] = this.parentNetwork().numberOfSharedPlaces();
		result[12][3] = this.parentNetwork().numberOfSharedTransitions();
		
		return result;
	}
	
	public List<TimedTransition> getOrphanTransitions(){
		List<TimedTransition> orphans = new ArrayList<TimedTransition>();
		
		for(TimedTransition transition:transitions){
			if(transition.isOrphan()){
				orphans.add(transition);
			}
		}
		
		return orphans;
	}

    public List<TimedPlace> getOrphanPlaces(){
        List<TimedPlace> orphans = new ArrayList<TimedPlace>();

        for (TimedPlace place : places) {
            if (place.isOrphan()) {
                orphans.add(place);
            }
        }
        return orphans;
    }

    public int getNumberOfTokensInNet(){
        int result = 0;
        for (TimedPlace place : places) {
            if (place.numberOfTokens == 0) {
                int tokens = countTokens(place.tokensAsExpression, 0);
                result += (
                    (tokens == 0 && marking().getTokensFor(place).size() > 0) ?
                        marking().getTokensFor(place).size() :
                        tokens
                    );
            } else {
                result += place.numberOfTokens();
            }
        }

        return result == 0 ? marking().size() : result;
    }

    private int countTokens(Expression expression, int numberOf) {
	    if (expression == null) return 0;
        if (expression instanceof TupleExpression) return countTupleTokens((TupleExpression) expression, numberOf);

	    int result = 0;
	    for (ExprStringPosition exprStringPosition : expression.getChildren()) {
	        Expression child = exprStringPosition.getObject();
	        if (child instanceof AllExpression) {
	            result += ((AllExpression) child).size() * numberOf;
            } else if (child instanceof NumberOfExpression) {
	            result += (countTokens(child, ((NumberOfExpression) child).getNumber()));
            } else {
	            result += countTokens(child, numberOf);
            }
        }
	    return result == 0 ? numberOf : result;
    }

    private int countTupleTokens(TupleExpression expression, int numberOf) {
        int result = 1;
        for (ColorExpression colorExpression : expression.getColors()) {
            if (colorExpression instanceof AllExpression) {
                if (result < numberOf)
                    result *= ((AllExpression) colorExpression).size() * numberOf;
                else
                    result *= ((AllExpression) colorExpression).size();
            }
        }
        return result;
    }

	public boolean hasWeights() {
		for(TimedInputArc t : inputArcs){
			if(t.getWeight().value() != 1){
				return true;
			}
		}
		
		for(TimedOutputArc t : outputArcs){
			if(t.getWeight().value() != 1){
				return true;
			}
		}
		
		for(TimedInhibitorArc t : inhibitorArcs){
			if(t.getWeight().value() != 1){
				return true;
			}
		}
		
		for(TransportArc t : transportArcs){
			if(t.getWeight().value() != 1){
				return true;
			}
		}
		
		
		return false;
	}
	
	public boolean isUntimed() {
		for(TimedInputArc t : inputArcs){
			if(!t.interval().equals(TimeInterval.ZERO_INF)){
				return false;
			}
		}
		if (transportArcs.size() > 0) {
		    return false;
        }

		if (hasUrgentTransitions()) {
		    return false;
        }

		for (TimedPlace p : places) {
		    if (!p.invariant().upperBound().toString().equals("inf")) {
		        return false;
            }
        }
		
		return true;
	}
	
	public boolean hasUrgentTransitions() {
		for(TimedTransition t : transitions){
			if(t.isUrgent()){
				return true;
			}
		}
		
		return false;
	}

    public boolean hasUncontrollableTransitions() {
        for(TimedTransition t : transitions){
            if(t.isUncontrollable()){
                return true;
            }
        }

        return false;
    }
	
	public boolean isNonStrict(){
		for(TimedInputArc t : inputArcs){
			if(!t.interval().isLowerBoundNonStrict() || (!t.interval().isUpperBoundNonStrict() && !(t.interval().upperBound() instanceof InfBound))){
				return false;
			}
		}
		
		for(TransportArc t : transportArcs){
			if(!t.interval().isLowerBoundNonStrict() || (!t.interval().isUpperBoundNonStrict() && !(t.interval().upperBound() instanceof InfBound))){
				return false;
			}
		}
		
		for(TimedPlace p : places){
			if(!p.invariant().isUpperNonstrict() && !(p.invariant().upperBound() instanceof InfBound)){
				return false;
			}
		}
		
		return true;
	}

	
	/**
	 * Finds the biggest constant in the net
	 * @return the biggest constant in the net or -1 if there are no constants in the net
	 */
	public int getBiggestConstant(){
		int biggestConstant = -1;
		for(TimedInputArc t : inputArcs){
			Bound max = IntervalOperations.getMaxNoInfBound(t.interval());
			if(max.value() > biggestConstant){
				biggestConstant = max.value();
			}
		}
		
		for(TransportArc t : transportArcs){
			Bound max = IntervalOperations.getMaxNoInfBound(t.interval());
			if(max.value() > biggestConstant){
				biggestConstant = max.value();
			}
		}
		
		for(TimedPlace t : places){
			if(!(t.invariant().upperBound() instanceof InfBound) && t.invariant().upperBound().value() > biggestConstant){
				biggestConstant = t.invariant().upperBound().value();
			}
		}
		
		return biggestConstant;
	}
	
	/**
	 * Finds the biggest constant which is associated with an enabled transition
	 * @return the biggest constant which is associated with an enabled transition or -1 if there are no such constants 
	 */
	public int getBiggestConstantEnabledTransitions(){
		int biggestConstant = -1;
		
		for(TimedTransition t : transitions){
			if(t.isDEnabled()){
				int tmp = t.getLagestAssociatedConstant(); 
				if(tmp > biggestConstant){
					biggestConstant = tmp;
				}
			}
		}
		return biggestConstant;
	}

	public boolean isCheckNames() {
		return checkNames;
	}

	public void setCheckNames(boolean checkNames) {
		this.checkNames = checkNames;
	}

}
