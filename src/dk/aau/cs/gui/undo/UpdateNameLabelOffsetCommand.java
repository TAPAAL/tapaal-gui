package dk.aau.cs.gui.undo;

import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PetriNetObjectWithLabel;

public class UpdateNameLabelOffsetCommand extends Command {
	int newXOffset, newYOffset, oldXOffset, oldYOffset;
	PetriNetObjectWithLabel obj;
	
	public UpdateNameLabelOffsetCommand(int newXOffset, int newYOffset, int oldXOffset, int oldYOffset, PetriNetObjectWithLabel obj) {
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
