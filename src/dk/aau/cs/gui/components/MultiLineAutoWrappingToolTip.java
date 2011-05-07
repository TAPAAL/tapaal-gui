package dk.aau.cs.gui.components;

import javax.swing.JToolTip;

public class MultiLineAutoWrappingToolTip extends JToolTip {
	private static final long serialVersionUID = 367044372428497513L;
	
	public MultiLineAutoWrappingToolTip() {
	    updateUI();
	}
	
	public void updateUI() {
	    setUI(MultiLineAutoWrappingTooltipUI.createUI(this));
	}
	
}
