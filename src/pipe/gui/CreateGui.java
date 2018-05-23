package pipe.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import com.sun.jna.Platform;
import net.tapaal.TAPAAL;
import net.tapaal.Preferences;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.gui.handler.SpecialMacHandler;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;


public class CreateGui {

	private static GuiFrame appGui;
	private static ArrayList<TabContent> tabs = new ArrayList<TabContent>();
	
	public static final String imgPath = "resources/Images/";

	//Used for latest open dialog path
	//Default value null makes the open dialog open default folder, For Windows, My Documents, For *nix  ~ , etc
	public static String userPath = null;

	public static void init() {
		appGui = new GuiFrame(TAPAAL.getProgramName());

		if (Platform.isMac()){
			SpecialMacHandler.postprocess();
		}

		appGui.setVisible(true);
		appGui.checkForUpdate(false);
	}

	public static DataLayer getModel() {
		return getModel(appGui.getSelectedTabIndex());
	}

	public static DataLayer getModel(int index) {
		if (index < 0) {
			return null;
		}

		TabContent tab = (tabs.get(index));
		return tab.getModel();
	}

	public static DrawingSurfaceImpl getDrawingSurface() {
		return getDrawingSurface(appGui.getSelectedTabIndex());
	}

	public static DrawingSurfaceImpl getDrawingSurface(int index) {

		if (index < 0) {
			return null;
		}

		TabContent tab = (tabs.get(index));

		//XXX: 2018-04-26//kyrke The following code should never be called, a TAB always has a driwingSurface,
		// if not seems wired to just create it.
		// Code left in place for history, in case we someday experience problems with this.
		// If no problems have been observed for some time, please remove the code and comment.
//		while (tab.drawingSurface() == null) {
//
//			try {
//				tab.setDrawingSurface(new DrawingSurfaceImpl(tab.getModel(),
//						tab));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}

		return tab.drawingSurface();
	}


	public static int getFreeSpace(NetType netType) {
		tabs.add(new TabContent(netType));
		return tabs.size() - 1;
	}

	public static void addTab (TabContent tab ) {
		tabs.add(tab);
	}

	public static void removeTab(int index) {
		tabs.remove(index);
	}

	public static TabContent getTab(int index) {
		return tabs.get(index);
	}

	public static TabContent getCurrentTab() {
		return getTab(appGui.getSelectedTabIndex());
	}

	/**
	 * @deprecated Use method getAnimator in GuiFrame
	 * @return
	 */
	@Deprecated
	public static Animator getAnimator() {
		return appGui.getAnimator();
	}
	
	//XXX Two Methodes to access same data (created after auto encapsulate)
	public static GuiFrame getApp() { // returns a reference to the application
		return getAppGui();
	}
	public static GuiFrame getAppGui() {
		return appGui;
	}
}
