package pipe.dataLayer.colors;

import pipe.gui.CreateGui;

public class IntOrConstant {

	private int value;
	private String name;

	public IntOrConstant(){
	}

	public IntOrConstant(String intOrConstant){
		this(intOrConstant, true);
	}

	public IntOrConstant(String intOrConstant, boolean checkUsage){
		try{
			value = Integer.parseInt(intOrConstant);
			if(value < 0) throw new IllegalArgumentException(intOrConstant);
		}catch(IllegalArgumentException e){
			if(checkUsage && !CreateGui.getModel().isConstantNameUsed(intOrConstant)){
				throw new IllegalArgumentException(intOrConstant);
			}else{
				name = intOrConstant;
			}
		}
	}

	public IntOrConstant(int value){
		this.value = value;
	}

	public IntOrConstant(IntOrConstant color) {
		this.value = color.value;
		this.name = color.name;
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
		return toString(false);
	}

	public String toString(boolean showValues){
		if(isUsingConstant()){
			if(showValues){
				return String.valueOf(getConstantValue());
			}else{
				return name;
			}
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

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IntOrConstant){
			IntOrConstant other = (IntOrConstant)obj;
			return value == other.value && name == other.name; 
		}

		return false;
	}

	public void updateConstantName(String oldName, String newName) {
		if(isUsingConstant() && name.equals(oldName)){
			name = newName;
		}
		
	}

}
