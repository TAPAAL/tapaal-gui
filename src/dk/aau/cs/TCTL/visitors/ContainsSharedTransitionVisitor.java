package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLTransitionNode;

public class ContainsSharedTransitionVisitor extends VisitorBase implements ITCTLVisitor {
	private String sharedTransitionName;

	public ContainsSharedTransitionVisitor(String sharedTransitionName){
		this.sharedTransitionName = sharedTransitionName;
	}

	@Override
	public void visit(TCTLTransitionNode transitionNode, Object context) {
		if(transitionNode.getTemplate().equals("") && transitionNode.getTransition().equals(sharedTransitionName))
			((BooleanResult)context).setResult(true);
	}
}
