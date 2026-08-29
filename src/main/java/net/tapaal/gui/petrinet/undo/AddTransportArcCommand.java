package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TransportArc;
import net.tapaal.gui.petrinet.model.PetriNetModelEditor;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class AddTransportArcCommand extends TAPNElementCommand {
	private final TimedTransportArcComponent transportArcComponent;
	private final TransportArc transportArc;
	private final PetriNetModelEditor modelEditor = new PetriNetModelEditor();

	public AddTransportArcCommand(TimedTransportArcComponent transportArcComponent, TransportArc transportArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.transportArcComponent = transportArcComponent;
		this.transportArc = transportArc;
	}

	@Override
	public void undo() {
		modelEditor.removeTransportArc(transportArc);

		TimedTransportArcComponent partner = transportArcComponent.getConnectedTo();

		guiModel.removePetriNetObject(transportArcComponent);
		guiModel.removePetriNetObject(partner);


	}

	@Override
	public void redo() {
		TimedTransportArcComponent partner = transportArcComponent.getConnectedTo();

		modelEditor.addTransportArc(tapn, transportArc);
		guiModel.addPetriNetObject(transportArcComponent);
		guiModel.addPetriNetObject(partner);
	}
}
