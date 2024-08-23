package pipe.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public abstract class TAPNElementCommand implements Command {

	protected final TimedArcPetriNet tapn;
	protected final DataLayer guiModel;

	public TAPNElementCommand(TimedArcPetriNet tapn, DataLayer guiModel) {
		super();
		this.tapn = tapn;
		this.guiModel = guiModel;
	}
}