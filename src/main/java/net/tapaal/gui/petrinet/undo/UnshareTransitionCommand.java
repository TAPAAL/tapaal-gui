package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedTransition;

public class UnshareTransitionCommand implements Command {
	private final SharedTransition sharedTransition;
	private final TimedTransition timedTransition;
	
	public UnshareTransitionCommand(SharedTransition sharedTransition, TimedTransition timedTransition){
		this.sharedTransition = sharedTransition;
		this.timedTransition = timedTransition;
	}
	
	@Override
	public void redo() {
		timedTransition.unshare();
	}

	@Override
	public void undo() {
		sharedTransition.makeShared(timedTransition);
	}
}
