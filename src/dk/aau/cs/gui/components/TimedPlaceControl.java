package dk.aau.cs.gui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.gui.Pipe;
import dk.aau.cs.gui.DrawingSurface;
import dk.aau.cs.gui.components.handlers.ClickHandler;
import dk.aau.cs.gui.components.handlers.DragHandler;
import dk.aau.cs.model.tapn.TimedPlace;

public class TimedPlaceControl extends PetriNetElementControl {
	private static final long serialVersionUID = 3512995997457683903L;

	private static final float DEFAULT_LINE_THICKNESS = 1.0f;
	private static final int DRAW_OFFSET = 1;

	private final TimedPlace timedPlace;
	private TextLabel nameLabel;
	private TextLabel invariantLabel;

	private float lineThickness = DEFAULT_LINE_THICKNESS;
	private boolean attributesVisible = true;

	public TimedPlaceControl(DrawingSurface parent, TimedPlace timedPlace,
			Point position) {
		super(parent, position);
		this.timedPlace = timedPlace;

		setLocation(position);
		setSize(Pipe.PLACE_TRANSITION_HEIGHT + DRAW_OFFSET,
				Pipe.PLACE_TRANSITION_HEIGHT + DRAW_OFFSET);
		this.setBorder(BorderFactory.createLineBorder(Color.red));

		initComponents();
		addMouseListeners();

	}

	private void initComponents() {
		Point position = getLocation();
		nameLabel = new TextLabel(parent(), this, new Point(position.x - 5,
				position.y), timedPlace.name());
		invariantLabel = new TextLabel(parent(), this, new Point(
				position.x - 5, position.y + Pipe.LABEL_DEFAULT_FONT_SIZE + 1),
				"inv: " + timedPlace.invariant().toString());
		addChildControls();
	}

	@Override
	public void addMouseListeners() {
		removeMouseListeners();
		addMouseMotionListener(new DragHandler(this, parent(), true));
		addMouseListener(new ClickHandler(this));

		nameLabel.addMouseListeners();
		invariantLabel.addMouseListeners();
	}

	@Override
	public void removeMouseListeners() {
		for (MouseMotionListener listener : getMouseMotionListeners())
			removeMouseMotionListener(listener);
		for (MouseListener listener : getMouseListeners())
			removeMouseListener(listener);
		nameLabel.removeMouseListeners();
		invariantLabel.removeMouseListeners();
	}

	public boolean isAttributesVisible() {
		return attributesVisible;
	}

	public void setAttributesVisible(boolean value) {
		if (value != attributesVisible) {
			this.attributesVisible = value;
			if (value) {
				addChildControls();
			} else {
				removeChildControls();
			}
		}
	}

	@Override
	public void addChildControls() {
		parent().add(nameLabel);
		parent().add(invariantLabel);
		// parent().surfaceChanged();
	}

	@Override
	public void removeChildControls() {
		parent().remove(nameLabel);
		parent().remove(invariantLabel);
		// parent().surfaceChanged();
	}

	@Override
	public void setLocation(int x, int y) {
		if (nameLabel != null)
			moveLabelRelativeToNewPlaceLocation(nameLabel, x, y);
		if (invariantLabel != null)
			moveLabelRelativeToNewPlaceLocation(invariantLabel, x, y);
		super.setLocation(x, y);
	}

	@Override
	public void zoom(int percentage) {
		super.zoom(percentage);
		nameLabel.zoom(percentage);
		invariantLabel.zoom(percentage);
		lineThickness = DEFAULT_LINE_THICKNESS * (percentage / 100.0f);
	}

	@Override
	protected void selectChildren() {
		nameLabel.setForeground(Pipe.SELECTION_TEXT_COLOUR);
		invariantLabel.setForeground(Pipe.SELECTION_TEXT_COLOUR);
	}

	@Override
	protected void deselectChildren() {
		nameLabel.setForeground(Pipe.ELEMENT_TEXT_COLOUR);
		invariantLabel.setForeground(Pipe.ELEMENT_TEXT_COLOUR);
	}

	@Override
	protected void paintControl(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		g2.setStroke(new BasicStroke(lineThickness));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(isSelected() ? Pipe.SELECTION_FILL_COLOUR
				: Pipe.ELEMENT_FILL_COLOUR);
		Ellipse2D.Double ellipse = new Ellipse2D.Double(0, 0, getSize().width
				- DRAW_OFFSET, getSize().height - DRAW_OFFSET);
		g2.fill(ellipse);

		g2.setPaint(isSelected() ? Pipe.SELECTION_LINE_COLOUR
				: Pipe.ELEMENT_LINE_COLOUR);
		g2.draw(ellipse);
	}

	@Override
	public JPopupMenu createPopupMenu() {
		JPopupMenu menu = new JPopupMenu();

		if (attributesVisible) {
			JMenuItem item = new JMenuItem("Hide Attributes");
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setAttributesVisible(false);
				}
			});
			menu.add(item);
		} else {
			JMenuItem item = new JMenuItem("Show Attributes");
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setAttributesVisible(true);
				}
			});
			menu.add(item);
		}

		JMenuItem deleteItem = new JMenuItem("Delete");
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// delete();
			}
		});
		menu.add(deleteItem);

		JMenuItem propertiesItem = new JMenuItem("Properties");
		propertiesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// showEditor();
			}
		});
		menu.add(propertiesItem);

		return menu;
	}
}
