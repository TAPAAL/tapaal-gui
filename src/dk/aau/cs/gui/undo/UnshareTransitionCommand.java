package dk.aau.cs.gui.undo;

import pipe.dataLayer.TimedTransitionComponent;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedTransition;

public class UnshareTransitionCommand extends Command {
	private final SharedTransition sharedTransition;
	private final TimedTransition timedTransition;
	private final TimedTransitionComponent transitionComponent;
	
	public UnshareTransitionCommand(SharedTransition sharedTransition, TimedTransition timedTransition, TimedTransitionComponent transitionComponent){
		this.sharedTransition = sharedTransition;
		this.timedTransition = timedTransition;
		this.transitionComponent = transitionComponent;
	}
	
	@Override
	public void redo() {
		timedTransition.unshare();
		transitionComponent.repaint();
	}

	@Override
	public void undo() {
		sharedTransition.makeShared(timedTransition);
		transitionComponent.setName(sharedTransition.name());
		transitionComponent.repaint();
	}
}
