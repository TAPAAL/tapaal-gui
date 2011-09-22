package pipe.gui.undo;

import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.DataLayer;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public abstract class TAPNElementCommand extends Command {

	protected final DrawingSurfaceImpl view;
	protected final TimedArcPetriNet tapn;
	protected final DataLayer guiModel;

	public TAPNElementCommand(TimedArcPetriNet tapn, DataLayer guiModel,
			DrawingSurfaceImpl view) {
		super();
		this.tapn = tapn;
		this.guiModel = guiModel;
		this.view = view;
	}
}