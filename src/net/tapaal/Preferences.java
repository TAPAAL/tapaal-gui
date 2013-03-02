package net.tapaal;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.prefs.BackingStoreException;

import org.jdesktop.swingx.MultiSplitLayout.Split;

import sun.security.provider.PolicyParser.GrantEntry;

import dk.aau.cs.model.tapn.simulation.DelayMode;
import dk.aau.cs.model.tapn.simulation.ManualDelayMode;
import dk.aau.cs.model.tapn.simulation.RandomDelayMode;
import dk.aau.cs.model.tapn.simulation.ShortestDelayMode;

public class Preferences {
	private static Preferences instance = null;
	private static java.util.prefs.Preferences pref;

	protected Preferences() {
		// Exists only to defeat instantiation.
		pref = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
		// Set subtree to version specific node
		pref = pref.node(pref.absolutePath() + TAPAAL.VERSION);
	}

	public void clear(){
		try {
			pref.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
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

	public String getVerifydtapnLocation() {
		return pref.get("dverifytapn.location", "");
	}

	public void setVerifydtapnLocation(String location) {
		final String key = "dverifytapn.location";

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

	public void setWindowSize(Dimension size) {
		try{
			saveSerilizableObject("appSize", size);
		} catch (Exception e){
			System.err.println("Something went wrong - couldn't save the app size");
		}
	}

	public Dimension getWindowSize(){
		try{
			return (Dimension)getSerilizableObject("appSize");
		} catch (Exception e){
			System.err.println("Something went wrong - couldn't load the appSize");
			return null;
		}
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

	public void setEditorModelRoot(Split modelRoot){
		try{
			saveSerilizableObject("editorModelRoot", modelRoot);
		} catch (IOException e){
			System.err.println("Something went wrong couldn't save editorResizings");
		}
	}

	public Split getEditorModelRoot(){
		Split result = null;
		try{
			result =  (Split)getSerilizableObject("editorModelRoot");
		} catch (Exception e){
			System.err.println("Something went wrong didn't load editorResizings");
		}
		return result;
	}

	//Simulator
	public void setShowEnabledTrasitions(boolean show){
		pref.putBoolean("enabledTransitionsPanel", show);
	}

	public boolean getShowEnabledTransitions(){
		return pref.getBoolean("enabledTransitionsPanel", true);
	}

	public void setShowBlueTransitions(boolean show){
		pref.putBoolean("enabledBlueTransitions", show);
	}

	public boolean getShowBlueTransitions(){
		return pref.getBoolean("enabledBlueTransitions", true);
	}
	
	public void setBlueTransitionGranularity(BigDecimal granularity){
		pref.put("blueTransitionGranularity", granularity.toString());
	}
	
	public BigDecimal getBlueTransitionGranularity(){
		return new BigDecimal(pref.get("blueTransitionGranularity", "0.1"));
		
	}
	
	public void setBlueTransitionDelayMode(DelayMode delayMode){
		if(delayMode instanceof ManualDelayMode){
			pref.putInt("blueTransitionDelayMode", 0);
		} else if(delayMode instanceof RandomDelayMode){
			pref.putInt("blueTransitionDelayMode", 1);
		} else if(delayMode instanceof ShortestDelayMode){
			pref.putInt("blueTransitionDelayMode", 2);
		} else {
			throw new IllegalArgumentException("Can only save ManualDelayMode, RandomDelayMode and ShortestDelayMode");
		}
	}
	
	public DelayMode getBlueTransitionDelayMode(){
		switch (pref.getInt("blueTransitionDelayMode", 2)) {
		case 0: return ManualDelayMode.getInstance();
		case 1: return RandomDelayMode.getInstance();
		case 2: return ShortestDelayMode.getInstance();
		default:
			throw new RuntimeException();
		}
	}
	
	public void setBlueTransitionIsRandomTransition(boolean isRandomTransition){
		pref.putBoolean("blueTransitionRandomTransition", isRandomTransition);
	}
	
	public boolean getBlueTransitionIsRandomTransition() {
		return pref.getBoolean("blueTransitionRandomTransition", false);
	}

	public void setSimulatorModelRoot(Split model){
		if(model == null) return;
		try{
			saveSerilizableObject("simulatorModelRoot", model);
		} catch (IOException e){
			System.err.println("Something went wrong couldn't save simulatorResizings");
		}
	}

	public Split getSimulatorModelRoot(){
		Split result = null;
		try{
			result =  (Split)getSerilizableObject("simulatorModelRoot");
		} catch (Exception e){
			System.err.println("Something went wrong didn't load simulatorResizings");
		}
		return result;
	}

	//Drawing surface
	public void setShowZeroInfIntervals(boolean show){
		pref.putBoolean("showZeroInfIntervals", show);
	}

	public boolean getShowZeroInfIntervals(){
		return pref.getBoolean("showZeroInfIntervals", true);
	}


	//Helper functions
	private void saveSerilizableObject(String key, Serializable o) throws IOException{
		if(o == null){
			throw new NullPointerException();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(out);

		//Serialize object
		oos.writeObject(o);
		oos.close();

		pref.putByteArray(key, out.toByteArray());
	}

	private Object getSerilizableObject(String key) throws ClassNotFoundException, IOException{
		byte[] model = pref.getByteArray(key, null);
		if(model == null){
			return null;
		}
		Object object = null;

		ByteArrayInputStream in = new ByteArrayInputStream(model);
		ObjectInputStream ois = new ObjectInputStream(in);

		//Read in the model
		object = ois.readObject();
		ois.close();

		return object;
	}
}
