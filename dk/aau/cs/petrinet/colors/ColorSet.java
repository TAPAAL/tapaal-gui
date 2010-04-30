package dk.aau.cs.petrinet.colors;

import java.util.SortedSet;
import java.util.TreeSet;

public class ColorSet {
	private SortedSet<IntegerRange> ranges;

	public ColorSet(){
		this(false);
	}
	
	public ColorSet(boolean isEmpty){
		if(!isEmpty){
			ranges = new TreeSet<IntegerRange>();
		}
	}
	
	
	public void addRange(IntegerRange range){
		if(ranges == null){
			ranges = new TreeSet<IntegerRange>();
		}
		ranges.add(range);
	}

	public ColorSet intersect(ColorSet colorInvariant) {
		ColorSet newSet = new ColorSet(true);
		
		for(IntegerRange range : ranges){
			for(IntegerRange other : colorInvariant.ranges){
				IntegerRange intersection = range.intersect(other);
				
				if(!intersection.isEmpty()){
					newSet.addRange(intersection);
				}
			}
		}
		return newSet;
	}

	public boolean contains(int outputValue) {
		if(ranges == null) return false;
		if(ranges.isEmpty()) return true;
		for(IntegerRange range : ranges){
			if(range.contains(outputValue)) return true;
		}
		
		return false;
	}

	public String convertToTAGuardString(String valueVarName) {
		if(ranges == null) return "false";
		if(ranges.isEmpty()) return "";
		
		StringBuilder builder = new StringBuilder("(");
		boolean first = true;
		for(IntegerRange range : ranges){
			if(!first){
				builder.append(" || ");
			}
			builder.append("(");
			builder.append(range.convertToTAGuardString(valueVarName));
			builder.append(")");
			first = false;
		}
		
		builder.append(")");
		
		return builder.toString();
	}
}
