package dk.aau.cs.gui.components;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class MultiLineAutoWrappingToolTip extends JToolTip {
	private static final long serialVersionUID = 7266812646748745742L;
	private JTextPane queryField;
	
	public MultiLineAutoWrappingToolTip() {
		queryField = new JTextPane();

		StyledDocument doc = queryField.getStyledDocument();

		// Set alignment to be centered for all paragraphs
		MutableAttributeSet standard = new SimpleAttributeSet();
		StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
		StyleConstants.setFontSize(standard, 14);
		doc.setParagraphAttributes(0, 0, standard, true);

		queryField.setBackground(getBackground());
		if(getToolTipText() != null)
			queryField.setText(getToolTipText());
		queryField.setEditable(false);

		// Put the text pane in a scroll pane.
		JScrollPane queryScrollPane = new JScrollPane(queryField);
		queryScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		Dimension d = new Dimension(750, 56);
		queryScrollPane.setPreferredSize(d);
		queryScrollPane.setMinimumSize(d);
	}
	
	
	

}
