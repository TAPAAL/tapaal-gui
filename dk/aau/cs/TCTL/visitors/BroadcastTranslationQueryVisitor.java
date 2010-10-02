package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;

public class BroadcastTranslationQueryVisitor extends QueryVisitor {
	protected static final String ID_TYPE = "id_t";
	protected static final String PLOCK = "P_lock";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";


	private boolean useSymmetry;
	private int totalTokens;

	public BroadcastTranslationQueryVisitor(boolean useSymmetry, int totalTokens)
	{
		this.useSymmetry = useSymmetry;
		this.totalTokens = totalTokens;
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

	protected void addEnding(QueryType type) {
		if(type == QueryType.EF || type == QueryType.AF){
			uppaalQuery.append(" && ");
		}else{
			uppaalQuery.append(" || !");
		}
		uppaalQuery.append("Control.");
		uppaalQuery.append(PLOCK);
	}
}
