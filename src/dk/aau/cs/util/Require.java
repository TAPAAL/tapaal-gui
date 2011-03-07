package dk.aau.cs.util;

public class Require {
	public static void that(boolean condition, String message) {
		if (!condition)
			throw new RequireException(message);
	}
	
	public static void notImplemented(){
		throw new RuntimeException("NOT IMPLEMENTED");
	}
}
