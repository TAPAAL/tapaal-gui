package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAndNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLOrNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.petrinet.TAPNQuery;

public abstract class QueryVisitor implements ITCTLVisitor {
	protected enum QueryType { EF, EG, AF, AG }
	private StringBuffer uppaalQuery;

	public QueryVisitor() {
	}
	
	protected void append(String string){
		uppaalQuery.append(string);
	}
	
	protected void append(int number){
		uppaalQuery.append(number);
	}

	public String getUppaalQueryFor(TAPNQuery tapnQuery) {
		this.uppaalQuery = new StringBuffer();
		tapnQuery.getProperty().accept(this);
		return uppaalQuery.toString();
	}

	public void visit(TCTLAFNode afNode) {
		uppaalQuery.append("A<> ");
		afNode.getProperty().accept(this);
		addEnding(QueryType.AF);
	}

	public void visit(TCTLAGNode agNode) {
		uppaalQuery.append("A[] ");
		agNode.getProperty().accept(this);
		addEnding(QueryType.AG);
	}

	public void visit(TCTLEFNode efNode) {
		uppaalQuery.append("E<> ");
		efNode.getProperty().accept(this);
		addEnding(QueryType.EF);
	}

	public void visit(TCTLEGNode egNode) {
		uppaalQuery.append("E[] ");
		egNode.getProperty().accept(this);
		addEnding(QueryType.EG);
	}

	public void visit(TCTLAndNode andNode) {
		uppaalQuery.append("(");
		andNode.getProperty1().accept(this);
		uppaalQuery.append(" && ");
		andNode.getProperty2().accept(this);
		uppaalQuery.append(")");
	}
	
	public void visit(TCTLAndListNode andListNode) {
		uppaalQuery.append("(");
		boolean firstTime = true;
		
		for (TCTLAbstractStateProperty p : andListNode.getProperties()) {
			if(!firstTime) {
				uppaalQuery.append(" && ");
			}
			
			p.accept(this);
			firstTime = false;
		}
		
		uppaalQuery.append(")");
		
	}

	public void visit(TCTLOrNode orNode) {
		uppaalQuery.append("(");
		orNode.getProperty1().accept(this);
		uppaalQuery.append(" || ");
		orNode.getProperty2().accept(this);
		uppaalQuery.append(")");
	}
	
	public void visit(TCTLOrListNode orListNode) {
		uppaalQuery.append("(");
		boolean firstTime = true;
		
		for (TCTLAbstractStateProperty p : orListNode.getProperties()) {
			if(!firstTime) {
				uppaalQuery.append(" && ");
			}
			
			p.accept(this);
			firstTime = false;
		}
		
		uppaalQuery.append(")");
		
	}
	
	public void visit(TCTLNotNode notNode) {
		uppaalQuery.append("!");
		uppaalQuery.append("(");
		notNode.getProperty().accept(this);
		uppaalQuery.append(")");
	}

	public void visit(TCTLStatePlaceHolder statePlaceHolderNode) {
	}

	public void visit(TCTLPathPlaceHolder pathPlaceHolderNode) {
	
	}

	protected String OperatorConversion(String op) {
		if(op.equals("="))
			return "==";
	
		return op;
	}

	public abstract void visit(TCTLAtomicPropositionNode atomicPropositionNode);
	protected abstract void addEnding(QueryType queryType);
}