package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public abstract class TAPNElementCommand extends Command {

	protected final TimedArcPetriNet tapn;
	protected final DataLayer guiModel;

	public TAPNElementCommand(TimedArcPetriNet tapn, DataLayer guiModel) {
		super();
		this.tapn = tapn;
		this.guiModel = guiModel;
	}
}