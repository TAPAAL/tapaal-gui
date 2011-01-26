package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredInterval;
import pipe.dataLayer.colors.ColoredTransportArc;

public class ColoredTransportArcTimeGuardEdit extends TimeGuardEdit {

	private ColoredTransportArc arc;
	public ColoredTransportArcTimeGuardEdit(ColoredTransportArc arc, ColoredInterval oldInterval, ColoredInterval newInterval){
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
