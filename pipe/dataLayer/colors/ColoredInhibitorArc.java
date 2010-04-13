package pipe.dataLayer.colors;

import pipe.dataLayer.NormalArc;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TAPNInhibitorArc;


public class ColoredInhibitorArc extends TAPNInhibitorArc {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6750834435940093632L;
	private ColorSet colorGuard;

	public ColoredInhibitorArc(NormalArc arc)
	{
		super(arc);
		colorGuard = new ColorSet();
	}
	public ColoredInhibitorArc(NormalArc arc, String guard)
	{
		super(arc, guard);
		colorGuard = new ColorSet();
	}

	public ColoredInhibitorArc(PlaceTransitionObject source) {
		super(source);
	}
	public boolean satisfiesGuard(ColoredToken token) {
		return !(colorGuard.contains(token.getColor()) || satisfiesGuard(token.getAge()));
	}

	public void updateWeightLabel(){ 

		String guard = timeInterval;

		if(colorGuard != null && !colorGuard.isEmpty()){
			guard += "\n" + colorGuard.toString();
		}

		weightLabel.setText(guard);

		this.setWeightLabelPosition();
	}


}
