package pipe.dataLayer.colors;

import pipe.dataLayer.NormalArc;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedArc;

public class ColoredInputArc extends TimedArc {

	public ColorSet integerGuard;
	/**
	 * 
	 */
	private static final long serialVersionUID = 4239344022098891387L;

	public ColoredInputArc(PlaceTransitionObject source) {
		super(source);
		
		integerGuard = new ColorSet();
	}
	
	public ColoredInputArc(NormalArc arc) {
		super(arc);
		
		integerGuard = new ColorSet();
	}

	public ColoredInputArc(NormalArc arc, String guard) {
		super(arc,guard);
		
		integerGuard = new ColorSet();
	}
		
	public boolean satisfiesGuard(ColoredToken token) {
		return integerGuard.contains(token.getColor()) && satisfiesGuard(token.getAge());
	}
	
	public void updateWeightLabel(){ 
		
		String guard = timeInterval;
		
		if(integerGuard != null && !integerGuard.isEmpty()){
			guard += "\n" + integerGuard.toString();
		}
		
		weightLabel.setText(guard);
		
		this.setWeightLabelPosition();
	}
}
