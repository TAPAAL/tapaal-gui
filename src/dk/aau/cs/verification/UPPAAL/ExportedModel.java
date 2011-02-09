package dk.aau.cs.verification.UPPAAL;

import dk.aau.cs.translations.TranslationNamingScheme;

public class ExportedModel {

	private final String queryFile;
	private final String modelFile;
	private TranslationNamingScheme namingScheme;

	ExportedModel(String modelFile, String queryFile,
			TranslationNamingScheme namingScheme) {
		this.modelFile = modelFile;
		this.queryFile = queryFile;
		this.namingScheme = namingScheme;
	}

	public String modelFile() {
		return modelFile;
	}

	public String queryFile() {
		return queryFile;
	}

	public TranslationNamingScheme namingScheme() {
		return namingScheme;
	}
}
