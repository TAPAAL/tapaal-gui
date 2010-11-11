package dk.aau.cs.model.petriNet;

import dk.aau.cs.util.Require;

public class InputArc {
	private Place source;
	private Transition destination;

	public InputArc(Place source, Transition destination){
		Require.that(source != null, "An arc must have a non-null source place");
		Require.that(destination != null, "An arc must have a non-null destination transition");
		
		this.source = source;
		this.destination = destination;
	}
	
	public Transition destination() {
		return destination;
	}

	public Place source() {
		return source;
	}
}
