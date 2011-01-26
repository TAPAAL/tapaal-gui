package dk.aau.cs.petrinet;

public class PrioritizedTAPNTransition extends TAPNTransition {
	private int priority = 1;
	public PrioritizedTAPNTransition(String name, int priority){
		super(name);
		this.priority = priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getPriority() {
		return priority;
	}
}
