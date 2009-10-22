package pipe.dataLayer;

public class Constant {
	private String name;
	private int value;
	private int lowerBound = 0;
	private int upperBound = Integer.MAX_VALUE;
	
	public Constant()
	{
		this("", 0);
	}
	
	public Constant(String name, int value){
		this.name = name;
		this.value = value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public String toString(){
		return name + " = " + value;
	}
	
	public int getLowerBound(){
		return lowerBound;
	}
	
	public void setLowerBound(int value){
		lowerBound = value;
	}
	
	public int getUpperBound(){
		return upperBound;
	}
	
	public void setUpperBound(int value){
		upperBound = value;
	}
}
