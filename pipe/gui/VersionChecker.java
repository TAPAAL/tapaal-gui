package pipe.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import dk.aau.cs.debug.Logger;

public class VersionChecker {
	private final static String versionURL = "http://www.tapaal.net/fileadmin/version.txt";
	private static final int timeoutMs = 6000;
	private URL url;
	private String newestVersion;

	public VersionChecker(){
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
		String delimiter = "\\.";
		String[] currentVersionNumbers = Pipe.VERSION.split(delimiter);
		String[] newestVersionNumbers = newestVersion.split(delimiter);

		int currentLength = currentVersionNumbers.length;
		int newestLength = newestVersionNumbers.length;
		
		int length = currentLength > newestLength ? 
				currentLength : newestLength;
		
		for (int i = 0; i < length; i++) {
				int current = i >= currentLength ? 0 : Integer.parseInt(currentVersionNumbers[i]);
				int newest = 0;
			try{ // in case a version like a.0.3 is given, we catch this and return false
				newest = i >= newestLength ? 0 : Integer.parseInt(newestVersionNumbers[i]);
			}catch(NumberFormatException e){
				return false;
			}
			
			if(newest > current) return true;
		}

		return false;
	}

	private void getNewestVersion() {
		try{
			URLConnection conn = url.openConnection();

			conn.setConnectTimeout(timeoutMs);
			conn.setReadTimeout(timeoutMs);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));

			newestVersion = in.readLine();
			in.close();
		}
		catch(Exception e){
			Logger.log("Could not check for new version.");
		}
	}

	public String getNewVersionNumber() {
		return newestVersion;
	}

}
