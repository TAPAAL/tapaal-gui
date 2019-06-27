package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
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

		//XXX: Should properly be part of the guiModel
		if (transportArcComponent.getSource() != null) transportArcComponent.getSource().removeFromArc(transportArcComponent);
		if (transportArcComponent.getTarget() != null) transportArcComponent.getTarget().removeToArc(transportArcComponent);

		if (partner.getSource() != null) partner.getSource().removeFromArc(partner);
		if (partner.getTarget() != null) partner.getTarget().removeToArc(partner);


		guiModel.removePetriNetObject(transportArcComponent);
		guiModel.removePetriNetObject(partner);


	}

	@Override
	public void redo() {
		TimedTransportArcComponent partner = transportArcComponent.getConnectedTo();

		guiModel.addPetriNetObject(transportArcComponent);
		guiModel.addPetriNetObject(partner);

		//XXX: Should properly be part of the guiModel
		if (transportArcComponent.getSource() != null) transportArcComponent.getSource().addConnectFrom(transportArcComponent);
		if (transportArcComponent.getTarget() != null) transportArcComponent.getTarget().addConnectTo(transportArcComponent);

		if (partner.getSource() != null) partner.getSource().addConnectFrom(partner);
		if (partner.getTarget() != null) partner.getTarget().addConnectTo(partner);

		tapn.add(transportArc);
	}
}
