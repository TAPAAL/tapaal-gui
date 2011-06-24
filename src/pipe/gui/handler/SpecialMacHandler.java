package pipe.gui.handler;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;



import pipe.gui.CreateGui;
import pipe.gui.Pipe;

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