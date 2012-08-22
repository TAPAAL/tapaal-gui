package dk.aau.cs.model.tapn;

public class IntWeight extends Weight{

	int value;
	
	public IntWeight(int value) {
		this.value = value;
	}
	
	@Override
	public int value() {
		return value;
	}

	@Override
	public Weight copy() {
		return new IntWeight(value);
	}
	
	@Override
	public String toString() {
		if(value > 1){
			return Integer.toString(value) + "x";
		} else {
			return "";
		}
	}
	
	public String toString(boolean displayConstantNames) {
		return toString();
	}
	
	public String nameForSaving(boolean writeConstantNames){
		return Integer.toString(value);
	}
}
