package pipe.dataLayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredInhibitorArc;
import pipe.dataLayer.colors.ColoredInputArc;
import pipe.dataLayer.colors.ColoredInterval;
import pipe.dataLayer.colors.ColoredOutputArc;
import pipe.dataLayer.colors.ColoredTimeInvariant;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.dataLayer.colors.IntOrConstant;
import pipe.dataLayer.colors.IntOrConstantRange;
import pipe.gui.CreateGui;
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
		if(oldName.equals(constant.getName()) || !constants.containsKey(constant.getName())){
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

	public boolean isConstantNameUsed(String name){
		return constants.containsKey(name);
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

		boolean isNotUsingColors = !CreateGui.getModel().isUsingColors();
		for(Place place : places){
			if(isNotUsingColors){
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
			}else{
				if(place instanceof ColoredTimedPlace){
					buildConstraint((ColoredTimedPlace)place);	
				}
			}
		}

		for(Arc arc : arcs){
			if(isNotUsingColors){
				if(arc instanceof TimedArc || arc instanceof TransportArc){
					buildConstraint((TimedArc)arc);
				}
			}else{
				if(arc instanceof ColoredInputArc){
					buildConstraint((ColoredInputArc)arc);
				}else if(arc instanceof ColoredOutputArc){
					buildConstraint((ColoredOutputArc)arc);
				}else if(arc instanceof ColoredTransportArc){
					buildConstraint((ColoredTransportArc)arc);
				}else if(arc instanceof ColoredInhibitorArc){
					buildConstraint((ColoredInhibitorArc)arc);
				}
			}
		}
	}

	private void buildConstraint(ColoredTimedPlace place) {
		ColorSet colorInvariant = place.getColorInvariant();
		processColorGuards(colorInvariant);
		
		ColoredTimeInvariant timeInvariant = place.getTimeInvariant();
		processTimeInvariant(timeInvariant);
		
	}

	private void processTimeInvariant(ColoredTimeInvariant timeInvariant) {
		for(String constantName : timeInvariant.getUsedConstantNames()){
			Constant constant = getConstant(constantName);
			constant.setIsUsed(true);
		}		
	}

	private void buildConstraint(ColoredOutputArc arc) {
		if(arc.getOutputValue().isUsingConstant()){
			Constant constant = getConstant(arc.getOutputValue().getConstantName());
			constant.setIsUsed(true);
		}
	}

	public void buildConstraint(ColoredInputArc arc){
		ColoredInterval interval = arc.getTimeGuard();
		processTimeGuards(interval);

		ColorSet colorGuard = arc.getColorGuard();
		processColorGuards(colorGuard);
	}

	private void processColorGuards(ColorSet colorGuard) {
		for(IntOrConstantRange range : colorGuard.getRanges()){
			if(range.usesConstants()){
				if(!range.goesToInfinity() && !range.isSingle()){
					IntOrConstant from = range.getFrom();
					IntOrConstant to = range.getTo();

					if(from.isUsingConstant()){
						Constant constant = getConstant(from.getConstantName());
						constant.setIsUsed(true);
						constant.setUpperBound(to.getValue());
					}

					if(to.isUsingConstant()){
						Constant constant = getConstant(to.getConstantName());
						constant.setIsUsed(true);
						constant.setLowerBound(from.getValue());
					}
				}
			}
		}
		//		for(String constantName : colorGuard.getUsedConstants()){
		//			Constant constant = getConstant(constantName);
		//			constant.setIsUsed(true);
		//		}
	}

	private void processTimeGuards(ColoredInterval interval) {
		for(String constantName : interval.getUsedConstants()){
			Constant constant = getConstant(constantName);
			constant.setIsUsed(true);
		}
	}

	public void buildConstraint(ColoredTransportArc arc){
		ColoredInterval interval = arc.getTimeGuard();
		processTimeGuards(interval);

		ColorSet colorGuard = arc.getColorGuard();
		processColorGuards(colorGuard);

		if(arc.getOutputValue().isUsingConstant()){
			Constant constant = getConstant(arc.getOutputValue().getConstantName());
			constant.setIsUsed(true);
		}
	}

	public void buildConstraint(ColoredInhibitorArc arc){
		ColoredInterval interval = arc.getTimeGuard();
		processTimeGuards(interval);

		ColorSet colorGuard = arc.getColorGuard();
		processColorGuards(colorGuard);
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

			if(!first.equals(second)){
				if(left.getValue()+diff > right.getLowerBound()){
					right.setLowerBound(left.getValue()+diff);				
				}

				if(right.getValue()-diff < left.getUpperBound()){
					left.setUpperBound(right.getValue()-diff);
				}
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
