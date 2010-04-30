package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredInhibitorArc;
import pipe.dataLayer.colors.ColoredInterval;

public class ColoredInhibitorArcTimeGuardEdit extends TimeGuardEdit {

	private ColoredInhibitorArc arc;
	public ColoredInhibitorArcTimeGuardEdit(ColoredInhibitorArc arc, ColoredInterval oldInterval, ColoredInterval newInterval){
		super(oldInterval,newInterval);
		this.arc = arc;
	}
	@Override
	public void redo() {
		arc.setTimeGuard(getNewInterval());
	}

	@Override
	public void undo() {
		arc.setTimeGuard(getOldInterval());
	}

}
