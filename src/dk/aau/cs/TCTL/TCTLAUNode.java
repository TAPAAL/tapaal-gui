package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLAUNode extends TCTLAbstractPathProperty {

	private TCTLAbstractStateProperty left;
	private TCTLAbstractStateProperty right;

	public TCTLAbstractStateProperty getLeft() {
		return this.left;
	}

	public void setLeft(TCTLAbstractStateProperty left) {
		this.left = left;
	}
	
	public TCTLAbstractStateProperty getRight() {
		return this.right;
	}

	public void setRight(TCTLAbstractStateProperty right) {
		this.right = right;
	}

	public TCTLAUNode(TCTLAbstractStateProperty left, TCTLAbstractStateProperty right) {
		this.left = left;
		this.right = right;
		this.left.setParent(this);
		this.right.setParent(this);
	}

	public TCTLAUNode() {
		left = new TCTLStatePlaceHolder();
		right = new TCTLStatePlaceHolder();
		left.setParent(this);
		right.setParent(this);
	}

	@Override
	public boolean isSimpleProperty() {
		return false;
	}

	@Override
	public StringPosition[] getChildren() {
		int leftStart = left.isSimpleProperty() ? 0 : 1;
		leftStart  += 3;
		int leftEnd = leftStart + left.toString().length();
		StringPosition leftPos = new StringPosition(leftStart, leftEnd, left);
		
		int rightStart = right.isSimpleProperty() ? 0 : 1;
		rightStart += leftEnd + 3 + + (left.isSimpleProperty() ? 0 : 1);
		int rightEnd = rightStart + right.toString().length();
		StringPosition rightPos = new StringPosition(rightStart, rightEnd, right);

		StringPosition[] children = { leftPos, rightPos };
		return children;
	}

	@Override
	public boolean equals(Object o) {	
		if (o instanceof TCTLAUNode) {
			TCTLAUNode node = (TCTLAUNode) o;
			return this.left.equals(node.getLeft()) 
					&& this.right.equals(node.getRight());
		}
		return false;
	}

	@Override
	public String toString() {
		String leftString = left.isSimpleProperty() ? left.toString() : "("
				+ left.toString() + ")";
		String rightString = right.isSimpleProperty() ? right.toString() : "("
				+ right.toString() + ")";
		
		return "A (" + leftString + " U " + rightString + ")";
	}

	@Override
	public TCTLAbstractPathProperty copy() {
		return new TCTLAUNode(left.copy(), right.copy());
	}

	@Override
	public TCTLAbstractPathProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractPathProperty) {
			return (TCTLAbstractPathProperty) object2;
		} else {
			left = left.replace(object1, object2);
			right = right.replace(object1, object2);
			return this;
		}		
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);
	}

	@Override
	public boolean containsPlaceHolder() {
		return left.containsPlaceHolder() 
				|| right.containsPlaceHolder();
	}
	
	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
		return left.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName)
				|| right.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName);
	}
	
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
		return right.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName) ||
				left.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName);
	}

	@Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		TCTLAbstractProperty result = left.findFirstPlaceHolder();
		if (result == null){
			return right.findFirstPlaceHolder();
		}
		return result;
	}

}
