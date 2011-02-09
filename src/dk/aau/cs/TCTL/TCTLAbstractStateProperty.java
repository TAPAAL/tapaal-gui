package dk.aau.cs.TCTL;

public abstract class TCTLAbstractStateProperty extends TCTLAbstractProperty {

	protected TCTLAbstractProperty parent;

	public TCTLAbstractProperty getParent() {
		return parent;
	}

	public void setParent(TCTLAbstractProperty parent) {
		this.parent = parent;
	}

	@Override
	public abstract TCTLAbstractStateProperty replace(
			TCTLAbstractProperty object1, TCTLAbstractProperty object2);

	@Override
	public abstract TCTLAbstractStateProperty copy();

	// public abstract void accept(ICSLVisitor visitor) throws
	// ModelCheckingException;

	// protected abstract void setCompositionality(boolean
	// withinSteadyStateOperator);

	// public void setCompositionality() {
	// setCompositionality(false);
	// }
	//	
	// public boolean isCompositional() {
	// return isCompositional;
	// }
}
