package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;

public class OptimizedStandardTranslationQueryVisitor extends QueryVisitor {
	protected static final String ID_TYPE = "pid_t";
	protected static final String LOCK_TEMPLATE = "Lock";
	protected static final String PLOCK = "P_lock";
	protected static final String CONTROL = "Control";
	protected static final String FINISH = "finish";
	protected static final String LOCK_BOOL = "lock";

	protected static final String TOKEN_TEMPLATE_NAME = "Token";
	private boolean useSymmetry;
	private int totalTokens;

	public OptimizedStandardTranslationQueryVisitor(int totalTokens, boolean useSymmetry) {
		this.useSymmetry = useSymmetry;
		this.totalTokens = totalTokens;
	}

	@Override
	public void visit(TCTLPlaceNode placeNode, Object context) {		
		if(useSymmetry) {
			append("(sum(i:");
			append(ID_TYPE);
			append(")");
			append(TOKEN_TEMPLATE_NAME);
			append("(i).");
			append(placeNode.getPlace());
			append(")");
		} else {
			append(createAtomicPropositionSum(placeNode.getPlace()));
		}
	}

	@Override
	protected void addEnding(QueryType type) {
		if (type == QueryType.EF || type == QueryType.AF) {
			append(" && ");
		} else {
			append(" || !");
		}
		if(useSymmetry)
			append(String.format("(%1$s.%2$s == 1 && %3$s.%4$s == 1 && %5$s == 0)",	LOCK_TEMPLATE, PLOCK, CONTROL, FINISH, LOCK_BOOL));
		else
			append(String.format("(%1$s.%2$s == 1)", LOCK_TEMPLATE, PLOCK));
	}

	private String createAtomicPropositionSum(String place) {
		StringBuffer sum = new StringBuffer("(");
		for (int i = 0; i < totalTokens; i++) {
			sum.append(String.format("%1$s%2$s.%3$s", TOKEN_TEMPLATE_NAME, i,
					place));
			if (i != totalTokens - 1) {
				sum.append(" + ");
			}
		}
		sum.append(')');
		return sum.toString();
	}
}
