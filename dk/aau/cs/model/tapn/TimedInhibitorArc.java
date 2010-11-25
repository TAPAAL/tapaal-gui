package dk.aau.cs.model.tapn;

import java.math.BigDecimal;

public class TimedInhibitorArc extends TimedInputArc {
	public TimedInhibitorArc(TimedPlace source, TimedTransition destination, TimeInterval interval) {
		super(source, destination, interval);
	}	
	
	@Override
	public boolean isEnabled() {
		return !super.isEnabled();
	}
	
	@Override
	public boolean isEnabledBy(BigDecimal token) {
		return !super.isEnabledBy(token);
	}
	
	@Override
	public void delete() {
		model().remove(this);
	}
}
