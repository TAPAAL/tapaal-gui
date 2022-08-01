package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.*;

public interface ITCTLVisitor {
	void visit(TCTLAFNode afNode, Object context);

	void visit(TCTLAGNode agNode, Object context);
	
	void visit(TCTLAXNode axNode, Object context);
	
	void visit(TCTLAUNode auNode, Object context);

    void visit(LTLFNode afNode, Object context);

    void visit(LTLGNode agNode, Object context);

    void visit(LTLXNode axNode, Object context);

    void visit(LTLUNode auNode, Object context);

	void visit(TCTLEFNode efNode, Object context);

	void visit(TCTLEGNode egNode, Object context);
	
	void visit(TCTLEXNode exNode, Object context);
	
	void visit(TCTLEUNode euNode, Object context);
	
	void visit(TCTLPathToStateConverter pathConverter, Object context);
	
	void visit(TCTLStateToPathConverter stateConverter, Object context);

	void visit(TCTLNotNode notNode, Object context);

	void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context);

	void visit(TCTLStatePlaceHolder statePlaceHolderNode, Object context);

	void visit(TCTLPathPlaceHolder pathPlaceHolderNode, Object context);

	void visit(TCTLAndListNode andListNode, Object context);

	void visit(TCTLOrListNode orListNode, Object context);

	void visit(TCTLTrueNode tctlTrueNode, Object context);
	void visit(TCTLFalseNode tctlFalseNode, Object context);
	void visit(TCTLDeadlockNode tctlDeadLockNode, Object context);

	void visit(AritmeticOperator aritmeticOperator, Object context);

	void visit(TCTLPlusListNode tctlPlusListNode, Object context);

	void visit(TCTLPlaceNode tctlPlaceNode, Object context);
	
	void visit(TCTLTransitionNode tctlTransitionNode, Object context);

	void visit(TCTLConstNode tctlConstNode, Object context);

	void visit(TCTLTermListNode tctlTermListNode, Object context);

    void visit(LTLANode ltlaNode, Object context);

    void visit(LTLENode ltleNode, Object context);
}
