package dk.aau.cs.verification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.util.MemoryMonitor;

public class ProcessRunner {

	private String file;
	private String arguments;
	private long runningTime = 0;
	private Process process;
	private BufferedReader bufferedReaderStdout;
	private BufferedReader bufferedReaderStderr;

	private boolean error = false;

	public ProcessRunner(String file, String arguments) {
		// this.setName("verification thread");

		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("file");
		}

		this.file = file;
		this.arguments = arguments;
	}

	public long getRunningTime() {
		return runningTime;
	}

	public BufferedReader standardOutput() {
		return bufferedReaderStdout;
	}

	public BufferedReader errorOutput() {
		return bufferedReaderStderr;
	}

	public boolean error() {
		return error;
	}

	public void kill() {
		if (process != null) {
			process.destroy();
		}
	}

	public void run() {
		long startTimeMs = 0, endTimeMs = 0;
		startTimeMs = System.currentTimeMillis();
		
		try {
			Logger.log("Running: "+ file + " " + arguments);
			process = Runtime.getRuntime().exec(getCmdArray());
			MemoryMonitor.attach(process);
		} catch (IOException e1) {
			error = true;
			return;
		}

		BufferDrain stdout = new BufferDrain(new BufferedReader(
				new InputStreamReader(process.getInputStream())));
		BufferDrain stderr = new BufferDrain(new BufferedReader(
				new InputStreamReader(process.getErrorStream())));

		stdout.start();
		stderr.start();

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			error = true;
			return;
		}
		endTimeMs = System.currentTimeMillis();

		try {
			stdout.join();
			stderr.join();
		} catch (InterruptedException e) {
			error = true;
			return;
		}

		bufferedReaderStdout = new BufferedReader(new StringReader(stdout
				.getString()));
		bufferedReaderStderr = new BufferedReader(new StringReader(stderr
				.getString()));

		runningTime = endTimeMs - startTimeMs;
	}
	
	// Return array containing executable path and arguments
	private String[] getCmdArray(){
		String[] argSplit = arguments.split("\\s+");		
		String[] cmdArray = new String[1 + argSplit.length];
		cmdArray[0] = file;
		System.arraycopy(argSplit, 0, cmdArray, 1, argSplit.length);
		
		return cmdArray;
	}
}
