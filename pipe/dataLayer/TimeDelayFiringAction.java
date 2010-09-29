package pipe.dataLayer;

import java.math.BigDecimal;

public class TimeDelayFiringAction implements FiringAction{

	BigDecimal timedelay;
	
	public TimeDelayFiringAction(BigDecimal delay) {
		this.timedelay = delay;
	}

	public BigDecimal getDealy() {
		return timedelay;
	}
	
	@Override
	public String toString() {
		return "Delay: " + timedelay;
	}
}
