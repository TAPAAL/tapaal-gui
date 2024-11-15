package dk.aau.cs.verification.observations.expressions;

public abstract class ObsOperator implements ObsExpression {
    protected ObsExpression left;
    protected ObsExpression right;
    protected ObsExpression parent;
    
    protected ObsOperator(ObsExpression left, ObsExpression right) {
        this.left = left;
        this.right = right;
    }

    protected ObsOperator() {
        this(new ObsPlaceHolder(), new ObsPlaceHolder());
    }

    public void insertLeftMost(ObsExpression expr) {
        if (expr.isOperator()) {
            ((ObsOperator)expr).parent = this;
        }

        if (left.isOperator()) {
            ((ObsOperator)left).insertLeftMost(expr);
        } else {
            left = expr;
        }
    }

    public void replace(ObsExpression selectedExpr, ObsExpression newExpr) {
        if (left.equals(selectedExpr) || right.equals(selectedExpr)) {
            if (newExpr.isOperator()) {
                ((ObsOperator)newExpr).parent = this;
                if (selectedExpr.isOperator()) {
                    ((ObsOperator)newExpr).left = ((ObsOperator)selectedExpr).left;
                    ((ObsOperator)newExpr).right = ((ObsOperator)selectedExpr).right;
                }
            }

            if (left.equals(selectedExpr)) {
                left = newExpr;
            } else if (right.equals(selectedExpr)) {
                right = newExpr;
            }
        }

        if (left.isOperator()) {
            ((ObsOperator)left).replace(selectedExpr, newExpr);
        }
        
        if (right.isOperator()) {
            ((ObsOperator)right).replace(selectedExpr, newExpr);
        }
    }

    @Override
    public ObsExpression copy() {
        try {
            return getClass()
                    .getConstructor(ObsExpression.class, ObsExpression.class)
                    .newInstance(left.copy(), right.copy());
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy ObsOperator", e);
        }
    }

    protected boolean addParenthesis() {
        return parent != null && parent.getClass() != getClass();
    }

    @Override
    public ObsExprPosition getObjectPosition(int index) {
        int leftLen = left.toString().length();
        int rightLen = right.toString().length();
        int totalLen = toString().length();
        int thisLen = totalLen - leftLen - rightLen;

        int parenOffset = addParenthesis() ? 1 : 0;
        if (index < leftLen + parenOffset) {
            return left.getObjectPosition(index - parenOffset).addOffset(parenOffset);
        } else if (index >= leftLen + thisLen - parenOffset) {
            return right.getObjectPosition(index - leftLen - thisLen).addOffset(leftLen + thisLen - parenOffset);
        }

        return new ObsExprPosition(0, totalLen, this);
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public boolean isPlaceHolder() {
        return false;
    }

    @Override
    public String toString() {
        String operator = getOperator();
        if (addParenthesis()) {
            return "(" + left + " " + operator + " " + right + ")";
        }

        return left + " " + operator + " " + right;
    }

    protected abstract String getOperator();
}