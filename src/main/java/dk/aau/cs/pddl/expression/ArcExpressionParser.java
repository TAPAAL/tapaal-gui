package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Expressions.AndExpression;
import dk.aau.cs.model.CPN.Expressions.Expression;

public class ArcExpressionParser {

    IExpression parse(Expression ae) {
        throw new RuntimeException("Unhandled expression type");
    }

    IExpression parse(AndExpression ae) {
        var andExp = new Expression_And();

        for (ExprStringPosition subexp: ae.getChildren()) {
            andExp.parameters.add(parse(subexp.getObject()));
        }

        return andExp;
    }

}
