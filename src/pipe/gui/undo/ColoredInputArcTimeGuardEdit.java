package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredInputArc;
import pipe.dataLayer.colors.ColoredInterval;

public class ColoredInputArcTimeGuardEdit extends TimeGuardEdit {

	private ColoredInputArc arc;
	public ColoredInputArcTimeGuardEdit(ColoredInputArc arc, ColoredInterval oldInterval, ColoredInterval newInterval){
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
