package dk.aau.cs.util;

public class ExecutabilityChecker {
	public static void check(String path) throws IllegalArgumentException{
		int rcode = -1;
		try {
			rcode = Runtime.getRuntime().exec(path + " -v").waitFor();	// Requires binary to accept -v flag
		} catch (Exception e) {
			// Do nothing
		}
		// Detect executable issues
		switch(rcode){
		case -1:
			break;
		case 0:
			break;
		case 126:
			throw new IllegalArgumentException("The selected file is not executable on this system.");
		default:
			throw new IllegalArgumentException("The selected file is not executable or not compatible with your system (return value "+rcode+").");
		}
	}
}
