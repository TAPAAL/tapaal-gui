package dk.aau.cs.verification;

public class ReductionStats {
	private final int removedTrantitions;
	private final int removedPlaces;
	private final int ruleA;
	private final int ruleB;
	private final int ruleC;
	private final int ruleD;
	private final int ruleE;
	private final int ruleF;
	private final int ruleG;
	private final int ruleH;
	private final int ruleI;

	
	public ReductionStats(int removedTransitions, int removedPlaces, int ruleA, int ruleB, int ruleC, int ruleD, int ruleE, int ruleF, int ruleG, int ruleH, int ruleI) {
		this.removedTrantitions = removedTransitions;
		this.removedPlaces = removedPlaces;
		this.ruleA = ruleA;
		this.ruleB = ruleB;
		this.ruleC = ruleC;
		this.ruleD = ruleD;
		this.ruleE = ruleE;
		this.ruleF = ruleF;
		this.ruleG = ruleG;
		this.ruleH = ruleH;
		this.ruleI = ruleI;

	}
	
	public int getRemovedTransitions() {
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
	
	public int getRuleF() {
		return ruleE;
	}
	
	public int getRuleG() {
		return ruleE;
	}
	
	public int getRuleH() {
		return ruleE;
	}
	
	public int getRuleI() {
		return ruleE;
	}
	
	@Override
	public String toString() {

        return
            "Removed places: " +
            removedPlaces +
            System.getProperty("line.separator") +
            "Removed transitions: " +
            removedTrantitions +
            System.getProperty("line.separator") +
            "Applications of rules A, B, C, D, E, F, G, H, I: (" +
            ruleA +
            ", " +
            ruleB +
            ", " +
            ruleC +
            ", " +
            ruleD +
            ", " +
            ruleE +
            ", " +
            ruleF +
            ", " +
            ruleG +
            ", " +
            ruleH +
            ", " +
            ruleI +
            ")";
	}
}
