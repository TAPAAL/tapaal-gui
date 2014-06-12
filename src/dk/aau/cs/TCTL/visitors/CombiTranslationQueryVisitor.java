package dk.aau.cs.TCTL.visitors;

import java.util.Hashtable;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;

public class CombiTranslationQueryVisitor extends QueryVisitor {
	protected static final String ID_TYPE = "id_t";
	protected static final String PLOCK = "P_lock";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";

	private boolean useSymmetry;
	private int totalTokens;
	private TimedArcPetriNet model;
	private Hashtable<String, Boolean> placeNameToTimed;
	private int maxDegDif;
	private int initTransitions;
	private int maxTimeIn;

	public CombiTranslationQueryVisitor(boolean useSymmetry, int totalTokens, TimedArcPetriNet model, Hashtable<String, Boolean> placeNameToTimed, int maxDegDif, int initTransitions, int maxTimeIn) {
		this.useSymmetry = useSymmetry;
		this.totalTokens = totalTokens;
		this.model = model;
		this.placeNameToTimed = placeNameToTimed;
		this.maxDegDif = maxDegDif; 
		this.initTransitions = initTransitions;
		this.maxTimeIn = maxTimeIn;
	}

	@Override
	public void visit(TCTLPlaceNode placeNode,
			Object context) {
		
		boolean timed = true;
		for (TimedPlace p : model.places()){
			if (!placeNameToTimed.get(p.name())){
				if (placeNode.getPlace().equals(p.name())){
					timed=false;
				}
			}
		}
		if (timed){
			if (maxDegDif==0 && initTransitions==0 && maxTimeIn==0) {
				append("0");
			} else if (useSymmetry) {
				append("(sum(i:");
				append(ID_TYPE);
				append(")");
				append(TOKEN_TEMPLATE_NAME);
				append("(i).");
				append(placeNode.getPlace());
				append(")");
			} else if (totalTokens == 0) {
				append("(");
				append(TOKEN_TEMPLATE_NAME);
				append(".");
				append(placeNode.getPlace());
				append(")");
			} else {
				append("(");
				for (int i = 0; i < totalTokens; i++) {
					if (i > 0) {
						append(" + ");
					}

					append(TOKEN_TEMPLATE_NAME);
					append(i);
					append(".");
					append(placeNode.getPlace());
				}
				append(")");
			}
		} else {
			append("X_");
			append(placeNode.getPlace());
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
