package pipe.dataLayer.constraints;

import pipe.dataLayer.ConstantStore;

public abstract class GuardConstraint {
	protected ConstantStore store;
	protected ConstraintComparison operator;
	
	protected GuardConstraint(ConstraintComparison operator){
		this.operator = operator;
	}
	
	public abstract boolean isSatisfied();
	
	public void setConstantStore(ConstantStore store){
		this.store = store;
	}
	
}
