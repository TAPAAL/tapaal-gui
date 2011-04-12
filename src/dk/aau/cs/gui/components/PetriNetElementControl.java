package dk.aau.cs.gui.components;

import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import dk.aau.cs.gui.DrawingSurface;

import pipe.gui.Pipe;

public abstract class PetriNetElementControl extends JComponent {
	private static final long serialVersionUID = -3429746860537818157L;

	private Point originalLocation;
	private final DrawingSurface parent;

	private boolean selected = false;

	public PetriNetElementControl(DrawingSurface parent, Point originalLocation) {
		this.parent = parent;
		this.originalLocation = originalLocation;
	}

	public boolean isSelected() {
		return selected;
	}

	protected DrawingSurface parent() {
		return parent;
	}

	public void select() {
		if (!selected) {
			selected = true;
			selectChildren();
			repaint();
		}
	}

	public void deselect() {
		if (selected) {
			selected = false;
			deselectChildren();
			repaint();
		}
	}

	public void zoom(int percentage) {
		double scaleFactor = percentage / 100.0;

		double positionX = originalLocation.x * scaleFactor;
		double positionY = originalLocation.y * scaleFactor;
		double width = Pipe.PLACE_TRANSITION_HEIGHT * scaleFactor;
		double height = Pipe.PLACE_TRANSITION_HEIGHT * scaleFactor;

		setLocation((int) positionX, (int) positionY);
		setSize((int) width, (int) height);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintControl(g);
	}

	public void showPopupMenu(int x, int y) {
		JPopupMenu menu = createPopupMenu();
		menu.show(this, x, y);
	}

	protected void moveLabelRelativeToNewPlaceLocation(JComponent component,
			int newPlaceX, int newPlaceY) {
		Point componentLocation = component.getLocation();
		Point placeLocation = getLocation();

		int componentPlaceDiffX = componentLocation.x - placeLocation.x;
		int componentPlaceDiffY = componentLocation.y - placeLocation.y;

		component.setLocation(newPlaceX + componentPlaceDiffX, newPlaceY
				+ componentPlaceDiffY);
	}

	protected void selectChildren() {
	}

	protected void deselectChildren() {
	}

	public void addMouseListeners() {
	}

	public void removeMouseListeners() {
	}

	protected abstract void paintControl(Graphics g);

	protected abstract JPopupMenu createPopupMenu();

	public void removeChildControls() {
	}

	public void addChildControls() {
	}
}
