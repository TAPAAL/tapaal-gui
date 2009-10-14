package pipe.dataLayer.constraints;

public class ConstantIntegerConstraint extends GuardConstraint {

	private String leftOperand;
	private int rightOperand;
	
	public ConstantIntegerConstraint(String leftOperand, int rightOperand, ConstraintComparison operator){
		super(operator);
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}
	
	public boolean isSatisfied() {
		int leftValue = store.getConstant(leftOperand).getValue();
		
		if(operator.equals(ConstraintComparison.GreaterThan)){
			return leftValue > rightOperand;
		}else if(operator.equals(ConstraintComparison.GreatherThanEqual)){
			return leftValue >= rightOperand;
		}else if(operator.equals(ConstraintComparison.LessThan)){
			return leftValue < rightOperand;
		}else if(operator.equals(ConstraintComparison.LessThanEqual)){
			return leftValue <= rightOperand;
		}else
			return false;
	}
}
