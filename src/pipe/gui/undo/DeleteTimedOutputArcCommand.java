package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
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

		guiModel.removePetriNetObject(arc);
	}

	@Override
	public void undo() {
	    arc.deselect();
		guiModel.addPetriNetObject(arc);
		tapn.add(arc.underlyingArc());
	}

}
