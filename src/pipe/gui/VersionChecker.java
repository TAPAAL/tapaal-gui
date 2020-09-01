package pipe.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

import net.tapaal.Preferences;

import net.tapaal.TAPAAL;

import dk.aau.cs.debug.Logger;
import net.tapaal.resourcemanager.ResourceManager;

public class VersionChecker {
	private static final String versionURL = "http://versioncheck.tapaal.net/version.txt";
	private static final int timeoutMs = 2500;
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

	public boolean checkForNewVersion(boolean forcecheck){
		//Disable the version check for DEV versions
		if (!TAPAAL.VERSION.equalsIgnoreCase("DEV") && url != null){
				getNewestVersion();
				boolean check;
				String ignoreversion = Preferences.getInstance().getLatestVersion();	
				if (ignoreversion==null || ignoreversion.isEmpty() || forcecheck || newestVersion==null || newestVersion.isEmpty() ) { check = true;}
				else { check = compareVersions(ignoreversion);}
				
				if(newestVersion != null && !newestVersion.isEmpty() && check){
					boolean result = compareVersions(TAPAAL.VERSION);
					if (forcecheck && !result) {
						JOptionPane.showMessageDialog(null, "There is no new version of TAPAAL available at the moment.", "No Update for " + TAPAAL.getProgramName(),
								JOptionPane.INFORMATION_MESSAGE, ResourceManager.appIcon());
					}
					return result;
				}
				else if (forcecheck){
					JOptionPane.showMessageDialog(null, "It is impossible to establish a connection to the server. Try again later.", "No Update for " + TAPAAL.getProgramName(),
							JOptionPane.INFORMATION_MESSAGE, ResourceManager.appIcon());	
				}
			}
		else if (forcecheck) {
			JOptionPane.showMessageDialog(null, "The development version of TAPAAL does not support update notification.", "No Update for " + TAPAAL.getProgramName(),
					JOptionPane.INFORMATION_MESSAGE, ResourceManager.appIcon());
		}
		return false;		
	}

	private boolean compareVersions(String versionString) {
		int[] currentVersionNumbers = null;
		int[] newestVersionNumbers = null;
		try{
			//currentVersionNumbers = getVersionNumbers(TAPAAL.VERSION);
			currentVersionNumbers = getVersionNumbers(versionString);	
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
			
			StringBuilder sb = new StringBuilder();
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
