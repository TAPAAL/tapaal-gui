package dk.aau.cs.model.tapn.event;

import dk.aau.cs.model.tapn.TimedPlaceInterface;

public class TimedPlaceEvent {
	private TimedPlaceInterface source;
	
	public TimedPlaceEvent(TimedPlaceInterface source){
		this.source = source;
	}
	
	public TimedPlaceInterface source(){
		return source;
	}
}
