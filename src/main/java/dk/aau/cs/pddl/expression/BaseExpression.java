package dk.aau.cs.pddl.expression;

import java.util.ArrayList;

public abstract class BaseExpression {
    public abstract String getName();

    protected ArrayList<IExpression> parameters = new ArrayList<>();

    public ArrayList<IExpression> getParameters() {
        return parameters;
    }

    public void addParameter(IExpression expression) {
        this.parameters.add(expression);
    }

    @Override
    public String toString() {
        return toString(" ");
    }

    public String toString(String separator) {
        StringBuilder sb = new StringBuilder();
        for(var param: getParameters()) {
            sb.append(separator);
            sb.append(param);
        }


        return "(" + getName() + sb + ")";
    }
}
