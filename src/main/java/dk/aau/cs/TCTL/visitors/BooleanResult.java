package dk.aau.cs.TCTL.visitors;

public class BooleanResult {
	private boolean result;

	public BooleanResult(){
		this(false);
	}
	
	public BooleanResult(boolean initialValue){
		result = initialValue;
	}

	public boolean result(){
		return result;
	}
	
	public void setResult(boolean result){
		this.result = result;
	}
}

