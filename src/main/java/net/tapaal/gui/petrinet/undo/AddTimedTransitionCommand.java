package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import net.tapaal.gui.petrinet.model.PetriNetModelEditor;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class AddTimedTransitionCommand extends TAPNElementCommand {

	private final TimedTransitionComponent transition;
	private final PetriNetModelEditor modelEditor = new PetriNetModelEditor();

	public AddTimedTransitionCommand(TimedTransitionComponent transition, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.transition = transition;
	}

	@Override
	public void undo() {
		modelEditor.removeTransition(tapn, transition.underlyingTransition());
		guiModel.removePetriNetObject(transition);
	}

	@Override
	public void redo() {
		modelEditor.addTransition(tapn, transition.underlyingTransition());
		guiModel.addPetriNetObject(transition);
	}

}
