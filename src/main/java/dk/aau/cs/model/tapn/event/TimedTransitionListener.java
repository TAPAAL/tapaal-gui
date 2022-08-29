package dk.aau.cs.model.tapn.event;

public interface TimedTransitionListener {
	void nameChanged(TimedTransitionEvent e);
	void sharedStateChanged(TimedTransitionEvent e);
}
