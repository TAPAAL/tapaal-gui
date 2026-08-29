package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import net.tapaal.gui.petrinet.model.PetriNetModelEditor;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class AddTimedInputArcCommand extends TAPNElementCommand {
	private final TimedInputArcComponent timedArc;
	private final PetriNetModelEditor modelEditor = new PetriNetModelEditor();

	public AddTimedInputArcCommand(TimedInputArcComponent timedArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.timedArc = timedArc;
	}

	@Override
	public void undo() {
		modelEditor.removeInputArc(timedArc.underlyingTimedInputArc());

		guiModel.removePetriNetObject(timedArc);
	}

	@Override
	public void redo() {
		modelEditor.addInputArc(tapn, timedArc.underlyingTimedInputArc());
		guiModel.addPetriNetObject(timedArc);
	}
}
