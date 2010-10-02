package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;

public class OptimizedStandardTranslationQueryVisitor extends QueryVisitor {
	protected static final String ID_TYPE = "pid_t";
	protected static final String LOCK_TEMPLATE = "Lock";
	protected static final String PLOCK = "P_lock";
	protected static final String CONTROL = "Control";
	protected static final String FINISH = "finish";
	protected static final String LOCK_BOOL = "lock";
	
	protected static final String TOKEN_TEMPLATE_NAME = "P";

	public void visit(TCTLAtomicPropositionNode atomicPropositionNode) {
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

	protected void addEnding(QueryType type) {
		if(type == QueryType.EF || type == QueryType.AF){
			uppaalQuery.append(" && ");
		}else{
			uppaalQuery.append(" || !");
		}
		uppaalQuery.append(String.format("(%1$s.%2$s == 1 && %3$s.%4$s == 1 && %5$s == 0)", LOCK_TEMPLATE, PLOCK, CONTROL, FINISH, LOCK_BOOL));
	}

}
