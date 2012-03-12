package net.tapaal;

public class Preferences {

	   private static Preferences instance = null;
	   private static java.util.prefs.Preferences pref;
	   
	   protected Preferences() {
	      // Exists only to defeat instantiation.
		   pref = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
	   }
	   
	   public static Preferences getInstance() {
	      if(instance == null) {
	         instance = new Preferences();
	      }
	      return instance;
	   }
	
	   public String getVerifytaLocation() {
		   
		   String location = pref.get("verifytapn.location", "");
		   
		   return location;
		   
	   }
	   
	   public void setVerifytaLocation(String location) {
		   
		   pref.put("verifytapn.location", location);
		   
	   }
	
}
