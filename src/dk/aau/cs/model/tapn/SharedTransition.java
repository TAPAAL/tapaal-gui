package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dk.aau.cs.util.Require;

public class SharedTransition {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
	
	private String name;
	private List<TimedTransition> transitions = new ArrayList<TimedTransition>();

	private TimedArcPetriNetNetwork network;
	
	public SharedTransition(String name){
		setName(name);
	}
	
	public TimedArcPetriNetNetwork network() {
		return network;
	}
	
	public void setNetwork(TimedArcPetriNetNetwork network){
		this.network = network;
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed transition must have a name");
		Require.that(isValid(newName), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*");
		this.name = newName;
		for(TimedTransition transition : transitions){
			transition.setName(newName);
		}
	}

	private boolean isValid(String newName) {
		return namePattern.matcher(newName).matches();
	}

	// TODO: Find a better name for this
	public void makeShared(TimedTransition transition){
		Require.that(transition != null, "transition cannot be null");
		Require.that(templateDoesNotContainSharedTransition(transition.model()), "Another transition in the same template is already shared under that name");
		transition.makeShared(this); // this will unshare first if part of another shared transition
		transitions.add(transition);
	}

	private boolean templateDoesNotContainSharedTransition(TimedArcPetriNet model) {
		for(TimedTransition transition : transitions){
			if(model.equals(transition.model())) return false;
		}
		return true;
	}

	// TODO: this should somehow change timedTransition also, but calling unshare yields infinite loop
	public void unshare(TimedTransition timedTransition) {
		Require.that(timedTransition != null, "timedTransition cannot be null");
		transitions.remove(timedTransition);
	}	

	public boolean isEnabled() {
		if(transitions.size() == 0) return false;

		for(TimedTransition transition : transitions){
			if(!transition.isEnabledAlone()) return false;
		}
		return true;
	}

	public String name() {
		return name;
	}
	
	public void delete(){
		// transition.delete() will call unshare and thus modify the transitions collection
		// which won't work while we are iterating through it, so we copy it first.
		ArrayList<TimedTransition> copy = new ArrayList<TimedTransition>(transitions);
		for(TimedTransition transition : copy){
			transition.delete();
		}
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
		if (!(obj instanceof SharedTransition))
			return false;
		SharedTransition other = (SharedTransition) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Iterable<TimedTransition> transitions() {
		return new ArrayList<TimedTransition>(transitions);
	}
}
