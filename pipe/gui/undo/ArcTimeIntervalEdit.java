package pipe.gui.undo;

import pipe.dataLayer.TimedInputArcComponent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;

public class ArcTimeIntervalEdit extends Command {
	private TimedInputArcComponent arc;
	TimeInterval oldTimeInterval;
	TimeInterval newTimeInterval;

	public ArcTimeIntervalEdit(TimedInputArcComponent arc,
			TimeInterval oldTimeInterval, TimeInterval newTimeInterval) {
		this.arc = arc;
		this.oldTimeInterval = oldTimeInterval;
		this.newTimeInterval = newTimeInterval;
	}

	@Override
	public void redo() {
		arc.setGuard(newTimeInterval);
	}

	@Override
	public void undo() {
		arc.setGuard(oldTimeInterval);
	}

	@Override
	public String toString() {
		return super.toString() + " " + arc.getName() + ", oldTimeInterval: "
				+ oldTimeInterval + ", newTimeInterval: " + newTimeInterval;
	}

}
