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

}
