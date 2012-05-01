package pipe.gui.undo;

import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;

public class ArcTimeIntervalEdit extends Command {
	private TimedInputArcComponent arc;
	TimeInterval oldTimeInterval;
	TimeInterval newTimeInterval;
	int oldWeight;
	int newWeight;

	public ArcTimeIntervalEdit(TimedInputArcComponent arc,
			TimeInterval oldTimeInterval, TimeInterval newTimeInterval, int oldWeight, int newWeight) {
		this.arc = arc;
		this.oldTimeInterval = oldTimeInterval;
		this.newTimeInterval = newTimeInterval;
		this.oldWeight = oldWeight;
		this.newWeight = newWeight;
	}

	@Override
	public void redo() {
		arc.setGuardAndWeight(newTimeInterval, newWeight);
	}

	@Override
	public void undo() {
		arc.setGuardAndWeight(oldTimeInterval, oldWeight);
	}

	@Override
	public String toString() {
		return super.toString() + " " + arc.getName() + ", oldTimeInterval: "
				+ oldTimeInterval + ", newTimeInterval: " + newTimeInterval;
	}

}
