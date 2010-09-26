package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;

import dk.aau.cs.TA.UppaalTrace;

public class VerifytaTraceParser {
	public UppaalTrace parseTrace(BufferedReader reader){
		String line;
		try {
			while((line = reader.readLine()) != null){
				if(line.toLowerCase().equals("state:")){
					
				}
			}
		} catch (IOException e) {
			return null;
		}
		
		return new UppaalTrace();
	}
}
