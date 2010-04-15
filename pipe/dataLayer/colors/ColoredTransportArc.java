package pipe.dataLayer.colors;

import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TransportArc;
import pipe.gui.undo.ColoredTransportArcColorGuardEdit;
import pipe.gui.undo.ColoredTransportArcPreserveEdit;
import pipe.gui.undo.ColoredTransportArcUpdateValueEdit;
import pipe.gui.undo.UndoableEdit;


public class ColoredTransportArc extends TransportArc {

	private ColorSet colorGuard;
	private Preserve preserves = Preserve.AgeAndValue;
	private OutputValue outputValue = new OutputValue();
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
			guard = "age \u2208 " + timeInterval + " : " + getGroup();
			
			if(colorGuard != null && !colorGuard.isEmpty()){
				guard += "\n val \u2208 " + colorGuard.toString();
			}
		} else {
			if(preserves == null){
				preserves = Preserve.AgeAndValue;
			}
			if(preserves.equals(Preserve.Age)){
				guard = "preserve age : " + getGroup() + "\n" + getOutputString();
			}else if(preserves.equals(Preserve.Value)){
				guard = "age := 0 : " + getGroup() + "\n preserve val";
			}else{
				guard = "preserve age : " + getGroup() + "\n preserve val";
			}
		}
		
		weightLabel.setText(guard);
		this.setWeightLabelPosition();
	}

	public String getColorGuardStringWithoutSetNotation() {
		return colorGuard.toStringNoSetNotation();
	}

	public UndoableEdit setColorGuard(ColorSet newColorGuard) {
		ColorSet old = this.colorGuard;
		this.colorGuard = newColorGuard;

		updateWeightLabel();

		return new ColoredTransportArcColorGuardEdit(this, old, newColorGuard);	
	}

	public Preserve getPreservation() {
		return preserves;
	}
	
	public OutputValue getOutputValue(){
		return outputValue;
	}
	
	public UndoableEdit setOutputValue(OutputValue value){
		OutputValue old = this.outputValue;
		this.outputValue = value;
		
		updateWeightLabel();
		
		return new ColoredTransportArcUpdateValueEdit(this, old, value);
	}

	public UndoableEdit setPreservation(Preserve newPreserve) {
		Preserve old = this.preserves;
		this.preserves = newPreserve;
		
		updateWeightLabel();
		
		return new ColoredTransportArcPreserveEdit(this, old, newPreserve);
	}

}
