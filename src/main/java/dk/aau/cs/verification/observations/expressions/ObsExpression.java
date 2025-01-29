package dk.aau.cs.verification.observations.expressions;

public interface ObsExpression {
    ObsExpression deepCopy();
    ObsExprPosition getObjectPosition(int index);
    boolean isOperator();
    boolean isLeaf();
    boolean isPlaceHolder();
    boolean isPlace();
    String toXml();
}
