package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;

public class StandardTranslationQueryVisitor extends QueryVisitor {
	protected static final String ID_TYPE = "pid_t";
	protected static final String LOCK_TEMPLATE = "Lock";
	protected static final String PLOCK = "P_lock";
	protected static final String CONTROL = "Control";
	protected static final String FINISH = "finish";
	protected static final String LOCK_BOOL = "lock";

	protected static final String TOKEN_TEMPLATE_NAME = "Token";

	private int totalTokens;

	public StandardTranslationQueryVisitor(int totalTokens)
	{
		this.totalTokens = totalTokens;
	}

	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context) {

		append("(");
		append(createAtomicPropositionSum(atomicPropositionNode.getPlace()));
		append(OperatorConversion(atomicPropositionNode.getOp()));
		append(" ");
		append(atomicPropositionNode.getN());
		append(")");
	}

	@Override
	protected void addEnding(QueryType type) {
		if(type == QueryType.EF || type == QueryType.AF){
			append(" && ");
		}else{
			append(" || !");
		}
		append("(");
		append(createAtomicPropositionSum(PLOCK));
		append(" == 1)");
	}
	
	private String createAtomicPropositionSum(String place) {
		StringBuffer sum = new StringBuffer("(");
		for(int i = 0; i < totalTokens; i++){
			sum.append(String.format("%1$s%2$s.%3$s", TOKEN_TEMPLATE_NAME, i, place));
			if(i != totalTokens-1){
				sum.append(" + ");
			}
		}
		sum.append(")");
		return sum.toString();
	}
}
