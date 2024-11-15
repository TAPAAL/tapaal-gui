package dk.aau.cs.verification.observations.expressions;

public interface ObsExpression {
    ObsExpression copy();
    ObsExprPosition getObjectPosition(int index);
    boolean isOperator();
    boolean isLeaf();
    boolean isPlaceHolder();
}
