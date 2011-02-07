package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.gui.DrawingSurfaceImpl;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedTransitionCommand extends TAPNElementCommand {
	private final TimedTransitionComponent transition;

	public DeleteTimedTransitionCommand(TimedTransitionComponent transition, TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.transition = transition;
	}

	@Override
	public void redo() {
		transition.delete();
		view.repaint();
	}

	@Override
	public void undo() {
		transition.undelete(view);
		tapn.add(transition.underlyingTransition());
		view.repaint();
	}

}
