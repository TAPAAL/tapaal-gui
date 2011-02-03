package dk.aau.cs.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import pipe.gui.Grid;

public class DragHandler extends javax.swing.event.MouseInputAdapter {
	private Point anchorPoint;
	private JComponent draggableComponent;
	private DrawingSurface drawingSurface;
	private boolean snapToGrid;

	public DragHandler(JComponent draggableComponent,
			DrawingSurface drawingSurface, boolean snapToGrid) {
		this.draggableComponent = draggableComponent;
		this.drawingSurface = drawingSurface;
		this.snapToGrid = snapToGrid;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		anchorPoint = e.getPoint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int anchorX = anchorPoint.x;
		int anchorY = anchorPoint.y;

		Point parentOnScreen = draggableComponent.getParent()
				.getLocationOnScreen();
		Point mouseOnScreen = e.getLocationOnScreen();

		int x = Math.max(0, mouseOnScreen.x - parentOnScreen.x - anchorX);
		int y = Math.max(0, mouseOnScreen.y - parentOnScreen.y - anchorY);

		if (snapToGrid) {
			x = Grid.getModifiedX(x);
			y = Grid.getModifiedY(y);
		}

		Point position = new Point(x, y);
		draggableComponent.setLocation(position);

		drawingSurface.updatePreferredSize();
	}
}