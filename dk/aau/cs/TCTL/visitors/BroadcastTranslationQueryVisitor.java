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
	protected static final String ID_TYPE_NAME = "id";
	protected static final String TOKEN_INTERMEDIATE_PLACE = "%1$s_%2$s_%3$d";
	protected static final String TEST_CHANNEL_NAME = "%1$s_test%2$s";
	protected static final String FIRE_CHANNEL_NAME = "%1$s_fire%2$s";
	protected static final String COUNTER_NAME = "count%1$d";
	protected static final String COUNTER_UPDATE = "%1$s%2$s";
	protected static final String TOKEN_CLOCK_NAME = "x";
	protected static final String PLOCK = "P_lock";
	protected static final String PCAPACITY = "P_capacity";
	protected static final String INITIALIZE_CHANNEL = "c%1$d%2$s";

	protected static final String CONTROL_TEMPLATE_NAME = "Control";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";
	protected static final String QUERY_PATTERN = "([a-zA-Z][a-zA-Z0-9_]*) (==|<|<=|>=|>) ([0-9])*";
	protected static final String LOCK_BOOL = "lock";
	
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
	
	@Override
	public void visit(TCTLAFNode afNode) {
			uppaalQuery.append("A<> ");
			afNode.getProperty().accept(this);
			addEnding(QueryType.AF);
	}

	@Override
	public void visit(TCTLAGNode agNode) {
		uppaalQuery.append("A[] ");
		agNode.getProperty().accept(this);
		addEnding(QueryType.AG);
	}

	@Override
	public void visit(TCTLEFNode efNode) {
		uppaalQuery.append("E<> ");
		efNode.getProperty().accept(this);
		addEnding(QueryType.EF);
	}

	@Override
	public void visit(TCTLEGNode egNode) {
		uppaalQuery.append("E[] ");
		egNode.getProperty().accept(this);
		addEnding(QueryType.EG);
	}

	@Override
	public void visit(TCTLAndNode andNode) {
		uppaalQuery.append("(");
		andNode.getProperty1().accept(this);
		uppaalQuery.append(" && ");
		andNode.getProperty2().accept(this);
		uppaalQuery.append(")");
	}

	@Override
	public void visit(TCTLOrNode orNode) {
		uppaalQuery.append("(");
		orNode.getProperty1().accept(this);
		uppaalQuery.append(" || ");
		orNode.getProperty2().accept(this);
		uppaalQuery.append(")");
	}

	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode) {
		
		if(useSymmetry){
			uppaalQuery.append("(sum(i:");
			uppaalQuery.append(ID_TYPE);
			uppaalQuery.append(")");
			uppaalQuery.append(TOKEN_TEMPLATE_NAME);
			uppaalQuery.append("(i).");
			uppaalQuery.append(atomicPropositionNode.getPlace());
			uppaalQuery.append(") ");
			uppaalQuery.append(atomicPropositionNode.getOp());
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
			uppaalQuery.append(atomicPropositionNode.getOp());
			uppaalQuery.append(" ");
			uppaalQuery.append(atomicPropositionNode.getN());
		}
	}

	@Override
	public void visit(TCTLStatePlaceHolder statePlaceHolderNode) {
	}

	@Override
	public void visit(TCTLPathPlaceHolder pathPlaceHolderNode) {
		
	}
	
	private void addEnding(QueryType type) {
		if(type == QueryType.EF || type == QueryType.AF){
			uppaalQuery.append(" and ");
		}else{
			uppaalQuery.append(" or !");
		}
		uppaalQuery.append("Control.");
		uppaalQuery.append(PLOCK);
	}

}
