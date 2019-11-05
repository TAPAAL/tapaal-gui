package pipe.gui;

class GuiFrameController implements GuiFrameControllerActions{

    GuiFrame guiFrameDirectAccess; //XXX - while refactoring shold only use guiFrameActions
    GuiFrameActions guiFrame;

    GuiFrameController(GuiFrame appGui) {
        super();

        guiFrame = appGui;
        guiFrameDirectAccess = appGui;

        appGui.registerController(this);


    }
}
