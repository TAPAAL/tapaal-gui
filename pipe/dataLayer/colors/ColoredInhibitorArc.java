package pipe.dataLayer.colors;

import pipe.dataLayer.NormalArc;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TAPNInhibitorArc;
import pipe.gui.undo.ColoredInhibArcColorGuardEdit;
import pipe.gui.undo.ColoredInhibitorArcTimeGuardEdit;
import pipe.gui.undo.UndoableEdit;


public class ColoredInhibitorArc extends TAPNInhibitorArc {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6750834435940093632L;
	private ColorSet colorGuard;
	private ColoredInterval timeGuard;

	public ColoredInhibitorArc(NormalArc arc)
	{
		super(arc);
		initialize();
	}
	public ColoredInhibitorArc(NormalArc arc, String guard)
	{
		super(arc, guard);
		initialize();
	}

	public ColoredInhibitorArc(PlaceTransitionObject source) {
		super(source);
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
		
		return !(colorGuard.contains(value) && timeGuard.contains(token));
	}

	@Override
	public void updateWeightLabel(){ 

		String guard = "age \u2208 " + timeGuard;

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
		return timeGuard;
	}

	public UndoableEdit setTimeGuard(ColoredInterval newTimeGuard) {
		ColoredInterval old = this.timeGuard;
		this.timeGuard = newTimeGuard;
		
		updateWeightLabel();
		
		return new ColoredInhibitorArcTimeGuardEdit(this, old, newTimeGuard);
	}
	public ColorSet getColorGuard() {
		return colorGuard;
	}



}
