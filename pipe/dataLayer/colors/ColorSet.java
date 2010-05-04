package pipe.dataLayer.colors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ColorSet {
	private SortedSet<IntOrConstantRange> ranges;
	private boolean displayValues = false;

	public ColorSet(){
		ranges = new TreeSet<IntOrConstantRange>();
	}

	public ColorSet(String colorSet) {
		this();
		if(colorSet != null && !colorSet.isEmpty()){
			String[] ranges = colorSet.split(",");
			for(String range : ranges){
				IntOrConstantRange ir = IntOrConstantRange.parse(range.trim(), true);
				add(ir);
			}
		}
	}

	public boolean contains(IntOrConstantRange range){
		for(IntOrConstantRange ir : ranges){
			if(ir.overlaps(range)){
				return true;
			}
		}

		return false;
	}

	public void add(IntOrConstantRange range){
		ranges.add(range);
	}

	public void remove(IntOrConstantRange range){
		ranges.remove(range);
	}

	public boolean contains(int color) {
		if(isEmpty()) return true;

		for(IntOrConstantRange ir : ranges){
			if(ir.isInRange(color)) return true;
		}

		return false;
	}

	public String toStringNoSetNotation(){
		if(ranges.isEmpty()) return "";
		StringBuilder builder = new StringBuilder("");

		boolean first = true;
		for(IntOrConstantRange ir : ranges){
			if(!first){
				builder.append(", ");
			}

			builder.append(ir.toString(displayValues));
			first = false;
		}

		return builder.toString();
	}

	@Override
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

	public List<String> getUsedConstants() {
		List<String> list = new ArrayList<String>();

		for(IntOrConstantRange range : ranges){
			list.addAll(range.getUsedConstants());
		}

		return list;		
	}

	public Set<IntOrConstantRange> getRanges() {
		return ranges;
	}
	
	public void displayValues(boolean display){
		this.displayValues = display;
	}
}
