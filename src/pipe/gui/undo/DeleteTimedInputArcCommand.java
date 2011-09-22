package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedInputArcCommand extends TAPNElementCommand {
	private final TimedInputArcComponent timedInputArc;

	public DeleteTimedInputArcCommand(TimedInputArcComponent timedInputArc, TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.timedInputArc = timedInputArc;
	}

	@Override
	public void redo() {
		timedInputArc.delete();
		view.repaint();
	}

	@Override
	public void undo() {
		timedInputArc.undelete(view);
		tapn.add(timedInputArc.underlyingTimedInputArc());
		view.repaint();
	}

}
