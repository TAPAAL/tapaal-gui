/**
 * 
 */
package pipe.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import dk.aau.cs.verification.BufferDrain; // TODO: MJ -- get rid of these references in pipe namespace to our work

import pipe.gui.widgets.RunningVerificationPanel;

public class RunUppaalVerification extends Thread{
	
	String verifyta=null;
	String verifytaOptions=null;
	File xmlfile=null, qfile=null;
	BufferedReader bufferedReaderStderr=null;
	BufferedReader bufferedReaderStdout=null;
	boolean error=true;
	RunningVerificationPanel dialog = null;
	long verificationtime=0;
	
	
	public RunUppaalVerification(String verifyta, String verifytaOptions, File xmlfile, File qfile, RunningVerificationPanel dialog) {
	
		this.verifyta = verifyta;
		this.verifytaOptions = verifytaOptions;
		
		this.xmlfile = xmlfile;
		this.qfile = qfile;
		this.dialog = dialog;
		
	}

	public void verifyStop() {
		
		child.destroy();
	}
	Process child=null;
	public void run() {

		try {
			// Execute a command with an argument that contains a space
			//String[] commands = new String[]{"/usr/bin/time", "-p", verifyta, verifytaOptions, xmlfile.getAbsolutePath(), qfile.getAbsolutePath()/*, " 2> ", tracefile.getAbsolutePath()*/};
			String[] commands;

			commands = new String[]{verifyta, verifytaOptions, xmlfile.getAbsolutePath(), qfile.getAbsolutePath()/*, " 2> ", tracefile.getAbsolutePath()*/};

			long startTimeMs=0, endTimeMs=0;
			
			startTimeMs = System.currentTimeMillis();
			child = Runtime.getRuntime().exec(commands);
			
			//Start drain for buffers
			
			BufferDrain stdout = new BufferDrain(new BufferedReader(new InputStreamReader(child.getInputStream())));
			BufferDrain stderr = new BufferDrain(new BufferedReader(new InputStreamReader(child.getErrorStream())));
			
			stdout.start();
			stderr.start();
			
			child.waitFor();
			endTimeMs  = System.currentTimeMillis();
			
			//Wait for the buffers to be drained3
			// XXX - kyrke - are thise subprocess killed right when 
			// mother process is killed?, or do we have to handle them better?
			stdout.join();
			stderr.join();

			bufferedReaderStdout = new BufferedReader(new StringReader(stdout.getString().toString()));
			bufferedReaderStderr = new BufferedReader(new StringReader(stderr.getString().toString()));

			/*for (String s : commands){
				System.out.print(s + " ");
			}*/ 
			
			verificationtime = endTimeMs-startTimeMs;
			
			dialog.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	


}