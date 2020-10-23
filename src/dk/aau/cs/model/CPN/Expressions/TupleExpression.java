package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class TupleExpression extends ColorExpression {
    private Vector<ColorExpression> colors;

    public Vector<ColorExpression> getColors() {
        return colors;
    }

    public TupleExpression(Vector<ColorExpression> colors) {
        this.colors = colors;
    }

    public Color eval(ExpressionContext context) {
        Vector<Color> colors = new Vector<Color>();
        Vector<ColorType> colorTypes = new Vector<ColorType>();

        for (ColorExpression ce : this.colors) {
            Color color = ce.eval(context);
            colors.add(color);
            colorTypes.add(color.getColorType());
        }

        ProductType pt = context.findProductColorType(colorTypes);
        return pt.getColor(colors);
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

    public void addColorExpression(ColorExpression expr) {
        colors.add(expr);
    }

    @Override
    public boolean isSimpleProperty() {return false;}


    @Override
    public ColorExpression replace(Expression object1, Expression object2) {
        if (object1 == this && object2 instanceof ColorExpression) {
            ColorExpression obj2 = (ColorExpression) object2;
            obj2.setParent(parent);
            return obj2;
        }
        else  {
            for (int i = 0; i < colors.size(); i++) {
                colors.set(i, colors.get(i).replace(object1, object2));
            }
            return this;
        }
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
    public ExprValues getValues(ExprValues exprValues) {
        for (ColorExpression color : colors) {
            exprValues = color.getValues(exprValues);
        }
        return exprValues;
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
}
