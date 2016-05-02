package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.AritmeticOperator;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAXNode;
import dk.aau.cs.TCTL.TCTLAUNode;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLEXNode;
import dk.aau.cs.TCTL.TCTLEUNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLPathToStateConverter;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLPlusListNode;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.TCTLStateToPathConverter;
import dk.aau.cs.TCTL.TCTLTermListNode;
import dk.aau.cs.TCTL.TCTLTransitionNode;
import dk.aau.cs.TCTL.TCTLTrueNode;

public interface ITCTLVisitor {
	void visit(TCTLAFNode afNode, Object context);

	void visit(TCTLAGNode agNode, Object context);
	
	void visit(TCTLAXNode axNode, Object context);
	
	void visit(TCTLAUNode auNode, Object context);

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
}
