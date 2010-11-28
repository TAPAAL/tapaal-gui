package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TimedOutputArcComponent;
import pipe.gui.DrawingSurfaceImpl;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedOutputArcCommand extends AddTAPNElementCommand {
	private final TimedOutputArcComponent outputArc;

	public AddTimedOutputArcCommand(TimedOutputArcComponent outputArc, TimedArcPetriNet tapn, DataLayer guiModel,
			DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		// TODO Auto-generated constructor stub
		this.outputArc = outputArc;
	}

	@Override
	public void undo() {
		outputArc.delete();
		view.repaint();
	}
	
	@Override
	public void redo() {
		outputArc.undelete(view);
		tapn.add(outputArc.underlyingArc());
		view.repaint();
	}

}
