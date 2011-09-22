package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedInhibitorArcCommand extends TAPNElementCommand {
	private final TimedInhibitorArcComponent inhibitorArc;

	public DeleteTimedInhibitorArcCommand(TimedInhibitorArcComponent inhibitorArc, TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.inhibitorArc = inhibitorArc;
	}

	@Override
	public void redo() {
		inhibitorArc.delete();
		view.repaint();
	}

	@Override
	public void undo() {
		inhibitorArc.undelete(view);
		tapn.add(inhibitorArc.underlyingTimedInhibitorArc());
		view.repaint();
	}
}
