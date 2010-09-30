package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAndNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLOrNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.petrinet.TAPNQuery;

public class BroadcastTranslationQueryVisitor implements ITCTLVisitor {
	protected enum QueryType { EF, EG, AF, AG }
	
	protected static final String ID_TYPE = "id_t";
	protected static final String PLOCK = "P_lock";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";

	
	private boolean useSymmetry;
	private StringBuffer uppaalQuery;
	private int totalTokens;
	
	public String getUppaalQueryFor(TAPNQuery tapnQuery) {
		this.uppaalQuery = new StringBuffer();
		tapnQuery.getProperty().accept(this);
		return uppaalQuery.toString();
	}
	
	
	public BroadcastTranslationQueryVisitor(boolean useSymmetry, int totalTokens)
	{
		this.useSymmetry = useSymmetry;
		this.totalTokens = totalTokens;
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

	public void visit(TCTLOrNode orNode) {
		uppaalQuery.append("(");
		orNode.getProperty1().accept(this);
		uppaalQuery.append(" || ");
		orNode.getProperty2().accept(this);
		uppaalQuery.append(")");
	}

	public void visit(TCTLAtomicPropositionNode atomicPropositionNode) {
		
		if(useSymmetry){
			uppaalQuery.append("(sum(i:");
			uppaalQuery.append(ID_TYPE);
			uppaalQuery.append(")");
			uppaalQuery.append(TOKEN_TEMPLATE_NAME);
			uppaalQuery.append("(i).");
			uppaalQuery.append(atomicPropositionNode.getPlace());
			uppaalQuery.append(") ");
			uppaalQuery.append(OperatorConversion(atomicPropositionNode.getOp()));
			uppaalQuery.append(" ");
			uppaalQuery.append(atomicPropositionNode.getN());
		}
		else {
			uppaalQuery.append("(");
			for(int i = 0; i < totalTokens-1; i++){
				if(i > 0){
					uppaalQuery.append(" + ");
				}

				uppaalQuery.append(TOKEN_TEMPLATE_NAME);
				uppaalQuery.append(i);
				uppaalQuery.append(".");
				uppaalQuery.append(atomicPropositionNode.getPlace());
			}
			uppaalQuery.append(") ");
			uppaalQuery.append(OperatorConversion(atomicPropositionNode.getOp()));
			uppaalQuery.append(" ");
			uppaalQuery.append(atomicPropositionNode.getN());
		}
	}

	public void visit(TCTLStatePlaceHolder statePlaceHolderNode) {
	}

	public void visit(TCTLPathPlaceHolder pathPlaceHolderNode) {
		
	}
	
	private void addEnding(QueryType type) {
		if(type == QueryType.EF || type == QueryType.AF){
			uppaalQuery.append(" && ");
		}else{
			uppaalQuery.append(" || !");
		}
		uppaalQuery.append("Control.");
		uppaalQuery.append(PLOCK);
	}
	
	private String OperatorConversion(String op){
		if(op.equals("="))
			return "==";
		
		return op;
	}

}
