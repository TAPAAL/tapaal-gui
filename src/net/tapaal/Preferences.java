package net.tapaal;

public class Preferences {

	   private static Preferences instance = null;
	   private static java.util.prefs.Preferences pref;
	   
	   protected Preferences() {
	      // Exists only to defeat instantiation.
		   pref = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
		   // Set subtree to version specific node
		   pref = pref.node(pref.absolutePath() + TAPAAL.VERSION);
	   }
	   
	   public static Preferences getInstance() {
	      if(instance == null) {
	         instance = new Preferences();
	      }
	      return instance;
	   }
	
	   public String getVerifytaLocation() {
		   	   
		   return pref.get("verifyta.location", "");
	   }
	   
	   public void setVerifytaLocation(String location) {
		   final String key = "verifyta.location";
		   
		   if (location == null || location.equals("")){
			   pref.remove(key);
		   } else {
			   pref.put("verifyta.location", location);
		   }
		   
		   
	   }
	   
	   public String getVerifytapnLocation() {
		   return pref.get("verifytapn.location", "");
	   }
	   
	   public String getVerifydtapnLocation() {
		   return pref.get("dverifytapn.location", "");
	   }
	   
	   public String getVerifydtapnLCLocation() {
		   return pref.get("dverifytapnLC.location", "");
	   }
	   public String getVerifydtapnWALocation() {
		   return pref.get("dverifytapnWA.location", "");
	   }
	   
	   public void setVerifytapnLocation(String location) {
		   final String key = "verifytapn.location";
		   
		   if (location == null || location.equals("")){
			   pref.remove(key);
		   }else {
			   pref.put(key, location);   
		   }
	   }
	   
	   public void setVerifydtapnLocation(String location) {
		   final String key = "dverifytapn.location";
		   
		   if (location == null || location.equals("")){
			   pref.remove(key);
		   }else {
			   pref.put(key, location);   
		   }
	   }
	   
	   public void setVerifydtapnLCLocation(String location) {
		   final String key = "dverifytapnLC.location";
		   
		   if (location == null || location.equals("")){
			   pref.remove(key);
		   }else {
			   pref.put(key, location);   
		   }
	   }
	   
	   public void setVerifydtapnWALocation(String location) {
		   final String key = "dverifytapnWA.location";
		   
		   if (location == null || location.equals("")){
			   pref.remove(key);
		   }else {
			   pref.put(key, location);   
		   }
	   }
	
	   
	   
	   public String getLatestVersion() {
		   return pref.get("tapaal.latestVersion", "");
	   }
	   
	   public void setLatestVersion(String version) {
		   final String key = "tapaal.latestVersion";
		   
		   if (version == null || version.equals("")){
			   pref.remove(key);
		   }else {
			   pref.put(key, version);   
		   }
	   }
	   
}
