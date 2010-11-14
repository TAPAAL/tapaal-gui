package dk.aau.cs.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;

import pipe.gui.Pipe;

public class TextLabel extends JLabel {
	private static final long serialVersionUID = 2836463890383282585L;
	private JComponent owner;
	private DrawingSurface drawingSurface;
	
	public TextLabel(DrawingSurface drawingSurface, JComponent owner, Point locationRelativeTo, String text){
		this.drawingSurface = drawingSurface;
		this.owner = owner;
		
		setFont(new Font(Pipe.LABEL_FONT, Font.BOLD, Pipe.LABEL_DEFAULT_FONT_SIZE));
		setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
		setFocusable(false);    
		setOpaque(false);
		setBackground(Pipe.BACKGROUND_COLOR);
		setText(text);
		setLocation(locationRelativeTo.x - getPreferredSize().width, locationRelativeTo.y);
		updateSize();

		addMouseListeners();
	}

	public void addMouseListeners() {
		MouseWheelAndClickPassThroughHandler clickAndWheelHandler = new MouseWheelAndClickPassThroughHandler(owner);
		addMouseListener(clickAndWheelHandler);
		addMouseWheelListener(clickAndWheelHandler);
		addMouseMotionListener(new DragHandler(this, drawingSurface));
	}
	
	public void removeMouseListeners(){
		for(MouseListener listener : getMouseListeners()) removeMouseListener(listener);
		for(MouseWheelListener listener : getMouseWheelListeners()) removeMouseWheelListener(listener);
		for(MouseMotionListener listener : getMouseMotionListeners()) removeMouseMotionListener(listener);
	}
	
	private void updateSize() {
		setSize(getPreferredSize().width, getPreferredSize().height);
	}
	
	public void zoom(int percentage){
		float newSize = Pipe.LABEL_DEFAULT_FONT_SIZE * (percentage/100.0f);
		Font newFont = getFont().deriveFont(newSize);
		setFont(newFont);
		updateSize();
	}
	
	private class MouseWheelAndClickPassThroughHandler 
	extends MouseInputAdapter{
		private Component targetComponent;
		
		public MouseWheelAndClickPassThroughHandler(Component target){
			this.targetComponent = target;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			targetComponent.dispatchEvent(e);
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			targetComponent.dispatchEvent(e);
		}
	}
}
