package dk.aau.cs.util;

public class UnsupportedModelException extends Exception {

	public UnsupportedModelException(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		return super.getMessage();
	}
}
