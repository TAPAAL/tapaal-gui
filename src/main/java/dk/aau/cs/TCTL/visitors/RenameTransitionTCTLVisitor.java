package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLTransitionNode;

public class RenameTransitionTCTLVisitor extends VisitorBase{

	private final String oldTransitionName;
	private final String newTransitionName;

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
