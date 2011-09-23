package dk.aau.cs.gui.undo;

import pipe.gui.graphicElements.tapn.TimedPlaceComponent;

// TODO: Fix this to work on the model class instead of the GUI class
public class TimedPlaceMarkingEdit extends Command {
	private int numberOfTokens;
	private final TimedPlaceComponent timedPlaceComponent;

	public TimedPlaceMarkingEdit(TimedPlaceComponent tpc, int numberOfTokens) {
		timedPlaceComponent = tpc;
		this.numberOfTokens = numberOfTokens;
	}

	@Override
	public void redo() {
		if (numberOfTokens > 0) {
			timedPlaceComponent.addTokens(Math.abs(numberOfTokens));
		} else {
			timedPlaceComponent.removeTokens(Math.abs(numberOfTokens));
		}
		timedPlaceComponent.repaint();
	}

	@Override
	public void undo() {
		if (numberOfTokens > 0) {
			timedPlaceComponent.removeTokens(Math.abs(numberOfTokens));
		} else {
			timedPlaceComponent.addTokens(Math.abs(numberOfTokens));
		}
		timedPlaceComponent.repaint();
	}

}
