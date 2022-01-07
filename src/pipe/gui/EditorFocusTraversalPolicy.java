package pipe.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

public class EditorFocusTraversalPolicy extends FocusTraversalPolicy {

	@Override
	public Component getComponentAfter(Container aContainer,
			Component aComponent) {
		return TAPAALGUI.getApp();
	}

	@Override
	public Component getComponentBefore(Container aContainer,
			Component aComponent) {
		return TAPAALGUI.getApp();
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
		return TAPAALGUI.getApp();
	}

}
