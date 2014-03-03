package pipe.gui.handler;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import net.tapaal.TAPAAL;
import pipe.gui.CreateGui;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.FullScreenUtilities;

import dk.aau.cs.debug.Logger;

/**
 * See http://developer.apple.com/library/mac/documentation/Java/Reference/JavaSE6_AppleExtensionsRef/api/com/apple/eawt/package-summary.html
 * @author kyrke
 *
 */
public class SpecialMacHandler implements AboutHandler, QuitHandler  /*, OpenFilesHandler, PreferencesHandler, PrintFilesHandler,  , AppReOpenedListener*/ {

	
	public SpecialMacHandler() {
		//Registrate the handlers
		Application app = Application.getApplication();
		app.setAboutHandler(this);
	    app.setQuitHandler(this);
	   
	    try {
	    	Image appImage;
			appImage = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(
					CreateGui.imgPath + "icon.png"));
			app.setDockIconImage(appImage);
		} catch (MalformedURLException e) {
			Logger.log("Error loading Image");
		} catch (IOException e) {
			Logger.log("Error loading Image");
		}
	    
	    //Set specific settings
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", TAPAAL.TOOLNAME);

        // Use native file chooser
        System.setProperty("apple.awt.fileDialogForDirectories", "false");
        
        // Grow size of boxes to add room for the resizer
        System.setProperty("apple.awt.showGrowBox", "true");
	}
	
	public void handleAbout(AboutEvent arg0) {
		CreateGui.appGui.showAbout();
	}

	public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
		CreateGui.appGui.exit();
	}

	public static void postprocess(){
		// Enable fullscreen on Mac
        FullScreenUtilities.setWindowCanFullScreen(CreateGui.appGui, true);
	}

}
