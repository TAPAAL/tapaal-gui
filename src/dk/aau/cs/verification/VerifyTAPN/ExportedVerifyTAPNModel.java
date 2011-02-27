package dk.aau.cs.verification.VerifyTAPN;

public class ExportedVerifyTAPNModel {
	private final String queryFile;
	private final String modelFile;

	ExportedVerifyTAPNModel(String modelFile, String queryFile) {
		this.modelFile = modelFile;
		this.queryFile = queryFile;
	}

	public String modelFile() {
		return modelFile;
	}

	public String queryFile() {
		return queryFile;
	}

}
