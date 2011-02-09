package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.petrinet.TAPNQuery;

public abstract class QueryVisitor implements ITCTLVisitor {
	protected enum QueryType {
		EF, EG, AF, AG
	}

	private StringBuffer uppaalQuery;

	public QueryVisitor() {
	}

	protected void append(String string) {
		uppaalQuery.append(string);
	}

	protected void append(int number) {
		uppaalQuery.append(number);
	}

	public String getUppaalQueryFor(TAPNQuery tapnQuery) {
		this.uppaalQuery = new StringBuffer();
		tapnQuery.getProperty().accept(this, null);
		return uppaalQuery.toString();
	}

	public void visit(TCTLAFNode afNode, Object context) {
		uppaalQuery.append("A<> ");
		afNode.getProperty().accept(this, context);
		addEnding(QueryType.AF);
	}

	public void visit(TCTLAGNode agNode, Object context) {
		uppaalQuery.append("A[] ");
		agNode.getProperty().accept(this, context);
		addEnding(QueryType.AG);
	}

	public void visit(TCTLEFNode efNode, Object context) {
		uppaalQuery.append("E<> ");
		efNode.getProperty().accept(this, context);
		addEnding(QueryType.EF);
	}

	public void visit(TCTLEGNode egNode, Object context) {
		uppaalQuery.append("E[] ");
		egNode.getProperty().accept(this, context);
		addEnding(QueryType.EG);
	}

	public void visit(TCTLAndListNode andListNode, Object context) {
		uppaalQuery.append("(");
		boolean firstTime = true;

		for (TCTLAbstractStateProperty p : andListNode.getProperties()) {
			if (!firstTime) {
				uppaalQuery.append(" && ");
			}

			p.accept(this, context);
			firstTime = false;
		}

		uppaalQuery.append(")");

	}

	public void visit(TCTLOrListNode orListNode, Object context) {
		uppaalQuery.append("(");
		boolean firstTime = true;

		for (TCTLAbstractStateProperty p : orListNode.getProperties()) {
			if (!firstTime) {
				uppaalQuery.append(" || ");
			}

			p.accept(this, context);
			firstTime = false;
		}

		uppaalQuery.append(")");

	}

	public void visit(TCTLNotNode notNode, Object context) {
		uppaalQuery.append("!");
		uppaalQuery.append("(");
		notNode.getProperty().accept(this, context);
		uppaalQuery.append(")");
	}

	public void visit(TCTLStatePlaceHolder statePlaceHolderNode, Object context) {
	}

	public void visit(TCTLPathPlaceHolder pathPlaceHolderNode, Object context) {

	}

	protected String OperatorConversion(String op) {
		if (op.equals("="))
			return "==";

		return op;
	}

	public abstract void visit(TCTLAtomicPropositionNode atomicPropositionNode,
			Object context);

	protected abstract void addEnding(QueryType queryType);
}