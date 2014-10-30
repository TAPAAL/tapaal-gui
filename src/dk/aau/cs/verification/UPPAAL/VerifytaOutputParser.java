package dk.aau.cs.verification.UPPAAL;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.verification.InconclusiveBoundednessAnalysisResult;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.QueryType;

public class VerifytaOutputParser {
	private static final String PROPERTY_IS_NOT_SATISFIED_STRING = "is NOT satisfied".toLowerCase();
	private static final String PROPERTY_IS_SATISFIED_STRING = "is satisfied".toLowerCase();
	private static final String DISCRETE_INCLUSION = "discrete inclusion".toLowerCase();
	private boolean error = false;
	private boolean discreteInclusion = false;
	private TAPNQuery query;

	public VerifytaOutputParser(TAPNQuery query){
		this.query = query;
	}
	
	public boolean error() {
		return error;
	}

	public QueryResult parseOutput(String output) {
		String[] lines = output.split(System.getProperty("line.separator"));
		try {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i].toLowerCase();
				if (line.contains(DISCRETE_INCLUSION)) { discreteInclusion = true;}
				if (line.contains(PROPERTY_IS_SATISFIED_STRING)) {
					return new QueryResult(true, new InconclusiveBoundednessAnalysisResult(), query, discreteInclusion);
				} else if (line.contains(PROPERTY_IS_NOT_SATISFIED_STRING)) {
					return new QueryResult(false, new InconclusiveBoundednessAnalysisResult(), query, discreteInclusion);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
}
