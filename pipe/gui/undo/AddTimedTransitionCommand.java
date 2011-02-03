package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.gui.DrawingSurfaceImpl;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedTransitionCommand extends AddTAPNElementCommand {

	private final TimedTransitionComponent transition;

	public AddTimedTransitionCommand(TimedTransitionComponent transition,
			TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.transition = transition;
	}

	@Override
	public void undo() {
		transition.delete();
		view.repaint();
	}

	@Override
	public void redo() {
		transition.undelete(view);
		tapn.add(transition.underlyingTransition());
		view.repaint();
	}

}
