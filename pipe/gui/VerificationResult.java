package pipe.gui;

public class VerificationResult {
	private boolean querySatisfied;
	// TODO: MJ -- add trace

	public boolean isQuerySatisfied() {
		return querySatisfied;
	}
	
	public VerificationResult(boolean isQuerySatisfied){
		this.querySatisfied = isQuerySatisfied;
	}
}
