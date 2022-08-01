package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.*;

public abstract class VisitorBase implements ITCTLVisitor {

	public void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context) { 
		atomicPropositionNode.getLeft().accept(this, context);
		atomicPropositionNode.getRight().accept(this, context);
	}

    public void visit(LTLANode aNode, Object context) { aNode.getProperty().accept(this, context); }
    public void visit(LTLENode eNode, Object context) { eNode.getProperty().accept(this, context); }
    public void visit(LTLFNode afNode, Object context) { afNode.getProperty().accept(this, context); }
    public void visit(LTLGNode agNode, Object context) { agNode.getProperty().accept(this, context); }
    public void visit(LTLXNode axNode, Object context) { axNode.getProperty().accept(this, context); }
    public void visit(LTLUNode auNode, Object context) { auNode.getLeft().accept(this, context);
                                                            auNode.getRight().accept(this, context);  }
    public void visit(TCTLAFNode afNode, Object context) { afNode.getProperty().accept(this, context); }
	public void visit(TCTLAGNode agNode, Object context) { agNode.getProperty().accept(this, context); }
	public void visit(TCTLAXNode axNode, Object context) { axNode.getProperty().accept(this, context); }
	public void visit(TCTLAUNode auNode, Object context) { auNode.getLeft().accept(this, context); 
															auNode.getRight().accept(this, context);  }
	public void visit(TCTLEFNode efNode, Object context) { efNode.getProperty().accept(this, context); }
	public void visit(TCTLEGNode egNode, Object context) { egNode.getProperty().accept(this, context); }
	public void visit(TCTLEXNode exNode, Object context) { exNode.getProperty().accept(this, context); }
	public void visit(TCTLEUNode euNode, Object context) { euNode.getLeft().accept(this, context);
															euNode.getRight().accept(this, context);}
	public void visit(TCTLPathToStateConverter pathConverter, Object context) { pathConverter.getProperty().accept(this, context); }
	public void visit(TCTLStateToPathConverter stateConverter, Object context) { stateConverter.getProperty().accept(this, context); }
	public void visit(TCTLStatePlaceHolder statePlaceHolderNode, Object context) {}
	public void visit(TCTLPathPlaceHolder pathPlaceHolderNode, Object context) {}
	public void visit(TCTLTrueNode tctlTrueNode, Object context) { }
	public void visit(TCTLFalseNode tctlFalseNode, Object context) { }
	public void visit(TCTLDeadlockNode tctlDeadLockNode, Object context) { }
	public void visit(TCTLNotNode notNode, Object context) { notNode.getProperty().accept(this, context); }

	public void visit(TCTLAndListNode andListNode, Object context) {
		for (TCTLAbstractStateProperty p : andListNode.getProperties()) {
			p.accept(this, context);
		}
	}

	public void visit(TCTLOrListNode orListNode, Object context) {
		for (TCTLAbstractStateProperty p : orListNode.getProperties()) {
			p.accept(this, context);
		}
	}

	public void visit(AritmeticOperator aritmeticOperator, Object context){ }

	public void visit(TCTLPlusListNode tctlPlusListNode, Object context){
		for(TCTLAbstractStateProperty p : tctlPlusListNode.getProperties()){
			p.accept(this, context);
		}
	}

	public void visit(TCTLPlaceNode tctlPlaceNode, Object context){ }
	public void visit(TCTLTransitionNode tctlTransitionNode, Object context){ }
	public void visit(TCTLConstNode tctlConstNode, Object context){ }

	public void visit(TCTLTermListNode tctlTermListNode, Object context){
		for(TCTLAbstractStateProperty p : tctlTermListNode.getProperties()){
			p.accept(this, context);
		}
	}
	
}