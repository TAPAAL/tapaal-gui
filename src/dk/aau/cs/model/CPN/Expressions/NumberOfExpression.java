package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;
import java.util.Vector;

public class NumberOfExpression extends ArcExpression {
    private Integer number;
    private Vector<ColorExpression> color;
    private AllExpression all;

    public Vector<ColorExpression> getNumberOfExpression() {
        return this.color;
    }


    public AllExpression getAllExpression(){
        return this.all;
    }

    public NumberOfExpression(Integer number, Vector<ColorExpression> color) {
        this.number = number;
        this.color = color;
        this.all = null;
    }

    public NumberOfExpression(Integer number, AllExpression all) {
        this.number = number;
        this.color = null;
        this.all = all;
    }

    public Integer getNumber() {return number;}
    public Vector<ColorExpression> getColor() {return color;}
    public AllExpression getAll() {return all;}

    @Override
    public ArcExpression replace(Expression object1, Expression object2) {
        if (object1 == this && object2 instanceof ArcExpression) {
            ArcExpression obj2 = (ArcExpression) object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            if (all != null) {
                all = all.replace(object1, object2);
                return this;
            }
            else {
                for (int i = 0; i < color.size(); i++) {
                    color.set(i, color.get(i).replace(object1, object2));
                }
                return this;
            }
        }
    }

    public ColorMultiset eval(ExpressionContext context) {
        if (all == null) {
            assert(!color.isEmpty());
            Vector<Color> colors = new Vector<Color>();
            ColorType ct = null;
            for (ColorExpression ce : color) {
                Color c = ce.eval(context);
                if (ct == null) {
                    ct = c.getColorType();
                } else {
                    assert(ct == c.getColorType());
                }
                colors.add(c);
            }
            return new ColorMultiset(ct, number, colors);
        } else {
            ColorType ct = all.eval(context);
            return new ColorMultiset(ct, number);
        }
    }

    public void expressionType() {

    }

    public Integer weight() {
        if (all == null) {
            return number * color.size();
        }
        else {
            return number * all.size();
        }
    }

    @Override
    public ArcExpression copy() {
        if (all != null) {
            return new NumberOfExpression(number, all);
        } else
        return new NumberOfExpression(number, color);
    }

    @Override
    public boolean containsPlaceHolder() {
        if (all != null) {
            return false;
        } else {
            for (ColorExpression expr : color) {
                if (expr.containsPlaceHolder()) {
                    return false;
                }
            }
            return false;
        }
    }

    @Override
    public ArcExpression findFirstPlaceHolder() {
        if (all != null) {
            return null;
        } else {
            for (ColorExpression expr : color) {
                if (expr.containsPlaceHolder()) {
                    return null;
                //  return expr.findFirstPlaceHolder();
                }
            }
            return null;
        }
    }

    @Override
    public ExprValues getValues(ExprValues exprValues) {
        if (all != null) {
            exprValues = all.getValues(exprValues);
            return exprValues;
        } else {
            for (ColorExpression colorExpression : color) {
                exprValues = colorExpression.getValues(exprValues);
            }
            return exprValues;
        }
    }

    public void getVariables(Set<Variable> variables) {
        if (all == null) {
            return;
        }
        for (ColorExpression element: color) {
            element.getVariables(variables);
        }
    }

    public String toString() {
        if (all != null) {
            return number.toString() + "'(" + all.toString() + ")";
        }
        String res = number.toString() + "'(" + color.get(0).toString() + ")";
        for (int i = 1; i < color.size(); ++i) {
            res += " + ";
            res += number.toString() + "'(" + color.get(i).toString() + ")";
        }
        return res;
    }

    public ExprStringPosition[] getChildren() {
        if (all != null) {
            int start = 4;
            int end = 0;

            end = start + all.toString().length();
            ExprStringPosition pos = new ExprStringPosition(start, end, all);
            ExprStringPosition[] children = {pos};
            return children;
        }
        else {
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

}
