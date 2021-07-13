package dk.aau.cs.model.tapn;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class LocalTimedPlace  extends TimedPlace {
    private ColorType colorType;
    private List<ColoredTimeInvariant> ctiList = new ArrayList<ColoredTimeInvariant>();
	private TimedArcPetriNet model;
    public LocalTimedPlace(String name){
        this(name, ColorType.COLORTYPE_DOT);

    }
    public LocalTimedPlace(String name, ColorType colorType) {
		this(name, TimeInvariant.LESS_THAN_INFINITY, colorType);
	}

	public LocalTimedPlace(String name, TimeInvariant invariant, ColorType ct) {
		setName(name);
		setInvariant(invariant);
		colorType = ct;
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
		LocalTimedPlace p = new LocalTimedPlace(name, colorType);
		if(tokensAsExpression != null){
            p.setTokenExpression(tokensAsExpression.deepCopy());
        }
		p.setCtiList(ctiList);

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
	@Override
    public List<ColoredTimeInvariant> getCtiList() {
        return ctiList;
    }

    public void setCtiList(List<ColoredTimeInvariant> ctiList) {
        List<ColoredTimeInvariant> found = new ArrayList<ColoredTimeInvariant>();
        for (ColoredTimeInvariant timeInvariant : ctiList) {
            if (timeInvariant == null)
                found.add(timeInvariant);
        }
        ctiList.removeAll(found);
        this.ctiList = ctiList;
    }
    public void setColorType(ColorType colorType) {
        if(!this.colorType.equals(colorType)) {
            currentMarking.getTokensFor(this).removeAll(currentMarking.getTokensFor(this));
            this.colorType = colorType;
            fireMarkingChanged();
        }
    }
    @Override
    public ColorType getColorType() {return colorType;}
}
