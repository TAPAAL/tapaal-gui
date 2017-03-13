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
	public boolean equals(Object o) {
		if (o instanceof TCTLStatePlaceHolder) {
			return true;
		}
		return false;
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty) object2;
			obj2.setParent(parent);
			return obj2;
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
