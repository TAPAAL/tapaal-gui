package pipe.dataLayer.colors;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TransportArc;
import pipe.gui.undo.ColoredTransportArcColorGuardEdit;
import pipe.gui.undo.ColoredTransportArcPreserveEdit;
import pipe.gui.undo.ColoredTransportArcTimeGuardEdit;
import pipe.gui.undo.ColoredTransportArcUpdateValueEdit;


public class ColoredTransportArc extends TransportArc {

	private ColorSet colorGuard;
	private Preserve preserves = Preserve.AgeAndValue;
	private IntOrConstant outputValue = new IntOrConstant();
	private ColoredInterval timeGuard;
	private boolean displayValues = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8952207202911264613L;

	public ColoredTransportArc(PlaceTransitionObject newSource, int groupNr,
			boolean isInPreSet) {
		super(newSource, groupNr, isInPreSet);
		initialize();
	}

	public ColoredTransportArc(TimedArc timedArc, int group, boolean isInPreset){
		super(timedArc, group, isInPreset);
		initialize();
	}

	private void initialize() {
		colorGuard = new ColorSet();
		timeGuard = new ColoredInterval();

		updateWeightLabel();
	}

	public boolean satisfiesGuard(ColoredToken token) {
		IntOrConstant val = token.getColor();
		int value = val.getValue();

		return colorGuard.contains(value) && timeGuard.contains(token);
	}

	public boolean satisfiesTargetInvariant(ColoredToken token) {
		ColoredTimedPlace place = getTargetPlace();
		ColoredToken newToken = generateOutputToken(token);
		return place.satisfiesInvariant(newToken);
	}

	private ColoredTimedPlace getTargetPlace() {
		if(getTarget() instanceof ColoredTimedPlace)
			return (ColoredTimedPlace)getTarget();
		else{
			return (ColoredTimedPlace)getConnectedTo().getTarget();
		}
	}

	public String getOutputString(){
		return "val := " + outputValue.toString(displayValues);
	}

	@Override
	public void updateWeightLabel(){ 
		String guard = null;
		if (isInPreSet()){
			guard = "age \u2208 " + timeGuard + " : " + getGroup();

			if(colorGuard != null && !colorGuard.isEmpty()){
				guard += "\n val \u2208 " + colorGuard.toString();
			}
		} else {
			guard = getPreservationString();
		}

		weightLabel.setText(guard);
		this.setWeightLabelPosition();
	}

	public String getPreservationString() {
		String guard;
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
		return guard;
	}

	public String getColorGuardStringWithoutSetNotation() {
		return colorGuard.toStringNoSetNotation();
	}

	public Command setColorGuard(ColorSet newColorGuard) {
		ColorSet old = this.colorGuard;
		this.colorGuard = newColorGuard;

		updateWeightLabel();

		return new ColoredTransportArcColorGuardEdit(this, old, newColorGuard);	
	}

	public Preserve getPreservation() {
		return preserves;
	}

	public IntOrConstant getOutputValue(){
		return outputValue;
	}

	public Command setOutputValue(IntOrConstant value){
		IntOrConstant old = this.outputValue;
		this.outputValue = value;

		updateWeightLabel();

		return new ColoredTransportArcUpdateValueEdit(this, old, value);
	}

	public Command setPreservation(Preserve newPreserve) {
		Preserve old = this.preserves;
		this.preserves = newPreserve;

		updateWeightLabel();

		return new ColoredTransportArcPreserveEdit(this, old, newPreserve);
	}

	public ColoredInterval getTimeGuard() {
		return timeGuard;
	}

	public Command setTimeGuard(ColoredInterval newTimeGuard) {
		ColoredInterval old = this.timeGuard;
		this.timeGuard = newTimeGuard;

		updateWeightLabel();

		return new ColoredTransportArcTimeGuardEdit(this, old, newTimeGuard);
	}

	public ColorSet getColorGuard() {
		return colorGuard;
	}

	public ColoredToken generateOutputToken(ColoredToken consumedToken) {
		if(preserves.equals(Preserve.Age)){
			return new ColoredToken(consumedToken.getAge(),getOutputValue());
		}else if(preserves.equals(Preserve.Value)){
			return new ColoredToken(consumedToken.getColor());
		}else{
			return new ColoredToken(consumedToken.getAge(), consumedToken.getColor());
		}
	}

	public void displayValues(boolean showValues) {
		this.displayValues = showValues;		
		this.timeGuard.displayValues(showValues);
		this.colorGuard.displayValues(showValues);
		updateWeightLabel();
	}

	public void updateConstantName(String oldName, String newName) {
		colorGuard.updateConstantName(oldName, newName);
		timeGuard.updateConstantName(oldName, newName);
		outputValue.updateConstantName(oldName, newName);
		updateWeightLabel();
	}

}
