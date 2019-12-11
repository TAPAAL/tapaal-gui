package dk.aau.cs.gui.undo;

import pipe.gui.graphicElements.PetriNetObject;

public class UpdateNameLabelOffsetCommand extends Command {
	double newXOffset, newYOffset, oldXOffset, oldYOffset;
	PetriNetObject obj;
	
	public UpdateNameLabelOffsetCommand(double newXOffset, double newYOffset, double oldXOffset, double oldYOffset, PetriNetObject obj) {
		this.obj = obj;
		this.newXOffset = newXOffset;
		this.newYOffset = newYOffset;
		this.oldXOffset = oldXOffset;
		this.oldYOffset = oldYOffset;
	}

	@Override
	public void undo() {
		obj.setNameOffsetX(oldXOffset);;
		obj.setNameOffsetY(oldYOffset);
		obj.updateOnMoveOrZoom();		
	}

	@Override
	public void redo() {
		obj.setNameOffsetX(newXOffset);
		obj.setNameOffsetY(newYOffset);
		obj.updateOnMoveOrZoom();		
	}

}
