package dk.aau.cs.verification.VerifyTAPN;

import java.util.ArrayList;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.BoundednessAnalysisResult;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.Stats;

public class VerifyTAPNCTLOutputParser{
	protected final int totalTokens;
	protected final TAPNQuery query;
	protected final int extraTokens;
	
	public VerifyTAPNCTLOutputParser(int totalTokens, int extraTokens, TAPNQuery query){
		this.totalTokens = totalTokens;
		this.extraTokens = extraTokens;
		this.query = query;
	}
	
	public Tuple<QueryResult, Stats> parseOutput(String output) {
		boolean result = false;
		boolean foundResult = false;
		String[] lines = output.split(System.getProperty("line.separator"));
		
        try {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (line.contains("TRUE")) {
					result = true;
					foundResult = true;
				} else if (line.contains("FALSE")) {
					result = false;
					foundResult = true;
				}
			}
			
			if(!foundResult) return null;			
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, 0, extraTokens);
			Tuple<QueryResult, Stats> value = new Tuple<QueryResult, Stats>(new QueryResult(result, boundedAnalysis, query, false), new Stats(0, 0, 0, new ArrayList<Tuple<String,Integer>>(), new ArrayList<Tuple<String,Integer>>()));
			return value; 	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
