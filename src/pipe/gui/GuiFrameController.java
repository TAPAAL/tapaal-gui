package pipe.gui;

import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;
import net.tapaal.Preferences;

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
}
