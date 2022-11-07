package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.*;

public class NumberOfExpression extends ArcExpression {
    private Integer number;
    private final Vector<ColorExpression> color;

    public Vector<ColorExpression> getNumberOfExpression() {
        return this.color;
    }

    public NumberOfExpression(Integer number, Vector<ColorExpression> color) {
        this.number = number;
        this.color = color;
    }

    public Integer getNumber() {return number;}
    public Vector<ColorExpression> getColor() {return color;}

    public boolean equalsColor(NumberOfExpression otherExpr){
        for(int i = 0; i < color.size(); i++){
            if(!(color.get(i).toString().equals(otherExpr.color.get(i).toString()))){
                return false;
            }
        }
        return true;
    }

    @Override
    public ArcExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (object1 == this && object2 instanceof ArcExpression) {
            ArcExpression obj2 = (ArcExpression) object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            for (int i = 0; i < color.size(); i++) {
                color.set(i, color.get(i).replace(object1, object2, replaceAllInstances));
            }
            return this;
        }
    }
    @Override
    public ArcExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    public ColorMultiset eval(ExpressionContext context) {
        assert(!color.isEmpty());
        Vector<Color> colors = new Vector<>();
        ColorType ct = null;
        for (ColorExpression ce : color) {
            if (ce instanceof AllExpression) {
                if (ct == null) {
                    ct = ((AllExpression) ce).getSort();
                } else {
                    assert(ct == ((AllExpression) ce).getSort());
                }
            }
            List<Color> c = ce.eval(context);
            if (ct == null) {
                ct = c.get(0).getColorType();
            } else {
                assert(ct == c.get(0).getColorType());
            }
            colors.addAll(c);
        }
        return new ColorMultiset(ct, number, colors, color);
    }

    public Integer weight() {
        return number * color.size();
    }

    @Override
    public boolean containsColor(Color color) {
        for(ColorExpression c : this.color){
            if(c.containsColor(color)){
                return true;
            }
        }
        return false;
    }

    @Override
    public ArcExpression copy() {
        return new NumberOfExpression(number, new Vector<>((Vector<ColorExpression>) color.clone()));
    }

    @Override
    public ArcExpression deepCopy() {
        Vector<ColorExpression> colorsCopy = new Vector<>();
        for (ColorExpression colorExpr : color) {
            colorsCopy.add(colorExpr.deepCopy());
        }
        return new NumberOfExpression(number, colorsCopy);
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
    public Expression findFirstPlaceHolder() {
        for (ColorExpression expr : color) {
            if (expr.containsPlaceHolder()) {
                return expr.findFirstPlaceHolder();
            }
        }
        return null;
    }

    @Override
    public void getValues(ExprValues exprValues) {
        for (ColorExpression colorExpression : color) {
            colorExpression.getValues(exprValues);
        }
    }

    public void getVariables(Set<Variable> variables) {
        for (ColorExpression element: color) {
            element.getVariables(variables);
        }
    }

    public String toString() {
        StringBuilder res = new StringBuilder(number.toString() + "'" + color.get(0).toString());
        for (int i = 1; i < color.size(); ++i) {
            res.append(" + ");
            res.append(number.toString()).append("'(").append(color.get(i).toString()).append(")");
        }
        return res.toString();
    }

    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[color.size()];

        int i = 0;
        int endPrev = 0;
        int start = 2;
        int end = 0;
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

    public void setNumber(int newNumber){
        number = newNumber;
    }

    @Override
    public ArcExpression getExprWithNewColorType(ColorType ct) {
        Vector<ColorExpression> colorExpressions = new Vector<>();
        ColorExpression colorExpression = color.get(0);

        if (colorExpression.colorType == null || colorExpression.colorType.getName().equals(ct.getName())) {
            colorExpressions.add(colorExpression.getExprWithNewColorType(ct));
        } else {
            colorExpressions.add(colorExpression);
        }
        return new NumberOfExpression(number, colorExpressions);
    }
}
