package net.tapaal.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class AddTimedTransitionCommand extends TAPNElementCommand {

	private final TimedTransitionComponent transition;

	public AddTimedTransitionCommand(TimedTransitionComponent transition, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.transition = transition;
	}

	@Override
	public void undo() {
		transition.underlyingTransition().delete();
		guiModel.removePetriNetObject(transition);
	}

	@Override
	public void redo() {
		guiModel.addPetriNetObject(transition);
		tapn.add(transition.underlyingTransition());
	}

}
