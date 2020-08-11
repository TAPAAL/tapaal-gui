package pipe.gui;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.sun.jna.Platform;
import dk.aau.cs.debug.Logger;
import net.tapaal.TAPAAL;
import net.tapaal.resourcemanager.ResourceManager;
import pipe.dataLayer.DataLayer;
import pipe.gui.canvas.DrawingSurfaceImpl;
import dk.aau.cs.gui.TabContent;

public class CreateGui {

	private final static GuiFrame appGui = new GuiFrame(TAPAAL.getProgramName());
    private final static GuiFrameController appGuiController = new GuiFrameController(appGui);

	private static final ArrayList<TabContent> tabs = new ArrayList<TabContent>();

	public static void init() {

	    try {
            Desktop.getDesktop().setAboutHandler(e -> appGuiController.showAbout());
        } catch (SecurityException | UnsupportedOperationException ignored) {
            Logger.log("Failed to set native about handler");
        }

	    try {
	        Desktop.getDesktop().setQuitHandler(
	            (e, response) -> {
	                appGuiController.exit();
	                response.cancelQuit(); //If we get here the request was canceled.
	            }
	        );

        } catch (SecurityException | UnsupportedOperationException ignored) {
	        Logger.log("Failed to set native quit handler");
        }

        try {
            Image appImage = ResourceManager.getIcon("icon.png").getImage();
            Taskbar.getTaskbar().setIconImage(appImage);

        } catch (SecurityException | UnsupportedOperationException ignored) {
            Logger.log("Failed to set DockIcon");
        }

		appGui.setVisible(true);
		appGuiController.checkForUpdate(false);
	}

	@Deprecated
	public static DataLayer getModel() {
        return getModel(appGui.getSelectedTabIndex());
	}

	@Deprecated
	public static DataLayer getModel(int index) {
		if (index < 0) {
			return null;
		}

		TabContent tab = (tabs.get(index));
		return tab.getModel();
	}

	@Deprecated
	public static DrawingSurfaceImpl getDrawingSurface() {
		return getDrawingSurface(appGui.getSelectedTabIndex());
	}

	@Deprecated
	public static DrawingSurfaceImpl getDrawingSurface(int index) {

		if (index < 0) {
			return null;
		}

		TabContent tab = (tabs.get(index));

		return tab.drawingSurface();
	}

	@Deprecated
	public static void addTab (TabContent tab ) {
		tabs.add(tab);
	}

	@Deprecated
	public static void removeTab(int index) {
		tabs.remove(index);
	}

	@Deprecated
	public static void removeTab(TabContent tab) {
		tabs.remove(tab);
	}

	@Deprecated
	public static TabContent getTab(int index) {
		if (index < 0) {
			return null;
		}
		return tabs.get(index);
	}

	@Deprecated
	public static List<TabContent> getTabs() {
		return tabs;
	}

	@Deprecated
	public static TabContent getCurrentTab() {
		return getTab(appGui.getSelectedTabIndex());
	}

	/**
	 * @deprecated Use method getAnimator in GuiFrame
	 */
	@Deprecated
	public static Animator getAnimator() {
		if (getCurrentTab() == null) {
			return null;
		}
		return getCurrentTab().getAnimator();
	}
	
	//XXX Two Methodes to access same data (created after auto encapsulate)
	@Deprecated
	public static GuiFrame getApp() { // returns a reference to the application
		return getAppGui();
	}
	@Deprecated
	public static GuiFrame getAppGui() {
		return appGui;
	}

	//XXX The following function should properly not be used and is only used while refactoring, but is better
	// that the chained access via guiFrame, App or drawingsurface now marked with deprecation.
	public static TabContent openNewTabFromStream(InputStream file, String name) throws Exception {
		TabContent tab = TabContent.createNewTabFromInputStream(file, name);
		appGuiController.openTab(tab);
		return tab;
	}
	public static TabContent openNewTabFromStream(TabContent tab) {
		appGuiController.openTab(tab);
		return tab;
	}

    @Deprecated
    public static GuiFrameController getAppGuiController() {
        return appGuiController;
    }


    public static boolean useExtendedBounds = false;

	//XXX Moved from guiframe to static access, while refactoring.
	@Deprecated
    public static Pipe.ElementType guiMode;

}
