package pipe.dataLayer.constraints;

public class ConstraintPair {
	public ConstraintPair(String constantName, GuardConstraint gc) {
		this.gc = gc;
		this.constantName = constantName;
	}

	
	public GuardConstraint getConstraint() {
		return gc;
	}
	
	public String getConstantName(){
		return constantName;
	}
	
	private GuardConstraint gc;
	public String constantName;
}
