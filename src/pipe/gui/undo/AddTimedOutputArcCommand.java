package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedOutputArcCommand extends TAPNElementCommand {
	private final TimedOutputArcComponent outputArc;

	public AddTimedOutputArcCommand(TimedOutputArcComponent outputArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.outputArc = outputArc;
	}

	@Override
	public void undo() {
		outputArc.underlyingArc().delete();

		//XXX: Should properly be part of the guiModel
		if (outputArc.getSource() != null) outputArc.getSource().removeFromArc(outputArc);
		if (outputArc.getTarget() != null) outputArc.getTarget().removeToArc(outputArc);

		guiModel.removePetriNetObject(outputArc);
	}

	@Override
	public void redo() {
		guiModel.addPetriNetObject(outputArc);

		//XXX: Should properly be part of the guiModel
		if (outputArc.getSource() != null) outputArc.getSource().addConnectFrom(outputArc);
		if (outputArc.getTarget() != null) outputArc.getTarget().addConnectTo(outputArc);

		tapn.add(outputArc.underlyingArc());
	}

}
