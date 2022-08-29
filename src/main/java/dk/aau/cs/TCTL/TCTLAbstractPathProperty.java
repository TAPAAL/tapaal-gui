package dk.aau.cs.TCTL;

public abstract class TCTLAbstractPathProperty extends TCTLAbstractProperty {

	protected TCTLAbstractProperty parent;
	
	public void setParent(TCTLAbstractProperty parent) {
		this.parent = parent;
	}
	
	@Override
	public abstract TCTLAbstractPathProperty copy();

	@Override
	public abstract TCTLAbstractPathProperty replace(
			TCTLAbstractProperty object1, TCTLAbstractProperty object2);

}
