package dk.aau.cs.model.petriNet;

public class InhibitorArc extends InputArc{
	public InhibitorArc(Place source, Transition destination){
		super(source, destination);
	}
}
