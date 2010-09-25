package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import dk.aau.cs.verification.QueryResult;

public class VerifytaOutputParser {
	private static final String PROPERTY_IS_NOT_SATISFIED_STRING = "Property is NOT satisfied";
	private static final String PROPERTY_IS_SATISFIED_STRING = "Property is satisfied";
	private QueryResult[] queryResults;
	private boolean error = false;

	public boolean error(){
		return error;
	}	

	public QueryResult[] parseOutput(BufferedReader reader) {
		ArrayList<QueryResult> results = new ArrayList<QueryResult>();

		String line=null;
		try {
			while ( (line = reader.readLine()) != null){
				if (line.contains(PROPERTY_IS_SATISFIED_STRING)) {
					reader.mark(250);

					if((line = reader.readLine()) != null && line.contains("sup")){
						line = reader.readLine();
						String number = line.substring(line.lastIndexOf(" ")).trim();
						results.add(new QueryResult(Integer.parseInt(number)));
						continue;
					}else{
						reader.reset();
					}
					results.add(new QueryResult(true));
				}else if(line.contains(PROPERTY_IS_NOT_SATISFIED_STRING)){
					results.add(new QueryResult(false));
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			error=true;
		}
		this.queryResults = new QueryResult[results.size()];		
		results.toArray(queryResults);
		return queryResults;
	}
}
