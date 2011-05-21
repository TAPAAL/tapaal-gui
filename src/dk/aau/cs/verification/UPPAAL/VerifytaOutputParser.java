package dk.aau.cs.verification.UPPAAL;

import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.QueryType;

public class VerifytaOutputParser {
	private static final String PROPERTY_IS_NOT_SATISFIED_STRING = "Property is NOT satisfied";
	private static final String PROPERTY_IS_SATISFIED_STRING = "Property is satisfied";
	private boolean error = false;
	private QueryType queryType;

	public VerifytaOutputParser(QueryType queryType){
		this.queryType = queryType;
	}
	
	public boolean error() {
		return error;
	}

	public QueryResult parseOutput(String output) {
		String[] lines = output.split(System.getProperty("line.separator"));
		try {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (line.contains(PROPERTY_IS_SATISFIED_STRING)) {
					return new QueryResult(true, queryType);
				} else if (line.contains(PROPERTY_IS_NOT_SATISFIED_STRING)) {
					return new QueryResult(false, queryType);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
}
