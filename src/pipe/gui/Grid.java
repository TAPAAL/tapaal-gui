package pipe.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.MovePlaceTransitionObject;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;

/**
 * @author Peter Kyme
 */
public class Grid {

	private static double gridSpacing = Pipe.PLACE_TRANSITION_HEIGHT / 2;
	private static final Color gridColor = new Color(240, 240, 255);
	private static GeneralPath gridDisplay;
	private static boolean enabled;
	private static int gridHeight, gridWidth;
	private static int gridCount = 1;

	private static void createGrid() {
		gridDisplay = new GeneralPath();

		for (double i = gridSpacing; i <= gridWidth; i += gridSpacing) {
			gridDisplay.moveTo(i, 2);
			gridDisplay.lineTo(i, gridHeight);
		}
		for (double i = gridSpacing; i <= gridHeight; i += gridSpacing) {
			gridDisplay.moveTo(2, i);
			gridDisplay.lineTo(gridWidth, i);
		}
	}

	public static void enableGrid() {
		if (!enabled) {
			enabled = true;
		}
	}

	public static void increment() {
		gridCount++;
		gridCount %= 4;

		if (gridCount == 3) {
			disableGrid();
		} else {
			enableGrid();
			setGridSpacing(Math.pow(2, gridCount - 2));
		}
	}

	private static void setGridSpacing(double spacing) {
		gridSpacing = (spacing * Pipe.PLACE_TRANSITION_HEIGHT);
	}

	public static void disableGrid() {
		if (enabled) {
			enabled = false;
		}
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void updateSize(Container parent) {
		if (enabled) {
			gridHeight = parent.getHeight();
			gridWidth = parent.getWidth();
			createGrid();
		}
	}

	public static void drawGrid(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g2d.setPaint(gridColor);
		g2d.draw(gridDisplay);
	}

	public static int getModifiedX(double x) {
		if (!enabled) {
			return (int) x;
		}
		return (int) (Math.round(x / gridSpacing) * gridSpacing);

	}

	public static int getModifiedY(double y) {
		if (!enabled) {
			return (int) y;
		}
		return (int) (Math.round(y / gridSpacing) * gridSpacing);
	}
	
	public static void alignPNObjectsToGrid() {
		ArrayList<PetriNetObject> petriNetObjects = CreateGui.getDrawingSurface().getGuiModel().getPlaceTransitionObjects();
		pipe.gui.undo.UndoManager undoManager = CreateGui.getCurrentTab().getUndoManager();
		undoManager.newEdit();
		
		for(PetriNetObject object : petriNetObjects) {
			PlaceTransitionObject ptobject = (PlaceTransitionObject)object;
			int x = Grid.getModifiedX(ptobject.getPositionX());
			int y = Grid.getModifiedY(ptobject.getPositionY());
			Point point = new Point(x,y);
			Command command = new MovePlaceTransitionObject(ptobject, point);
			command.redo();
			undoManager.addEdit(command);
			ptobject.updateOnMoveOrZoom();
		}
		
	}

}
