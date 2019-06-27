package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
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

		//XXX: Should properly be part of the guiModel
		if (timedInputArc.getSource() != null) timedInputArc.getSource().removeFromArc(timedInputArc);
		if (timedInputArc.getTarget() != null) timedInputArc.getTarget().removeToArc(timedInputArc);

		guiModel.removePetriNetObject(timedInputArc);
	}

	@Override
	public void undo() {

		guiModel.addPetriNetObject(timedInputArc);

		//XXX: Should properly be part of the guiModel
		if (timedInputArc.getSource() != null) timedInputArc.getSource().addConnectFrom(timedInputArc);
		if (timedInputArc.getTarget() != null) timedInputArc.getTarget().addConnectTo(timedInputArc);

		tapn.add(timedInputArc.underlyingTimedInputArc());
	}

}
