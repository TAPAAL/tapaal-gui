package pipe.dataLayer.colors;

public class OutputValue {

	private int value;
	private String name;
	
	public OutputValue(){
		
	}
	
	public OutputValue(int value){
		this.value = value;
	}
	
	public OutputValue(String name){
		this.name = name;
	}
	
	public boolean isUsingConstant(){
		return name != null;
	}
	
	public int getIntegerValue(){
		return value;
	}
	
	public String getConstantName(){
		return name;
	}
	
	public void setOutputValue(int value){
		this.value = value;
		this.name = null;	
	}
	
	public void setConstantName(String name){
		if(name != null && !name.isEmpty()){
			this.name = name;
			this.value = 0;
		}
	}
	
	public String toString(){
		if(isUsingConstant()){
			return name;
		}else{
			return String.valueOf(value);
		}
	}
}
