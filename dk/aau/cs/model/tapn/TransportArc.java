package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class TransportArc {
	private TimedPlace source;
	private TimedTransition transition;
	private TimedPlace destination;

	private TimeInterval interval;
	private int group;

	public TransportArc(TimedPlace source, TimedTransition transition, TimedPlace destination,
			TimeInterval interval, int group){
		Require.that(source != null, "The source place cannot be null");
		Require.that(transition != null, "The associated transition cannot be null");
		Require.that(destination != null, "The destination place cannot be null");
		Require.that(group > 0, "group must be larger than 0");
		
		this.source = source;
		this.transition = transition;
		this.destination = destination;
		this.group = group;
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
	
	public int group(){
		return group;
	}
}
