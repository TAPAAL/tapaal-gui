package dk.aau.cs.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecutabilityChecker {
	public static void check(String path) throws IllegalArgumentException{
		int rcode = -1;
		try {
			Process p = Runtime.getRuntime().exec(new String[] { path, "-v" });
			
			// Because some native platforms only provide limited buffer size 
			// for standard input and output streams, failure to promptly write 
			// the input stream or read the output stream of the subprocess may 
			// cause the subprocess to block, and even deadlock.
			
			InputStream stream = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);
            while (br.readLine() != null){}
            
            stream = p.getErrorStream();
            isr = new InputStreamReader(stream);
            br = new BufferedReader(isr);
            while (br.readLine() != null){}
            
			rcode = p.waitFor();	// Requires binary to accept -v flag
		} catch (Exception e) {
			// Do nothing
		}
		// Detect executable issues
		switch(rcode){
		case 0:
			break;
		case 126:
			throw new IllegalArgumentException("The selected file is not executable on this system.");
		default:
			throw new IllegalArgumentException("The selected file is not executable or not compatible with your system (return value "+rcode+").");
		}
	}
}