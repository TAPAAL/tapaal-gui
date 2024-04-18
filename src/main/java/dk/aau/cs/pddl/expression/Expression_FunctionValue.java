package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.tapn.TimedPlace;

import java.util.ArrayList;

public class Expression_FunctionValue extends BaseExpression implements IExpression {
    private String name;
//    private ArrayList<IExpression_Value> parameters;

    public Expression_FunctionValue(String name, ArrayList<IExpression_Value> parameters) {
        this.name = name;
        this.parameters = new ArrayList<>(parameters);
    }

//    public Expression_FunctionValue(TimedPlace place, ArrayList<IExpression_Value> parameters) {
//        this.name = place.name();
//        this.parameters = parameters;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


//    public ArrayList<IExpression_Value> getParameters() {
//        return parameters;
//    }
//
//
//    public void setParameters(ArrayList<IExpression_Value> parameters) {
//        this.parameters = parameters;
//    }


//    @Override
//    public String toString() {
//        return "(" + name + " " + parameters.toString() + ")";
//    }
}
