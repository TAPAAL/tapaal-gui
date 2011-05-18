package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class ContainsAtomicPropositionsWithDisabledTemplateVisitor extends VisitorBase implements ITCTLVisitor {
	
	private TimedArcPetriNetNetwork network;

	public ContainsAtomicPropositionsWithDisabledTemplateVisitor(TimedArcPetriNetNetwork network){
		this.network = network;
	}

	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context) {
		if(atomicPropositionNode.getTemplate().equals("") && network.getSharedPlaceByName(atomicPropositionNode.getPlace()) != null)
			return;
		
		for(TimedArcPetriNet net : network.activeTemplates()) {
			if(atomicPropositionNode.getTemplate().equals(net.name()) && net.getPlaceByName(atomicPropositionNode.getPlace()) != null) 
				return;
		}
		
		if(context instanceof BooleanResult)
			((BooleanResult)context).setResult(false);
	}
}
