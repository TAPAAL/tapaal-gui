package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public abstract class Expression {
    // used to determine whether to put parenthesis around the property
    // when printing it to a string.
    protected Expression parent;
    public boolean isSimpleProperty() {
        return true;
    }

    public void setParent(Expression parent) {this.parent = parent;}

    public ExprStringPosition objectAt(int index) {
        ExprStringPosition[] children = getChildren();
        for (int i = 0; i < children.length; i++) {
            ExprStringPosition child = children[i];
            if (child.getStart() <= index && index <= child.getEnd()) {
                int start = child.getStart();
                return child.getObject().objectAt(index - start).addOffset(start);
            }
        }
        return new ExprStringPosition(0 , toString().length(), this);
    }

    public ExprStringPosition indexOf(Expression property) {
        if (this == property) {
            return new ExprStringPosition(0, toString().length(), this);
        }
        else {
            ExprStringPosition[] children = getChildren();
            for (int i = 0; i < children.length; i++) {
                ExprStringPosition position = children[i].getObject().indexOf(property);
                if (position != null) {
                    return position.addOffset(children[i].getStart());
                }
            }
            return null;
        }
    }

    public abstract Expression replace(Expression object1, Expression object2,boolean replaceAllInstances);
    public abstract Expression replace(Expression object1, Expression object2);

    public abstract Expression copy();

    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = {};
        return children;
    }

    public abstract boolean containsPlaceHolder();

    public abstract Expression findFirstPlaceHolder();

    public abstract void getValues(ExprValues exprValues);

    public abstract void getVariables(Set<Variable> variables);
    public abstract boolean containsColor(Color color);
}
