package pipe.gui.handler;

import java.awt.Image;
import java.awt.Window;
import java.io.IOException;
import java.lang.reflect.Method;
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

	}
	
	public void handleAbout(AboutEvent arg0) {
		CreateGui.getAppGui().showAbout();
	}

	public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
		CreateGui.getAppGui().exit();
	}

	public static void postprocess(){
		// Enable fullscreen on Mac
		// Use reflection to prevent compile errors
		try {
	        Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
	        Class params[] = new Class[]{Window.class, Boolean.TYPE};
	        Method method = util.getMethod("setWindowCanFullScreen", params);
	        method.invoke(util, CreateGui.getAppGui(), true);
	    } catch (Exception e) {
	    	// Fullscreen not supported
	    }
	}

}
