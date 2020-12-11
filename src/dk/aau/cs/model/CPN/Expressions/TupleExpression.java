package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;

import java.util.*;

public class TupleExpression extends ColorExpression {
    private Vector<ColorExpression> colors;

    public Vector<ColorExpression> getColors() {
        return colors;
    }

    public TupleExpression(Vector<ColorExpression> colors) {
        this.colors = colors;
    }

    public List<Color> eval(ExpressionContext context) {
        Vector<Color> colors = new Vector<Color>();
        Vector<ColorType> colorTypes = new Vector<ColorType>();

        for (ColorExpression ce : this.colors) {
            List<Color> color = ce.eval(context);
            colors.addAll(color);
            colorTypes.add(color.get(0).getColorType());
        }

        ProductType pt = context.findProductColorType(colorTypes);
        return Arrays.asList(pt.getColor(colors));
    }

    @Override
    public boolean hasColor(Color color) {
        List<ColorExpression> toRemove = new ArrayList<>();
        for (ColorExpression expr : colors){
            if(expr.hasColor(color)){
                toRemove.add(expr);
            }
        }
        for(ColorExpression expr : toRemove){
            colors.remove(expr);
        }
        if(colors.isEmpty()){
            return true;
        } else{
            return false;
        }
    }

    @Override
    public ColorExpression updateColor(Color color, ColorType newColorType) {
        Vector<ColorExpression> newColors = new Vector<>();
        for (ColorExpression expr : colors){
            ColorExpression newExpr = expr.updateColor(color, newColorType);
            if (newExpr != null) {
                newColors.add(newExpr);
            } else {
                return null;
            }
        }

        colors = newColors;
        return this;
    }

    @Override
    public boolean hasVariable(List<Variable> variables) {
        for(ColorExpression expr : colors) {
            if(expr.hasVariable(variables)){
                return true;
            }
        }
        return false;
    }

    @Override
    public ColorType getColorType(List<ColorType> colorTypes) {
        Vector<ColorType> expressionColorTypes = new Vector<ColorType>();

        for (ColorExpression ce : this.colors) {
            expressionColorTypes.add(ce.getColorType(colorTypes));
        }

        for (ColorType ct : colorTypes) {
            if (ct instanceof ProductType) {
                ProductType pt = (ProductType) ct;
                if (pt.containsTypes(expressionColorTypes)) {
                    return pt;
                }
            }
        }

        return  null;
    }
    @Override
    public boolean isComparable(ColorExpression otherExpr){
        otherExpr = otherExpr.getButtomColorExpression();
        if(!(otherExpr instanceof TupleExpression)){
            return false;
        }
        TupleExpression otherTupleExpression = (TupleExpression) otherExpr;

        if(otherTupleExpression.getColors().size() != getColors().size()){
            return false;
        }

        for(int i = 0; i < getColors().size(); i++){
            if(!getColors().get(i).isComparable(otherTupleExpression.getColors().get(i))){
                return false;
            }
        }
        return true;
    }

    public void addColorExpression(ColorExpression expr) {
        colors.add(expr);
    }

    @Override
    public boolean isSimpleProperty() {return false;}


    @Override
    public ColorExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (object1 == this && object2 instanceof ColorExpression) {
            ColorExpression obj2 = (ColorExpression) object2;
            obj2.setParent(parent);
            return obj2;
        }
        else  {
            for (int i = 0; i < colors.size(); i++) {
                colors.set(i, colors.get(i).replace(object1, object2, replaceAllInstances));
            }
            return this;
        }
    }
    @Override
    public ColorExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public ColorExpression copy() {
        return new TupleExpression(new Vector<>(colors));
    }

    @Override
    public TupleExpression deepCopy() {
        Vector<ColorExpression> colorsCopy = new Vector<>();

        for (ColorExpression expr: colors) {
            colorsCopy.add(expr.deepCopy());
        }

        return new TupleExpression(colorsCopy);
    }

    @Override
    public boolean containsPlaceHolder() {
        for (ColorExpression expr : colors) {
            if (expr.containsPlaceHolder()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ColorExpression findFirstPlaceHolder() {
        for (ColorExpression expr : colors) {
            if (expr.containsPlaceHolder()) {
                return expr.findFirstPlaceHolder();
            }
        }
        return null;
    }

    @Override
    public void getValues(ExprValues exprValues) {
        for (ColorExpression color : colors) {
            color.getValues(exprValues);
        }
    }

    public void getVariables(Set<Variable> variables) {
        for (ColorExpression element : colors) {
            element.getVariables(variables);
        }
    }

    public String toString() { /*
        boolean firstTime = true;
        String test = "";
        for (ColorExpression element : colors) {
            if (firstTime) {

            }
        }
        */


        String res = "(" + colors.get(0);
        for (int i = 1; i < colors.size(); i++) {
            res += ", " + colors.get(i).toString();
        }
        res += ")";
        return res;
    }

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[colors.size()];

        int i = 0;
        int endPrev = 0;
        for (ColorExpression p : colors) {

            int start = 1;
            int end = 0;

            if (i == 0) {
                end = start + p.toString().length();
                endPrev = end;

            } else {
                start = endPrev + 2;
                end = start + p.toString().length();
                endPrev = end;
            }

            ExprStringPosition pos = new ExprStringPosition(start, end, p);

            children[i] = pos;
            i++;
        }

        return children;
    }

    @Override
    public ColorExpression getButtomColorExpression(){
        return this;
    }

    @Override
    public Vector<ColorType> getColorTypes(){
        Vector<ColorType> constituentColorTypes = new Vector<ColorType>();
        //assumes single level producttypes
        for(ColorExpression uexpr : getColors()){
            constituentColorTypes.addAll(uexpr.getColorTypes());
        }
        return constituentColorTypes;
    }
}
