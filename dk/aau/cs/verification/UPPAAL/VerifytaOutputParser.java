package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;

import dk.aau.cs.verification.QueryResult;

public class VerifytaOutputParser {
	private static final String PROPERTY_IS_NOT_SATISFIED_STRING = "Property is NOT satisfied";
	private static final String PROPERTY_IS_SATISFIED_STRING = "Property is satisfied";
	private boolean error = false;

	public boolean error(){
		return error;
	}	

	public QueryResult parseOutput(BufferedReader reader) {
		String line=null;
		try {
			while ( (line = reader.readLine()) != null){
				if (line.contains(PROPERTY_IS_SATISFIED_STRING)) {
					reader.mark(250);

					if((line = reader.readLine()) != null && line.contains("sup")){
						line = reader.readLine();
						String number = line.substring(line.lastIndexOf(" ")).trim();
						return new QueryResult(Integer.parseInt(number));
					}else{
						reader.reset();
					}
					return new QueryResult(true);
				}else if(line.contains(PROPERTY_IS_NOT_SATISFIED_STRING)){
					return new QueryResult(false);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			error=true;
		}
		return null;
	}
}
