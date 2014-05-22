package dk.aau.cs.TCTL.visitors;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TCTL.AritmeticOperator;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLPlusListNode;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.TCTLTermListNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.TAPNQuery;

public abstract class QueryVisitor extends VisitorBase {
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
		uppaalQuery = new StringBuffer();
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
		createList(andListNode.getProperties(), context, "&&");
	}
	
	public void visit(TCTLOrListNode orListNode, Object context) {
		createList(orListNode.getProperties(), context, "||");
	}

	private void createList(List<TCTLAbstractStateProperty> properties, Object context, String seperator) {
		uppaalQuery.append('(');
		boolean firstTime = true;

		for (TCTLAbstractStateProperty p : properties) {
			if (!firstTime) {
				uppaalQuery.append(" " + seperator + " ");
			}

			p.accept(this, context);
			firstTime = false;
		}

		uppaalQuery.append(')');
	}

	public void visit(TCTLNotNode notNode, Object context) {
		uppaalQuery.append('!');
		uppaalQuery.append('(');
		notNode.getProperty().accept(this, context);
		uppaalQuery.append(')');
	}
	
	public void visit(TCTLTrueNode tctlTrueNode, Object context) {
		uppaalQuery.append(tctlTrueNode.toString());		
	}
	
	public void visit(TCTLFalseNode tctlFalseNode, Object context) {
		uppaalQuery.append(tctlFalseNode.toString());
	}
	
	public void visit(TCTLDeadlockNode tctlDeadLockNode, Object context) {
		uppaalQuery.append(tctlDeadLockNode.toString());
	}
	
	public void visit(AritmeticOperator aritmeticOperator, Object context){
		uppaalQuery.append(aritmeticOperator.toString());
	}

	public void visit(TCTLPlusListNode tctlPlusListNode, Object context){
		createArithmeticList(tctlPlusListNode.getProperties(), context);
	}
	
	public void visit(TCTLTermListNode tctlTermListNode, Object context){
		createArithmeticList(tctlTermListNode.getProperties(), context);
	}

	private void createArithmeticList(List<TCTLAbstractStateProperty> properties,
			Object context) {
		for(TCTLAbstractStateProperty prop : properties){
			if(prop.isSimpleProperty()){
				prop.accept(this, context);
			} else {
				uppaalQuery.append("(");
				prop.accept(this, context);
				uppaalQuery.append(")");
			}
			
			uppaalQuery.append(" ");
		}
	}

	public void visit(TCTLConstNode tctlConstNode, Object context){
		append(tctlConstNode.getConstant());
	}
	
	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode,
			Object context) {
		append("(");
		atomicPropositionNode.getLeft().accept(this, context);
		append(operatorConversion(atomicPropositionNode.getOp()));
		append(" ");
		atomicPropositionNode.getRight().accept(this, context);
		append(")");
	}

	protected String operatorConversion(String op) {
		if (op.equals("="))
			return "==";

		return op;
	}

	protected abstract void addEnding(QueryType queryType);
}