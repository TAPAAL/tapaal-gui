package dk.aau.cs.util;

public class UnsupportedQueryException extends Exception {

	public UnsupportedQueryException(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		return super.getMessage();
	}
}
