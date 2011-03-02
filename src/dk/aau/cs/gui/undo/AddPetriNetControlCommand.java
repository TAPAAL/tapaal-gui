package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.DrawingSurface;
import dk.aau.cs.gui.components.PetriNetElementControl;

public class AddPetriNetControlCommand extends Command {
	private PetriNetElementControl control;
	private DrawingSurface surface;

	public AddPetriNetControlCommand(PetriNetElementControl control,
			DrawingSurface surface) {
		this.surface = surface;
		this.control = control;
	}

	@Override
	public void redo() {
		control.addChildControls();
		surface.add(control);
		// surface.surfaceChanged();
	}

	@Override
	public void undo() {
		control.removeChildControls();
		surface.remove(control);
		// surface.surfaceChanged();
	}
}
