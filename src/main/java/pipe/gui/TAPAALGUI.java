package pipe.gui;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.debug.Logger;
import net.tapaal.TAPAAL;
import net.tapaal.gui.GuiFrameController;
import net.tapaal.resourcemanager.ResourceManager;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.animation.Animator;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.petrinet.PetriNetTab;

import javax.swing.*;

public class TAPAALGUI {

	private final static GuiFrame appGui = new GuiFrame(TAPAAL.getProgramName());
    private final static GuiFrameController appGuiController = new GuiFrameController(appGui);

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

    //XXX Please avoid using the function below. It is only used for legacy code, or quick prototyping
    // instead pass down arguments using the constructor.

	@Deprecated
	public static PetriNetTab getCurrentTab() {
		return appGuiController.getTabs().get(appGui.getSelectedTabIndex());
	}

	/**
	 * @deprecated Use method getAnimator in GuiFrame
	 */
	@Deprecated
	public static Animator getAnimator() {
        var tab = getCurrentTab();
		if (tab == null) {
			return null;
		}
		return tab.getAnimator();
	}
	
	//XXX Two Methodes to access same data (created after auto encapsulate)
	//Used for setting parent in popups
    @Deprecated
	public static JFrame getApp() { // returns a reference to the application
		return getAppGui();
	}

    //Used for aceessing the GuiFrame
    @Deprecated
	public static GuiFrame getAppGui() {
		return appGui;
	}

	//XXX The following function should properly not be used and is only used while refactoring, but is better
	// that the chained access via guiFrame, App or drawingsurface now marked with deprecation.
    @Deprecated
	public static PetriNetTab openNewTabFromStream(InputStream file, String name) throws Exception {
		PetriNetTab tab = PetriNetTab.createNewTabFromInputStream(file, name);
		appGuiController.openTab(tab);
		return tab;
	}
    @Deprecated
	public static PetriNetTab openNewTabFromStream(PetriNetTab tab) {
		appGuiController.openTab(tab);
		return tab;
	}

    @Deprecated
    public static GuiFrameController getAppGuiController() {
        return appGuiController;
    }

    @Deprecated
    public static boolean useExtendedBounds = false;

}
