package dk.aau.cs.verification.batchProcessing;


public interface BatchProcessingListener {
	void fireVerificationTaskStarted();
	void fireVerificationTaskComplete(VerificationTaskCompleteEvent e);
	void fireStatusChanged(StatusChangedEvent e);
	void fireFileChanged(FileChangedEvent e);
}
