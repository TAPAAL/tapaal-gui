package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class VariableExpression extends ColorExpression {

    private Variable variable;

    public Variable getVariable() {
        return this.variable;
    }

    public VariableExpression(Variable variable) {
        this.variable = variable;
    }

    public List<Color> eval(ExpressionContext context) {
        return Arrays.asList(context.binding.get(variable.getName()));
    }

    @Override
    public boolean containsColor(Color color) {
        //This should also have been fixed beforehand
        return false;
    }

    @Override
    public ColorExpression updateColor(Color color, ColorType newColorType) {
        return this;
    }

    @Override
    public boolean hasVariable(List<Variable> variables) {
        for(Variable var : variables) {
            if (variable.getName().equals(var.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ColorType getColorType(List<ColorType> colorTypes) {
        return variable.getColorType();
    }

    @Override
    public ColorExpression replace(Expression object1, Expression object2, boolean replaceAllInstances) {
        if(replaceAllInstances) {
            if (this.equals(object1) && object2 instanceof ColorExpression) {
                ColorExpression obj2 = (ColorExpression) object2;
                obj2.setParent(parent);
                return obj2;
            } else {
                return this;
            }
        } else{
            //This is needed so that we can replace the exact object and not every instance
            if (this.strictEquals(object1) && object2 instanceof ColorExpression) {
                ColorExpression obj2 = (ColorExpression)object2;
                obj2.setParent(parent);
                return obj2;
            }
            else {
                return this;
            }
        }
    }
    @Override
    public ColorExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public ColorExpression copy() {
        return new VariableExpression(this.variable);
    }

    @Override
    public VariableExpression deepCopy() {
        return new VariableExpression(new Variable(this.variable.getName(), this.variable.getColorType().copy()));
    }

    @Override
    public boolean containsPlaceHolder() {
        return false;
    }

    @Override
    public ColorExpression findFirstPlaceHolder() {
        return null;
    }

    @Override
    public void getValues(ExprValues exprValues) {
        exprValues.addVariable(variable);
    }

    public void getVariables(Set<Variable> variables) {
        variables.add(variable);
    }

    @Override
    public String toString() {
        return variable.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VariableExpression) {
            VariableExpression expr = (VariableExpression) o;
            return variable.getName().equals(expr.variable.getName());
        }

        return false;
    }
    //this should only be used in replace function
    public boolean strictEquals(Object o) {
        if (o instanceof VariableExpression) {
            VariableExpression expr = (VariableExpression) o;
            return variable.getName().equals(expr.variable.getName()) && this == expr;
        }

        return false;
    }

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[0];
        return children;
    }

    @Override
    public boolean isSimpleProperty() {
        return false;
    }


    public int hashCode() {
        int result = 17;
        result = 31 * result + variable.hashCode();
        return result;
    }
    @Override
    public boolean isComparable(ColorExpression otherExpr){
        otherExpr = otherExpr.getButtomColorExpression();
        if(otherExpr instanceof TupleExpression){
            return false;
        } else if (otherExpr instanceof UserOperatorExpression){
            UserOperatorExpression otherUserOpExpression = (UserOperatorExpression) otherExpr;
            if(!variable.getColorType().equals(otherUserOpExpression.getUserOperator().getColorType())){
                return false;
            } else {
                return true;
            }
        }
        else if(!(otherExpr instanceof VariableExpression)){
            return false;
        }
        VariableExpression otherUserOpExpression = (VariableExpression) otherExpr;
        if(!variable.getColorType().equals(otherUserOpExpression.variable.getColorType())){
            return false;
        }
        return true;
    }

    @Override
    public ColorExpression getButtomColorExpression(){
        return this;
    }

    @Override
    public Vector<ColorType> getColorTypes(){

        return new Vector<>(Arrays.asList(variable.getColorType()));
    }

}
