package dk.aau.cs.verification;

public class ReductionStats {
	private final int removedTrantitions;
	private final int removedPlaces;
	
	public ReductionStats(int removedTransitions, int removedPlaces) {
		this.removedTrantitions = removedTransitions;
		this.removedPlaces = removedPlaces;
	}
	
	public int getRemovedTransitions() {
		return removedTrantitions;
	}

	public int getRemovedPlaces() {
		return removedPlaces;
	}

    @Override
	public String toString() {

        return
            "Removed places: " +
            removedPlaces +
            System.getProperty("line.separator") +
            "Removed transitions: " +
            removedTrantitions;
	}
}
