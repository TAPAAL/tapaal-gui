package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLTransitionNode;
import dk.aau.cs.util.Require;

public class MakeTransitionSharedVisitor extends VisitorBase {
	private final String oldTemplateName;
	private final String oldTransitionName;
	private final String newTemplateName;
	private final String newTransitionName;

	public MakeTransitionSharedVisitor(String oldTemplateName, String oldTransitionName, String newTemplateName, String newTransitionName){
		Require.that(oldTemplateName != null, "oldTemplateName cannot be null");
		Require.that(oldTransitionName != null && !oldTransitionName.isEmpty(), "oldTransitionName cannot be null or empty");
		Require.that(newTemplateName != null, "newTemplateName cannot be null");
		Require.that(newTransitionName != null && !newTransitionName.isEmpty(), "newTransitionName cannot be null or empty");
		
		this.oldTemplateName = oldTemplateName;
		this.oldTransitionName = oldTransitionName;
		this.newTemplateName = newTemplateName;
		this.newTransitionName = newTransitionName;
	}
	
	@Override
	public void visit(TCTLTransitionNode node, Object context) {
		if(node.getTemplate().equals(oldTemplateName) && node.getTransition().equals(oldTransitionName)) {
			node.setTemplate(newTemplateName);
			node.setTransition(newTransitionName);
			if(context instanceof BooleanResult)
				((BooleanResult)context).setResult(true);
		}
			
	}
}