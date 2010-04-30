package dk.aau.cs.petrinet.colors;

import pipe.dataLayer.colors.IntOrConstant;
import pipe.dataLayer.colors.IntOrConstantRange;



public class IntegerRange implements Comparable<IntegerRange> {
	private int from;
	private int to;
	private boolean goesToInfinity = false;
	
	public IntegerRange(int single, boolean goesToInfinity){
		this(single);
		this.goesToInfinity = goesToInfinity;
	}
	
	public IntegerRange(int single){
		from = single;
		to = single;
	}
	
	public IntegerRange(int from, int to){
		if(from > to) throw new IllegalArgumentException();
		
		this.from = from;
		this.to = to;
	}
	
	public IntegerRange(String range) {
		if(range.contains("-")){
			if(range.indexOf("-") == range.length()-1){ // Format: x-
				from = Integer.parseInt(range.substring(0,range.length()-1));
				goesToInfinity = true;
			}else{ // Format: x-y
				String[] limits = range.split("-");
				if(limits.length > 2) throw new IllegalArgumentException("does not match range format");
				
				from = Integer.parseInt(limits[0]);
				to = Integer.parseInt(limits[1]);
			}
		}else{ // Format: x
			from = Integer.parseInt(range);
			to = from;
		}
	}

	public void setTo(int to) {
		this.to = to;
	}
	public int getTo() {
		return to;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getFrom() {
		return from;
	}

	public int compareTo(IntegerRange range) {
		return from - range.from;
	}
}
