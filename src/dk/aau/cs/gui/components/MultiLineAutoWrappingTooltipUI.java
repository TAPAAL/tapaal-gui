package dk.aau.cs.gui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

public class MultiLineAutoWrappingTooltipUI extends BasicToolTipUI {
	private static MultiLineAutoWrappingTooltipUI sharedInstance = new MultiLineAutoWrappingTooltipUI();			     
	protected CellRendererPane rendererPane;
	
	private static JTextArea textArea ;
	
	public static ComponentUI createUI(JComponent c) {
	    return sharedInstance;
	}
	
	public MultiLineAutoWrappingTooltipUI() {
	    super();
	}
	
	public void installUI(JComponent c) {
	    super.installUI(c);
	    rendererPane = new CellRendererPane();
	    c.add(rendererPane);
	}
	
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		
	    c.remove(rendererPane);
	    rendererPane = null;
	}
	
	public void paint(Graphics g, JComponent c) {
	    Dimension size = c.getSize();
	    textArea.setBackground(new ColorUIResource(255, 247, 200));
		rendererPane.paintComponent(g, textArea, c, 1, 1, size.width - 1, size.height - 1, true);
	}
	
	public Dimension getPreferredSize(JComponent tooltip) {
		if(tooltip instanceof JToolTip) {
			String tipText = ((JToolTip)tooltip).getTipText();
			if (tipText == null)
				return new Dimension(0,0);
			
			textArea = new JTextArea(tipText);
			Dimension originalDim = textArea.getPreferredSize();	
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setMargin(new Insets(5, 5, 5, 5));
			textArea.setBackground(tooltip.getBackground());
			textArea.setForeground(tooltip.getForeground());
			
			int width = 400;
			Dimension dim = textArea.getPreferredSize();
			dim.width = (originalDim.width > width) ? width : originalDim.width + 10;
			dim.height++;
			textArea.setSize(dim);
			
			rendererPane.removeAll();
			rendererPane.add(textArea);
			rendererPane.setBackground(tooltip.getBackground());

			dim = textArea.getPreferredSize();	
			dim.height += 1;
			dim.width += 1;
			return dim;
		}
		else
			return new Dimension(0,0);
	}
	
	public Dimension getMinimumSize(JComponent c) {
	    return getPreferredSize(c);
	}
	
	public Dimension getMaximumSize(JComponent c) {
	    return getPreferredSize(c);
	}

}
