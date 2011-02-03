package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredInterval;
import dk.aau.cs.gui.undo.Command;

public abstract class TimeGuardEdit extends Command {

	private ColoredInterval newInterval;
	private ColoredInterval oldInterval;

	public TimeGuardEdit(ColoredInterval oldInterval,
			ColoredInterval newInterval) {
		super();
		this.oldInterval = oldInterval;
		this.newInterval = newInterval;
	}

	protected void setOldInterval(ColoredInterval oldInterval) {
		this.oldInterval = oldInterval;
	}

	protected ColoredInterval getOldInterval() {
		return oldInterval;
	}

	protected void setNewInterval(ColoredInterval newInterval) {
		this.newInterval = newInterval;
	}

	protected ColoredInterval getNewInterval() {
		return newInterval;
	}

}