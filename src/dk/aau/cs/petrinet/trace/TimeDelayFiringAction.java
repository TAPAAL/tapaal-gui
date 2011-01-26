package dk.aau.cs.petrinet.trace;

import java.math.BigDecimal;

public class TimeDelayFiringAction implements TAPNFiringAction {
	private BigDecimal delay;
	
	public TimeDelayFiringAction(BigDecimal delay){
		this.delay = delay;
	}
	
	public BigDecimal delay(){
		return delay;
	}
}
