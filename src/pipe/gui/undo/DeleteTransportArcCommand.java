package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TransportArc;

public class DeleteTransportArcCommand extends TAPNElementCommand {
	private final TimedTransportArcComponent transportArcComponent;
	private final TransportArc transportArc;

	public DeleteTransportArcCommand(TimedTransportArcComponent transportArcComponent, TransportArc transportArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.transportArcComponent = transportArcComponent;
		this.transportArc = transportArc;
	}

	@Override
	public void redo() {
		transportArc.delete();

		TimedTransportArcComponent partner = transportArcComponent.getConnectedTo();

		guiModel.removePetriNetObject(transportArcComponent);
		guiModel.removePetriNetObject(partner);

	}

	@Override
	public void undo() {
		TimedTransportArcComponent partner = transportArcComponent.getConnectedTo();

        transportArcComponent.deselect();

        guiModel.addPetriNetObject(transportArcComponent);
		guiModel.addPetriNetObject(partner);

		tapn.add(transportArc);
	}

}
