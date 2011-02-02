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
			if(checkUsage && !CreateGui.getCurrentTab().network().isConstantNameUsed(intOrConstant)){
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

	@Override
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
		return CreateGui.getCurrentTab().network().getConstantValue(name);
	}

	public int getValue() {
		if(isUsingConstant()){
			return getConstantValue();
		}else{
			return getIntegerValue();
		}
	}



	public void updateConstantName(String oldName, String newName) {
		if(isUsingConstant() && name.equals(oldName)){
			name = newName;
		}
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntOrConstant other = (IntOrConstant) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value != other.value)
			return false;
		return true;
	}

}
