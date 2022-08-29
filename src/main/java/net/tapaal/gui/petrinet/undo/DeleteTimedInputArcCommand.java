package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class DeleteTimedInputArcCommand extends TAPNElementCommand {
	private final TimedInputArcComponent timedInputArc;

	public DeleteTimedInputArcCommand(TimedInputArcComponent timedInputArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.timedInputArc = timedInputArc;
	}

	@Override
	public void redo() {
		timedInputArc.underlyingTimedInputArc().delete();

		guiModel.removePetriNetObject(timedInputArc);
	}

	@Override
	public void undo() {
        timedInputArc.deselect();
		guiModel.addPetriNetObject(timedInputArc);
		tapn.add(timedInputArc.underlyingTimedInputArc());
	}

}
