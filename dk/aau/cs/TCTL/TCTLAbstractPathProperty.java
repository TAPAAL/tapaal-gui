package dk.aau.cs.TCTL;

public abstract class TCTLAbstractPathProperty extends TCTLAbstractProperty {
	
	@Override
	public abstract TCTLAbstractPathProperty copy();
	
	@Override
	public abstract TCTLAbstractPathProperty replace(TCTLAbstractProperty object1, TCTLAbstractProperty object2);
	
	//public abstract void accept(ICSLVisitor visitor) throws ModelCheckingException;
}
