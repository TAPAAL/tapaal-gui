package pipe.dataLayer.colors;

import pipe.dataLayer.NormalArc;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TAPNInhibitorArc;
import pipe.gui.CreateGui;
import pipe.gui.undo.ColoredInhibArcColorGuardEdit;
import pipe.gui.undo.UndoableEdit;


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
		colorGuard = new ColorSet();
	}
	public boolean satisfiesGuard(ColoredToken token) {
		IntOrConstant val = token.getColor();
		int value = 0;
		if(val.isUsingConstant()){
			value = CreateGui.getModel().getConstantValue(val.getConstantName());
		}else{
			value = val.getIntegerValue();
		}
		
		return !(colorGuard.contains(value) && satisfiesGuard(token.getAge()));
	}

	public void updateWeightLabel(){ 

		String guard = "age \u2208 " + timeInterval;

		if(colorGuard != null && !colorGuard.isEmpty()){
			guard += "\n val \u2208 " + colorGuard.toString();
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

		return new ColoredInhibArcColorGuardEdit(this, old, newColorGuard);	
	}
	public ColoredInterval getTimeGuard() {
		// TODO Auto-generated method stub
		return null;
	}
	public UndoableEdit setTimeGuard(ColoredInterval timeGuard) {
		// TODO Auto-generated method stub
		return null;
	}



}
