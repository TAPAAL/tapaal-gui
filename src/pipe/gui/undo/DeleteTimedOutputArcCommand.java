package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedOutputArcCommand extends TAPNElementCommand {
	private final TimedOutputArcComponent arc;

	public DeleteTimedOutputArcCommand(TimedOutputArcComponent arc, TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel);
		this.arc = arc;
	}

	@Override
	public void redo() {
		arc.delete();
	}

	@Override
	public void undo() {
		arc.undelete();
		tapn.add(arc.underlyingArc());
	}

}
