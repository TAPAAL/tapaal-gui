package dk.aau.cs.TCTL;

public class RenamePlaceTCTLVisitor implements ITCTLVisitor {

	private String oldPlaceName;
	private String newPlaceName;
	
	public RenamePlaceTCTLVisitor(String oldName, String newName)
	{
		this.oldPlaceName = oldName;
		this.newPlaceName = newName;
	}
	
	@Override
	public void visit(TCTLAFNode afNode) {	}

	@Override
	public void visit(TCTLAGNode agNode) {	}

	@Override
	public void visit(TCTLEFNode efNode) {  }

	@Override
	public void visit(TCTLEGNode egNode) {	}

	@Override
	public void visit(TCTLAndNode andNode) { }

	@Override
	public void visit(TCTLOrNode orNode) { }

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
