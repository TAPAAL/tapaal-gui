package pipe.gui.petrinet.animation;

import pipe.gui.GuiFrame;
import pipe.gui.TAPAALGUI;

import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

public final class SimulatorFocusTraversalPolicy extends FocusTraversalPolicy {

    private final JTextField timeDelayField;
    public SimulatorFocusTraversalPolicy(JTextField timeDelayField) {
        this.timeDelayField = timeDelayField;
    }

    @Override
	public Component getComponentAfter(Container aContainer,
			Component aComponent) {
		
		Component comp = TAPAALGUI.getApp();
		if(aComponent instanceof GuiFrame){
			comp = timeDelayField;
		}
		
		return comp;
	}

	@Override
	public Component getComponentBefore(Container aContainer,
			Component aComponent) {
		Component comp = TAPAALGUI.getApp();
		if(aComponent instanceof GuiFrame){
			comp = timeDelayField;
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
		return timeDelayField;
	}

}
