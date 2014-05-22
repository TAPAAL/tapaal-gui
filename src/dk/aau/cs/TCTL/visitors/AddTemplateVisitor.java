package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLPlaceNode;

public class AddTemplateVisitor extends VisitorBase {
	private String templateName;

	public AddTemplateVisitor(String templateName) {
		this.templateName = templateName;
	}

	@Override
	public void visit(TCTLPlaceNode tctlPlaceNode, Object context) {
		tctlPlaceNode.setTemplate(templateName);
	}
}
