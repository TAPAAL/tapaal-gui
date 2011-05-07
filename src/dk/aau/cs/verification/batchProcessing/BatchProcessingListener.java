package dk.aau.cs.verification.batchProcessing;


public interface BatchProcessingListener {
	void fireStatusChanged(StatusChangedEvent e);
	void fireFileChanged(FileChangedEvent e);
}
