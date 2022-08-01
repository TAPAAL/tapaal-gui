package dk.aau.cs.verification.batchProcessing;

public class VerificationTaskCompleteEvent {
	private final int verificationTasksCompleted;
	
	public VerificationTaskCompleteEvent(int verificationTasksCompleted) {
		this.verificationTasksCompleted = verificationTasksCompleted;
	}
	
	public int verificationTasksCompleted() {
		return verificationTasksCompleted;
	}
}
