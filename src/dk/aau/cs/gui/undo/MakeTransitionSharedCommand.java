package dk.aau.cs.gui.undo;

import pipe.dataLayer.TimedTransitionComponent;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedTransition;

public class MakeTransitionSharedCommand extends Command {
	private final SharedTransition sharedTransition;
	private final TimedTransition timedTransition;
	private final TimedTransitionComponent transitionComponent;
	private final String oldName;
	
	public MakeTransitionSharedCommand(SharedTransition sharedTransition, TimedTransition timedTransition, TimedTransitionComponent transitionComponent){
		this.sharedTransition = sharedTransition;
		this.timedTransition = timedTransition;
		this.transitionComponent = transitionComponent;
		this.oldName = timedTransition.name();
	}
	
	@Override
	public void redo() {
		sharedTransition.makeShared(timedTransition);
		transitionComponent.setName(sharedTransition.name());
		transitionComponent.repaint();
	}

	@Override
	public void undo() {
		timedTransition.unshare();
		transitionComponent.setName(oldName);
		transitionComponent.repaint();
	}

}
