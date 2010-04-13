package pipe.dataLayer.colors;

import java.util.SortedSet;

import java.util.TreeSet;

public class ColorSet {
	private SortedSet<IntegerRange> ranges;
	
	public ColorSet(){
		ranges = new TreeSet<IntegerRange>();
	}
	
	public boolean contains(IntegerRange range){
		for(IntegerRange ir : ranges){
			if(ir.overlaps(range)){
				return true;
			}
		}
		
		return false;
	}
	
	public void add(IntegerRange range){
		if(!contains(range)){
			ranges.add(range);
		}
	}
	
	public void remove(IntegerRange range){
		ranges.remove(range);
	}
	
	public boolean contains(int color) {
		if(isEmpty()) return true;
		
		for(IntegerRange ir : ranges){
			if(ir.isInRange(color)) return true;
		}
		
		return false;
	}
	
	public String toStringNoSetNotation(){
		if(ranges.isEmpty()) return "";
		StringBuilder builder = new StringBuilder("");
		
		boolean first = true;
		for(IntegerRange ir : ranges){
			if(!first){
				builder.append(", ");
			}
			
			builder.append(ir.toString());
			first = false;
		}
		
		return builder.toString();
	}
	
	public String toString() {
		String str = toStringNoSetNotation();
		if(!str.isEmpty()){
			return String.format("{%1s}", str);
		}else{
			return "";
		}
	}

	public boolean isEmpty() {
		return ranges.isEmpty();
	}
}
