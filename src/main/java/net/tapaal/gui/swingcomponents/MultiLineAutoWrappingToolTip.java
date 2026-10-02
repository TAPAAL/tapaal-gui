package net.tapaal.gui.swingcomponents;

import javax.swing.JComponent;
import javax.swing.JToolTip;

public class MultiLineAutoWrappingToolTip extends JToolTip {
	
	public MultiLineAutoWrappingToolTip() {
	    updateUI();
	}

	public MultiLineAutoWrappingToolTip(JComponent owner) {
		this();
		setComponent(owner);
	}
	
	public void updateUI() {
	    setUI(MultiLineAutoWrappingTooltipUI.createUI(this));
	}
	
}
