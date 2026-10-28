package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import net.tapaal.gui.petrinet.model.PetriNetModelEditor;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class AddTimedOutputArcCommand extends TAPNElementCommand {
	private final TimedOutputArcComponent outputArc;
	private final PetriNetModelEditor modelEditor = new PetriNetModelEditor();

	public AddTimedOutputArcCommand(TimedOutputArcComponent outputArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.outputArc = outputArc;
	}

	@Override
	public void undo() {
		modelEditor.removeOutputArc(outputArc.underlyingArc());

		guiModel.removePetriNetObject(outputArc);
	}

	@Override
	public void redo() {
		modelEditor.addOutputArc(tapn, outputArc.underlyingArc());
		guiModel.addPetriNetObject(outputArc);
	}

}
