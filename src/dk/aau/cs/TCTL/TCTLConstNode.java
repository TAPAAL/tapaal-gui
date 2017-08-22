package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLConstNode extends TCTLAbstractStateProperty {

	int constant;

	public TCTLConstNode(int constant) {
		this.constant = constant;
	}

	public int getConstant(){
		return constant;
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
	public TCTLAbstractStateProperty copy() {
		return new TCTLConstNode(constant);
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);

	}

	@Override
	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(
			String templateName, String placeName) {
		return false;
	}
	
	@Override
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(
			String templateName, String transitionName) {
		return false;
	}

	@Override
	public boolean containsPlaceHolder() {
		return false;
	}

	@Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		return null;
	}
	
	@Override
	public String toString() {
		return Integer.toString(constant);
	}

}


