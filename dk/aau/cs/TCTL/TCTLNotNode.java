package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLNotNode extends TCTLAbstractStateProperty {
	
	private TCTLAbstractStateProperty property;

	public void setProperty(TCTLAbstractStateProperty property) {
		this.property = property;
	}

	public TCTLAbstractStateProperty getProperty() {
		return property;
	}
	
	public TCTLNotNode() {
		this.property = new TCTLStatePlaceHolder();
	}

	public TCTLNotNode(TCTLAbstractStateProperty property) {
		this.property = property;
	}
	
	@Override
	public boolean isSimpleProperty() {
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TCTLNotNode) {
			TCTLNotNode node = (TCTLNotNode)o;
			return property.equals(node.property);
		}
		return false;
	}
	
	@Override
	public String toString() {
		String s = "(" + property.toString() + ")";
		return "!" + s;
	}
	
	@Override
	public StringPosition[] getChildren() {
		int start = property.isSimpleProperty() ? 0 : 1;
		start = start + 1;
		int end = start + property.toString().length();
		StringPosition position = new StringPosition(start, end, property);
		
		StringPosition[] children = { position };
		return children;
	}
	
	@Override
	public TCTLAbstractStateProperty copy() {
		return new TCTLNotNode(property.copy());
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1, TCTLAbstractProperty object2) {
		if (this.equals(object1) && object2 instanceof TCTLAbstractStateProperty) {
			return (TCTLAbstractStateProperty)object2;
		} else {
			property = property.replace(object1, object2);
			return this;
		}
	}

	@Override
	public void accept(ITCTLVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean containsAtomicPropWithSpecificPlace(String placeName) {
		return property.containsAtomicPropWithSpecificPlace(placeName);
	}

	@Override
	public boolean containsPlaceHolder() {
		return property.containsPlaceHolder();
	}

}
