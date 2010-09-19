package dk.aau.cs.verification;

public class VerificationResult {
	private boolean querySatisfied;
	// TODO: MJ -- add trace

	public boolean isQuerySatisfied() {
		return querySatisfied;
	}
	
	public void setQuerySatisfied(boolean result){
		querySatisfied = result;
	}
	
	public VerificationResult(){
		this(false);
	}	
	public VerificationResult(boolean isQuerySatisfied){
		this.querySatisfied = isQuerySatisfied;
	}
}
