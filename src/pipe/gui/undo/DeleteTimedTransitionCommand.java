package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedTransitionCommand extends TAPNElementCommand {
	private final TimedTransitionComponent transition;
	private SharedTransition sharedTransition;

	public DeleteTimedTransitionCommand(TimedTransitionComponent transition, TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.transition = transition;

		sharedTransition = transition.underlyingTransition().sharedTransition();
	}

	@Override
	public void redo() {
		transition.delete();
		view.repaint();
	}

	@Override
	public void undo() {
		transition.undelete(view);
		if(sharedTransition != null) {
			sharedTransition.makeShared(transition.underlyingTransition());
		}
		tapn.add(transition.underlyingTransition());
		view.repaint();
	}

}
