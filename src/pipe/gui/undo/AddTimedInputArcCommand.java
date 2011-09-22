package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedInputArcCommand extends TAPNElementCommand {
	private final TimedInputArcComponent timedArc;

	public AddTimedInputArcCommand(TimedInputArcComponent timedArc,
			TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.timedArc = timedArc;
	}

	@Override
	public void undo() {
		timedArc.delete();
		view.repaint();
	}

	@Override
	public void redo() {
		timedArc.undelete(view);
		tapn.add(timedArc.underlyingTimedInputArc());
		view.repaint();
	}
}
