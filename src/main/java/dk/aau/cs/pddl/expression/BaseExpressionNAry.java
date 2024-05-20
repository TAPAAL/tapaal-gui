package dk.aau.cs.pddl.expression;

public abstract class BaseExpressionNAry extends BaseExpression {
    public BaseExpressionNAry(IExpression... params) {
        for(var param: params) {
            this.addParameter(param);
        }
    }
}
