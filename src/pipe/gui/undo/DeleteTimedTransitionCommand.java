package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedTransitionCommand extends TAPNElementCommand {
	private final TimedTransitionComponent transition;
	private final SharedTransition sharedTransition;

	public DeleteTimedTransitionCommand(TimedTransitionComponent transition, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.transition = transition;

		sharedTransition = transition.underlyingTransition().sharedTransition();
	}

	@Override
	public void redo() {
		transition.underlyingTransition().delete();
		guiModel.removePetriNetObject(transition);
	}

	@Override
	public void undo() {
        transition.deselect();
		guiModel.addPetriNetObject(transition);
		if(sharedTransition != null) {
			sharedTransition.makeShared(transition.underlyingTransition());
		}
		tapn.add(transition.underlyingTransition());
	}

}
