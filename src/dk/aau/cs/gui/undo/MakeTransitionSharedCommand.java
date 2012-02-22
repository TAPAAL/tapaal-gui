package dk.aau.cs.gui.undo;

import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedTransition;

public class MakeTransitionSharedCommand extends Command {
	private final SharedTransition sharedTransition;
	private final TimedTransition timedTransition;
	private final String oldName;
	
	public MakeTransitionSharedCommand(SharedTransition sharedTransition, TimedTransition timedTransition){
		this.sharedTransition = sharedTransition;
		this.timedTransition = timedTransition;
		oldName = timedTransition.name();
	}
	
	@Override
	public void redo() {
		sharedTransition.makeShared(timedTransition);
	}

	@Override
	public void undo() {
		timedTransition.unshare();
		timedTransition.setName(oldName);
	}

}
