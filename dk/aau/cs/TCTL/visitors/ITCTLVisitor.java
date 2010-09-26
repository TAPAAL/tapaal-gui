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


public interface ITCTLVisitor {
	void visit(TCTLAFNode afNode);
	void visit(TCTLAGNode agNode);
	void visit(TCTLEFNode efNode);
	void visit(TCTLEGNode egNode);
	void visit(TCTLAndNode andNode);
	void visit(TCTLOrNode orNode);
	void visit(TCTLAtomicPropositionNode atomicPropositionNode);
	void visit(TCTLStatePlaceHolder statePlaceHolderNode);
	void visit(TCTLPathPlaceHolder pathPlaceHolderNode);
}
