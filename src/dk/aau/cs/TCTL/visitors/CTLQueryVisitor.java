package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.TAPNQuery;

public class CTLQueryVisitor extends VisitorBase {
	private StringBuffer XMLQuery;
	
	public String getXMLQueryFor(TAPNQuery tapnQuery) {
		XMLQuery = new StringBuffer();
		XMLQuery.append("<?xml version=\"1.0\"?>\n");
		tapnQuery.getProperty().accept(this, null);
		return XMLQuery.toString();
	}
	
	public void visit(TCTLEFNode efNode, Object context) {
		XMLQuery.append("<exists−path>\n");
		XMLQuery.append("<finally>\n");
		efNode.getProperty().accept(this, context);
		XMLQuery.append("<\\finally>\n");
		XMLQuery.append("<\\exists−path>\n");
	}
	
	public void visit(TCTLTrueNode tctlTrueNode, Object context) {
		XMLQuery.append(tctlTrueNode.toString());		
	}
}
