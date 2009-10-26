package pipe.dataLayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

import pipe.gui.undo.AddConstantEdit;
import pipe.gui.undo.RemoveConstantEdit;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.undo.UpdateConstantEdit;

public class ConstantStore {
	private TreeMap<String, Constant> constants = new TreeMap<String, Constant>();
	private int largest = -1;
	
	public ConstantStore(){
	}
	
	public Collection<Constant> getConstants()
	{
		return constants.values();
	}
	
	public UndoableEdit updateConstant(String oldName, Constant constant, DataLayer model)
	{
		if(oldName.equals(constant.getName()) || !constants.containsKey(constant.getName()))
			if(constants.containsKey(oldName)){
				Constant old = constants.get(oldName);
				boolean isUsed = old.getIsUsed();
				int lower = old.getLowerBound();
				int upper = old.getUpperBound();
				constant.setIsUsed(isUsed);
				constant.setLowerBound(lower);
				constant.setUpperBound(upper);
				constants.remove(oldName);
				constants.put(constant.getName(), constant);
				findLargestValue();
				return new UpdateConstantEdit(old, constant, this, model);
			}
		return null;
	}

	private void findLargestValue() {
		largest = -1;
		for(Constant c : constants.values()){
			if(c.getValue() > largest)
				largest = c.getValue();
		}
		
	}

	private boolean isNamedInf(String name) {
		return name.toLowerCase().equals("inf");
	}

	public UndoableEdit addConstant(String name, int value) {	
		if(isNamedInf(name)) return null;
		
		if(!constants.containsKey(name)){
			Constant constant = new Constant(name, value);
			add(constant);
			return new AddConstantEdit(constant, this);
		}
		return null;
	}

	public UndoableEdit removeConstant(String name){
		if(!isConstantInUse(name)){
			if(constants.containsKey(name)){
				Constant constant = constants.get(name);
				constants.remove(name);
				findLargestValue();
				return new RemoveConstantEdit(constant, this);
			}
		}
		return null;
	}


	private boolean isConstantInUse(String name) {
		return constants.get(name).getIsUsed();
	}

	public Set<String> getConstantNames() {
		return constants.keySet();
	}

	public Constant getConstant(String constantName) {
		return constants.get(constantName);
	}
	
	public int getLargestConstantValue(){
		return largest;
	}

	public void buildConstraints(ArrayList<Place> places, ArrayList<Arc> arcs) {
		for(Constant c : constants.values()){
			c.reset();
		}
		
		for(Place place : places){
			if(place instanceof TimedPlace){
				TimedPlace tp = (TimedPlace)place;
				String inv = tp.getInvariant();
				int substringStart = 0;
				if (inv.contains("<=")){
					substringStart = 2;
				}else {
					substringStart = 1;
				}
				String val = inv.substring(substringStart);
				if(constants.containsKey(val)){
					constants.get(val).setIsUsed(true);
				}
			}
		}
		
		for(Arc arc : arcs){
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
		
		int diff = getDiffForConstraint(leftDelim, rightDelim);
		if(!isFirstNumber && !isSecondNumber){
			Constant left = getConstant(first);
			Constant right = getConstant(second);
			
			left.setIsUsed(true);
			right.setIsUsed(true);
			
			if(left.getValue()+diff > right.getLowerBound()){
				right.setLowerBound(left.getValue()+diff);				
			}
			
			if(right.getValue()-diff < left.getUpperBound()){
				left.setUpperBound(right.getValue()-diff);
			}
			
			
		}
		else if(!isFirstNumber){
			Constant left = getConstant(first);
			left.setIsUsed(true);
			
			if(secondValue-diff < left.getUpperBound()){
				left.setUpperBound(secondValue-diff);
			}			
		}else if(!isSecondNumber){
			Constant right = getConstant(second);
			right.setIsUsed(true);
			
			if(firstValue+diff > right.getLowerBound()){
				right.setLowerBound(firstValue+diff);				
			}
		}
	}
	
	public int getDiffForConstraint(String leftDelimiter, String rightDelimiter) {
		if(leftDelimiter.equals("[") && rightDelimiter.equals("]"))
			return 0;
		else 
			return 1;
	}

	public void add(Constant constant) {
		constants.put(constant.getName(), constant);
		if(constant.getValue() > largest) largest = constant.getValue();
		
	}

	public void remove(Constant constant) {
		constants.remove(constant.getName());
		findLargestValue();		
	}
}
