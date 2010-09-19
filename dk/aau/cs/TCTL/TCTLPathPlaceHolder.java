package dk.aau.cs.TCTL;



public class TCTLPathPlaceHolder extends TCTLAbstractPathProperty {
	
	public TCTLPathPlaceHolder() {
	}
	
	public TCTLAbstractPathProperty copy() {
		return new TCTLPathPlaceHolder();
	}

	public boolean isSimple() {
		return true;
	}

	public String toString() {
		return "<*>";
	}
	
	public boolean containsPlaceHolder() {
		return true;
	}
	
	public TCTLAbstractPathProperty replace(TCTLAbstractProperty object1, TCTLAbstractProperty object2) {		
		if (this == object1 && object2 instanceof TCTLAbstractPathProperty) {
			return (TCTLAbstractPathProperty)object2;
		} else {
			return this;
		}
	}

	public boolean equals(Object o) {
		if (o instanceof TCTLPathPlaceHolder) {
			return true;
		}
		return false;
	}
	
	@Override
	public void accept(ITCTLVisitor visitor) {
		visitor.visit(this);
	}
}
