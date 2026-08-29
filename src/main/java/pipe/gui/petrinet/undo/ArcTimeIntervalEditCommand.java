package pipe.gui.petrinet.undo;

import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.Weight;

public class ArcTimeIntervalEditCommand implements Command {
	private final TimedOutputArcComponent arc;
	final TimeInterval oldTimeInterval;
	final TimeInterval newTimeInterval;
	final Weight oldWeight;
	final Weight newWeight;

	public ArcTimeIntervalEditCommand(TimedOutputArcComponent arc,
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
