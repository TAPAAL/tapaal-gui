package pipe.gui;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import pipe.dataLayer.DataLayer;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.verification.UPPAAL.Verifyta;


public class CreateGui {

	private static final double DIVIDER_LOCATION = 0.5;
	public static GuiFrame appGui;
	private static Animator animator;
	private static JTabbedPane appTab;
	private static ArrayList<TabContent> tabs = new ArrayList<TabContent>();

	public static String imgPath, userPath; // useful for stuff
	
	public static void init() {
		imgPath = "Images" + System.getProperty("file.separator");

		// make the initial dir for browsing be My Documents (win), ~ (*nix), etc
		userPath = null; 


		appGui = new GuiFrame(Pipe.TOOL + " " + Pipe.VERSION);



		Grid.enableGrid();

		appTab = new JTabbedPane();

		animator = new Animator();
		appGui.setTab();   // sets Tab properties


		appGui.getContentPane().add(appTab);

		//appGui.createNewTabFromFile(null);

		appGui.setVisible(true);
		appGui.init();
		Verifyta.trySetupFromEnvironmentVariable();

		VersionChecker versionChecker = new VersionChecker();
		if(versionChecker.checkForNewVersion()){
			StringBuffer message = new StringBuffer(
			"There is a new version of TAPAAL available at www.tapaal.net.");
			message.append("\n\nCurrent version: ");
			message.append(Pipe.VERSION);
			message.append("\nNew version: ");
			message.append(versionChecker.getNewVersionNumber());			

			JOptionPane.showMessageDialog(appGui, 
					message .toString(),
					"New version available!",
					JOptionPane.INFORMATION_MESSAGE);			
		}
	}


	public static GuiFrame getApp() {  //returns a reference to the application
		return appGui;
	}


	public static DataLayer getModel() {
		return getModel(appTab.getSelectedIndex());
	}

	public static DataLayer getModel(int index) {
		if (index < 0) {
			return null;
		}

		TabContent tab = (tabs.get(index));
		if (tab.getModel() == null) {
			tab.setModel(new DataLayer());
		}
		return tab.getModel();
	}


	public static DrawingSurfaceImpl getDrawingSurface() {
		return getDrawingSurface(appTab.getSelectedIndex());
	}

	public static DrawingSurfaceImpl getDrawingSurface(int index) {
		if (index < 0) {
			return null;
		}

		TabContent tab = (tabs.get(index));
		while (tab.getDrawingSurface() == null) {
			try {
				tab.setDrawingSurface(new DrawingSurfaceImpl(tab.getModel()));
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return tab.getDrawingSurface();
	}


	public static DrawingSurfaceImpl getView() {
		return getDrawingSurface(appTab.getSelectedIndex());
	}


	public static File getFile() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getFile();
	}


	public static void setFile(File modelfile, int fileNo) {
		if (fileNo >= tabs.size()) {
			return;
		}
		TabContent tab = (tabs.get(fileNo));
		tab.setFile(modelfile);
	}


	public static int getFreeSpace() {
		tabs.add(new TabContent());
		return tabs.size() - 1;
	}


	public static void removeTab(int index) {
		tabs.remove(index);
	}


	public static JTabbedPane getTab() {
		return appTab;
	}
	
	public static TabContent getTab(int index) {
		return tabs.get(index);
	}

	public static Animator getAnimator() {
		return animator;
	}

	/** returns the current dataLayer object - 
	 *  used to get a reference to pass to the modules */
	public static DataLayer currentPNMLData() {
		if (appTab.getSelectedIndex() < 0) {
			return null;
		}
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getModel();
	}

	public static void setupModelForSimulation(){
		if (appTab.getSelectedIndex() >= 0) {
			TabContent tab = (tabs.get(appTab.getSelectedIndex()));
			tab.setupModelForSimulation();
		}
	}


	public static void restoreModelForEditing(){
		if (appTab.getSelectedIndex() >= 0) {
			TabContent tab = (tabs.get(appTab.getSelectedIndex()));
			tab.restoreModelForEditing();
		}
	}


	/** Creates a new animationHistory text area, and returns a reference to it*/
	public static void addAnimationHistory() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.addAnimationHistory();
	}

	public static AnimationHistory getAbstractAnimationPane(){
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getAbstractAnimationPane();
	}

	public static void addAbstractAnimationPane() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.addAbstractAnimationPane();
	}
	
	public static AnimationController getAnimationController()
	{
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getAnimationController();
	}

	public static void removeAbstractAnimationPane() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.removeAbstractAnimationPane();
	}

	public static void addAnimationController() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.addAnimationController();
	}

	public static void removeAnimationHistory() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.removeAnimationHistory();
	}
	
	public static void removeAnimationController() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.removeAnimationController();
	}


	public static AnimationHistory getAnimationHistory() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getAnimationHistory();
	}

	public static void createLeftPane(){
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.createLeftPane();
	}

	public static void updateConstantsList(){
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));	
		tab.updateConstantsList();
	}

	public static void updateLeftPanel() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.updateLeftPanel();
	}


	public static void undoGetFreeSpace() {
		tabs.remove(tabs.size()-1);
	}

}
