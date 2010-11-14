package dk.aau.cs.gui;

import java.awt.Font;
import java.awt.Point;

import javax.swing.JLabel;

import pipe.gui.Pipe;

public class TextLabel extends JLabel {
	private static final long serialVersionUID = 2836463890383282585L;

	public TextLabel(DrawingSurface drawingSurface, Point locationRelativeTo, String text){
		setFont(new Font(Pipe.LABEL_FONT, Font.BOLD, Pipe.LABEL_DEFAULT_FONT_SIZE));
		setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
		setFocusable(false);    
		setOpaque(false);
		setBackground(Pipe.BACKGROUND_COLOR);
		setText(text);
		setLocation(locationRelativeTo.x - getPreferredSize().width, locationRelativeTo.y);
		setSize((int)(getPreferredSize().width * 1.2), (int)(getPreferredSize().height * 1.2));

		addMouseMotionListener(new DragHandler(this, drawingSurface));
	}
}
