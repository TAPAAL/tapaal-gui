package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;

import dk.aau.cs.TA.UppaalTrace;
import dk.aau.cs.TA.trace.SymbolicState;

public class VerifytaTraceParser {
	public UppaalTrace parseTrace(BufferedReader reader){
		try {
			while(reader.ready()){
				StringBuffer buffer = new StringBuffer();
				String line;
				while((line = reader.readLine()) != null && !line.isEmpty()){
					buffer.append(line);
					buffer.append("\n");
				}
				
				String element = buffer.toString();
				if(element.contains("State:\n")){
					SymbolicState state = SymbolicState.parse(element);
				}else if(element.contains("Delay:")){

				}else if(element.contains("Transitions:")){
					
				}
			}
		} catch (IOException e) {
			return null;
		}
		
		return new UppaalTrace();
	}
}
