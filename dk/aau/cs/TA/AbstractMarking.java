package dk.aau.cs.TA;

import java.util.ArrayList;
import java.util.HashMap;

public class AbstractMarking {
	
	
	ArrayList<String> clockExpressions = new ArrayList<String>(); 
	HashMap<String, ArrayList<String>> placeToTokenMap = new HashMap<String, ArrayList<String>>();
	String firedTransition = "";
	
	public void addToken(String tokenName, String placename){
		
		if (placeToTokenMap.get(placename)==null){
			
			ArrayList a = new ArrayList<String>();
			a.add(tokenName);
			placeToTokenMap.put(placename, a);	
			
		} else {
			
			placeToTokenMap.get(placename).add(tokenName);
			
		}		
		
	}
	
	public void addClockExpression(String expression){
		clockExpressions.add(expression);
		
	}
	
	public void setFiredTranstiion(String fired){
		firedTransition = fired;
	}
	
	public String getFiredTranstiion(){
		return firedTransition ;
	}
	
}
