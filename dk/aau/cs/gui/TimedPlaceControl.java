package dk.aau.cs.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import pipe.gui.Pipe;
import dk.aau.cs.model.tapn.TimedPlace;

public class TimedPlaceControl extends JComponent{
	private static final long serialVersionUID = 3512995997457683903L;
	private static final int DRAW_OFFSET = 1;

	private final TimedPlace timedPlace;
	private TextLabel nameLabel;
	private TextLabel invariantLabel;
	private DrawingSurface parent;
	
	public TimedPlaceControl(DrawingSurface parent, TimedPlace timedPlace, Point position) {
		this.timedPlace = timedPlace;
		this.parent = parent;

		setLocation(position);
		setSize(Pipe.PLACE_TRANSITION_HEIGHT + DRAW_OFFSET, Pipe.PLACE_TRANSITION_HEIGHT + DRAW_OFFSET);
		this.setBorder(BorderFactory.createLineBorder(Color.red));
		
		initComponents();
		addMouseMotionListener(new DragHandler(this, parent));
		
	}

	private void initComponents() {
		Point position = getLocation();
		nameLabel = new TextLabel(parent, new Point(position.x - 2, position.y), timedPlace.name());
		invariantLabel = new TextLabel(parent, new Point(position.x - 2, position.y + Pipe.LABEL_DEFAULT_FONT_SIZE + 1), "inv: " + timedPlace.invariant().toString());
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

		g2.setStroke(new BasicStroke(1.0f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
		Ellipse2D.Double ellipse = new Ellipse2D.Double(0, 0, Pipe.PLACE_TRANSITION_HEIGHT, Pipe.PLACE_TRANSITION_HEIGHT);		
		g2.fill(ellipse);

		g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		g2.draw(ellipse);
	}
}
