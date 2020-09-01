package dk.aau.cs.gui.undo;

import java.awt.Point;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.PlaceTransitionObject;

public class MovePlaceTransitionObject extends Command {
	
	private final int newY;
	private final int newX;
	private final PlaceTransitionObject objectToBeMoved;
	private final int oldY;
	private final int oldX;
	
	
	public MovePlaceTransitionObject(PlaceTransitionObject object, Point point) {
		objectToBeMoved = object;
		this.newX = point.x;
		this.newY = point.y;
        this.oldY = objectToBeMoved.getOriginalY();
        this.oldX = objectToBeMoved.getOriginalX();
	}

	@Override
	public void undo() {
		objectToBeMoved.setOriginalX(oldX);
		objectToBeMoved.setOriginalY(oldY);
		
		
		objectToBeMoved.updateOnMoveOrZoom();
		objectToBeMoved.repaint();
		CreateGui.getDrawingSurface().updatePreferredSize();
	}

	@Override
	public void redo() {
		objectToBeMoved.setOriginalX(newX);
		objectToBeMoved.setOriginalY(newY);
		

		objectToBeMoved.updateOnMoveOrZoom();
		objectToBeMoved.repaint();
		CreateGui.getDrawingSurface().updatePreferredSize();

	}

}
