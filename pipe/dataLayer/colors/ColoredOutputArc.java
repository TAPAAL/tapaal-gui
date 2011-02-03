package pipe.dataLayer.colors;

import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedOutputArcComponent;
import pipe.gui.undo.ColoredOutputArcOutputValueEdit;
import dk.aau.cs.gui.undo.Command;

public class ColoredOutputArc extends TimedOutputArcComponent {
	private IntOrConstant outputValue = new IntOrConstant();
	private boolean displayValues = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = -8410344461976132988L;

	public ColoredOutputArc(double startPositionXInput,
			double startPositionYInput, double endPositionXInput,
			double endPositionYInput, PlaceTransitionObject sourceInput,
			PlaceTransitionObject targetInput, int weightInput, String idInput,
			boolean taggedInput) {
		super(startPositionXInput, startPositionYInput, endPositionXInput,
				endPositionYInput, sourceInput, targetInput, weightInput,
				idInput, taggedInput);
		updateWeightLabel(true);
	}

	public ColoredOutputArc(PlaceTransitionObject newSource) {
		super(newSource);
		updateWeightLabel(true);
	}

	public ColoredOutputArc(TimedOutputArcComponent arc) {
		super(arc);
		updateWeightLabel(true);
	}

	public Command setOutputValue(IntOrConstant newOutputValue) {
		IntOrConstant old = this.outputValue;
		this.outputValue = newOutputValue;

		updateWeightLabel(true);

		return new ColoredOutputArcOutputValueEdit(this, old, newOutputValue);
	}

	public IntOrConstant getOutputValue() {
		if (outputValue == null) {
			outputValue = new IntOrConstant();
		}
		return outputValue;
	}

	public String getOutputString() {
		return "val := " + getOutputValue().toString(displayValues);
	}

	@Override
	public void updateWeightLabel(boolean displayConstantNames) {
		weightLabel.setText(getOutputString());

		this.setWeightLabelPosition();
	}

	public ColoredToken generateOutputToken() {
		return new ColoredToken(getOutputValue());
	}

	public void displayValues(boolean showValues) {
		this.displayValues = showValues;
		updateWeightLabel(true);
	}

	public void updateConstantName(String oldName, String newName) {
		outputValue.updateConstantName(oldName, newName);
		updateWeightLabel(true);
	}
}
