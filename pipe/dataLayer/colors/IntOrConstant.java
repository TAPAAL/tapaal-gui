package pipe.dataLayer.colors;

import pipe.gui.CreateGui;

public class IntOrConstant {

	private int value;
	private String name;
	
	public IntOrConstant(){
	}
	
	public IntOrConstant(String intOrConstant){
		try{
			value = Integer.parseInt(intOrConstant);
			if(value < 0) throw new IllegalArgumentException();
		}catch(IllegalArgumentException e){
			if(!CreateGui.getModel().isConstantNameUsed(intOrConstant)){
				throw new IllegalArgumentException("Cannot parse as integer and wrong constant name");
			}else{
				name = intOrConstant;
			}
		}
	}
	
	public IntOrConstant(int value){
		this.value = value;
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

	public int getConstantValue() {
		return CreateGui.getModel().getConstantValue(name);
	}

	public int getValue() {
		if(isUsingConstant()){
			return getConstantValue();
		}else{
			return getIntegerValue();
		}
	}
}
