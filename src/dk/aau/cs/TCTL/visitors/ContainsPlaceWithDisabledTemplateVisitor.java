package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class ContainsPlaceWithDisabledTemplateVisitor extends VisitorBase implements ITCTLVisitor {
	
	private TimedArcPetriNetNetwork network;

	public ContainsPlaceWithDisabledTemplateVisitor(TimedArcPetriNetNetwork network){
		this.network = network;
	}

	@Override
	public void visit(TCTLPlaceNode placeNode, Object context) {
		if(placeNode.getTemplate().equals("") && network.getSharedPlaceByName(placeNode.getPlace()) != null)
			return;
		
		for(TimedArcPetriNet net : network.activeTemplates()) {
			if(placeNode.getTemplate().equals(net.name()) && net.getPlaceByName(placeNode.getPlace()) != null) 
				return;
		}
		
		if(context instanceof BooleanResult)
			((BooleanResult)context).setResult(false);
	}
}
