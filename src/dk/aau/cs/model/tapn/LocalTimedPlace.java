package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Tuple;

public class LocalTimedPlace  extends TimedPlace {

	private TimedArcPetriNet model;

    public LocalTimedPlace(String name) {
		this(name, TimeInvariant.LESS_THAN_INFINITY);
	}

	public LocalTimedPlace(String name, TimeInvariant invariant) {
		setName(name);
		setInvariant(invariant);
	}
	
	public TimedArcPetriNet model() {
		return model;
	}

	public void setModel(TimedArcPetriNet model) {
		this.model = model;
	}

	public boolean isShared() {
		return false;
	}

	public LocalTimedPlace copy() {
		LocalTimedPlace p = new LocalTimedPlace(name);

		p.invariant = invariant.copy();

		return p;
	}

	@Override
	public String toString() {
		if (model() != null)
			return model().name() + "." + name;
		else
			return name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model() == null) ? 0 : model().hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof LocalTimedPlace))
			return false;
		LocalTimedPlace other = (LocalTimedPlace) obj;
		if (model() == null) {
			if (other.model() != null)
				return false;
		} else if (!model().equals(other.model()))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public Tuple<PlaceType, Integer> extrapolate(){
		if(extrapolation.value2() > -2)	return extrapolation;
		
		PlaceType type = PlaceType.Dead;
		int cmax = -1;
		
		extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
		
		if(invariant != TimeInvariant.LESS_THAN_INFINITY){
			cmax = Math.max(cmax, invariant.upperBound().value());
		}
		
		// Invariant place
		if(cmax > -1){
			extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Dead, -2);
			return new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Invariant, cmax);
		}
		
		
		for(TimedInputArc arc : model.inputArcs()){
			if(!arc.source().equals(this))	continue;
			if(!arc.interval().upperBound().equals(Bound.Infinity)){
				cmax = Math.max(cmax, arc.interval().upperBound().value());
			}else if(arc.interval().lowerBound().value() > 0){
				cmax = Math.max(cmax, arc.interval().lowerBound().value());
			}
			
			if(type == PlaceType.Dead && arc.interval().upperBound().equals(Bound.Infinity)){
				type = PlaceType.Standard;
			}
			
			extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
		}
		
		for(TransportArc arc : model.transportArcs()){
			if(!arc.source().equals(this))	continue;
			if(!arc.interval().upperBound().equals(Bound.Infinity)){
				cmax = Math.max(cmax, arc.interval().upperBound().value());
			}else if(arc.interval().lowerBound().value() > 0){
				cmax = Math.max(cmax, arc.interval().lowerBound().value());
			}
			
			if(type == PlaceType.Dead && arc.interval().upperBound().equals(Bound.Infinity)){
				type = PlaceType.Standard;
			}
			
			Tuple<PlaceType, Integer> other = arc.source().extrapolate();
			if(other.value2() > cmax){
				cmax = other.value2();
			}
			
			if(type == PlaceType.Dead && arc.interval().upperBound().equals(Bound.Infinity)){
				type = PlaceType.Standard;
			}
			
			extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
		}
		
		extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Dead, -2);
		
		return new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
	}
}
