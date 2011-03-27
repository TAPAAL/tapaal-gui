package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;

public class ContainsAtomicPropWithSharedPlaceVisitor extends VisitorBase implements ITCTLVisitor {
	private String sharedPlaceName;

	public ContainsAtomicPropWithSharedPlaceVisitor(String sharedPlaceName){
		this.sharedPlaceName = sharedPlaceName;
	}

	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context) {
		if(atomicPropositionNode.getTemplate().equals("") && atomicPropositionNode.getPlace().equals(sharedPlaceName)) ((BooleanResult)context).setResult(true);
	}

}
