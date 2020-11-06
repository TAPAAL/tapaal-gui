package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

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
