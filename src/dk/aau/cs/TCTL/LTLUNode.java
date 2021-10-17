package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class LTLUNode extends TCTLAbstractPathProperty {
    private TCTLAbstractStateProperty left;
    private TCTLAbstractStateProperty right;

    public TCTLAbstractStateProperty getLeft() {
        return this.left;
    }

    public void setLeft(TCTLAbstractStateProperty left) {
        this.left = left;
    }

    public TCTLAbstractStateProperty getRight() {
        return this.right;
    }

    public void setRight(TCTLAbstractStateProperty right) {
        this.right = right;
    }

    public LTLUNode(TCTLAbstractStateProperty left, TCTLAbstractStateProperty right) {
        this.left = left;
        this.right = right;
        this.left.setParent(this);
        this.right.setParent(this);
    }

    public LTLUNode() {
        left = new TCTLStatePlaceHolder();
        right = new TCTLStatePlaceHolder();
        left.setParent(this);
        right.setParent(this);
    }

    @Override
    public String toString() {
        String leftString = left.isSimpleProperty() ? left.toString() : "("
            + left.toString() + ")";
        String rightString = right.isSimpleProperty() ? right.toString() : "("
            + right.toString() + ")";

        return leftString + " U " + rightString;
    }

    @Override
    public StringPosition[] getChildren() {
        int leftStart = left.isSimpleProperty() ? 0 : 1;
        leftStart  += 0;
        int leftEnd = leftStart + left.toString().length();
        StringPosition leftPos = new StringPosition(leftStart, leftEnd, left);

        int rightStart = right.isSimpleProperty() ? 0 : 1;
        rightStart += leftEnd + 3 + + (left.isSimpleProperty() ? 0 : 1);
        int rightEnd = rightStart + right.toString().length();
        StringPosition rightPos = new StringPosition(rightStart, rightEnd, right);

        StringPosition[] children = { leftPos, rightPos };
        return children;
    }

    @Override
    public TCTLAbstractPathProperty replace(TCTLAbstractProperty object1,
                                            TCTLAbstractProperty object2) {
        if (this == object1 && object2 instanceof TCTLAbstractPathProperty) {
            return (TCTLAbstractPathProperty) object2;
        } else {
            left = left.replace(object1, object2);
            right = right.replace(object1, object2);
            return this;
        }
    }

    @Override
    public void convertForReducedNet(String templateName) {
        left.convertForReducedNet(templateName);
        right.convertForReducedNet(templateName);
    }

    @Override
    public boolean isSimpleProperty() {
        return false;
    }

    @Override
    public boolean containsPlaceHolder() {
        return left.containsPlaceHolder() || right.containsPlaceHolder();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LTLUNode) {
            LTLUNode node = (LTLUNode) o;
            return this.left.equals(node.getLeft())
                && this.right.equals(node.getRight());
        }
        return false;
    }

    @Override
    public TCTLAbstractPathProperty copy() {
        return new LTLUNode(left.copy(), right.copy());
    }

    @Override
    public boolean hasNestedPathQuantifiers() {
        return left instanceof TCTLPathToStateConverter || right instanceof TCTLPathToStateConverter
            || left.hasNestedPathQuantifiers() || right.hasNestedPathQuantifiers();
    }

    public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
        return left.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName)
            || right.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName);
    }

    public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
        return right.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName) ||
            left.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName);
    }

    @Override
    public TCTLAbstractProperty findFirstPlaceHolder() {
        TCTLAbstractProperty result = left.findFirstPlaceHolder();
        if (result == null){
            return right.findFirstPlaceHolder();
        }
        return result;
    }

    @Override
    public void accept(ITCTLVisitor visitor, Object context) {
        visitor.visit(this, context);
    }
}
