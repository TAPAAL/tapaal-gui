package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

// atomic propositions are of the form: <place_name> <operator> n,
// where <operator> = {<, <=, =, >=, >}
public class TCTLAtomicPropositionNode extends TCTLAbstractStateProperty {

	// TODO: make this more object oriented, i.e. use something like TAPNPlace
	// instead of String for places.
	private String template;
	private String place;
	private String op;
	private int n;

	public String getPlace() {
		return place;
	}

	public String getTemplate() {
		return template;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public TCTLAtomicPropositionNode(String place, String op, int n) {
		this("", place, op, n);
	}

	public TCTLAtomicPropositionNode(String template, String place, String op,
			int n) {
		this.template = template;
		this.place = place;
		this.op = op;
		this.n = n;

	}

	@Override
	public TCTLAbstractStateProperty copy() {
		return new TCTLAtomicPropositionNode(template, place, op, n);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TCTLAtomicPropositionNode) {
			TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode) o;
			// TODO: Not sure if this is intentional but this is reference
			// equals and not equality
			return this.template.equals(node.template)
					&& this.place.equals(node.getPlace()) && this.op.equals(node.getOp())
					&& this.n == node.getN();
		}
		return false;
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty) object2;
			obj2.setParent(this.parent);
			return obj2;
		} else {
			return this;
		}
	}

	@Override
	public String toString() {
		String value = place + "" + op + "" + n;
		return template == null || template.isEmpty() ? value : template + "."
				+ value;
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);

	}

	@Override
	public boolean containsPlaceHolder() {
		return false;
	}

	@Override
	public boolean containsAtomicPropWithSpecificPlace(String placeName) {
		return place.equals(placeName);
	}

	@Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		return null;
	}

	public void setTemplate(String string) {
		this.template = string;
	}
	
	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
		return template.equals(templateName) && place.equals(placeName);
	}

}
