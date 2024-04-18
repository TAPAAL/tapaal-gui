package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Expressions.AddExpression;
import dk.aau.cs.model.CPN.Expressions.AndExpression;
import dk.aau.cs.model.CPN.Expressions.Expression;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.Transition;

import java.util.ArrayList;
import java.util.HashMap;

public class ArcExpressionParser {

    private Place place;
    private Transition transition;

    public ArcExpressionParser(Place place, Transition transition, Expression arcExpression) {



        HashMap<Place, ArrayList<HashMap<IExpression_Value, Integer>>> expressionWeights;
        ArrayList<HashMap<IExpression_Value, Integer>> perArc;

    }


    IExpression parse(Expression ae) {
        throw new RuntimeException("Unhandled expression type");
    }

    IExpression parse(AddExpression ae) {
        var andExp = new Expression_And();

        for (ExprStringPosition subexp: ae.getChildren()) {
            andExp.parameters.add(parse(subexp.getObject()));
        }

        return andExp;
    }

}
