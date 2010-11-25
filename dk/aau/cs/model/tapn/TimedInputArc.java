package dk.aau.cs.model.tapn;

import java.math.BigDecimal;

import dk.aau.cs.util.Require;

public class TimedInputArc extends TAPNElement {
	private TimedPlace source;
	private TimeInterval interval;
	private TimedTransition destination;
	
	public TimedInputArc(TimedPlace source, TimedTransition destination, TimeInterval interval){
		Require.that(source != null, "A timed input arc cannot have a null source place");
		Require.that(destination != null, "A timed input arc cannot have a null destination transition");
						
		this.source = source;
		this.destination = destination;
		setTimeInterval(interval);
	}
	
	public TimedPlace source(){
		return source;
	}
	
	public TimeInterval interval(){
		return interval;
	}
	
	public void setTimeInterval(TimeInterval interval){
		Require.that(interval != null, "A timed input arc cannot have a null interval");
		
		this.interval = interval;
	}
	
	public TimedTransition destination(){
		return destination;
	}

	public boolean isEnabled() {
		return source.hasTokenSatisfyingInterval(interval);
		
	}

	public boolean isEnabledBy(BigDecimal token) {
		return interval.isIncluded(token);
	}

	public void delete() {
		model().remove(this);		
	}
}
