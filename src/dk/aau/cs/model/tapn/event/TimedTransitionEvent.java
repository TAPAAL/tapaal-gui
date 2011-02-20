package dk.aau.cs.model.tapn.event;

import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

public class TimedTransitionEvent {
	private TimedTransition source;
	
	public TimedTransitionEvent(TimedTransition source){
		Require.that(source != null, "source cannot be null");
		this.source = source;
	}
	
	public TimedTransition source(){
		return source;
	}
}
