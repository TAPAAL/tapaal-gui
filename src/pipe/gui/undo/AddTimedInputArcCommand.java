package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
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

		//XXX: Should properly be part of the guiModel
		if (timedArc.getSource() != null) timedArc.getSource().removeFromArc(timedArc);
		if (timedArc.getTarget() != null) timedArc.getTarget().removeToArc(timedArc);

		guiModel.removePetriNetObject(timedArc);
	}

	@Override
	public void redo() {
		guiModel.addPetriNetObject(timedArc);

		//XXX: Should properly be part of the guiModel
		if (timedArc.getSource() != null) timedArc.getSource().addConnectFrom(timedArc);
		if (timedArc.getTarget() != null) timedArc.getTarget().addConnectTo(timedArc);

		tapn.add(timedArc.underlyingTimedInputArc());
	}
}
