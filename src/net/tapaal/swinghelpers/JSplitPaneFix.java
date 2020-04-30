package net.tapaal.swinghelpers;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JSplitPane;

public class JSplitPaneFix extends JSplitPane {

	private boolean isPainted;
	private boolean hasProportionalLocation;
	private double proportionalLocation;

	public JSplitPaneFix(int verticalSplit, Component component1, Component component2) {
		super(verticalSplit, component1, component2);

	}

	public JSplitPaneFix(int verticalSplit) {
		super(verticalSplit);
	}

	@Override
	public void setDividerLocation(double proportionalLocation) {
		if (!isPainted) {
			hasProportionalLocation = true;
			this.proportionalLocation = proportionalLocation;
		} else
			super.setDividerLocation(proportionalLocation);
	}

	@Override
	public void paint(Graphics g) {
		if (!isPainted) {
			if (hasProportionalLocation)
				super.setDividerLocation(proportionalLocation);
			isPainted = true;
		}
		super.paint(g);
	}

}
