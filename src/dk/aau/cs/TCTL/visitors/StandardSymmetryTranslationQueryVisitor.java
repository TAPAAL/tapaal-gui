package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;

public class StandardSymmetryTranslationQueryVisitor extends QueryVisitor {
	private static final String ID_TYPE = "pid_t";
	private static final String LOCK_TEMPLATE = "Lock";
	private static final String PLOCK = "P_lock";
	private static final String CONTROL = "Control";
	private static final String FINISH = "finish";

	private static final String TOKEN_TEMPLATE_NAME = "Token";

	@Override
	public void visit(TCTLPlaceNode placeNode,
			Object context) {
		append("(sum(i:");
		append(ID_TYPE);
		append(")");
		append(TOKEN_TEMPLATE_NAME);
		append("(i).");
		append(placeNode.getPlace());
		append(")");
	}

	@Override
	protected void addEnding(QueryType type) {
		if (type == QueryType.EF || type == QueryType.AF) {
			append(" && ");
		} else {
			append(" || !");
		}
		append(String.format("(%1$s.%2$s == 1 && %3$s.%4$s == 1)",
				LOCK_TEMPLATE, PLOCK, CONTROL, FINISH));
	}

}
