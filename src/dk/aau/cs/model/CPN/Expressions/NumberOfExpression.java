package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class NumberOfExpression extends ArcExpression {
    private Integer number;
    private Vector<ColorExpression> color;

    public Vector<ColorExpression> getNumberOfExpression() {
        return this.color;
    }

    public NumberOfExpression(Integer number, Vector<ColorExpression> color) {
        this.number = number;
        this.color = color;
    }

    public Integer getNumber() {return number;}
    public Vector<ColorExpression> getColor() {return color;}

    @Override
    public ArcExpression replace(Expression object1, Expression object2) {
        if (object1 == this && object2 instanceof ArcExpression) {
            ArcExpression obj2 = (ArcExpression) object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            for (int i = 0; i < color.size(); i++) {
                color.set(i, color.get(i).replace(object1, object2));
            }
            return this;
        }
    }

    public ColorMultiset eval(ExpressionContext context) {
        assert(!color.isEmpty());
        Vector<Color> colors = new Vector<Color>();
        ColorType ct = null;
        for (ColorExpression ce : color) {
            if(ce instanceof AllExpression){
                if (ct == null) {
                    ct = ((AllExpression) ce).getSort();
                } else {
                    assert(ct == ((AllExpression) ce).getSort());
                }
            }
            Color c = ce.eval(context);
            if (ct == null) {
                ct = c.getColorType();
            } else {
                assert(ct == c.getColorType());
            }
            colors.add(c);
        }
        return new ColorMultiset(ct, number, colors);
    }

    public void expressionType() {

    }
    //TODO: is this correct with AllExpressions to?
    public Integer weight() {
        return number * color.size();
    }

    @Override
    public ArcExpression removeColorFromExpression(Color color) {
        List<ColorExpression> toRemove = new ArrayList<>();
        for(ColorExpression color1 : this.color){
            if(color1.hasColor(color)){
                toRemove.add(color1);
            }
        }
        for (ColorExpression expr : toRemove){
            this.color.remove(expr);
        }
        if(this.color.isEmpty()){
            return null;
        } else{
            return this;
        }
    }

    @Override
    public ArcExpression copy() {
        return new NumberOfExpression(number, color);
    }

    @Override
    public boolean containsPlaceHolder() {
        for (ColorExpression expr : color) {
            if (expr.containsPlaceHolder()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ArcExpression findFirstPlaceHolder() {
        for (ColorExpression expr : color) {
            if (expr.containsPlaceHolder()) {
                return null;
            //  return expr.findFirstPlaceHolder();
            }
        }
        return null;
    }

    @Override
    public ExprValues getValues(ExprValues exprValues) {
        for (ColorExpression colorExpression : color) {
            exprValues = colorExpression.getValues(exprValues);
        }
        return exprValues;
    }

    public void getVariables(Set<Variable> variables) {
        for (ColorExpression element: color) {
            element.getVariables(variables);
        }
    }

    public String toString() {
        String res = number.toString() + "'(" + color.get(0).toString() + ")";
        for (int i = 1; i < color.size(); ++i) {
            res += " + ";
            res += number.toString() + "'(" + color.get(i).toString() + ")";
        }
        return res;
    }

    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[color.size()];

        int i = 0;
        int endPrev = 0;
        int start = 3;
        int end = 0;
        boolean wasPrevSimple = false;
        for (ColorExpression p : color) {
            if (i == 0) {
                end = start + p.toString().length();
                endPrev = end;

            } else {
                start = endPrev;
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
