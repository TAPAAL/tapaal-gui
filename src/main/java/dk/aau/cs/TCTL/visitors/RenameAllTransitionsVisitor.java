package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLTransitionNode;
import dk.aau.cs.verification.NameMapping;

public class RenameAllTransitionsVisitor extends VisitorBase {

	private final NameMapping mapping;

	public RenameAllTransitionsVisitor(NameMapping mapping) {
		this.mapping = mapping;
	}

	public void visit(TCTLTransitionNode transitionNode,
			Object context) {
		String mappedName = mapping.map(transitionNode.getTemplate(),
				transitionNode.getTransition());
		if (mappedName != null) {
			transitionNode.setTransition(mappedName);
			transitionNode.setTemplate("");
		}
	}
}
