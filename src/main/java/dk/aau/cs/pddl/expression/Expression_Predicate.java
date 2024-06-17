package dk.aau.cs.pddl.expression;

import java.util.ArrayList;

public class Expression_Predicate extends BaseExpression implements IExpression {
    private String name;
//    private ArrayList<IExpression_Value> parameters;

    public Expression_Predicate(String name, ArrayList<IExpression_Value> parameters) {
        this.name = name;
        this.parameters = new ArrayList<>(parameters);
    }

    @Override
    public String getName() {
        return name;
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
