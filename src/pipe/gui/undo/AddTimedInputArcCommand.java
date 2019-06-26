package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedInputArcCommand extends TAPNElementCommand {
	private final TimedInputArcComponent timedArc;

	public AddTimedInputArcCommand(TimedInputArcComponent timedArc,
			TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.timedArc = timedArc;
	}

	@Override
	public void undo() {
		timedArc.delete();
	}

	@Override
	public void redo() {
		timedArc.undelete();
		tapn.add(timedArc.underlyingTimedInputArc());
	}
}
