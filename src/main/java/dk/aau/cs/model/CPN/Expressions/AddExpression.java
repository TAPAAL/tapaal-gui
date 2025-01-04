package dk.aau.cs.model.CPN.Expressions;

import java.util.Set;
import java.util.Vector;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.util.Require;

public class AddExpression extends ArcExpression {

    private final Vector<ArcExpression> constituents;

    public AddExpression(ArcExpression left, ArcExpression right) {
        Require.notNull(left);
        Require.notNull(right);
        Vector<ArcExpression> constituents = new Vector<>();
        constituents.add(left);
        constituents.add(right);
        this.constituents = constituents;
    }

    public AddExpression(Vector<ArcExpression> constituents) {
        Require.notNull(constituents);
        Require.that(constituents.size() > 0, "Constituents can't be empty");
        Require.notNull(constituents, "Constituents can not container null");
        this.constituents = constituents;
    }

    public AddExpression(AddExpression otherExpr)  {
        super(otherExpr);
        var constituents = otherExpr.constituents;
        Require.notNull(constituents);
        Require.that(constituents.size() > 0, "Constituents can't be empty");
        Require.notNull(constituents, "Constituents can not container null");
        this.constituents = new Vector<>(otherExpr.constituents);
    }

    public Vector<ArcExpression> getAddExpression (){return constituents;}

    public ColorMultiset eval(ExpressionContext context) {
        ColorMultiset result = null;
        //Start with null, to use colortype of first constituent
        for (ArcExpression constituent : constituents) {
            if (result == null) {
                result = constituent.eval(context);
            } else {
                ColorMultiset cm = constituent.eval(context);
                if (constituent instanceof NumberOfExpression) {
                    result.addAll(cm, ((NumberOfExpression) constituent).getColor(), ((NumberOfExpression) constituent).getNumber());
                } else {
                    result.addAll(cm, null, 1);
                }
            }
        }
        assert(result != null);
        return result;
    }

    public Integer weight() {
        Integer res = 0;
        for (ArcExpression element : constituents) {
            res += element.weight();
        }
        return res;
    }

    @Override
    public ArcExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public ArcExpression replace(Expression object1, Expression object2, boolean replaceAllInstances) {
        if (object1 == this && object2 instanceof ArcExpression) {
            ArcExpression obj2 = (ArcExpression) object2;
            obj2.setParent(parent);
            return obj2;
        } else {
            for (int i = 0; i < constituents.size(); i++) {
                constituents.set(i, constituents.get(i).replace(object1, object2, replaceAllInstances));
            }
            return this;
        }
    }

    @Override
    public ArcExpression copy() {
        return new AddExpression(constituents);
    }

    @Override
    public ArcExpression deepCopy() {
        Vector<ArcExpression> constituentsCopy = new Vector<>();
        for (ArcExpression expr : constituents) {
            constituentsCopy.add(expr.deepCopy());
        }
        return new AddExpression(constituentsCopy);
    }

    @Override
    public boolean containsPlaceHolder() {
        for (ArcExpression constituent : constituents) {
            if (constituent.containsPlaceHolder()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Expression findFirstPlaceHolder() {
        for (ArcExpression constituent : constituents) {
            if (constituent.containsPlaceHolder()) {
                return constituent.findFirstPlaceHolder();
            }
        }
        return null;
    }

    @Override
    public void getValues(ExprValues exprValues) {
        for (ArcExpression constituent : constituents) {
            constituent.getValues(exprValues);
        }
    }

    @Override
    public boolean isSimpleProperty() {return false; }

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[constituents.size()];
        int i = 0;
        int endPrev = 0;
        for (ArcExpression p : constituents) {

            int start = 1;
            int end = 0;

            if (i == 0) {
                end = start + p.toString().length();
                endPrev = end;
            } else {
                start = endPrev + 3;
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
    public boolean equals(Object o) {
        if (o instanceof AddExpression) {
            AddExpression expr = (AddExpression) o;
            return constituents.equals(expr.constituents);
        }
        return false;
    }

    public void getVariables(Set<Variable> variables) {
        for (ArcExpression element : constituents) {
            element.getVariables(variables);
        }
    }

    @Override
    public boolean containsColor(Color color) {
        for(var c : constituents){
            if(c.containsColor(color)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(constituents.get(0).toString());
        for (int i = 1; i < constituents.size(); ++i) {
            res.append(" + ").append(constituents.get(i).toString());
        }

        return res.toString();
    }

    public String toTokenString() {
        StringBuilder res = new StringBuilder();
        for (ArcExpression constituent : constituents) {
            res.append(constituent.toString()).append("\n");
        }
        return res.toString();
    }

    public AddExpression getExprWithNewColorType(ColorType ct) {
        Vector<ArcExpression> arcExpressions = new Vector<>();
        for (ArcExpression expr : constituents) {
            arcExpressions.add(expr.getExprWithNewColorType(ct));
        }
        return new AddExpression(arcExpressions);
    }

    @Override
    public ArcExpression getExprConverted(ColorType oldCt, ColorType newCt) {
        Vector<ArcExpression> arcExpressions = new Vector<>();
        for (ArcExpression expr : constituents) {
            arcExpressions.add(expr.getExprConverted(oldCt, newCt));
        }
        return new AddExpression(arcExpressions);
    }
}
