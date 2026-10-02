package net.tapaal.gui.swingcomponents;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

public class MultiLineAutoWrappingTooltipUI extends BasicToolTipUI {
	private static final int MAX_WIDTH = 400;
	private static final int SCREEN_MARGIN = 10;

	private final CellRendererPane rendererPane = new CellRendererPane();
	private final JTextArea textArea = new JTextArea();

	public static ComponentUI createUI(JComponent c) {
		return new MultiLineAutoWrappingTooltipUI();
	}

	public MultiLineAutoWrappingTooltipUI() {
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setMargin(new Insets(5, 5, 5, 5));
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		c.add(rendererPane);
	}

	@Override
	public void uninstallUI(JComponent c) {
		c.remove(rendererPane);
		super.uninstallUI(c);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		if (!(c instanceof JToolTip)) return;

		JToolTip tooltip = (JToolTip) c;
		updateTextArea(tooltip);
		Dimension size = c.getSize();
		Insets insets = c.getInsets();
		rendererPane.paintComponent(g, textArea, c, insets.left + 1, insets.top + 1,
			size.width - insets.left - insets.right - 2, size.height - insets.top - insets.bottom - 2, true);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		if (!(c instanceof JToolTip)) return new Dimension(0, 0);

		JToolTip tooltip = (JToolTip) c;
		String tipText = tooltip.getTipText();
		if (tipText == null) return new Dimension(0, 0);

		updateTextArea(tooltip);
		Insets tooltipInsets = tooltip.getInsets();
		int width = Math.max(1, getMaximumWidth(tooltip) - tooltipInsets.left - tooltipInsets.right - 2);
		textArea.setSize(new Dimension(width, Short.MAX_VALUE));

		Dimension size = textArea.getPreferredSize();
		size.width = Math.min(size.width, width);
		size.height++;
		size.width += tooltipInsets.left + tooltipInsets.right + 2;
		size.height += tooltipInsets.top + tooltipInsets.bottom;
		return size;
	}

	@Override
	public Dimension getMinimumSize(JComponent c) {
		return getPreferredSize(c);
	}

	@Override
	public Dimension getMaximumSize(JComponent c) {
		return getPreferredSize(c);
	}

	private void updateTextArea(JToolTip tooltip) {
		textArea.setText(tooltip.getTipText());
		textArea.setFont(tooltip.getFont());
		textArea.setBackground(tooltip.getBackground());
		textArea.setForeground(tooltip.getForeground());
	}

	private int getMaximumWidth(JToolTip tooltip) {
		int screenWidth = MAX_WIDTH;
		Component owner = tooltip.getComponent();
		if (owner != null && owner.getGraphicsConfiguration() != null) {
			java.awt.Rectangle bounds = owner.getGraphicsConfiguration().getBounds();
			Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(owner.getGraphicsConfiguration());
			screenWidth = bounds.width - screenInsets.left - screenInsets.right - 2 * SCREEN_MARGIN;
		}
		return Math.max(1, Math.min(MAX_WIDTH, screenWidth));
	}
}
