package dk.aau.cs.verification.UPPAAL;

public class ExportedModel {

	private final String queryFile;
	private final String modelFile;

	public ExportedModel(String modelFile, String queryFile) {
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
