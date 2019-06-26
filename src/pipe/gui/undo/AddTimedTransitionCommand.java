package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedTransitionCommand extends TAPNElementCommand {

	private final TimedTransitionComponent transition;

	public AddTimedTransitionCommand(TimedTransitionComponent transition,
			TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel);
		this.transition = transition;
	}

	@Override
	public void undo() {
		transition.delete();
	}

	@Override
	public void redo() {
		transition.undelete();
		tapn.add(transition.underlyingTransition());
	}

}
