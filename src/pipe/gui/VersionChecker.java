package pipe.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.verification.BufferDrain;

public class VersionChecker {
	private final static String versionURL = "http://www.tapaal.net/fileadmin/version.txt";
	private static final int timeoutMs = 6000;
	private URL url;
	private String newestVersion;

	public VersionChecker() {
		try {
			url = new URL(versionURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean checkForNewVersion(){
		//Disable the version check for DEV versions
		if (!Pipe.VERSION.equalsIgnoreCase("DEV")){
			if(url != null){

				getNewestVersion();

				if(newestVersion != null && !newestVersion.isEmpty()){
					return compareVersions();
				}

			}
			return false;
		}else {
			return false;
		}
	}

	private boolean compareVersions() {
		int[] currentVersionNumbers = null;
		int[] newestVersionNumbers = null;
		try{
			currentVersionNumbers = getVersionNumbers(Pipe.VERSION);
			newestVersionNumbers = getVersionNumbers(newestVersion);
		}catch(Exception e){
			return false;
		}
		if(currentVersionNumbers.length != 3 || newestVersionNumbers.length != 3) return false;
		
		if(newestVersionNumbers[0] > currentVersionNumbers[0]){
			return true;
		}else if(newestVersionNumbers[0] == currentVersionNumbers[0] && newestVersionNumbers[1] > currentVersionNumbers[1]){
			return true;
		}else if(newestVersionNumbers[0] == currentVersionNumbers[0] && newestVersionNumbers[1] == currentVersionNumbers[1] && newestVersionNumbers[2] > currentVersionNumbers[2]){
			return true;
		}
		
		return false;
	}

	private int[] getVersionNumbers(String version) {
		String delimiter = "\\.";
		String[] split = version.split(delimiter);
		return new int[]{ Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
	}

	private void getNewestVersion() {
		try {
			URLConnection conn = url.openConnection();

			conn.setConnectTimeout(timeoutMs);
			conn.setReadTimeout(timeoutMs);

			BufferedReader in = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));

			newestVersion = in.readLine();
			
			// Read the changelog part
			
			//Skip first line
			
			in.readLine();
			
			StringBuffer sb = new StringBuffer();
			String s;
			
		    while ((s = in.readLine()) != null) {
		      sb.append(s + '\n');
		    }
		    
		    changelog = sb.toString();
		    changelog = changelog.replace("Changelog:\n", "");

			in.close();
		} catch (Exception e) {
			Logger.log("Could not check for new version.");
		}
	}

	public String getNewVersionNumber() {
		return newestVersion;
	}
	String changelog = "";
	public String getChangelog(){
		return changelog;
	}

}
