package dk.aau.cs.TCTL.visitors;

import java.util.Hashtable;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;

public class CombiTranslationQueryVisitor extends QueryVisitor {
	protected static final String ID_TYPE = "id_t";
	protected static final String PLOCK = "P_lock";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";

	private boolean useSymmetry;
	private int totalTokens;
	private TimedArcPetriNet model;
	private Hashtable<String, Boolean> placeNameToTimed;

	public CombiTranslationQueryVisitor(boolean useSymmetry, int totalTokens, TimedArcPetriNet model, Hashtable<String, Boolean> placeNameToTimed) {
		this.useSymmetry = useSymmetry;
		this.totalTokens = totalTokens;
		this.model = model;
		this.placeNameToTimed = placeNameToTimed;
	}

	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode,
			Object context) {
		boolean timed = true;
		for (TimedPlace p : model.places()){
			if (!placeNameToTimed.get(p.name())){
				if (atomicPropositionNode.getPlace().equals(p.name())){
					timed=false;
				}
			}
		}
		if (timed){
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
		} else {
			append("X_");
			append(atomicPropositionNode.getPlace());
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
