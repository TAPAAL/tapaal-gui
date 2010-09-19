package dk.aau.cs.TCTL;

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
