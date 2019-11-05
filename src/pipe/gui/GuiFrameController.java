package pipe.gui;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;
import net.tapaal.Preferences;
import pipe.gui.widgets.EngineDialogPanel;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

class GuiFrameController implements GuiFrameControllerActions{

    GuiFrame guiFrameDirectAccess; //XXX - while refactoring shold only use guiFrameActions
    GuiFrameActions guiFrame;

    GuiFrameController(GuiFrame appGui) {
        super();

        guiFrame = appGui;
        guiFrameDirectAccess = appGui;

        appGui.registerController(this);


    }

    @Override
    public void clearPreferences() {
        // Clear persistent storage
        Preferences.getInstance().clear();
        // Engines reset individually to remove preferences for already setup engines
        Verifyta.reset();
        VerifyTAPN.reset();
        VerifyTAPNDiscreteVerification.reset();
    }

    @Override
    public void showEngineDialog() {
        new EngineDialogPanel().showDialog();
    }

    //XXX: should properly not have address as argument, make one function per page
    @Override
    public void openURL(String address) {
        showInBrowser(address);
    };

    private static void openBrowser(URI url){
        //open the default bowser on this page
        try {
            java.awt.Desktop.getDesktop().browse(url);
        } catch (IOException e) {
            Logger.log("Cannot open the browser.");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There was a problem opening the default web browser \n" +
                            "Please open the url in your browser by entering " + url.toString(),
                    "Error opening browser", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    //XXX should be private, but for now used in action not yet moved to controller
    @Deprecated
    public static void showInBrowserDeprecatedDirectCall(String address) {
        showInBrowser(address);
    }
    private static void showInBrowser(String address) {
        try {
            URI url = new URI(address);
            openBrowser(url);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            Logger.log("Error convering to URL");
            e.printStackTrace();
        }
    }
}
