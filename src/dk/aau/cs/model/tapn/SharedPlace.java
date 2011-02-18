package dk.aau.cs.model.tapn;

import java.util.regex.Pattern;

import dk.aau.cs.util.Require;

public class SharedPlace {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
	private String name;

	public SharedPlace(String name){
		setName(name);
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed transition must have a name");
		Require.that(isValid(newName), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*");
		this.name = newName;
	}
	
	private boolean isValid(String newName) {
		return namePattern.matcher(newName).matches();
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

	public String name() {
		return name;
	}
	
	
}
