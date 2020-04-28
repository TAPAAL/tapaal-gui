package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TransportArc;

public class AddTransportArcCommand extends TAPNElementCommand {
	private final TimedTransportArcComponent transportArcComponent;
	private final TransportArc transportArc;

	public AddTransportArcCommand(TimedTransportArcComponent transportArcComponent, TransportArc transportArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.transportArcComponent = transportArcComponent;
		this.transportArc = transportArc;
	}

	@Override
	public void undo() {
		transportArc.delete();

		TimedTransportArcComponent partner = transportArcComponent.getConnectedTo();

		guiModel.removePetriNetObject(transportArcComponent);
		guiModel.removePetriNetObject(partner);


	}

	@Override
	public void redo() {
		TimedTransportArcComponent partner = transportArcComponent.getConnectedTo();

		guiModel.addPetriNetObject(transportArcComponent);
		guiModel.addPetriNetObject(partner);

		tapn.add(transportArc);
	}
}
