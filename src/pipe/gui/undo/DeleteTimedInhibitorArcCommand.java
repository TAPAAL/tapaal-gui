package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedInhibitorArcCommand extends TAPNElementCommand {
	private final TimedInhibitorArcComponent inhibitorArc;

	public DeleteTimedInhibitorArcCommand(TimedInhibitorArcComponent inhibitorArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.inhibitorArc = inhibitorArc;
	}

	@Override
	public void redo() {
		inhibitorArc.underlyingTimedInhibitorArc().delete();

		//XXX: Should properly be part of the guiModel
		if (inhibitorArc.getSource() != null) inhibitorArc.getSource().removeFromArc(inhibitorArc);
		if (inhibitorArc.getTarget() != null) inhibitorArc.getTarget().removeToArc(inhibitorArc);

		guiModel.removePetriNetObject(inhibitorArc);
	}

	@Override
	public void undo() {
		guiModel.addPetriNetObject(inhibitorArc);

		//XXX: Should properly be part of the guiModel
		if (inhibitorArc.getSource() != null) inhibitorArc.getSource().addConnectFrom(inhibitorArc);
		if (inhibitorArc.getTarget() != null) inhibitorArc.getTarget().addConnectTo(inhibitorArc);

		tapn.add(inhibitorArc.underlyingTimedInhibitorArc());
	}
}
