package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLPathPlaceHolder extends TCTLAbstractPathProperty {

	public TCTLPathPlaceHolder() {
	}

	@Override
	public TCTLAbstractPathProperty copy() {
		return new TCTLPathPlaceHolder();
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
	public boolean equals(Object o) {
		if (o instanceof TCTLPathPlaceHolder) {
			return true;
		}
		return false;
	}

	@Override
	public TCTLAbstractPathProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractPathProperty) {
			return (TCTLAbstractPathProperty) object2;
		} else {
			return this;
		}
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);
	}

	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
		return false;
	}
	
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
		return false;
	}

	@Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		return this;
	}
}
