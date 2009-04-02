package pipe.dataLayer;

public class TimeDelayFiringAction implements FiringAction{

	float timedelay;
	
	public TimeDelayFiringAction(float delay) {
		this.timedelay = delay;
	}

	public Float getDealy() {
		return timedelay;
	}
	
}
