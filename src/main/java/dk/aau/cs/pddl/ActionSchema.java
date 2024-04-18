package dk.aau.cs.pddl;

import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.pddl.expression.*;

import java.util.*;

public class ActionSchema {
    private String name;
    private HashMap<String, Parameter> parameters = new HashMap<>();
    private Expression_And precondition;
    private Expression_And effects;

    public String getName() {
        return name;
    }

    public HashMap<String, Parameter> getParameters() {
        return parameters;
    }


    public IExpression getPrecondition() {
        return precondition;
    }


    public IExpression getEffects() {
        return effects;
    }


    public ActionSchema(TimedTransition transition) {
        parseTransition(transition);
    }


    private void parseTransition(TimedTransition transition) {
        this.name = transition.name();
        parseParameters(transition);
        this.precondition = generatePrecondition(transition);
        this.effects = generateEffects(transition);
    }

    //region parseParameters
    private void parseParameters(Expression expression) {
        var expType = expression.getClass();

        if (expType.equals(NumberOfExpression.class)) {
            parseParameters((NumberOfExpression) expression);
        } else if (expType.equals(AddExpression.class)) {
            parseParameters((AddExpression) expression);
        } else if (expType.equals(TupleExpression.class)) {
            parseParameters((TupleExpression) expression);
        } else if (expType.equals(VariableExpression.class)) {
            parseParameters((VariableExpression) expression);
        } else if (expType.equals(UserOperatorExpression.class)) { // Color Literal
            return;
        } else {
            throw new RuntimeException("Unhandled expression type: " + expType.getName());
        }
    }

    private void parseParameters(TimedTransition transition) {
        List<TimedInputArc> inArcs = transition.getInputArcs();
        List<TimedOutputArc> outArcs = transition.getOutputArcs();

        for (TimedInputArc arc : inArcs) {
            parseParameters(arc.getArcExpression());
        }

        for (TimedOutputArc arc : outArcs) {
            parseParameters(arc.getExpression());
        }
    }

    private void parseParameters(AddExpression expression) {
        for (var subExp : expression.getAddExpression()) {
            this.parseParameters(subExp);
        }
    }

    private void parseParameters(NumberOfExpression expression) {
        var color = expression.getColor();
        for (var atom : color) {
            this.parseParameters(atom);
        }
    }

    private void parseParameters(TupleExpression expression) {
        for (var subExp : expression.getChildren()) {
            parseParameters(subExp.getObject());
        }
    }

    private void parseParameters(VariableExpression expression) {
        var variable = expression.getVariable();
        var name = variable.getName();

        if(!parameters.containsKey(name)) {
            var userType = new UserType(variable.getColorType());

            parameters.put(name, new Parameter(name, userType));
        }
    }

    //endregion


    private Expression_And generatePrecondition(TimedTransition transition) {
        PlaceWeights placeWeights = parseTransitionPreset(transition);

        Expression_And precondition = new Expression_And();

        for(var e: placeWeights.entrySet()) {
            var place = e.getKey();
            var weights = e.getValue();
            for(var e2: weights.entrySet()) {
                var value = e2.getKey();
                var weight = e2.getValue();

                var func = new Expression_FunctionValue(
                    place.name(),
                    value
                );

                var weight_exp = new Expression_IntegerLiteral(weight);

                precondition.addParameter(
                    new Expresssion_GreaterOrEqual(
                        func,
                        weight_exp
                    )
                );
            }
        }

        return precondition;
    }

    private Expression_And generateEffects(TimedTransition transition) {

        PlaceWeights postset = parseTransitionPostset(transition);
        PlaceWeights preset = parseTransitionPreset(transition);

        preset.multiply(-1);
        postset.add(preset);

        Expression_And effects = new Expression_And();

        for(var e: postset.entrySet()) {
            var place = e.getKey();
            var weights = e.getValue();
            for(var e2: weights.entrySet()) {
                var value = e2.getKey();
                var weight = e2.getValue();

                var func = new Expression_FunctionValue(
                    place.name(),
                    value
                );

                if(weight > 0) {
                    effects.addParameter(
                        new Expresssion_Increment(
                            func,
                            new Expression_IntegerLiteral(weight)
                        )
                    );
                } else if (weight < 0) {
                    effects.addParameter(
                        new Expresssion_Decrement(
                            func,
                            new Expression_IntegerLiteral(-weight)
                        )
                    );
                }


            }
        }

        return effects;
    }




    //region parseExpressionToWeights


    private Weights parseExpressionToWeights(Expression expression) {
        var expType = expression.getClass();

        if (expType.equals(NumberOfExpression.class)) {
            return parseExpressionToWeights((NumberOfExpression) expression);
        } else if (expType.equals(AddExpression.class)) {
            return parseExpressionToWeights((AddExpression) expression);
//        } else if (expType.equals(TupleExpression.class)) {
//            return parseExpressionToWeights((TupleExpression) expression);
//        } else if (expType.equals(VariableExpression.class)) {
//            return parseExpressionToWeights((VariableExpression) expression);
//        } else if (expType.equals(UserOperatorExpression.class)) {
//            return parseExpressionToWeights((UserOperatorExpression) expression); // Color literal
        } else {
            throw new RuntimeException("Unhandled expression type: " + expType.getName());
        }
    }


    private PlaceWeights parseTransitionPreset(TimedTransition transition) {
        PlaceWeights placeWeights = new PlaceWeights();

        for (TimedInputArc arc : transition.getInputArcs()) {
            TimedPlace place = arc.source();
            ArcExpression exp = arc.getArcExpression();

            var weights = parseExpressionToWeights(exp);
            placeWeights.put(place, weights);
        }

        return placeWeights;
    }
    private PlaceWeights parseTransitionPostset(TimedTransition transition) {
        PlaceWeights placeWeights = new PlaceWeights();

        for (TimedOutputArc arc : transition.getOutputArcs()) {
            TimedPlace place = arc.destination();
            ArcExpression exp = arc.getExpression();

            var weights = parseExpressionToWeights(exp);
            placeWeights.put(place, weights);
        }

        return placeWeights;
    }

    private Weights parseExpressionToWeights(AddExpression expression) {
        Weights weights = new Weights();

        for (var subExp : expression.getAddExpression()) {
            weights.add(this.parseExpressionToWeights(subExp));
        }

        return weights;
    }

    private Weights parseExpressionToWeights(NumberOfExpression expression) {
        int multiplier = expression.getNumber();
        ArrayList<IExpression_Value> values = parseExpressionToWeights(expression.getColor());

        Weights weights = new Weights();
        weights.put(values, multiplier);

        return weights;
    }


    private ArrayList<IExpression_Value> parseExpressionToWeights(Vector<ColorExpression> expression) {
        ArrayList<IExpression_Value> values = new ArrayList<>();

        for(var e: expression) {
            var eClass = e.getClass();
            if(eClass == VariableExpression.class)
                values.add(parseVariableExpression((VariableExpression)e));
            else if (eClass == UserOperatorExpression.class)
                values.add(parseColorLiteral((UserOperatorExpression)e));
            else if (eClass == TupleExpression.class)
                values.addAll(parseTupleExpression((TupleExpression)e));
        }

        return values;
    }

    private ArrayList<IExpression_Value> parseTupleExpression(TupleExpression expression) {
        return parseExpressionToWeights(expression.getColors());
    }

    private Parameter parseVariableExpression(VariableExpression expression) {
        var variable = expression.getVariable();
        var name = variable.getName();

        return parameters.get(name);
    }

    private Expression_ColorLiteral parseColorLiteral(UserOperatorExpression expression) { // Color Literal
        var color = expression.getUserOperator();
        return new Expression_ColorLiteral(color);
    }

    //endregion
}
