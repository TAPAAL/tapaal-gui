package dk.aau.cs.gui.undo;

import pipe.gui.graphicElements.PetriNetObject;

public class ChangeNameOffsetCommand extends Command {
	PetriNetObject object;
	double newX;
	double newY;
	double oldY;
	double oldX;
		
	public ChangeNameOffsetCommand(PetriNetObject object, double xOffset, double yOffset) {
		this.object = object;
		this.newX = xOffset;
		this.newY = yOffset;
		
		
	}

	@Override
	public void undo() {
		object.setNameOffsetX(oldX);
		object.setNameOffsetY(oldY);
		
		object.updateOnMoveOrZoom();
	}

	@Override
	public void redo() {
		oldY = object.getNameOffsetXObject();
		oldX = object.getNameOffsetYObject();
		
		
		object.setNameOffsetX(newX);
		object.setNameOffsetY(newY);
		
		//object.updateOnMoveOrZoom();
	}
}


