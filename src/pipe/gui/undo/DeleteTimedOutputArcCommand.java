package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedOutputArcCommand extends TAPNElementCommand {
	private final TimedOutputArcComponent arc;

	public DeleteTimedOutputArcCommand(TimedOutputArcComponent arc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.arc = arc;
	}

	@Override
	public void redo() {

		arc.underlyingArc().delete();

		//XXX: Should properly be part of the guiModel
		if (arc.getSource() != null) arc.getSource().removeFromArc(arc);
		if (arc.getTarget() != null) arc.getTarget().removeToArc(arc);

		guiModel.removePetriNetObject(arc);
	}

	@Override
	public void undo() {
		guiModel.addPetriNetObject(arc);
		tapn.add(arc.underlyingArc());

		arc.getSource().addConnectFrom(arc);
		arc.getTarget().addConnectTo(arc);
	}

}
