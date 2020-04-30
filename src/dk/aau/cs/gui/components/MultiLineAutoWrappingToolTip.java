package dk.aau.cs.gui.components;

import javax.swing.JToolTip;

public class MultiLineAutoWrappingToolTip extends JToolTip {
	
	public MultiLineAutoWrappingToolTip() {
	    updateUI();
	}
	
	public void updateUI() {
	    setUI(MultiLineAutoWrappingTooltipUI.createUI(this));
	}
	
}
