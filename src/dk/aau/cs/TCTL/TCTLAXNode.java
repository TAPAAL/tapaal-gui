package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLAXNode extends TCTLAbstractPathProperty {

	private TCTLAbstractStateProperty property;

	public void setProperty(TCTLAbstractStateProperty property) {
		this.property = property;
		this.property.setParent(this);
	}

	public TCTLAbstractStateProperty getProperty() {
		return property;
	}

	public TCTLAXNode(TCTLAbstractStateProperty property) {
		this.property = property;
		this.property.setParent(this);
	}

	public TCTLAXNode() {
		property = new TCTLStatePlaceHolder();
		property.setParent(this);
	}

	@Override
	public boolean isSimpleProperty() {
		return false;
	}

	@Override
	public StringPosition[] getChildren() {
		int start = property.isSimpleProperty() ? 0 : 1;
		start = start + 3;
		int end = start + property.toString().length();
		StringPosition position = new StringPosition(start, end, property);

		StringPosition[] children = { position };
		return children;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TCTLAXNode) {
			TCTLAXNode node = (TCTLAXNode) o;
			return property.equals(node.getProperty());
		}
		return false;
	}

	@Override
	public String toString() {
		String s = property.isSimpleProperty() ? property.toString() : "("
				+ property.toString() + ")";
		return "AX " + s;
	}

	@Override
	public TCTLAbstractPathProperty copy() {
		return new TCTLAXNode(property.copy());
	}

	@Override
	public TCTLAbstractPathProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractPathProperty) {
			return (TCTLAbstractPathProperty) object2;
		} else {
			property = property.replace(object1, object2);
			return this;
		}
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);
	}

	@Override
	public boolean containsPlaceHolder() {
		return property.containsPlaceHolder();
	}
	
	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
		return property.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName);
	}
	
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
		return property.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName);
	}

	@Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		return property.findFirstPlaceHolder();
	}

}
