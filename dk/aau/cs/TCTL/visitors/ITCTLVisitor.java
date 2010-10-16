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
	void visit(TCTLAFNode afNode);
	void visit(TCTLAGNode agNode);
	void visit(TCTLEFNode efNode);
	void visit(TCTLEGNode egNode);
	void visit(TCTLNotNode notNode);
	void visit(TCTLAtomicPropositionNode atomicPropositionNode);
	void visit(TCTLStatePlaceHolder statePlaceHolderNode);
	void visit(TCTLPathPlaceHolder pathPlaceHolderNode);
	void visit(TCTLAndListNode andListNode);
	void visit(TCTLOrListNode orListNode);
}
