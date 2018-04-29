package pipe.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import net.tapaal.TAPAAL;
import net.tapaal.Preferences;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.gui.handler.SpecialMacHandler;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.components.TransitionFireingComponent;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;


public class CreateGui {

	private static GuiFrame appGui;
	private static Animator animator;
	private static JTabbedPane appTab;
	private static ArrayList<TabContent> tabs = new ArrayList<TabContent>();
	
	private static boolean usingGTKFileBrowser = true;
	
	private static boolean showZeroToInfinityIntervals = true;
	private static boolean showTokenAge = true;

	public static String imgPath, userPath; // useful for stuff
	
	public static Integer MaximalNumberOfTokensAllowed = 999;

	public static void checkForUpdate(boolean forcecheck) {
		final VersionChecker versionChecker = new VersionChecker();
		if (versionChecker.checkForNewVersion(forcecheck))  {
			StringBuffer message = new StringBuffer("There is a new version of TAPAAL available at www.tapaal.net.");
			message.append("\n\nCurrent version: ");
			message.append(TAPAAL.VERSION);
			message.append("\nNew version: ");
			message.append(versionChecker.getNewVersionNumber());
			String changelog = versionChecker.getChangelog();
			if (!changelog.equals("")){
				message.append('\n');
				message.append('\n');
				message.append("Changelog:");
				message.append('\n');
				message.append(changelog);
			}
			JOptionPane optionPane = new JOptionPane();
		    optionPane.setMessage(message.toString());
		    optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
		    JButton updateButton, laterButton, ignoreButton;
		    updateButton = new JButton("Update now");
		    updateButton.setMnemonic(KeyEvent.VK_C);
			optionPane.add(updateButton);
            laterButton = new JButton("Update later"); 
            laterButton.setMnemonic(KeyEvent.VK_C);
            optionPane.add(laterButton);
            ignoreButton = new JButton("Ignore this update"); 
            laterButton.setMnemonic(KeyEvent.VK_C);
            optionPane.add(ignoreButton);
            
		    optionPane.setOptions(new Object[] {updateButton, laterButton, ignoreButton});
		   
		  
		    final JDialog dialog = optionPane.createDialog(null, "New Version of TAPAAL");
		    laterButton.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				Preferences.getInstance().setLatestVersion(null);
    				dialog.setVisible(false);
    				dialog.dispose ();
    			}
    		});
		    updateButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Preferences.getInstance().setLatestVersion(null);
					dialog.setVisible(false);
					dialog.dispose();
					pipe.gui.GuiFrame.showInBrowser("http://www.tapaal.net/download");
				}
			});
		    ignoreButton.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				Preferences.getInstance().setLatestVersion(versionChecker.getNewVersionNumber());
    				dialog.setVisible(false);
    				dialog.dispose ();
    			}
    		});
		    
		    updateButton.requestFocusInWindow();	
		    dialog.getRootPane().setDefaultButton(updateButton);
		    dialog.setVisible(true);
		}
	}
	

	public static void init() {
		
		imgPath = "resources/Images/";

		// make the initial dir for browsing be My Documents (win), ~ (*nix), etc
		userPath = null;

		appGui = new GuiFrame(TAPAAL.getProgramName());
		
		if (getAppGui().isMac()){
			try {
				SpecialMacHandler.postprocess();
			} catch (NoClassDefFoundError e) {
				//Failed loading special mac handler, ignore and run program without MacOS integration
			}	
		}
		
		Grid.enableGrid();

		appTab = new JTabbedPane();
		animator = new Animator();

		getAppGui().setTab(); // sets Tab properties

		getAppGui().getContentPane().add(appTab);

		getAppGui().setVisible(true);
		getAppGui().activateSelectAction();
		Verifyta.trySetup();
		VerifyTAPN.trySetup();
		VerifyTAPNDiscreteVerification.trySetup();
		VerifyPN.trySetup();

		checkForUpdate(false);
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

	public static int getFreeSpace(NetType netType) {
		tabs.add(new TabContent(netType));
		return tabs.size() - 1;
	}

	public static void removeTab(int index) {
		tabs.remove(index);
	}

	public static JTabbedPane getTabs() {
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


	/** Creates a new animationHistory text area, and returns a reference to it */

	public static AnimationHistoryComponent getAbstractAnimationPane() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getUntimedAnimationHistory();
	}

	public static AnimationController getAnimationController() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getAnimationController();
	}
		
	public static TransitionFireingComponent getTransitionFireingComponent() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getTransitionFireingComponent();
	}	

	public static void removeAbstractAnimationPane() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		tab.removeAbstractAnimationPane();
	}

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
	
	public static void toggleShowZeroToInfinityIntervals() {
		showZeroToInfinityIntervals = !showZeroToInfinityIntervals;
	}
	
	public static boolean showZeroToInfinityIntervals() {
		return showZeroToInfinityIntervals;
	}

	public static boolean showTokenAge(){
		return showTokenAge;
	}

	public static void toggleShowTokenAge(){
		showTokenAge = !showTokenAge;
	}


	//XXX Two Methodes to access same data (created after auto encapsulate)
	public static GuiFrame getApp() { // returns a reference to the application
		return getAppGui();
	}
	public static GuiFrame getAppGui() {
		return appGui;
	}
}
