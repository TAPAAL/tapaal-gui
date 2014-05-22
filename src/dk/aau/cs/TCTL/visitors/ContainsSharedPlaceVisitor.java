package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLPlaceNode;

public class ContainsSharedPlaceVisitor extends VisitorBase implements ITCTLVisitor {
	private String sharedPlaceName;

	public ContainsSharedPlaceVisitor(String sharedPlaceName){
		this.sharedPlaceName = sharedPlaceName;
	}

	@Override
	public void visit(TCTLPlaceNode placeNode, Object context) {
		if(placeNode.getTemplate().equals("") && placeNode.getPlace().equals(sharedPlaceName)) 
			((BooleanResult)context).setResult(true);
	}

}
