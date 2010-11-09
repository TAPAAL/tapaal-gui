package dk.aau.cs.model.petriNet;

public class InputArc {
	private Place source;
	private Transition destination;

	private InputArc(Place source, Transition destination){
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
