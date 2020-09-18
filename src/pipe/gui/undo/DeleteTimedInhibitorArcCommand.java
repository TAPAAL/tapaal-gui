package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
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

		guiModel.removePetriNetObject(inhibitorArc);
	}

	@Override
	public void undo() {
	    inhibitorArc.deselect();
		guiModel.addPetriNetObject(inhibitorArc);
		tapn.add(inhibitorArc.underlyingTimedInhibitorArc());
	}
}
