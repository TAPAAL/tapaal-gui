package dk.aau.cs.model.tapn;

public class TimedInhibitorArc extends TimedInputArc {
	public TimedInhibitorArc(TimedPlace source, TimedTransition destination, TimeInterval interval) {
		super(source, destination, interval);
	}	
}
