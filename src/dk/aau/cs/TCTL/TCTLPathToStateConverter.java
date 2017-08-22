package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLPathToStateConverter extends TCTLAbstractStateProperty{

	private TCTLAbstractPathProperty property;
	
	public void setProperty(TCTLAbstractPathProperty property) {
		this.property = property;
		this.property.setParent(this);
	}

	public TCTLAbstractPathProperty getProperty() {
		return property;
	}

	public TCTLPathToStateConverter(TCTLAbstractPathProperty property) {
		this.property = property;
		this.property.setParent(this);
	}

	public TCTLPathToStateConverter() {
		property = new TCTLPathPlaceHolder();
		property.setParent(this);
	}

	@Override
	public boolean isSimpleProperty() {
		return property.isSimpleProperty();
	}

	@Override
	public StringPosition[] getChildren() {
		StringPosition position = new StringPosition(0, property.toString().length(), property);
		StringPosition[] children = { position };
		return children;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TCTLEFNode) {
			TCTLEFNode node = (TCTLEFNode) o;
			return property.equals(node.getProperty());
		}
		return false;
	}

	@Override
	public String toString() {
		return property.toString();
	}

	@Override
	public TCTLAbstractStateProperty copy() {
		return new TCTLPathToStateConverter(property.copy());
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			return (TCTLAbstractStateProperty) object2;
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
