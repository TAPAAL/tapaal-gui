package dk.aau.cs.util;

public class FormatException extends Exception {

	public FormatException(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		return super.getMessage();
	}
}
