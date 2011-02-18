package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dk.aau.cs.util.Require;

public class SharedTransition {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
	private String name;
	private List<TimedTransition> transitions;
	
	public SharedTransition(String name){
		setName(name);
		transitions = new ArrayList<TimedTransition>();
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed transition must have a name");
		Require.that(isValid(newName), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*");
		this.name = newName;
	}
	
	private boolean isValid(String newName) {
		return namePattern.matcher(newName).matches();
	}

	// TODO: Find a better name for this
	public void makeShared(TimedTransition transition){
		transitions.add(transition);
		transition.setName(name);
	}
	
	public void unshare(TimedTransition timedTransition) {
		transitions.remove(timedTransition);
	}	

	public boolean isEnabled() {
		if(transitions.size() == 0) return false;
		
		for(TimedTransition transition : transitions){
			if(!transition.isEnabledAlone()) return false;
		}
		return true;
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

	public String name() {
		return name;
	}
	
}
