package dk.aau.cs.gui.undo;

import java.awt.Point;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.PlaceTransitionObject;

public class MovePlaceTransitionObject extends Command {
	
	private int newY;
	private int newX;
	private PlaceTransitionObject objectToBeMoved;
	private int oldY;
	private int oldX;
	private boolean doUpdate = false;
	
	
	public MovePlaceTransitionObject(PlaceTransitionObject object, Point point) {
		objectToBeMoved = object;
		this.newX = point.x;
		this.newY = point.y;
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
		oldY = objectToBeMoved.getOriginalY();
		oldX = objectToBeMoved.getOriginalX();
		
		objectToBeMoved.setOriginalX(newX);
		objectToBeMoved.setOriginalY(newY);
		
		if(doUpdate) {
			objectToBeMoved.updateOnMoveOrZoom();
			objectToBeMoved.repaint();
			CreateGui.getDrawingSurface().updatePreferredSize();
		}
		doUpdate = true;
		
	}

}
