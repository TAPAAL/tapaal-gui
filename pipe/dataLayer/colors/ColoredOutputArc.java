package pipe.dataLayer.colors;

import pipe.dataLayer.NormalArc;
import pipe.dataLayer.PlaceTransitionObject;

public class ColoredOutputArc extends NormalArc {

	private int outputValue;
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
				endPositionYInput, sourceInput, targetInput, weightInput, idInput,
				taggedInput);
		updateWeightLabel();
	}

	public ColoredOutputArc(PlaceTransitionObject newSource) {
		super(newSource);
		updateWeightLabel();
	}


	public ColoredOutputArc(NormalArc arc) {
		super(arc);
		updateWeightLabel();
	}

	public void setOutputValue(int outputValue) {
		this.outputValue = outputValue;
	}

	public int getOutputValue() {
		return outputValue;
	}
	
	public String getOutputString(){
		return "v := " + outputValue;
	}
	
	public void updateWeightLabel(){ 		
		weightLabel.setText(getOutputString());
		
		this.setWeightLabelPosition();
	}
}
