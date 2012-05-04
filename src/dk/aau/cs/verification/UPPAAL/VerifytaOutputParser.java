package dk.aau.cs.verification.UPPAAL;

import dk.aau.cs.verification.InconclusiveBoundednessAnalysisResult;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.QueryType;

public class VerifytaOutputParser {
	private static final String PROPERTY_IS_NOT_SATISFIED_STRING = "Property is NOT satisfied";
	private static final String PROPERTY_IS_SATISFIED_STRING = "Property is satisfied";
	private static final String DISCRETE_INCLUSION = "discrete inclusion";
	private boolean error = false;
	private boolean discreteInclusion = false;
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
				if (line.contains(DISCRETE_INCLUSION)) { discreteInclusion = true;}
				if (line.contains(PROPERTY_IS_SATISFIED_STRING)) {
					return new QueryResult(true, new InconclusiveBoundednessAnalysisResult(), queryType, discreteInclusion);
				} else if (line.contains(PROPERTY_IS_NOT_SATISFIED_STRING)) {
					return new QueryResult(false, new InconclusiveBoundednessAnalysisResult(), queryType, discreteInclusion);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
}
