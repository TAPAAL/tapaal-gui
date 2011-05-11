package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;

public class UpwardsClosedVisitor extends VisitorBase {
	public boolean isUpwardClosed(TCTLAbstractProperty query){
		if(query instanceof TCTLPathPlaceHolder)
			return true;
		
		Context c = new Context();
		c.result = true; // assume upward closed
		query.accept(this, c);
		return (c.queryType.equals(QueryType.EF) || c.queryType.equals(QueryType.AG)) && c.result;
	}
	
	public void visit(TCTLEFNode efNode, Object context) {
		((Context)context).queryType = QueryType.EF;
		super.visit(efNode, context);
	}
	
	public void visit(TCTLEGNode egNode, Object context) {
		((Context)context).queryType = QueryType.EG;
		super.visit(egNode, context);
	}
	
	public void visit(TCTLAFNode afNode, Object context) {
		((Context)context).queryType = QueryType.AF;
		super.visit(afNode, context);
	}
	
	public void visit(TCTLAGNode agNode, Object context) {
		((Context)context).queryType = QueryType.AG;
		super.visit(agNode, context);
	}
	
	public void visit(TCTLNotNode notNode, Object context) {
		Context c = (Context)context;
		c.result = false;
		//super.visit(notNode, context); // At the moment we dont want to allow not in upward closed queries so no need to visit subexpressions
	}
	
	public void visit(TCTLAtomicPropositionNode node, Object context) {
		Context c = (Context)context;
		if(c.queryType.equals(QueryType.EF)){
			if(!node.getOp().equals(">") && !node.getOp().equals(">=")){
				c.result = false;
			}
		}else if(c.queryType.equals(QueryType.AG)){
			if(!node.getOp().equals("<") && !node.getOp().equals("<=")){
				c.result = false;
			}
		}else{
			c.result = false;
		}
	}
	
	private enum QueryType { EF, AG, AF, EG }
	private class Context
	{
		public boolean result;
		public QueryType queryType;
	}
}
