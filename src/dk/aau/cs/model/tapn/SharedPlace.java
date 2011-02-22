package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dk.aau.cs.util.Require;

public class SharedPlace {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
	
	private String name;
	private List<TimedPlace> places = new ArrayList<TimedPlace>();
	
	private TimedArcPetriNetNetwork network;

	public SharedPlace(String name){
		setName(name);
	}
	

	public void delete() {
		// place.delete() will call unshare and thus modify the place collection
		// which won't work while we are iterating through it, so we copy it first.
		ArrayList<TimedPlace> copy = new ArrayList<TimedPlace>(places);
		for(TimedPlace place : copy){
			place.delete();
		}
	}

	public void setNetwork(TimedArcPetriNetNetwork network) {
		this.network = network;		
	}
	
	public TimedArcPetriNetNetwork network(){
		return network;
	}
	
	public String name() {
		return name;
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed transition must have a name");
		Require.that(isValid(newName), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*");
		this.name = newName;
		for(TimedPlace place : places){
			place.setName(newName);
		}
	}
	
	private boolean isValid(String newName) {
		return namePattern.matcher(newName).matches();
	}
	
	// TODO: Find a better name for this
	public void makeShared(TimedPlace place){
		Require.that(place != null, "place cannot be null");
		Require.that(templateDoesNotContainSharedPlace(place.model()), "Another place in the same template is already shared under that name");
		place.makeShared(this); // this will unshare first if part of another shared transition
		places.add(place);
		place.setName(name);
	}

	private boolean templateDoesNotContainSharedPlace(TimedArcPetriNet model) {
		for(TimedPlace place : places){
			if(model.equals(place.model())) return false;
		}
		return true;
	}

	// TODO: this should somehow change timedPlace also, but calling unshare yields infinite loop
	public void unshare(TimedPlace timedPlace) {
		Require.that(timedPlace != null, "timedPlace cannot be null");
		places.remove(timedPlace);
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
}
