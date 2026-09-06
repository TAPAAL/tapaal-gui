package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import net.tapaal.gui.petrinet.model.PetriNetModelEditor;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class AddTimedPlaceCommand extends TAPNElementCommand {
	final TimedPlaceComponent timedPlace;
	private final PetriNetModelEditor modelEditor = new PetriNetModelEditor();

	public AddTimedPlaceCommand(TimedPlaceComponent timedPlace,
			TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.timedPlace = timedPlace;
	}

	@Override
	public void undo() {
		modelEditor.removePlace(tapn, timedPlace.underlyingPlace());
		guiModel.removePetriNetObject(timedPlace);
	}

	@Override
	public void redo() {
		modelEditor.addPlace(tapn, timedPlace.underlyingPlace());
		// Attach the domain object first; adding it to a connected DataLayer can
		// repaint immediately and the component reads its marking during update.
		guiModel.addPetriNetObject(timedPlace);
	}
}
