package dk.aau.cs.util;

public class UnsupportedQueryException extends Exception {
	private static final long serialVersionUID = 5361335540140443712L;

	public UnsupportedQueryException(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		return super.getMessage();
	}
}
