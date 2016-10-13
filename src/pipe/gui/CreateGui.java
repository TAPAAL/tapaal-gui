package pipe.gui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
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

	public static GuiFrame appGui;
	private static Animator animator;
	private static JTabbedPane appTab;
	private static ArrayList<TabContent> tabs = new ArrayList<TabContent>();
	
	private static boolean usingGTKFileBrowser = true;
	
	private static boolean showZeroToInfinityIntervals = true;
	private static boolean showTokenAge = true;

	public static String imgPath, userPath; // useful for stuff
	
	public static Integer MaximalNumberOfTokensAllowed = new Integer(999);

	
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
				//    appGui.exit();
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

		// make the initial dir for browsing be My Documents (win), ~ (*nix),
		// etc
		userPath = null;

		appGui = new GuiFrame(TAPAAL.getProgramName());
		
		if (appGui.isMac()){ 
			SpecialMacHandler.postprocess();
		}
		
		Grid.enableGrid();

		appTab = new JTabbedPane();
		
		//TODO
		/*
		appTab.addChangeListener(new ChangeListener() {
			int oldIndex = 0;
			@Override
			public void stateChanged(ChangeEvent arg0) {
				Split model = getTab(oldIndex).getModelRoot();
				getCurrentTab().setModelRoot(model);
				oldIndex = appTab.getSelectedIndex();
			}
		});
		*/
		

		animator = new Animator();

		appGui.setTab(); // sets Tab properties

		appGui.getContentPane().add(appTab);

		// appGui.createNewTabFromFile(null);

		appGui.setVisible(true);
		appGui.activateSelectAction();
		Verifyta.trySetup();
		VerifyTAPN.trySetup();
		VerifyTAPNDiscreteVerification.trySetup();
		VerifyPN.trySetup();

		checkForUpdate(false);
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

	public static int getFreeSpace(NetType netType) {
		tabs.add(new TabContent(netType));
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
		tab.switchToAnimationComponents(true);
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
	
	
	public static TransitionFireingComponent getTransitionFireingComponent() {
		TabContent tab = (tabs.get(appTab.getSelectedIndex()));
		return tab.getTransitionFireingComponent();

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

	public static void verifyQuery() {
		TabContent tab = getCurrentTab();
		if (tab.isQueryPossible()) {
			getCurrentTab().verifySelectedQuery();
		}
	}
}
