package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedTransitionCommand extends TAPNElementCommand {

	private final TimedTransitionComponent transition;

	public AddTimedTransitionCommand(TimedTransitionComponent transition, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.transition = transition;
	}

	@Override
	public void undo() {
		transition.underlyingTransition().delete();
		guiModel.removePetriNetObject(transition);
	}

	@Override
	public void redo() {
		guiModel.addPetriNetObject(transition);
		tapn.add(transition.underlyingTransition());
	}

}
