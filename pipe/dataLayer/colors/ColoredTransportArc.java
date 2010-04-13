package pipe.dataLayer.colors;

import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TransportArc;


public class ColoredTransportArc extends TransportArc {

	private ColorSet colorGuard;
	private Preserve preserves = Preserve.AgeAndValue;
	private int outputValue;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8952207202911264613L;

	public ColoredTransportArc(PlaceTransitionObject newSource, int groupNr,
			boolean isInPreSet) {
		super(newSource, groupNr, isInPreSet);
		colorGuard = new ColorSet();
	}

	public ColoredTransportArc(TimedArc timedArc, int group, boolean isInPreset){
		super(timedArc, group, isInPreset);
		colorGuard = new ColorSet();
	}

	public boolean satisfiesGuard(ColoredToken token) {
		return colorGuard.contains(token.getColor()) && satisfiesGuard(token.getAge());
	}
	
	public String getOutputString(){
		return "v := " + outputValue;
	}

	public void updateWeightLabel(){ 

		String guard = null;
		if (isInPreSet()){
			guard = timeInterval + " : " + getGroup();
			
			if(colorGuard != null && !colorGuard.isEmpty()){
				guard += "\n" + colorGuard.toString();
			}
		} else {
			if(preserves == null){
				preserves = Preserve.AgeAndValue;
			}
			if(preserves.equals(Preserve.Age)){
				guard = "(age) : " + getGroup() + "\n" + getOutputString();
			}else if(preserves.equals(Preserve.Value)){
				guard = "(val) : " + getGroup();
			}else{
				guard = "(age,val) : " + getGroup();
			}
		}
		
		weightLabel.setText(guard);
		this.setWeightLabelPosition();
	}

}
