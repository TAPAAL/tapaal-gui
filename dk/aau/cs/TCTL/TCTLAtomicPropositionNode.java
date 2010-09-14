package dk.aau.cs.TCTL;

// atomic propositions are of the form: <place_name> <operator> n,
// where <operator> = {<, <=, =, >=, >}
public class TCTLAtomicPropositionNode extends TCTLAbstractStateProperty {
	
	public enum ComparisonOperator { LESS_THAN, LEQ, EQUAL, GEQ, GREATER_THAN }
	
	private String place;
	private ComparisonOperator op;
	private int n;
	
	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public ComparisonOperator getOp() {
		return op;
	}

	public void setOp(ComparisonOperator op) {
		this.op = op;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public TCTLAtomicPropositionNode(String place, ComparisonOperator op, int n) {
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
		String s = place;
		
		switch (op) {
		case LESS_THAN:
			s = s + " < ";
			break;
		case LEQ:
			s = s + " <= ";
			break;
		case EQUAL:
			s = s + " = ";
			break;
		case GEQ:
			s = s + " >= ";
			break;
		case GREATER_THAN:
			s = s + " > ";
		}
		
		s = s + n;
		return s;
	}

}
