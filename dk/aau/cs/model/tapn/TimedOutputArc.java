package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class TimedOutputArc {
	private TimedTransition source;
	private TimedPlace destination;
	
	public TimedOutputArc(TimedTransition source, TimedPlace destination){
		Require.that(source != null, "An arc must have a non-null source transition");
		Require.that(destination != null, "An arc must have a non-null destination place");
		
		this.source = source;
		this.destination = destination;
	}
	
	public TimedTransition source() {
		return source;
	}

	public TimedPlace destination() {
		return destination;
	}
}
