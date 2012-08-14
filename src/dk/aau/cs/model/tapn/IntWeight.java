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
		return Integer.toString(value);
	}
	
	public String toString(boolean displayConstantNames) {
		return toString();
	}
}
