package dk.aau.cs.model.tapn.event;

public interface TimedPlaceListener {
	void nameChanged(TimedPlaceEvent e);
	void invariantChanged(TimedPlaceEvent e);
	void markingChanged(TimedPlaceEvent e);
}
