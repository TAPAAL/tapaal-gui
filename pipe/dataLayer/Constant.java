package pipe.dataLayer;

public class Constant {
	private String name;
	private int value;
	
	public Constant()
	{
		this("", 0);
	}
	
	public Constant(String name, int value){
		this.name = name;
		this.value = value;
	}

	private void setName(String name) {
		this.name = name;
	}

	private String getName() {
		return name;
	}

	private void setValue(int value) {
		this.value = value;
	}

	private int getValue() {
		return value;
	}
}
