package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLStateToPathConverter extends TCTLAbstractPathProperty{

	private TCTLAbstractStateProperty property;
	
	public void setProperty(TCTLAbstractStateProperty property) {
		this.property = property;
		this.property.setParent(this);
	}

	public TCTLAbstractStateProperty getProperty() {
		return property;
	}

	public TCTLStateToPathConverter(TCTLAbstractStateProperty property) {
		this.property = property;
		this.property.setParent(this);
	}

	public TCTLStateToPathConverter() {
		property = new TCTLStatePlaceHolder();
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
	public TCTLAbstractPathProperty copy() {
		return new TCTLStateToPathConverter(property.copy());
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
	
	@Override
	public StringPosition objectAt(int index) {
		StringPosition[] children = getChildren();
		
		for (int i = 0; i < children.length; i++) {
			StringPosition child = children[i];
			if (child.getStart() <= index && index <= child.getEnd()) {
				children = child.getObject().getChildren();
				break;
			}
		}
		
		for (int i = 0; i < children.length; i++) {
			StringPosition child = children[i];
			if (child.getStart() <= index && index <= child.getEnd()) {
				int start = child.getStart();
				return child.getObject().objectAt(index - start).addOffset(
						start);
			}
		}
		return new StringPosition(0, toString().length(), this);
	}
}
