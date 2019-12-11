package dk.aau.cs.gui.undo;

import pipe.gui.Grid;
import pipe.gui.graphicElements.PetriNetObject;

public class SetNameLabelPositionCommand extends Command {
	PetriNetObject object;
	double newX;
	double newY;
	double oldY;
	double oldX;
		
	public SetNameLabelPositionCommand(PetriNetObject object, double xOffset, double yOffset) {
		this.object = object;
		this.newX = xOffset;
		this.newY = yOffset;
	}

	@Override
	public void undo() {
		object.getNameLabel().setPosition(object.getX() + Grid.getModifiedX(oldX), object.getY() + Grid.getModifiedX(oldY));
		object.updateOnMoveOrZoom();
	}

	@Override
	public void redo() {
		oldY = object.getNameOffsetXObject();
		oldX = object.getNameOffsetYObject();
		
		object.getNameLabel().setPosition(object.getX() + Grid.getModifiedX(newX), object.getY() + Grid.getModifiedX(newY));
		object.updateOnMoveOrZoom();
	}
}


