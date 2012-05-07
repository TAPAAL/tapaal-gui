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
	   
	   public void setVerifytapnLocation(String location) {
		   final String key = "verifytapn.location";
		   
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
	   
	   /* Workspace */
	   //General
	   public void setShowToolTips(boolean show){
		   pref.putBoolean("showToolTips", show);
	   }
	   
	   public boolean getShowToolTips(){
		   return pref.getBoolean("showToolTips", true);
	   }
	   
	   //Queries
	   public void setAdvancedQueryView(boolean advanced){
		   pref.putBoolean("queryAdvanced", advanced);
	   }
	   
	   public boolean getAdvancedQueryView(){
		   return pref.getBoolean("queryAdvanced", false);
	   }
	   
	   //Editor
	   public void setShowComponents(boolean show){
		   pref.putBoolean("componentsPanel", show);
	   }
	   
	   public boolean getShowComponents(){
		   return pref.getBoolean("componentsPanel", true);
	   }
	   
	   public void setShowQueries(boolean show){
		   pref.putBoolean("QueriesPanel", show);
	   }
	   
	   public boolean getShowQueries(){
		   return pref.getBoolean("QueriesPanel", true);
	   }
	   
	   public void setShowConstants(boolean show){
		   pref.putBoolean("constantPanel", show);
	   }
	   
	   public boolean getShowConstants(){
		   return pref.getBoolean("constantPanel", true);
	   }
	   
	   //Simulator
	   public void setShowEnabledTrasitions(boolean show){
		   pref.putBoolean("enabledTransitionsPanel", show);
	   }
	   
	   public boolean getShowEnabledTransitions(){
		   return pref.getBoolean("enabledTransitionsPanel", true);
	   }
	   
	   //Drawing surface
	   public void setShowZeroInfIntervals(boolean show){
		   pref.putBoolean("showZeroInfIntervals", show);
	   }
	   
	   public boolean getShowZeroInfIntervals(){
		   return pref.getBoolean("showZeroInfIntervals", true);
	   }
}
