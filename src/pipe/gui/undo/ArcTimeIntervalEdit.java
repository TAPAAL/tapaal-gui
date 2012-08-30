package pipe.gui.undo;

import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.Weight;

public class ArcTimeIntervalEdit extends Command {
	private TimedOutputArcComponent arc;
	TimeInterval oldTimeInterval;
	TimeInterval newTimeInterval;
	Weight oldWeight;
	Weight newWeight;

	public ArcTimeIntervalEdit(TimedOutputArcComponent arc,
			TimeInterval oldTimeInterval, TimeInterval newTimeInterval, Weight oldWeight, Weight newWeight) {
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
