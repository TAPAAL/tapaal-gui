package pipe.dataLayer.colors;

import java.util.ArrayList;
import java.util.List;

public class IntOrConstantRange implements Comparable<IntOrConstantRange> {
	private IntOrConstant from;
	private IntOrConstant to;
	
	private boolean goesToInfinity;
	
	public IntOrConstantRange(IntOrConstant single, boolean goesToInfinity){
		this(single);
		this.goesToInfinity = goesToInfinity;
	}
	
	public IntOrConstantRange(IntOrConstant single){
		from = single;
		to = single;
	}
	
	public IntOrConstantRange(IntOrConstant from, IntOrConstant to){
		if(from.getValue() > to.getValue()) throw new IllegalArgumentException();
		
		this.from = from;
		this.to = to;
	}
	
	public boolean isInRange(int value){
		if(goesToInfinity){
			return value >= from.getValue();
		}else{
			return from.getValue() <= value && value <= to.getValue();
		}
	}
	
	public boolean usesConstants(){
		return from.isUsingConstant() || to.isUsingConstant();
	}
	
	public boolean isSingle(){
		return from.equals(to) && !goesToInfinity;
	}
	
	public boolean goesToInfinity(){
		return goesToInfinity;
	}

	@Override
	public String toString(){
		return toString(false);
	}

	public String toString(boolean showValues){
		if(goesToInfinity){
			return from.toString(showValues) + "-";
		}else if(from.equals(to)){
			return from.toString(showValues);
		}else {
			return from.toString(showValues) + "-" + to.toString(showValues);
		}
	}

	public boolean overlaps(IntOrConstantRange range) {
		int largestFrom = from.getValue();
		int smallestTo = goesToInfinity ? Integer.MAX_VALUE : to.getValue();
		
		if(range.from.getValue() > largestFrom){
			largestFrom = range.from.getValue();
		}
		
		if(!range.goesToInfinity && range.to.getValue() < smallestTo){
			smallestTo = range.to.getValue();
		}
		
		return largestFrom <= smallestTo;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IntOrConstantRange){
			return equals((IntOrConstantRange)obj);
		}else{
			return false;
		}
	}
	
	private boolean equals(IntOrConstantRange obj) {
		return from.equals(obj.from) && to.equals(obj.to) && goesToInfinity == obj.goesToInfinity;
	}

	public int compareTo(IntOrConstantRange range) {
		return from.getValue() - range.from.getValue();
	}

	public static IntOrConstantRange parse(String range, boolean checkUsage) {
		if(range.contains("-")){
			if(range.indexOf("-") == range.length()-1){ // Format: x-
				IntOrConstant i = new IntOrConstant(range.substring(0,range.length()-1),checkUsage);
				return new IntOrConstantRange(i, true);
			}else{ // Format: x-y
				String[] limits = range.split("-");
				if(limits.length > 2) throw new IllegalArgumentException("does not match range format");
				
				IntOrConstant lower = new IntOrConstant(limits[0],checkUsage);
				IntOrConstant upper = new IntOrConstant(limits[1],checkUsage);
				
				return new IntOrConstantRange(lower,upper);
			}
		}else{ // Format: x
			IntOrConstant i = new IntOrConstant(range, checkUsage);
			return new IntOrConstantRange(i);
		}
	}

	public List<String> getUsedConstants() {
		List<String> list = new ArrayList<String>();
		
		if(from.isUsingConstant()){
			list.add(from.getConstantName());
		}
		
		if(to.isUsingConstant()){
			list.add(to.getConstantName());
		}
		
		return list;
	}

	public IntOrConstant getFrom() {
		return from;
	}
	
	public IntOrConstant getTo() {
		return to;
	}

	public static IntOrConstantRange parse(String str) {
		return parse(str, true);
	}

	public String toStringWithoutConstants() {
		if(goesToInfinity){
			return String.valueOf(from.getValue()) + "-";
		}else if(from.equals(to)){
			return String.valueOf(from.getValue());
		}else {
			return String.valueOf(from.getValue()) + "-" + String.valueOf(to.getValue());
		}
	}

	public void updateConstantName(String oldName, String newName) {
		from.updateConstantName(oldName, newName);
		to.updateConstantName(oldName, newName);		
	}
	
}
