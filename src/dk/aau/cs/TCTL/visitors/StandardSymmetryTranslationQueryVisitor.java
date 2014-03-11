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
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode,
			Object context) {
		assert(atomicPropositionNode.getRight() instanceof TCTLPlaceNode && atomicPropositionNode.getLeft() instanceof TCTLConstNode):
			"The " + getClass().getCanonicalName() + " cannot translate this query, as the prepositions are too complex";
		TCTLPlaceNode placeNode = (TCTLPlaceNode) atomicPropositionNode.getLeft();
		TCTLConstNode constNode = (TCTLConstNode) atomicPropositionNode.getRight();
		
		append("(sum(i:");
		append(ID_TYPE);
		append(")");
		append(TOKEN_TEMPLATE_NAME);
		append("(i).");
		append(placeNode.getPlace());
		append(") ");
		append(operatorConversion(atomicPropositionNode.getOp()));
		append(" ");
		append(constNode.getConstant());
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
