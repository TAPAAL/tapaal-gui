package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLTransitionNode;

public class RenameTransitionTCTLVisitor extends VisitorBase{

	private String oldTransitionName;
	private String newTransitionName;

	public RenameTransitionTCTLVisitor(String oldName, String newName) {
		oldTransitionName = oldName;
		newTransitionName = newName;
	}

	public void visit(TCTLTransitionNode transitionNode,
			Object context) {
		if (transitionNode.getTransition().equals(oldTransitionName)) {
			transitionNode.setTransition(newTransitionName);
		}
	}
}
