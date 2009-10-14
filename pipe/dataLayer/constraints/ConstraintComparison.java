package pipe.dataLayer.constraints;

public enum ConstraintComparison {
	LessThan,
	LessThanEqual,
	GreaterThan,
	GreatherThanEqual;
	
	public static ConstraintComparison invertOperator(
			ConstraintComparison operator) {
		if(operator.equals(ConstraintComparison.GreaterThan))
			return ConstraintComparison.LessThan;
		else if(operator.equals(ConstraintComparison.GreatherThanEqual))
			return ConstraintComparison.LessThanEqual;
		else if(operator.equals(ConstraintComparison.LessThan))
			return ConstraintComparison.GreaterThan;
		else
			return ConstraintComparison.GreatherThanEqual;
	}
	
	public static ConstraintComparison getOperatorForConstraint(String leftDelimiter, String rightDelimiter) {
		if(leftDelimiter.equals("[") && rightDelimiter.equals("]"))
			return ConstraintComparison.LessThanEqual;
		else 
			return ConstraintComparison.LessThan;
	}
}
