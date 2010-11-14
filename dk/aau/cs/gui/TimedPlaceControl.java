package dk.aau.cs.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import pipe.gui.Pipe;
import dk.aau.cs.model.tapn.TimedPlace;

public class TimedPlaceControl 
extends JComponent
implements PetriNetElementControl {
	private static final long serialVersionUID = 3512995997457683903L;
	
	private static final float DEFAULT_LINE_THICKNESS = 1.0f;
	private static final int DRAW_OFFSET = 1;

	private final TimedPlace timedPlace;
	private TextLabel nameLabel;
	private TextLabel invariantLabel;
	
	private DrawingSurface parent;
	private boolean selected = false;
	
	private Point originalLocation;
	private float lineThickness = DEFAULT_LINE_THICKNESS;

	public TimedPlaceControl(DrawingSurface parent, TimedPlace timedPlace, Point position) {
		this.timedPlace = timedPlace;
		this.parent = parent;
		this.originalLocation = position;
		
		setLocation(position);
		setSize(Pipe.PLACE_TRANSITION_HEIGHT + DRAW_OFFSET, Pipe.PLACE_TRANSITION_HEIGHT + DRAW_OFFSET);
		this.setBorder(BorderFactory.createLineBorder(Color.red));

		initComponents();
		addMouseListeners();

	}

	public void addMouseListeners() {
		addMouseMotionListener(new DragHandler(this, parent));
		addMouseListener(new ClickHandler(this));
	}
	
	public void removeMouseListeners() {
		for(MouseMotionListener listener : getMouseMotionListeners()) removeMouseMotionListener(listener);
		for(MouseListener listener : getMouseListeners()) removeMouseListener(listener);
	}

	private void initComponents() {
		Point position = getLocation();
		nameLabel = new TextLabel(parent, this, new Point(position.x - 5, position.y), timedPlace.name());
		invariantLabel = new TextLabel(parent, this, new Point(position.x - 5, position.y + Pipe.LABEL_DEFAULT_FONT_SIZE + 1), "inv: " + timedPlace.invariant().toString());
		parent.add(nameLabel);
		parent.add(invariantLabel);
	}

	public void removeChildControls(){
		parent.remove(nameLabel);
		parent.remove(invariantLabel);
	}

	@Override
	public void setLocation(int x, int y) {
		if(nameLabel != null) moveLabelRelativeToNewPlaceLocation(nameLabel, x, y);
		if(invariantLabel != null) moveLabelRelativeToNewPlaceLocation(invariantLabel, x, y);
		super.setLocation(x,y);
	}

	private void moveLabelRelativeToNewPlaceLocation(JComponent component, int newPlaceX, int newPlaceY) {
		Point componentLocation = component.getLocation();
		Point placeLocation = getLocation();

		int componentPlaceDiffX = componentLocation.x - placeLocation.x;
		int componentPlaceDiffY = componentLocation.y - placeLocation.y;

		component.setLocation(newPlaceX + componentPlaceDiffX, newPlaceY + componentPlaceDiffY);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;

		g2.setStroke(new BasicStroke(lineThickness));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(
				selected ? Pipe.SELECTION_FILL_COLOUR : Pipe.ELEMENT_FILL_COLOUR
		);
		Ellipse2D.Double ellipse = new Ellipse2D.Double(0, 0, getSize().width - DRAW_OFFSET, getSize().height - DRAW_OFFSET);		
		g2.fill(ellipse);

		g2.setPaint(
				selected ? Pipe.SELECTION_LINE_COLOUR : Pipe.ELEMENT_LINE_COLOUR
		);
		g2.draw(ellipse);
	}

	public void showEditor() {

	}

	public void showPopupMenu() {

	}

	public void select(){
		if(!selected){
			selected = true;
			nameLabel.setForeground(Pipe.SELECTION_TEXT_COLOUR);
			invariantLabel.setForeground(Pipe.SELECTION_TEXT_COLOUR);
			repaint();
		}
	}

	public void deselect(){
		if(selected){
			selected = false;
			nameLabel.setForeground(Pipe.ELEMENT_TEXT_COLOUR);
			invariantLabel.setForeground(Pipe.ELEMENT_TEXT_COLOUR);
			repaint();
		}
	}

	public void zoom(int percentage) {
		double scaleFactor = percentage / 100.0;

		double positionX = originalLocation.x * scaleFactor;
		double positionY = originalLocation.y * scaleFactor;
		double width = Pipe.PLACE_TRANSITION_HEIGHT * scaleFactor;
		double height = Pipe.PLACE_TRANSITION_HEIGHT * scaleFactor;
		
		nameLabel.zoom(percentage);
		invariantLabel.zoom(percentage);
		
		setLocation((int)positionX, (int)positionY);
		setSize((int)width, (int)height);
		lineThickness = DEFAULT_LINE_THICKNESS * (float)scaleFactor;
	}
}
