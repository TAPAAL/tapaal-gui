package dk.aau.cs.pddl;

import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.pddl.expression.*;

import javax.swing.*;
import java.util.*;

import static java.util.Map.entry;

public class ActionSchema {
    private String name;
    private HashMap<String, Parameter> parameters = new HashMap<>();
    private Expression_And precondition;
    private String effect;

    public String getName() {
        return name;
    }

    public HashMap<String, Parameter> getParameters() {
        return parameters;
    }

    public IExpression getPrecondition() {
        return precondition;
    }

    public String effect() {
        return name;
    }


    public ActionSchema(TimedTransition transition) {
        parseTransition(transition);
    }

    private void parseTransition(TimedTransition transition) {
        parseParameters(transition);
        this.precondition = generatePrecondition(transition);
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

    //region parsePreconditions
    private HashMap<Parameter, Integer> parsePreconditions(Expression expression) {
        var expType = expression.getClass();

        if (expType.equals(NumberOfExpression.class)) {
            return parsePreconditions((NumberOfExpression) expression);
        } else if (expType.equals(AddExpression.class)) {
            return parsePreconditions((AddExpression) expression);
        } else if (expType.equals(TupleExpression.class)) {
            return parsePreconditions((TupleExpression) expression);
        } else if (expType.equals(VariableExpression.class)) {
            return parsePreconditions((VariableExpression) expression);
        } else {
            throw new RuntimeException("Unhandled expression type: " + expType.getName());
        }
    }


    private void addHashMap(HashMap<Parameter, Integer> m1, HashMap<Parameter, Integer> m2) {
//        HashMap<Parameter, Integer> outDict = new HashMap<>();

//        for (var k1: m1.keySet())
//            outDict.put(k1, m1.get(k1));

        for (var k2: m2.keySet())
            m1.put(k2, m1.getOrDefault(k2, 0) + m2.get(k2));

//        return outDict;
    }


    private Expression_And generatePrecondition(TimedTransition transition) {
        HashMap<Parameter, Integer> weights = parsePreconditions(transition);

        Expression_And precondition = new Expression_And();

        for(var entry: weights.entrySet()) {
            Parameter param = entry.getKey();
            int weight = entry.getValue();

            var paramExp = new Expression_Parameter(param);
            var weightExp = new Expression_IntegerLiteral(weight);

            var greaterOrEqualExp = new Expresssion_GreaterOrEqual(paramExp, weightExp);

            precondition.addParameter(greaterOrEqualExp);
        }

        return precondition;
    }

    private HashMap<Parameter, Integer> parsePreconditions(TimedTransition transition) {
        HashMap<Parameter, Integer> weights = new HashMap<>();
        List<TimedInputArc> inArcs = transition.getInputArcs();
        for (TimedInputArc arc : inArcs) {
            addHashMap(weights, parsePreconditions(arc.getArcExpression()));
        }

        return weights;


//            precondition.addParameter(parsePreconditions(arc.getArcExpression()));
//        Expression_And precondition = new Expression_And();



//        this.precondition = precondition;

//        return outDict;
    }

    private HashMap<Parameter, Integer> parsePreconditions(AddExpression expression) {
        HashMap<Parameter, Integer> outDict = new HashMap<>();

        for (var subExp : expression.getAddExpression()) {
            addHashMap(outDict, this.parsePreconditions(subExp));
        }

        return outDict;
    }

    private HashMap<Parameter, Integer> parsePreconditions(NumberOfExpression expression) {
        int multiplier = expression.getNumber();
        HashMap<Parameter, Integer> dict = parsePreconditions(expression.getColor());

        for(var entry: dict.entrySet()) {
            Parameter k = entry.getKey();
            Integer v = entry.getValue();

            dict.put(k, v*multiplier);
        }

        return dict;
    }

    private HashMap<Parameter, Integer> parsePreconditions(Vector<ColorExpression> expression) {
        HashMap<Parameter, Integer> dict = new HashMap<>();

        for(var e: expression) {
            addHashMap(dict, parsePreconditions(e));
        }

        return dict;
    }

    private HashMap<Parameter, Integer> parsePreconditions(TupleExpression expression) {
        return parsePreconditions(expression.getColors());
    }

    private HashMap<Parameter, Integer> parsePreconditions(VariableExpression expression) {
        HashMap<Parameter, Integer> dict = new HashMap<>();

        var variable = expression.getVariable();
        var name = variable.getName();

        var param = parameters.get(name);

        dict.put(param, 1);

        return dict;
    }

    //endregion
}
