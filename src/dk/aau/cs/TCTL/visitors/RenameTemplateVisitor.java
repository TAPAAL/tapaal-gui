package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.util.Require;

public class RenameTemplateVisitor extends VisitorBase {
	private final String oldName;
	private final String newName;

	public RenameTemplateVisitor(String oldName, String newName){
		Require.that(oldName != null && !oldName.isEmpty(), "oldName cannot be null or empty");
		Require.that(newName != null && !newName.isEmpty(), "newName cannot be null or empty");
		this.oldName = oldName;
		this.newName = newName;
	}
	
	@Override
	public void visit(TCTLAtomicPropositionNode node, Object context) {
		if(node.getTemplate().equals(oldName)) node.setTemplate(newName);
	}
}
