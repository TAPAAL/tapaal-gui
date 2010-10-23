package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;


public interface ITCTLVisitor {
	void visit(TCTLAFNode afNode, Object context);
	void visit(TCTLAGNode agNode, Object context);
	void visit(TCTLEFNode efNode, Object context);
	void visit(TCTLEGNode egNode, Object context);
	void visit(TCTLNotNode notNode, Object context);
	void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context);
	void visit(TCTLStatePlaceHolder statePlaceHolderNode, Object context);
	void visit(TCTLPathPlaceHolder pathPlaceHolderNode, Object context);
	void visit(TCTLAndListNode andListNode, Object context);
	void visit(TCTLOrListNode orListNode, Object context);
}
