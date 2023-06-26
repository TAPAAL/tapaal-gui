package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLTransitionNode;


public class RenameSharedTransitionVisitor extends VisitorBase  {

	private final String oldName;
	private final String newName;

	public RenameSharedTransitionVisitor(String oldName, String newName) {
		this.oldName = oldName;
		this.newName = newName;
	}
	
	@Override
	public void visit(TCTLTransitionNode node, Object context) {
		if(node.getTemplate().equals("") && node.getTransition().equals(oldName)){
			node.setTransition(newName);
		}
	}
}