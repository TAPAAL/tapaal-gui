package dk.aau.cs.model.tapn.event;

import dk.aau.cs.model.tapn.TimedPlace;

public class TimedPlaceEvent {
	private final TimedPlace source;
	
	public TimedPlaceEvent(TimedPlace source){
		this.source = source;
	}
	
	public TimedPlace source(){
		return source;
	}
}
