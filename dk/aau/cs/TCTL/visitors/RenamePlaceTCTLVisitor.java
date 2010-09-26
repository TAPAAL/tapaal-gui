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


public class RenamePlaceTCTLVisitor implements ITCTLVisitor {

	private String oldPlaceName;
	private String newPlaceName;
	
	public RenamePlaceTCTLVisitor(String oldName, String newName)
	{
		this.oldPlaceName = oldName;
		this.newPlaceName = newName;
	}
	
	@Override
	public void visit(TCTLAFNode afNode) { afNode.getProperty().accept(this); }

	@Override
	public void visit(TCTLAGNode agNode) { agNode.getProperty().accept(this); }

	@Override
	public void visit(TCTLEFNode efNode) { efNode.getProperty().accept(this); }

	@Override
	public void visit(TCTLEGNode egNode) { egNode.getProperty().accept(this); }

	@Override
	public void visit(TCTLAndNode andNode) { 
		andNode.getProperty1().accept(this);
		andNode.getProperty2().accept(this);
	}

	@Override
	public void visit(TCTLOrNode orNode) { 
		orNode.getProperty1().accept(this);
		orNode.getProperty2().accept(this);
	}

	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode) {
		if(atomicPropositionNode.getPlace().equals(oldPlaceName)) {
			atomicPropositionNode.setPlace(newPlaceName);
		}
	}

	@Override
	public void visit(TCTLStatePlaceHolder statePlaceHolderNode) { }

	@Override
	public void visit(TCTLPathPlaceHolder pathPlaceHolderNode) { }

}
