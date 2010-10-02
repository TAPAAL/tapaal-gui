package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;


public class TCTLStatePlaceHolder extends TCTLAbstractStateProperty {

	public TCTLStatePlaceHolder() {
	}
	
	@Override
	public TCTLAbstractStateProperty copy() {
		return new TCTLStatePlaceHolder();
	}

	public boolean isSimple() {
		return true;
	}

	@Override
	public String toString() {
		return "<*>";
	}
	
	@Override
	public boolean containsPlaceHolder() {
		return true;
	}
	
	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1, TCTLAbstractProperty object2) {		
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			return (TCTLAbstractStateProperty)object2;
		} else {
			return this;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TCTLStatePlaceHolder) {
			return true;
		}
		return false;
	}
	

	@Override
	public void accept(ITCTLVisitor visitor) {
		visitor.visit(this);
		
	}
	
	@Override
	public boolean containsAtomicPropWithSpecificPlace(String placeName) {
			return false;
	}

}
