package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedOutputArcCommand extends TAPNElementCommand {
	private final TimedOutputArcComponent arc;

	public DeleteTimedOutputArcCommand(TimedOutputArcComponent arc, TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.arc = arc;
	}

	@Override
	public void redo() {
		arc.delete();
		view.repaint();
	}

	@Override
	public void undo() {
		arc.undelete(view);
		tapn.add(arc.underlyingArc());
		view.repaint();
	}

}
