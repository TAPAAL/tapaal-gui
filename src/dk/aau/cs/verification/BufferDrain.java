/**
 * 
 */
package dk.aau.cs.verification;

import java.io.BufferedReader;
import java.io.IOException;

public class BufferDrain extends Thread {

	BufferedReader drain = null;
	StringBuffer string = null;
	boolean running;

	public BufferDrain(BufferedReader drain) {
		this.drain = drain;
		string = new StringBuffer();
	}

	@Override
	public void run() {

		try {
			running = true;

			int c;
			while (running) {

				c = drain.read();

				if (c != -1) {
					string.append((char) c);
				} else {
					running = false;
				}
			}

		} catch (IOException e) {

			e.printStackTrace();
			running = false;
		}

	}

	public String getString() {
		return string.toString();
	}

}