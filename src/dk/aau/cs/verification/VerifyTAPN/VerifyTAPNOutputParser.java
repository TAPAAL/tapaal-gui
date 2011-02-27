package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.QueryResult;

public class VerifyTAPNOutputParser {
	private static final String Query_IS_NOT_SATISFIED_STRING = "Query is NOT satisfied";
	private static final String Query_IS_SATISFIED_STRING = "Query is satisfied";

	public QueryResult parseOutput(String output) {
		String[] lines = output.split(System.getProperty("line.separator"));
		try {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (line.contains(Query_IS_SATISFIED_STRING)) {
					return new QueryResult(true);
				} else if (line.contains(Query_IS_NOT_SATISFIED_STRING)) {
					return new QueryResult(false);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

}
