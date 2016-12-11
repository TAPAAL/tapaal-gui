package dk.aau.cs.verification;

public class ReductionStats {
	private int removedTrantitions;
	private int removedPlaces;
	private int ruleA;
	private int ruleB;
	private int ruleC;
	private int ruleD;
	private int ruleE;
	
	public ReductionStats(int removedTransitions, int removedPlaces, int ruleA, int ruleB, int ruleC, int ruleD, int ruleE) {
		this.removedTrantitions = removedTransitions;
		this.removedPlaces = removedPlaces;
		this.ruleA = ruleA;
		this.ruleB = ruleB;
		this.ruleC = ruleC;
		this.ruleD = ruleD;
		this.ruleE = ruleE;
	}
	
	public int getRemovedTrantitions() {
		return removedTrantitions;
	}

	public int getRemovedPlaces() {
		return removedPlaces;
	}

	public int getRuleA() {
		return ruleA;
	}

	public int getRuleB() {
		return ruleB;
	}

	public int getRuleC() {
		return ruleC;
	}

	public int getRuleD() {
		return ruleD;
	}
	
	public int getRuleE() {
		return ruleE;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();		
		buffer.append("Removed places: ");
		buffer.append(removedPlaces);
		buffer.append(System.getProperty("line.separator"));
		
		buffer.append("Removed transitions: ");
		buffer.append(removedTrantitions);
		buffer.append(System.getProperty("line.separator"));
		
		buffer.append("Applications of rules A, B, C, D, E: (");
		buffer.append(ruleA);
		buffer.append(", ");
		buffer.append(ruleB);
		buffer.append(", ");
		buffer.append(ruleC);
		buffer.append(", ");
		buffer.append(ruleD);
		buffer.append(", ");
		buffer.append(ruleE);
		buffer.append(")");
		
		return buffer.toString();
	}
}
