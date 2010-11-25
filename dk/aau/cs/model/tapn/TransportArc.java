package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class TransportArc {
	private TimedPlace source;
	private TimedTransition transition;
	private TimedPlace destination;

	private TimeInterval interval;

	public TransportArc(TimedPlace source, TimedTransition transition, TimedPlace destination,
			TimeInterval interval){
		Require.that(source != null, "The source place cannot be null");
		Require.that(transition != null, "The associated transition cannot be null");
		Require.that(destination != null, "The destination place cannot be null");
			
		this.source = source;
		this.transition = transition;
		this.destination = destination;
		setTimeInterval(interval);
	}
	
	public TimedPlace source(){
		return source;
	}
	
	public TimedTransition transition(){
		return transition;
	}
	
	public TimedPlace destination(){
		return destination;
	}

	public TimeInterval timeInterval(){
		return interval;
	}
	
	public void setTimeInterval(TimeInterval interval){
		Require.that(interval != null, "A transport arc must have an associated interval");
		
		this.interval = interval;
	}
}
