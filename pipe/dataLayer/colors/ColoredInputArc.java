package pipe.dataLayer.colors;

import pipe.dataLayer.NormalArc;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedArc;
import pipe.gui.CreateGui;
import pipe.gui.undo.ColoredInputArcColorGuardEdit;
import pipe.gui.undo.UndoableEdit;

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
		IntOrConstant val = token.getColor();
		int value = 0;
		if(val.isUsingConstant()){
			value = CreateGui.getModel().getConstantValue(val.getConstantName());
		}else{
			value = val.getIntegerValue();
		}
		
		return integerGuard.contains(value) && satisfiesGuard(token.getAge());
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
}
