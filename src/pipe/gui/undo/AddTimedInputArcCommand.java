package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedInputArcCommand extends TAPNElementCommand {
	private final TimedInputArcComponent timedArc;

	public AddTimedInputArcCommand(TimedInputArcComponent timedArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.timedArc = timedArc;
	}

	@Override
	public void undo() {
		timedArc.underlyingTimedInputArc().delete();

		guiModel.removePetriNetObject(timedArc);
	}

	@Override
	public void redo() {
		guiModel.addPetriNetObject(timedArc);

		tapn.add(timedArc.underlyingTimedInputArc());
	}
}
