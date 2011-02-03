package dk.aau.cs.util;

public class RequireException extends RuntimeException {
	private static final long serialVersionUID = -2454347432706081497L;

	public RequireException(String message) {
		super(message);
	}
}
