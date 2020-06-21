package dk.aau.cs.model.tapn;

import java.util.ArrayList;

import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import dk.aau.cs.util.Tuple;

public class SharedPlace extends TimedPlace{

    private TimedArcPetriNetNetwork network;

    public SharedPlace(String name){
		this(name, TimeInvariant.LESS_THAN_INFINITY);
	}
	
	public SharedPlace(String name, TimeInvariant invariant){
		setName(name);
		setInvariant(invariant);
	}

    public void setNetwork(TimedArcPetriNetNetwork network) {
		this.network = network;		
	}
	
	public TimedArcPetriNetNetwork network(){
		return network;
	}
	


	public TimedPlace copy() {
		return new SharedPlace(this.name(), this.invariant().copy());
	}

	public boolean isShared() {
		return true;
	}

	
	public ArrayList<String> getComponentsUsingThisPlace(){
		ArrayList<String> components = new ArrayList<String>();
		for(Template t : CreateGui.getCurrentTab().allTemplates()){
			TimedPlace tp = t.model().getPlaceByName(SharedPlace.this.name);
			if(tp != null){
				components.add(t.model().name());
			}
		}
		return components;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SharedPlace))
			return false;
		SharedPlace other = (SharedPlace) obj;
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
		
		for(Template t : CreateGui.getCurrentTab().activeTemplates()){
			TimedPlace tp = t.model().getPlaceByName(SharedPlace.this.name);
			if(tp != null){
				cmax = Math.max(cmax, tp.extrapolate().value2());
				if(tp.extrapolate().value1() == PlaceType.Invariant || (type == PlaceType.Dead && tp.extrapolate().value1() == PlaceType.Standard)){
					type = tp.extrapolate().value1();
				}
				extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
			}
		}
		
		extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Dead, -2);
		
		return new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
	}
}
