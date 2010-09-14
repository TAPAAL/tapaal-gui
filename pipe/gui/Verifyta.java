package pipe.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Verifyta implements ModelChecker {
	private static final String NEED_TO_LOCATE_VERIFYTA_MSG = "TAPAAL needs to know the location of the file verifyta.\n\n"+
				"Verifyta is a part of the UPPAAL distribution and it is\n" +
				"normally located in uppaal/bin-Linux or uppaal/bin-Win32,\n" +
				"depending on the operating system used.";
	
	private String verifytapath="";
	private FileFinder fileFinder;
	private Messenger messenger;

	public Verifyta(FileFinder fileFinder, Messenger messenger){
		this.fileFinder = fileFinder;
		this.messenger = messenger;
	}

	public boolean setup(){ // TODO: Should maybe eliminate boolean return value from here?
		if(isNotSetup()){
			boolean success = setVerifytaFromEnvironmentVariable();
			if(!success){
				messenger.displayInfoMessage(NEED_TO_LOCATE_VERIFYTA_MSG, "Locate UPPAAL Verifyta");

				try {
					File file = fileFinder.ShowFileBrowserDialog("Uppaal Verifyta","");
					verifytapath=file.getAbsolutePath();
				} catch (Exception e) {
					messenger.displayErrorMessage(
							"There were errors performing the requested action:\n" + e,
					"Error");
				}
			}
		}

		return isNotSetup() ? false : true;
	}
	
	public String getPath(){
		return verifytapath; // TODO: MJ -- delete me
	}

	public String getVersion(){
		String result = null;

		if (!isNotSetup()){
			String[] commands;
			commands = new String[]{verifytapath, "-v"};

			InputStream stream = null;
			try {
				Process child = Runtime.getRuntime().exec(commands);
				child.waitFor();
				stream = child.getInputStream();
			} catch (IOException e){
			} catch (InterruptedException e) {
			}

			if(stream != null){
				result = readVersionNumberFrom(stream);
			}
		}

		return result;
	}

	public boolean isCorrectVersion(){
		if (isNotSetup()){
			messenger.displayErrorMessage("No verifyta specified: The verification is cancelled",
			"Verification Error");
			return false;
		}

		String versionAsString = getVersion();

		if(versionAsString == null){
			messenger.displayErrorMessage(
					"The program can not be verified as being verifyta.\n" +
					"The verifyta path will be reset. Please try again, " + 
					"to manually set the verifyta path.",
			"Verifyta Error");
			return false;
		}else{		
			int version = Integer.parseInt(versionAsString);

			if (version < Pipe.verifytaMinRev){
				messenger.displayErrorMessage(
						"The specified version of the file verifyta is too old.\n\n" +
						"Get the latest development version of UPPAAL from \n" +
						"www.uppaal.com.",
				"Verifyta Error");
				return false;
			}
		}

		return true;
	}

	private boolean isNotSetup() {
		return verifytapath == null || verifytapath.equals("");
	}

	private String readVersionNumberFrom(InputStream stream) {
		String result = null;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

		String versioninfo = null;
		try {
			versioninfo = bufferedReader.readLine();
		} catch (IOException e) {
			result = null;
		}

		Pattern pattern = Pattern.compile("\\(rev. (\\d+)\\)");
		Matcher m =  pattern.matcher(versioninfo);
		m.find();
		result =  m.group(1);
		return result;
	}

	private boolean setVerifytaFromEnvironmentVariable() {
		String verifyta = System.getenv("verifyta");
		if(verifyta != null && !verifyta.equals("")){
			verifytapath = verifyta;
			return true;
		}
		return false;
	}
}
