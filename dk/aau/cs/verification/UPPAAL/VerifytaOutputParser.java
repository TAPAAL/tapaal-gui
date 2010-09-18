package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;

public class VerifytaOutputParser {
	private boolean property = false;
	private boolean error = false;

	public boolean isPropertySatisfied(){
		return property;
	}
	
	public boolean error(){
		return error;
	}	
	
	public void parseOutput(BufferedReader reader) {
		String line=null;
		try {
			while ( (line = reader.readLine()) != null){
				if (line.contains("Property is satisfied")) {
					property = true;
					break;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			error=true;
		}
	}
}
