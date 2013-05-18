package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;

public class BroadcastTranslationQueryVisitor extends QueryVisitor {
	protected static final String ID_TYPE = "id_t";
	protected static final String PLOCK = "P_lock";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";

	private boolean useSymmetry;
	private int totalTokens;

	public BroadcastTranslationQueryVisitor(boolean useSymmetry, int totalTokens) {
		this.useSymmetry = useSymmetry;
		this.totalTokens = totalTokens;
	}

	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode,
			Object context) {

		if (useSymmetry) {
			append("(sum(i:");
			append(ID_TYPE);
			append(")");
			append(TOKEN_TEMPLATE_NAME);
			append("(i).");
			append(atomicPropositionNode.getPlace());
			append(") ");
			append(operatorConversion(atomicPropositionNode.getOp()));
			append(" ");
			append(atomicPropositionNode.getN());
		} else if (totalTokens == 0) {
			append("(");
			append(TOKEN_TEMPLATE_NAME);
			append(".");
			append(atomicPropositionNode.getPlace());
			append(") ");
			append(operatorConversion(atomicPropositionNode.getOp()));
			append(" ");
			append(atomicPropositionNode.getN());
		} else {
			append("(");
			for (int i = 0; i < totalTokens; i++) {
				if (i > 0) {
					append(" + ");
				}

				append(TOKEN_TEMPLATE_NAME);
				append(i);
				append(".");
				append(atomicPropositionNode.getPlace());
			}
			append(") ");
			append(operatorConversion(atomicPropositionNode.getOp()));
			append(" ");
			append(atomicPropositionNode.getN());
		}
	}

	@Override
	protected void addEnding(QueryType type) {
		if (type == QueryType.EF || type == QueryType.AF) {
			append(" && ");
		} else {
			append(" || !");
		}
		append("Control.");
		append(PLOCK);
	}
}
