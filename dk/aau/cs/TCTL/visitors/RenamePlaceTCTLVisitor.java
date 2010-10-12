package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAndNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLOrNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;


public class RenamePlaceTCTLVisitor implements ITCTLVisitor {

	private String oldPlaceName;
	private String newPlaceName;
	
	public RenamePlaceTCTLVisitor(String oldName, String newName)
	{
		this.oldPlaceName = oldName;
		this.newPlaceName = newName;
	}
	
	public void visit(TCTLAFNode afNode) { afNode.getProperty().accept(this); }

	public void visit(TCTLAGNode agNode) { agNode.getProperty().accept(this); }

	public void visit(TCTLEFNode efNode) { efNode.getProperty().accept(this); }

	public void visit(TCTLEGNode egNode) { egNode.getProperty().accept(this); }

	public void visit(TCTLAndNode andNode) { 
		andNode.getProperty1().accept(this);
		andNode.getProperty2().accept(this);
	}

	public void visit(TCTLOrNode orNode) { 
		orNode.getProperty1().accept(this);
		orNode.getProperty2().accept(this);
	}

	public void visit(TCTLAtomicPropositionNode atomicPropositionNode) {
		if(atomicPropositionNode.getPlace().equals(oldPlaceName)) {
			atomicPropositionNode.setPlace(newPlaceName);
		}
	}

	public void visit(TCTLStatePlaceHolder statePlaceHolderNode) { }

	public void visit(TCTLPathPlaceHolder pathPlaceHolderNode) { }

	public void visit(TCTLNotNode notNode) {
		notNode.getProperty().accept(this);
	}

	public void visit(TCTLAndListNode andListNode) {
		for (TCTLAbstractStateProperty p : andListNode.getProperties()) {
			p.accept(this);
		}		
	}
	
	public void visit(TCTLOrListNode orListNode) {
		for (TCTLAbstractStateProperty p : orListNode.getProperties()) {
			p.accept(this);
		}		
	}

}
