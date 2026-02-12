package dk.aau.cs.verification.observations.expressions;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.TCTL.visitors.BooleanResult;

public abstract class ObsOperator extends ObsExpression {
    protected ObsExpression left;
    protected ObsExpression right;
    private boolean hadParentheses = false;
    
    protected ObsOperator(ObsExpression left, ObsExpression right) {
        this.left = left;
        this.right = right;
    }

    protected ObsOperator() {
        this(new ObsPlaceHolder(), new ObsPlaceHolder());
    }

    @Override
    public ObsExpression replacePlace(TimedPlace toReplace, TimedPlace replacement, TimedArcPetriNet tapn, BooleanResult affected) {
        ObsExpression newLeft = left.replacePlace(toReplace, replacement, tapn, affected);
        if (newLeft != left) {
            setLeft(newLeft);
            newLeft.setParent(this);
        }

        ObsExpression newRight = right.replacePlace(toReplace, replacement, tapn, affected);
        if (newRight != right) {
            setRight(newRight);
            newRight.setParent(this);
        }
        
        return this;
    }

    public void insertLeftMost(ObsExpression expr) {
        expr.parent = this;

        if (left.isOperator()) {
            ((ObsOperator)left).insertLeftMost(expr);
        } else {
            left = expr;
        }
    }

    public void setLeft(ObsExpression left) {
        this.left = left;
    }

    public void setRight(ObsExpression right) {
        this.right = right;
    }

    public void replace(ObsExpression selectedExpr, ObsExpression newExpr) {
        if (left.equals(selectedExpr) || right.equals(selectedExpr)) {
            newExpr.parent = this;

            if (left.equals(selectedExpr)) {
                left = newExpr;
            } else if (right.equals(selectedExpr)) {
                right = newExpr;
            }
        } else {
            if (left.isOperator()) {
                ((ObsOperator)left).replace(selectedExpr, newExpr);
            } 
            
            if (right.isOperator()) {
                ((ObsOperator)right).replace(selectedExpr, newExpr);
            }
        }
    }

    @Override
    public ObsExpression deepCopy() {
        try {
            ObsOperator copy = getClass()
                    .getConstructor(ObsExpression.class, ObsExpression.class)
                    .newInstance(left.deepCopy(), right.deepCopy());
            copy.setParent(parent);
            return copy;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy ObsOperator", e);
        }
    }

    public void hadParentheses(boolean hadParentheses) {
        this.hadParentheses = hadParentheses;
    }

    private boolean addParenthesis() {
        return (parent != null && parent.getClass() != getClass()) || hadParentheses;
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
    public ObsExprPosition getObjectPosition(ObsExpression expr) {
        if (equals(expr)) {
            return new ObsExprPosition(0, toString().length(), this);
        }

        int parenOffset = addParenthesis() ? 1 : 0;
        int leftLen = left.toString().length();
        int thisLen = toString().length() - leftLen - right.toString().length();
        
        ObsExprPosition leftPos = left.getObjectPosition(expr);
        if (leftPos != null) {
            return leftPos.addOffset(parenOffset);
        }

        ObsExprPosition rightPos = right.getObjectPosition(expr);
        if (rightPos != null) {
            return rightPos.addOffset(leftLen + thisLen - parenOffset);
        }

        return null;
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    public ObsExpression getLeft() {
        return left;
    }

    public ObsExpression getRight() {
        return right;
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