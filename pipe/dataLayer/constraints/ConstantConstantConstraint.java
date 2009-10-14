package pipe.dataLayer.constraints;

public class ConstantConstantConstraint extends GuardConstraint {
	private String leftOperand;
	private String rightOperand;
	
	public ConstantConstantConstraint(String leftOperand, String rightOperand, ConstraintComparison operator){
		super(operator);
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}
	
	public boolean isSatisfied() {
		int leftValue = store.getConstant(leftOperand).getValue();
		int rightValue = store.getConstant(rightOperand).getValue();
		
		if(operator.equals(ConstraintComparison.GreaterThan)){
			return leftValue > rightValue;
		}else if(operator.equals(ConstraintComparison.GreatherThanEqual)){
			return leftValue >= rightValue;
		}else if(operator.equals(ConstraintComparison.LessThan)){
			return leftValue < rightValue;
		}else if(operator.equals(ConstraintComparison.LessThanEqual)){
			return leftValue <= rightValue;
		}else
			return false;
	}
}
