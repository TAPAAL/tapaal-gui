package pipe.dataLayer.colors;

import pipe.dataLayer.NormalArc;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedArc;
import pipe.gui.CreateGui;
import pipe.gui.undo.ColoredInputArcColorGuardEdit;
import pipe.gui.undo.UndoableEdit;

public class ColoredInputArc extends TimedArc {

	private ColorSet integerGuard;
	private ColoredInterval timeGuard;
	/**
	 * 
	 */
	private static final long serialVersionUID = 4239344022098891387L;

	public ColoredInputArc(PlaceTransitionObject source) {
		super(source);
		
		initialize();
	}

	public ColoredInputArc(NormalArc arc) {
		super(arc);
		
		initialize();
	}

	public ColoredInputArc(NormalArc arc, String guard) {
		super(arc,guard);
		
		initialize();
	}
	
	private void initialize() {
		integerGuard = new ColorSet();
		timeGuard = new ColoredInterval();
		
		updateWeightLabel();
	}
		
	public boolean satisfiesGuard(ColoredToken token) {
		IntOrConstant val = token.getColor();
		int value = 0;
		if(val.isUsingConstant()){
			value = CreateGui.getModel().getConstantValue(val.getConstantName());
		}else{
			value = val.getIntegerValue();
		}
		
		return integerGuard.contains(value) && timeGuard.contains(token);
	}
	
	public void updateWeightLabel(){ 
		
		String guard = "age \u2208 " + timeInterval;
		
		if(integerGuard != null && !integerGuard.isEmpty()){
			guard += "\n val \u2208 " + integerGuard.toString();
		}
		
		weightLabel.setText(guard);
		
		this.setWeightLabelPosition();
	}
	
	public String getColorGuardStringWithoutSetNotation() {
		return integerGuard.toStringNoSetNotation();
	}

	public UndoableEdit setColorGuard(ColorSet newColorGuard) {
		ColorSet old = this.integerGuard;
		this.integerGuard = newColorGuard;

		updateWeightLabel();

		return new ColoredInputArcColorGuardEdit(this, old, newColorGuard);	
	}

	public ColoredInterval getTimeGuard() {
		return timeGuard;
	}
}
