package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class ConstantBound implements Bound {
	private Constant constant;
	
	public ConstantBound(Constant constant){
		Require.that(constant != null, "Argument must be a non-null constant");
		this.constant = constant;
	}
		
	public int value() {
		return constant.value();
	}
	
	public String name() {
		return constant.name();
	}
	
	public ConstantBound copy()
	{
		return new ConstantBound(constant.copy());
	}

	public void setConstant(Constant newConstant) {
		Require.that(newConstant != null, "Constant cannot be null");
		
		this.constant = newConstant;	
	}

}
