package pipe.dataLayer.colors;

public class IntegerRange implements Comparable<IntegerRange> {
	private int from;
	private int to;
	
	private boolean goesToInfinity;
	
	public IntegerRange(int single, boolean goesToInfinity){
		this(single);
		this.goesToInfinity = goesToInfinity;
	}
	
	public IntegerRange(int single){
		if(single < 0) throw new IllegalArgumentException("argument must be greater than 0");
		
		from = single;
		to = single;
	}
	
	public IntegerRange(int from, int to){
		if(from < 0 || to < 0) throw new IllegalArgumentException("argument(s) must be greater than 0");
		
		if(from > to){
			int swap = from;
			from = to;
			to = swap;
		}
		
		this.from = from;
		this.to = to;
	}
	
	public boolean isInRange(int value){
		if(goesToInfinity){
			return value >= from;
		}else{
			return from <= value && value <= to;
		}
	}
	
	public String toString(){
		if(goesToInfinity){
			return String.valueOf(from) + "-";
		}else if(from == to){
			return String.valueOf(from);
		}else {
			return String.valueOf(from) + "-" + String.valueOf(to);
		}
	}

	public boolean overlaps(IntegerRange range) {
		int largestFrom = from;
		int smallestTo = goesToInfinity ? Integer.MAX_VALUE : to;
		
		if(range.from > largestFrom){
			largestFrom = range.from;
		}
		
		if(!range.goesToInfinity && range.to < smallestTo){
			smallestTo = range.to;
		}
		
		return largestFrom <= smallestTo;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof IntegerRange){
			return equals((IntegerRange)obj);
		}else{
			return false;
		}
	}
	
	private boolean equals(IntegerRange obj) {
		return from == obj.from && to == obj.to && goesToInfinity == obj.goesToInfinity;
	}

	public int compareTo(IntegerRange range) {
		return from - range.from;
	}

	public static IntegerRange parse(String range) {
		if(range.contains("-")){
			if(range.indexOf("-") == range.length()-1){ // Format: x-
				int i = Integer.parseInt(range.substring(0,range.length()-1));
				return new IntegerRange(i, true);
			}else{ // Format: x-y
				String[] limits = range.split("-");
				if(limits.length > 2) throw new IllegalArgumentException("does not match range format");
				
				int lower = Integer.parseInt(limits[0]);
				int upper = Integer.parseInt(limits[1]);
				
				return new IntegerRange(lower,upper);
			}
		}else{ // Format: x
			int i = Integer.parseInt(range);
			return new IntegerRange(i);
		}
	}
	
	
}
