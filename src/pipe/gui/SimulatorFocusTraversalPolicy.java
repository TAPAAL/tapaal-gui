package pipe.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

public class SimulatorFocusTraversalPolicy extends FocusTraversalPolicy {

	@Override
	public Component getComponentAfter(Container aContainer,
			Component aComponent) {
		
		Component comp = CreateGui.getApp();
		if(aComponent instanceof GuiFrame){
			comp = CreateGui.getCurrentTab().getAnimationController().TimeDelayField;
		}
		
		return comp;
	}

	@Override
	public Component getComponentBefore(Container aContainer,
			Component aComponent) {
		Component comp = CreateGui.getApp();
		if(aComponent instanceof GuiFrame){
			comp = CreateGui.getCurrentTab().getAnimationController().TimeDelayField;
		}
		return comp;
	}

	@Override
	public Component getDefaultComponent(Container aContainer) {
		return CreateGui.getApp();
	}

	@Override
	public Component getFirstComponent(Container aContainer) {
		return CreateGui.getApp();
	}

	@Override
	public Component getLastComponent(Container aContainer) {
		return CreateGui.getCurrentTab().getAnimationController().TimeDelayField;
	}

}
