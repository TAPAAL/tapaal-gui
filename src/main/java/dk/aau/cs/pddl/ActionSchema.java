package dk.aau.cs.pddl;

import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.pddl.expression.*;

import java.util.*;

import static dk.aau.cs.pddl.util.getAllPossibleColors;

public class ActionSchema {
    private String name;
    private HashMap<String, Parameter> parameters = new HashMap<>();
    private Expression_And precondition = new Expression_And();
    private Expression_And effects;

    public String getName() {
        return name;
    }

    public HashMap<String, Parameter> getParameters() {
        return parameters;
    }


    public boolean hasPrecondition() {
        return this.precondition.getClass() != Expression_And.class
            || !this.precondition.getParameters().isEmpty();
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

        GuardExpression guard = transition.getGuard();
        if(guard != null) {
            parseParameters(guard);
            this.precondition.addParameter(this.parseGuard(guard));
        }

        for(var param: generatePrecondition((transition)).getParameters()) {
            this.precondition.addParameter(param);
        }

        this.effects = generateEffects(transition);
    }

    //region parseParameters
    private void parseParameters(Expression expression) {
        var expType = expression.getClass();

        if (expType.equals(NumberOfExpression.class)) {
            parseParameters((NumberOfExpression) expression);
        } else if (expType.equals(PredecessorExpression.class)) {
            parseParameters((PredecessorExpression) expression);
        } else if (expType.equals(SuccessorExpression.class)) {
            parseParameters((SuccessorExpression) expression);
        } else if (expType.equals(AddExpression.class)) {
            parseParameters((AddExpression) expression);
        } else if (expType.equals(TupleExpression.class)) {
            parseParameters((TupleExpression) expression);
        } else if (expType.equals(VariableExpression.class)) {
            parseParameters((VariableExpression) expression);
        } else if (expType.equals(UserOperatorExpression.class)) { // Color Literal
            return;
        } else if (expType.equals(DotConstantExpression.class)) {
            return;
        } else if (expType.equals(AllExpression.class)) {
            return;
        } else if(expType == AndExpression.class) { // Guard exps
            parseParameters((AndExpression) expression);
        } else if(expType == NotExpression.class) {
            parseParameters((NotExpression) expression);
        } else if(expType == OrExpression.class) {
            parseParameters((OrExpression) expression);
        } else if (expType == EqualityExpression.class) {
            parseParameters((EqualityExpression) expression);
        } else if (expType == GreaterThanEqExpression.class) {
            parseParameters((GreaterThanEqExpression) expression);
        } else if (expType == GreaterThanExpression.class) {
            parseParameters((GreaterThanExpression) expression);
        } else if (expType == InequalityExpression.class) {
            parseParameters((InequalityExpression) expression);
        } else if (expType == LessThanEqExpression.class) {
            parseParameters((LessThanEqExpression) expression);
        } else if (expType == LessThanExpression.class) {
            parseParameters((LessThanExpression) expression);
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

    private void parseParameters(PredecessorExpression expression) {
        var param = expression.getBottomColorExpression();
        var variable = (VariableExpression)param;
        this.parseParameters(variable);

        var stored_param = this.parameters.get(variable.getVariable().getName());
        var predecessor_param = new Parameter(Expression_Predecessor.getName(stored_param), stored_param.getType());

        if(!this.parameters.containsKey(predecessor_param.getName())) {
            this.parameters.put(predecessor_param.getName(), predecessor_param);
            this.precondition.addParameter(new Expression_Predicate_IsPredecessor(stored_param, predecessor_param));
        }

    }

    private void parseParameters(SuccessorExpression expression) {
        var param = expression.getBottomColorExpression();
        var variable = (VariableExpression)param;
        this.parseParameters(variable);

        var stored_param = this.parameters.get(variable.getVariable().getName());
        var successor_param = new Parameter(Expression_Successor.getName(stored_param), stored_param.getType());

        if(!this.parameters.containsKey(successor_param.getName())) {
            this.parameters.put(successor_param.getName(), successor_param);
            this.precondition.addParameter(new Expression_Predicate_IsSuccessor(stored_param, successor_param));
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

    //region parse parameters in guard

    public void parseParameters(AndExpression guardExp) {
        parseParameters(guardExp.getLeftExpression());
        parseParameters(guardExp.getRightExpression());
    }

    public void parseParameters(NotExpression guardExp) {
        parseParameters(guardExp.getExpression());
    }

    public void parseParameters(OrExpression guardExp) {
        parseParameters(guardExp.getLeftExpression());
        parseParameters(guardExp.getRightExpression());
    }

    public void parseParameters(EqualityExpression guardExp) {
        parseParameters(guardExp.getLeftExpression());
        parseParameters(guardExp.getRightExpression());
    }

    public void parseParameters(GreaterThanEqExpression guardExp) {
        parseParameters(guardExp.getLeftExpression());
        parseParameters(guardExp.getRightExpression());
    }

    public void parseParameters(GreaterThanExpression guardExp) {
        parseParameters(guardExp.getLeftExpression());
        parseParameters(guardExp.getRightExpression());
    }

    public void parseParameters(InequalityExpression guardExp) {
        parseParameters(guardExp.getLeftExpression());
        parseParameters(guardExp.getRightExpression());
    }

    public void parseParameters(LessThanEqExpression guardExp) {
        parseParameters(guardExp.getLeftExpression());
        parseParameters(guardExp.getRightExpression());
    }

    public void parseParameters(LessThanExpression guardExp) {
        parseParameters(guardExp.getLeftExpression());
        parseParameters(guardExp.getRightExpression());
    }


    //endregion

    //endregion


    //region parseGuard
    public IExpression parseGuard(GuardExpression guardExp) {
        var type = guardExp.getClass();

        if(type == AndExpression.class) {
            return parseGuard((AndExpression) guardExp);
        } else if(type == NotExpression.class) {
            return parseGuard((NotExpression) guardExp);
        } else if(type == OrExpression.class) {
            return parseGuard((OrExpression) guardExp);
        } else if (type == EqualityExpression.class) {
            return parseGuardComparison((LeftRightGuardExpression) guardExp, Expression_Compare.ComparisonTypes.eq);
        } else if (type == GreaterThanEqExpression.class) {
            return parseGuardComparison((LeftRightGuardExpression) guardExp, Expression_Compare.ComparisonTypes.gteq);
        } else if (type == GreaterThanExpression.class) {
            return parseGuardComparison((LeftRightGuardExpression) guardExp, Expression_Compare.ComparisonTypes.gt);
        } else if (type == InequalityExpression.class) {
            return parseGuardComparison((LeftRightGuardExpression) guardExp, Expression_Compare.ComparisonTypes.neq);
        } else if (type == LessThanEqExpression.class) {
            return parseGuardComparison((LeftRightGuardExpression) guardExp, Expression_Compare.ComparisonTypes.lteq);
        } else if (type == LessThanExpression.class) {
            return parseGuardComparison((LeftRightGuardExpression) guardExp, Expression_Compare.ComparisonTypes.lt);
        } else {
            throw new RuntimeException("Unhandled guard expression type: " + type + ": `" + guardExp + "`");
        }
    }

    public IExpression parseGuard(AndExpression guardExp) {
        var left = parseGuard(guardExp.getLeftExpression());
        var right = parseGuard(guardExp.getRightExpression());

        return new Expression_And(left, right);
    }

    public IExpression parseGuard(NotExpression guardExp) {
        var exp = parseGuard(guardExp.getExpression());

        return new Expression_Not(exp);
    }

    public IExpression parseGuard(OrExpression guardExp) {
        var left = parseGuard(guardExp.getLeftExpression());
        var right = parseGuard(guardExp.getRightExpression());

        return new Expression_Or(left, right);
    }

    //region Compare
    private IExpression_Value parseGuard(ColorExpression value) {
        var parsedValue = parseColorToValues(value).get(0).get(0);

        return new Expression_ToInt(parsedValue);
    }

    public IExpression parseGuardComparison(LeftRightGuardExpression guardExp, Expression_Compare.ComparisonTypes compType) {
        var left = parseGuard(guardExp.getLeftExpression());
        var right = parseGuard(guardExp.getRightExpression());

        return Expression_Compare.Make_Comparison(left, compType, right);
    }
    //endregion

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
                    new Expression_GreaterOrEqual(
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
                        new Expression_Increment(
                            func,
                            new Expression_IntegerLiteral(weight)
                        )
                    );
                } else if (weight < 0) {
                    effects.addParameter(
                        new Expression_Decrement(
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
        Vector<ColorExpression> colorExps = expression.getColor();

        ArrayList<ArrayList<IExpression_Value>> valuesSets = parseColorToValues(colorExps);


        Weights weights = new Weights();
        for(var valueSet: valuesSets) {
            weights.put(valueSet, multiplier);
        }

        return weights;
    }


    private ArrayList<ArrayList<IExpression_Value>> parseColorToValues(AllExpression expression) {

        ArrayList<ArrayList<IExpression_Value>> allColors = new ArrayList<>();
        for(var colorProduct: getAllPossibleColors(expression.getSort())) {
            ArrayList<IExpression_Value>  returnProduct = new ArrayList<>();
            for(var atom: colorProduct) {
                returnProduct.add(new Expression_ColorLiteral(atom));
            }
            allColors.add(returnProduct);
        }

        return allColors;
    }



    private ArrayList<ArrayList<IExpression_Value>> parseColorToValues(Vector<ColorExpression> expression) {
        ArrayList<ArrayList<IExpression_Value>> values = new ArrayList<>();

        for(var e: expression) {
            var eClass = e.getClass();

            if (eClass.equals(VariableExpression.class)) {
                values.add(squeeze(parseColorToValues((VariableExpression) e)));
            } else if (eClass.equals(PredecessorExpression.class)) {
                values.add(squeeze(parseColorToValues((PredecessorExpression) e)));
            } else if (eClass.equals(SuccessorExpression.class)) {
                values.add(squeeze(parseColorToValues((SuccessorExpression) e)));
            } else if (eClass.equals(UserOperatorExpression.class)) {
                values.add(squeeze(parseColorToValues((UserOperatorExpression) e)));
            } else if (eClass.equals(DotConstantExpression.class)) {
                values.add(squeeze(parseColorToValues((DotConstantExpression) e)));
            } else if (eClass.equals(TupleExpression.class)) {
                values.addAll(parseColorToValues((TupleExpression) e));
            } else if (eClass.equals(AllExpression.class)) {
                values.addAll(parseColorToValues(((AllExpression) e)));
            } else {
                throw new RuntimeException("Unhandled expression type " + e);
            }
        }

        return values;
    }

    private static<T> ArrayList<T> squeeze (T elm) {
        return new ArrayList<>() {{
            add(elm);
        }};
    }

    private ArrayList<ArrayList<IExpression_Value>> parseColorToValues(ColorExpression expression) {
        return parseColorToValues(new Vector<>() {{ add(expression); }});
    }

    private ArrayList<ArrayList<IExpression_Value>> parseColorToValues(TupleExpression expression) {

        var tuple_constituents = expression.getColors();

        ArrayList<Iterable<IExpression_Value>> subColors = new ArrayList<>();
        for(var sub: tuple_constituents) {
            subColors.addAll(parseColorToValues(sub));
        }

        var cartesian = util.cartesian(subColors);

        return cartesian;
    }

    private Parameter parseColorToValues(VariableExpression expression) {
        var variable = expression.getVariable();
        var name = variable.getName();

        return parameters.get(name);
    }

    private Expression_ColorLiteral parseColorToValues(UserOperatorExpression expression) { // Color Literal
        var color = expression.getUserOperator();
        return new Expression_ColorLiteral(color);
    }

    private Expression_ColorLiteral parseColorToValues(DotConstantExpression expression) { // Color Literal

        var color = expression.getUserOperator();
        return new Expression_ColorLiteral(color, "dot_obj");
    }

    private Expression_Predecessor parseColorToValues(PredecessorExpression expression) {
        var param = expression.getPredecessorExpression();

        Parameter subExp = parseColorToValues((VariableExpression)param);

        return new Expression_Predecessor(subExp);
    }

    private Expression_Successor parseColorToValues(SuccessorExpression expression) {
        var param = expression.getSuccessorExpression();

        Parameter subExp = parseColorToValues((VariableExpression)param);

        return new Expression_Successor(subExp);
    }

    //endregion
}
