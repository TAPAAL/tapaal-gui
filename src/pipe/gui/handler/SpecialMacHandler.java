package pipe.gui.handler;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;

import dk.aau.cs.debug.Logger;



import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

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
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", Pipe.TOOL);

        // Use native file chooser
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        
        // Grow size of boxes to add room for the resizer
        System.setProperty("apple.awt.showGrowBox", "true");

	    
	}
	
	public void handleAbout(AboutEvent arg0) {
		CreateGui.appGui.showAbout();
	}

	public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
		CreateGui.appGui.exit();
	}

 

}