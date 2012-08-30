package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class ConstantWeight extends Weight{
	private Constant constant;

	public ConstantWeight(Constant constant) {
		Require.that(constant != null, "Argument must be a non-null constant");
		this.constant = constant;
	}

	public int value() {
		return constant.value();
	}

	public Weight copy() {
		return new ConstantWeight(constant.copy());
	}

	public Constant constant() {
		return constant;
	}
	
	public void setConstant(Constant newConstant) {
		Require.that(newConstant != null, "Constant cannot be null");

		constant = newConstant;
	}

	public String toString() {
		return constant.name();
	}
	
	public String toString(boolean displayConstantNames) {
		if(displayConstantNames){
			return constant.name() + " x";
		} else if(constant.value() > 1){
			return Integer.toString(constant.value()) + "x";
		} else {
			return "";
		}
	}
	
	public String nameForSaving(boolean writeConstantNames){
		if(writeConstantNames){
			return constant.name();
		} else {
			return Integer.toString(constant.value());
		}
		
	}
}
