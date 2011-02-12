package pipe.gui;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import pipe.dataLayer.DataLayer;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.verification.UPPAAL.Verifyta;

public class CreateGui {

	public static GuiFrame appGui;
	private static Animator animator;
	private static JTabbedPane appTab;
	private static ArrayList<TabContent> tabs = new ArrayList<TabContent>();
	
	private static boolean usingGTKFileBrowser = true;

	public static String imgPath, userPath; // useful for stuff

	public static void init() {
		imgPath = "resources/Images" + System.getProperty("file.separator");

		// make the initial dir for browsing be My Documents (win), ~ (*nix),
		// etc
		userPath = null;

		appGui = new GuiFrame(Pipe.TOOL + " " + Pipe.VERSION);

		Grid.enableGrid();

		appTab = new JTabbedPane();

		animator = new Animator();

		appGui.setTab(); // sets Tab properties

		appGui.getContentPane().add(appTab);

		// appGui.createNewTabFromFile(null);

		appGui.setVisible(true);
		appGui.init();
		Verifyta.trySetupFromEnvironmentVariable();

		VersionChecker versionChecker = new VersionChecker();
		if (versionChecker.checkForNewVersion()) {
			StringBuffer message = new StringBuffer("There is a new version of TAPAAL available at www.tapaal.net.");
			message.append("\n\nCurrent version: ");
			message.append(Pipe.VERSION);
			message.append("\nNew version: ");
			message.append(versionChecker.getNewVersionNumber());

			JOptionPane.showMessageDialog(appGui, message.toString(),
					"New version available!", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public static GuiFrame getApp() { // returns a reference to the application
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
		while (tab.drawingSurface() == null) {

			try {
				tab.setDrawingSurface(new DrawingSurfaceImpl(tab.getModel(),
						tab));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tab.drawingSurface();
	}

	public static DrawingSurfaceImpl getView() {
		return getDrawingSurface(appTab.getSelectedIndex());
	}

	public static File getFile() {
		return getFile(appTab.getSelectedIndex());
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

	public static TabContent getCurrentTab() {
		return tabs.get(appTab.getSelectedIndex());
	}

	public static Animator getAnimator() {
		return animator;
	}

	/**
	 * returns the current dataLayer object - used to get a reference to pass to
	 * the modules
	 */
	public static DataLayer currentPNMLData() {
		if (appTab.getSelectedIndex() < 0) {
			return null;
		}

		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getModel();
	}

	/** Creates a new animationHistory text area, and returns a reference to it */
	public static void switchToAnimationComponents() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.switchToAnimationComponents();
	}

	public static void switchToEditorComponents() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.switchToEditorComponents();
	}

	public static AnimationHistoryComponent getAbstractAnimationPane() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getUntimedAnimationHistory();

	}

	public static void addAbstractAnimationPane() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.addAbstractAnimationPane();
	}

	public static AnimationController getAnimationController() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getAnimationController();

	}

	public static void removeAbstractAnimationPane() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.removeAbstractAnimationPane();
	}

	// public static void addAnimationController() {
	// TabContent tab = (tabs.get(appTab.getSelectedIndex()));
	// //tab.addAnimationController();
	// }

	// public static void removeAnimationHistory() {
	// TabContent tab = (tabs.get(appTab.getSelectedIndex()));
	// tab.removeAnimationHistory();
	// }
	//	
	// public static void removeAnimationController() {
	// TabContent tab = (tabs.get(appTab.getSelectedIndex()));
	// tab.removeAnimationController();
	// }

	public static AnimationHistoryComponent getAnimationHistory() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getAnimationHistory();
	}

	public static void updateConstantsList() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.updateConstantsList();
	}

	public static void undoGetFreeSpace() {
		tabs.remove(tabs.size() - 1);
	}

	public static File getFile(int index) {
		TabContent tab = tabs.get(index);
		return tab.getFile();
	}

	public static void setUsingGTKFileBrowser(boolean useGTKFileBrowser) {
		usingGTKFileBrowser = useGTKFileBrowser;
	}
	
	public static boolean usingGTKFileBrowser() {
		return usingGTKFileBrowser;
	}
}
