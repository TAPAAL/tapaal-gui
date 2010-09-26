package dk.aau.cs.TCTL;

// atomic propositions are of the form: <place_name> <operator> n,
// where <operator> = {<, <=, =, >=, >}
public class TCTLAtomicPropositionNode extends TCTLAbstractStateProperty {
	
	// TODO: make this more object oriented, i.e. use something like TAPNPlace instead of String for places.
	private String place;
	private String op;
	private int n;
	
	public String getPlace() {
		return place;
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
		this.place = place;
		this.op = op;
		this.n = n;
	}

	@Override
	public TCTLAbstractStateProperty copy() {
		return new TCTLAtomicPropositionNode(place, op, n);
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
		if (o instanceof TCTLAtomicPropositionNode) {
			TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode)o;
			return place.equals(node.getPlace()) && op.equals(node.getOp()) && (n == node.getN());
		}
		return false;
	}

	@Override
	public String toString() {
		return place + " " + op + " " + n;
	}
	
	@Override
	public void accept(ITCTLVisitor visitor) {
		visitor.visit(this);
		
	}

	@Override
	public boolean containsPlaceHolder() {
		return false;
	}

	@Override
	public boolean containsAtomicPropWithSpecificPlace(String placeName) {
		return place.equals(placeName);
	}

}
