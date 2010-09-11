package dk.aau.cs.TA;

import java.io.BufferedReader;
import java.util.ArrayList;

public class SymbolicUppaalTrace {
	
	
	public static ArrayList<AbstractMarking> parseUppaalAbstractTrace(BufferedReader traceReader) throws Exception{
		ArrayList<AbstractMarking> trace = new ArrayList<AbstractMarking>();
		
		int lastSequencenumber = 0;
		String line="";
		AbstractMarking marking = new AbstractMarking();
		
		while ((line = traceReader.readLine()) != null){
				
			if (line.contains("Transitions:")){
			
				String line1 = traceReader.readLine();
				traceReader.readLine();
				
				
				
				// Get name of fired transition
				int first = line1.indexOf("{");
				int second = line1.indexOf("}");

				String tmp = line1.substring(first+1, second);
				String[] tmpArray=tmp.split(",");
				
				
				String tokenName = ((line1.trim().split("\\."))[0]);
				if (tokenName.equals("Control")){
					continue;
				}
				
				String transitionName = tmpArray[1].replace("!", "");
				
				
				String sequencenumberString = "";
				int newSequencenumber = 0;
				//Number of firing sequence
				if (transitionName.contains("_T")){

					tmpArray = transitionName.split("_");
					sequencenumberString = tmpArray[tmpArray.length-1].replace("T", "").trim();
					newSequencenumber = Integer.parseInt(sequencenumberString);

				}
				
				if (sequencenumberString.equals("")){
					marking.setFiredTranstiion(transitionName);
					trace.add(marking);
					marking= new AbstractMarking();
				} else if (newSequencenumber <= lastSequencenumber){
					
					marking.setFiredTranstiion(transitionName.substring(0, transitionName.lastIndexOf("_")));
					
					trace.add(marking);
					marking= new AbstractMarking();
					
				}
				
				lastSequencenumber = newSequencenumber;

				

			}else if (line.contains("State")){
				
				traceReader.readLine(); // Read and disregard blank line
				
				//Read the location vector
				line =  traceReader.readLine();
				
				String[] locationVectorArray = line.split(" ");
				
				for (int i=1; i < locationVectorArray.length-1; i++){
					
					String[] tmp = locationVectorArray[i].split("\\.");
					String tokenName = tmp[0].trim();
					String placeName = tmp[1].trim();
					marking.addToken(tokenName, placeName);
				}
						
				
				line =  traceReader.readLine();
				
				String[] tmp = line.split(",");
				
				for (int i=0; i< tmp.length; i++){
					
					marking.addClockExpression(tmp[i].trim());
					
				}
				
				
			
			}
		}
		
		
		
		
		
		
		
		
		
		return trace;
	}
	
	
}
