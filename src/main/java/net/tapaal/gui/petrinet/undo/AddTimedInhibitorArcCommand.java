package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import net.tapaal.gui.petrinet.model.PetriNetModelEditor;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class AddTimedInhibitorArcCommand extends TAPNElementCommand {
	private final TimedInhibitorArcComponent inhibitorArc;
	private final PetriNetModelEditor modelEditor = new PetriNetModelEditor();

	public AddTimedInhibitorArcCommand(TimedInhibitorArcComponent inhibitorArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.inhibitorArc = inhibitorArc;
	}

	@Override
	public void undo() {
		modelEditor.removeInhibitorArc(inhibitorArc.underlyingTimedInhibitorArc());

		guiModel.removePetriNetObject(inhibitorArc);
	}

	@Override
	public void redo() {
		modelEditor.addInhibitorArc(tapn, inhibitorArc.underlyingTimedInhibitorArc());
		guiModel.addPetriNetObject(inhibitorArc);
	}

}
