//package pipe.dataLayer;
//
//public class Constant {
//	private String name;
//	private int value;
//	private int lowerBound;
//	private int upperBound;
//	private boolean isUsed;
//	
//	public Constant()
//	{
//		this("", 0);
//	}
//	
//	public Constant(String name, int value){
//		this.name = name;
//		this.value = value;
//		reset();
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public void setValue(int value) {
//		this.value = value;
//	}
//
//	public int getValue() {
//		return value;
//	}
//	
//	@Override
//	public String toString(){
//		return name + " = " + value;
//	}
//	
//	public int getLowerBound(){
//		return isUsed ? lowerBound : 0;
//	}
//	
//	public void setLowerBound(int value){
//		if(value > lowerBound){
//			lowerBound = value;
//		}
//	}
//	
//	public int getUpperBound(){
//		return isUsed ? upperBound : Integer.MAX_VALUE;
//	}
//	
//	public void setUpperBound(int value){
//		if(value < upperBound){
//			upperBound = value;
//		}
//	}
//	
//	public void setIsUsed(boolean value){
//		isUsed = value;
//	}
//	
//	public boolean getIsUsed(){
//		return isUsed;
//	}
//
//	public void reset() {
//		isUsed = false;
//		lowerBound = 0;
//		upperBound = Integer.MAX_VALUE;
//	}
//}
