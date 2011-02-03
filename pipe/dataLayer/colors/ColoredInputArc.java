package pipe.dataLayer.colors;

import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedOutputArcComponent;
import pipe.gui.undo.ColoredInputArcColorGuardEdit;
import pipe.gui.undo.ColoredInputArcTimeGuardEdit;
import dk.aau.cs.gui.undo.Command;

public class ColoredInputArc extends TimedInputArcComponent {

	private ColorSet colorGuard;
	private ColoredInterval timeGuard;
	/**
	 * 
	 */
	private static final long serialVersionUID = 4239344022098891387L;

	public ColoredInputArc(PlaceTransitionObject source) {
		super(source);

		initialize();
	}

	public ColoredInputArc(TimedOutputArcComponent arc) {
		super(arc);

		initialize();
	}

	public ColoredInputArc(TimedOutputArcComponent arc, String guard) {
		super(arc, guard);

		initialize();
	}

	private void initialize() {
		colorGuard = new ColorSet();
		timeGuard = new ColoredInterval();

		updateWeightLabel(true);
	}

	public boolean satisfiesGuard(ColoredToken token) {
		IntOrConstant val = token.getColor();
		int value = val.getValue();

		return colorGuard.contains(value) && timeGuard.contains(token);
	}

	@Override
	public void updateWeightLabel(boolean displayConstantNames) {

		String guard = "age \u2208 " + timeGuard;

		if (colorGuard != null && !colorGuard.isEmpty()) {
			guard += "\n val \u2208 " + colorGuard.toString();
		}

		weightLabel.setText(guard);

		this.setWeightLabelPosition();
	}

	public String getColorGuardStringWithoutSetNotation() {
		return colorGuard.toStringNoSetNotation();
	}

	public ColorSet getColorGuard() {
		return colorGuard;
	}

	public Command setColorGuard(ColorSet newColorGuard) {
		ColorSet old = this.colorGuard;
		this.colorGuard = newColorGuard;

		updateWeightLabel(true);

		return new ColoredInputArcColorGuardEdit(this, old, newColorGuard);
	}

	public ColoredInterval getTimeGuard() {
		return timeGuard;
	}

	public Command setTimeGuard(ColoredInterval newTimeGuard) {
		ColoredInterval old = this.timeGuard;
		this.timeGuard = newTimeGuard;

		updateWeightLabel(true);

		return new ColoredInputArcTimeGuardEdit(this, old, newTimeGuard);
	}

	public void displayValues(boolean showValues) {
		timeGuard.displayValues(showValues);
		colorGuard.displayValues(showValues);
		updateWeightLabel(true);
	}

	public void updateConstantName(String oldName, String newName) {
		timeGuard.updateConstantName(oldName, newName);
		colorGuard.updateConstantName(oldName, newName);
		updateWeightLabel(true);
	}
}
