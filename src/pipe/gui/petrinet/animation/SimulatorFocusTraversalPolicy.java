package pipe.gui.petrinet.animation;

import pipe.gui.GuiFrame;
import pipe.gui.TAPAALGUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

public class SimulatorFocusTraversalPolicy extends FocusTraversalPolicy {

	@Override
	public Component getComponentAfter(Container aContainer,
			Component aComponent) {
		
		Component comp = TAPAALGUI.getApp();
		if(aComponent instanceof GuiFrame){
			comp = TAPAALGUI.getCurrentTab().getAnimationController().TimeDelayField;
		}
		
		return comp;
	}

	@Override
	public Component getComponentBefore(Container aContainer,
			Component aComponent) {
		Component comp = TAPAALGUI.getApp();
		if(aComponent instanceof GuiFrame){
			comp = TAPAALGUI.getCurrentTab().getAnimationController().TimeDelayField;
		}
		return comp;
	}

	@Override
	public Component getDefaultComponent(Container aContainer) {
		return TAPAALGUI.getApp();
	}

	@Override
	public Component getFirstComponent(Container aContainer) {
		return TAPAALGUI.getApp();
	}

	@Override
	public Component getLastComponent(Container aContainer) {
		return TAPAALGUI.getCurrentTab().getAnimationController().TimeDelayField;
	}

}
