package pipe.dataLayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class ConstantStore {
	private TreeMap<String, Constant> constants = new TreeMap<String, Constant>();
	private HashMap<String, Integer> inUse = new HashMap<String, Integer>();
	private int largest = 0;
	
	public ConstantStore(){
	}
	
	public Collection<Constant> getConstants()
	{
		return constants.values();
	}
	
	public boolean updateConstant(String oldName, Constant constant)
	{
			if(constants.containsKey(oldName)){
				constants.remove(oldName);
				constants.put(constant.getName(), constant);
				int count = inUse.get(oldName);
				inUse.remove(oldName);
				inUse.put(constant.getName(), count);
				findLargestValue();
				return true;
			}
		return false;
	}

	private void findLargestValue() {
		largest = 0;
		for(Constant c : constants.values()){
			if(c.getValue() > largest)
				largest = c.getValue();
		}
		
	}

	private boolean isNamedInf(String name) {
		return name.toLowerCase().equals("inf");
	}

	public boolean addConstant(String name, int value) {	
		if(isNamedInf(name)) return false;
		
		if(!constants.containsKey(name)){
			constants.put(name, new Constant(name, value));
			inUse.put(name, 0);
			if(value > largest) largest = value;
			
			return true;
		}
		return false;
	}

	public boolean removeConstant(String name){
		if(!isConstantInUse(name)){
			if(constants.containsKey(name)){
				constants.remove(name);
				inUse.remove(name);
				findLargestValue();
				return true;
			}
		}
		return false;
	}


	private boolean isConstantInUse(String name) {
		return inUse.get(name).intValue() > 0;
	}

	public Set<String> getConstantNames() {
		return constants.keySet();
	}

	public void incrementConstantUsage(String constantName) {
		inUse.put(constantName, inUse.get(constantName)+1);
	}

	public void decrementConstantUsage(String constantName) {
		inUse.put(constantName, inUse.get(constantName)-1);
	}

	public Constant getConstant(String constantName) {
		return constants.get(constantName);
	}
	
	public int getLargestConstantValue(){
		return largest;
	}

	public void buildConstraints(ArrayList<Arc> arcsArray) {
		for(Arc arc : arcsArray){
			if(arc instanceof TimedArc || arc instanceof TransportArc)
				buildConstraint((TimedArc)arc);
		}
		
	}

	public void buildConstraint(TimedArc arc) {
		String guard = arc.getGuard();
		
		boolean isFirstNumber = true;
		boolean isSecondNumber = true;
		int firstValue = 0;
		int secondValue = 0;
		String leftDelim = guard.substring(0,1);
		String rightDelim = guard.substring(guard.length()-1, guard.length());
		String first = guard.substring(1, guard.indexOf(","));
		String second = guard.substring(guard.indexOf(",")+1, guard.length()-1);
		
		try{
			firstValue = Integer.parseInt(first);
		}catch(NumberFormatException e){
			isFirstNumber = false;
		}
		
		try{
			secondValue = Integer.parseInt(second);
		}catch(NumberFormatException e){
			if(second.equals("inf")){
				secondValue = Integer.MAX_VALUE;
			}
			else{
				isSecondNumber = false;
			}
		}
		
	//	ConstraintComparison operator = 
	//		ConstraintComparison.getOperatorForConstraint(leftDelim, rightDelim);
		int diff = getDiffForConstraint(leftDelim, rightDelim);
		if(!isFirstNumber && !isSecondNumber){
			Constant left = getConstant(first);
			Constant right = getConstant(second);
			
			if(left.getValue()+diff > right.getLowerBound()){
				right.setLowerBound(left.getValue()+diff);				
			}
			
			if(right.getValue()-diff < left.getUpperBound()){
				left.setUpperBound(right.getValue()-diff);
			}
			
//			GuardConstraint gc = new ConstantConstantConstraint(
//					first,
//					second,
//					operator);
//			
//			addConstraint(arc, first, gc);
//			addConstraint(arc, second,gc);
		}
		else if(!isFirstNumber){
			Constant left = getConstant(first);
			if(secondValue-diff < left.getUpperBound()){
				left.setUpperBound(secondValue-diff);
			}
			
			if(diff == 1 && left.getLowerBound() == 0){
				left.setLowerBound(1);
			}
			
			
//			GuardConstraint gc = new ConstantIntegerConstraint(
//					first,
//					secondValue,
//					operator);
//			addConstraint(arc, first, gc);
		}else if(!isSecondNumber){
			Constant right = getConstant(second);
			
			if(firstValue+diff > right.getLowerBound()){
				right.setLowerBound(firstValue+diff);				
			}
//			GuardConstraint gc = new ConstantIntegerConstraint(
//					second,
//					firstValue,
//					ConstraintComparison.invertOperator(operator));
//			addConstraint(arc, second, gc);
		}
	}
	
	public int getDiffForConstraint(String leftDelimiter, String rightDelimiter) {
		if(leftDelimiter.equals("[") && rightDelimiter.equals("]"))
			return 0;
		else 
			return 1;
	}
}
