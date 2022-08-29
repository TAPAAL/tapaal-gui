package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.*;

public class VariableExpression extends ColorExpression {

    private final Variable variable;

    public Variable getVariable() {
        return this.variable;
    }

    public VariableExpression(Variable variable) {
<<<<<<< HEAD
=======
        this(variable, variable.getColorType());
    }
    public VariableExpression(Variable variable, ColorType colorType) {
        super(colorType);
>>>>>>> origin/cpn
        this.variable = variable;
    }

    public List<Color> eval(ExpressionContext context) {
        return Collections.singletonList(context.binding.get(variable.getName()));
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
<<<<<<< HEAD
    public ColorType getColorType(List<ColorType> colorTypes) {
        return variable.getColorType();
    }

    @Override
=======
>>>>>>> origin/cpn
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

    //If we ever create a new variable here instead of using the same variable
    //Make sure to update TimedArcPetriNetNetwork.canVariableBeRemoved() so that it does not work on reference
    //but rather equals()
    //https://bugs.launchpad.net/tapaal/+bug/1938806
    @Override
    public VariableExpression deepCopy() {
        return new VariableExpression(this.variable);
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
        return new ExprStringPosition[0];
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
        otherExpr = otherExpr.getBottomColorExpression();
        if(otherExpr instanceof TupleExpression){
            return false;
        } else if (otherExpr instanceof UserOperatorExpression){
            UserOperatorExpression otherUserOpExpression = (UserOperatorExpression) otherExpr;
            return variable.getColorType().equals(otherUserOpExpression.getUserOperator().getColorType());
        }
        else if(!(otherExpr instanceof VariableExpression)){
            return false;
        }
        VariableExpression otherUserOpExpression = (VariableExpression) otherExpr;
        return variable.getColorType().equals(otherUserOpExpression.variable.getColorType());
    }

    @Override
    public ColorExpression getBottomColorExpression(){
        return this;
    }

    @Override
<<<<<<< HEAD
    public Vector<ColorType> getColorTypes(){

=======
    public Vector<ColorType> getColorTypes() {
>>>>>>> origin/cpn
        return new Vector<>(Collections.singletonList(variable.getColorType()));
    }

}
