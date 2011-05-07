package dk.aau.cs.util;

public class UnsupportedModelException extends Exception {
	private static final long serialVersionUID = -456845792030308112L;
	
	public UnsupportedModelException(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		return super.getMessage();
	}
}
