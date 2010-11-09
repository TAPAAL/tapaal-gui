package dk.aau.cs.model.petriNet;

public class OutputArc {
	private Transition source;
	private Place destination;

	private OutputArc(Transition source, Place destination){
		this.source = source;
		this.destination = destination;
	}
	
	public Transition destination() {
		return source;
	}

	public Place source() {
		return destination;
	}
}
