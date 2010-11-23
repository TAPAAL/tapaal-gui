package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class Constant {
	private String name;
	private int value;
	private int lowerBound;
	private int upperBound;
		
	public Constant(String name, int value){
		setName(name);
		setValue(value);
		reset();
	}
	
	public Constant(Constant constant) {
		Require.that(constant != null, "Constant cannot be null");
		
		this.name = constant.name;
		this.value = constant.value;
		this.lowerBound = constant.lowerBound;
		this.upperBound = constant.upperBound;
	}

	public void setName(String name) {
		Require.that(name != null && !name.isEmpty(), "A constant must have a name");
		this.name = name;
	}

	public String name() {
		return name;
	}

	public void setValue(int value) {
		Require.that(value >= 0, "value must be non-negative");
		this.value = value;
	}

	public int value() {
		return value;
	}
	
	@Override
	public String toString(){
		return name + " = " + value;
	}
	
	public int lowerBound(){
		return lowerBound;
	}
	
	public void setLowerBound(int value){
		if(value > lowerBound){
			lowerBound = value;
		}
	}
	
	public int upperBound(){
		return upperBound;
	}
	
	public void setUpperBound(int value){
		if(value >= 0 && value < upperBound){
			upperBound = value;
		}
	}
	
	public void reset() {
		lowerBound = 0;
		upperBound = Integer.MAX_VALUE;
	}

	public Constant copy() {
		return new Constant(this);
	}
}
