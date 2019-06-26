package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedOutputArcCommand extends TAPNElementCommand {
	private final TimedOutputArcComponent outputArc;

	public AddTimedOutputArcCommand(TimedOutputArcComponent outputArc,
			TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.outputArc = outputArc;
	}

	@Override
	public void undo() {
		outputArc.delete();
	}

	@Override
	public void redo() {
		outputArc.undelete();
		tapn.add(outputArc.underlyingArc());
	}

}
