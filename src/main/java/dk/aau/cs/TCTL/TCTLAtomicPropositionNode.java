package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLAtomicPropositionNode extends TCTLAbstractStateProperty {

	private TCTLAbstractStateProperty left;
	private TCTLAbstractStateProperty right;
	private String op;

	public TCTLAbstractStateProperty getLeft() {
		return left;
	}

	public void setLeft(TCTLAbstractStateProperty left) {
		this.left = left;
	}
	
	public TCTLAbstractStateProperty getRight() {
		return right;
	}

	public void setRight(TCTLAbstractStateProperty right) {
		this.right = right;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public TCTLAtomicPropositionNode(TCTLAbstractStateProperty left, String op, TCTLAbstractStateProperty right) {
        this.left = left;
        this.op = op;
        this.right = right;
        
        if (this.left != null) this.left.setParent(this);
        if (this.right != null) this.right.setParent(this);
    }
	
    @Override
    public StringPosition[] getChildren() {
        boolean leftParenthesis = op.equals("*") && left instanceof TCTLAtomicPropositionNode && (((TCTLAtomicPropositionNode) left).getOp().equals("+") || ((TCTLAtomicPropositionNode) left).getOp().equals("-"));
        boolean rightParenthesis = op.equals("*") && right instanceof TCTLAtomicPropositionNode && (((TCTLAtomicPropositionNode) right).getOp().equals("+") || ((TCTLAtomicPropositionNode) right).getOp().equals("-"));

        int leftOffset = leftParenthesis ? 1 : 0;
        int leftLength = left.toString().length();
        int opLength = op.length();
        
        int rightOffset = rightParenthesis ? 1 : 0;
        int rightStart = (leftParenthesis ? 2 : 0) + leftLength + 1 + opLength + 1;
        
        StringPosition[] children = new StringPosition[2];
        children[0] = new StringPosition(leftOffset, leftOffset + leftLength, left);
        children[1] = new StringPosition(rightStart + rightOffset, rightStart + rightOffset + right.toString().length(), right);
        
        return children;
    }

	@Override
	public TCTLAbstractStateProperty copy() {
		return new TCTLAtomicPropositionNode(left.copy(), op, right.copy());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TCTLAtomicPropositionNode) {
			TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode) o;
			// TODO: Not sure if this is intentional but this is reference
			// equals and not equality
			return left.equals(node.left)
					&& right.equals(node.right) && op.equals(node.getOp());
		}
		return false;
	}

    @Override
    public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
            TCTLAbstractProperty object2) {
        if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
            TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty)object2;
            obj2.setParent(parent);
            return obj2;
        } else {
            if (left != null) {
                left = (TCTLAbstractStateProperty)left.replace(object1, object2);
                left.setParent(this);
            }

            if (right != null) {
                right = (TCTLAbstractStateProperty)right.replace(object1, object2);
                right.setParent(this);
            }

            return this;
        }
    }

    @Override
    public void convertForReducedNet(String templateName) {
        left.convertForReducedNet(templateName);
        right.convertForReducedNet(templateName);
    }

    @Override
	public String toString() {
		String leftString = left.toString();
        String rightString = right.toString();

        if (op.equals("*")) {
            if (left instanceof TCTLAtomicPropositionNode && (((TCTLAtomicPropositionNode) left).getOp().equals("+") || ((TCTLAtomicPropositionNode) left).getOp().equals("-"))) {
                leftString = "(" + leftString + ")";
            }
            if (right instanceof TCTLAtomicPropositionNode && (((TCTLAtomicPropositionNode) right).getOp().equals("+") || ((TCTLAtomicPropositionNode) right).getOp().equals("-"))) {
                rightString = "(" + rightString + ")";
            }
        }

		return leftString + " " + op + " " + rightString;
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);

	}

	@Override
	public boolean containsPlaceHolder() {
		return left.containsPlaceHolder() || right.containsPlaceHolder();
	}

    @Override
    public boolean hasNestedPathQuantifiers() {
        return false;
    }

    @Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		TCTLAbstractProperty rightP = right.findFirstPlaceHolder(); 
		
		return rightP == null ? left.findFirstPlaceHolder() : rightP;
	}

	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
		return right.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName) ||
				left.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName);
	}
	
	
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
		return right.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName) ||
				left.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName);
	}
}
