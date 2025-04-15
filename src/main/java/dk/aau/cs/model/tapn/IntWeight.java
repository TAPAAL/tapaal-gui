package dk.aau.cs.model.tapn;

public class IntWeight extends Weight{

	final int value;
	
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
			return value + "x";
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IntWeight)) return false;
        IntWeight other = (IntWeight)obj;
        return value == other.value;
    }
}
