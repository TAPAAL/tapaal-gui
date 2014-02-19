package dk.aau.cs.TCTL.visitors;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLTrueNode;

public class SimplifyPropositionsVisitor extends VisitorBase {
	List<TCTLAtomicPropositionNode> truePropositions = new ArrayList<TCTLAtomicPropositionNode>();
	List<TCTLAtomicPropositionNode> falsePropositions = new ArrayList<TCTLAtomicPropositionNode>();
	
	public void findAndReplaceTrueAndFalsePropositions(TCTLAbstractProperty query) {
		query.accept(this, null);
		
		for(TCTLAtomicPropositionNode node : truePropositions)
			query.replace(node, new TCTLTrueNode());
		
		for(TCTLAtomicPropositionNode node : falsePropositions)
			query.replace(node, new TCTLFalseNode());
	}
	
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context) {
		//TODO Evaluate the preposition to determine if its always satisfied / not satisfied  
		/*
		if(atomicPropositionNode.getOp().equals(">=") && atomicPropositionNode.getN() == 0)
			truePropositions.add(atomicPropositionNode);
		else if(atomicPropositionNode.getOp().equals("<") && atomicPropositionNode.getN() == 0)
			falsePropositions.add(atomicPropositionNode);
		*/
	}
}
