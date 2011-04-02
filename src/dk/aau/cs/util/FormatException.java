package dk.aau.cs.util;

public class FormatException extends Exception {
	private static final long serialVersionUID = -4616597068661723522L;

	public FormatException(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		return super.getMessage();
	}
}
