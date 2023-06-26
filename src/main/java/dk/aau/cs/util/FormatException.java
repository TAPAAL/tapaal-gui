package dk.aau.cs.util;

public class FormatException extends RuntimeException {

	public FormatException(String message) {
		super(message);
	}

    public FormatException(String message, Throwable e) {
        super(message, e);
    }
	
	@Override
	public String toString() {
		return super.getMessage();
	}
}
