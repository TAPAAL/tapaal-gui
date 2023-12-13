package dk.aau.cs.model.CPN;

import dk.aau.cs.model.tapn.*;

import java.util.Vector;

public class ColoredTimeInvariant extends TimeInvariant {
    private Color color;
    public static final ColoredTimeInvariant LESS_THAN_INFINITY_AND_STAR = new ColoredTimeInvariant(false, Bound.Infinity, Color.STAR_COLOR);

    public ColoredTimeInvariant(boolean isUpperIncluded, Bound upper, Color color) {
        super(isUpperIncluded, upper);
        this.color = color;
    }

    public static  ColoredTimeInvariant LESS_THAN_INFINITY_DYN_COLOR(Color color) {
        return new ColoredTimeInvariant(false, Bound.Infinity, color);
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        if (color.getTuple() != null)
            return color.getTuple().toString() + " " + super.toString();
        else
            return color.toString() + " " + super.toString();
    }

    @SuppressWarnings("Duplicates")
    public static ColoredTimeInvariant parse(String invariant, ConstantStore constants, Vector<Color> colors) {
        Color color;
        if (colors.size() == 1)
            color = colors.get(0);
        else
            color = new Color(colors.get(0).getColorType(), 0, colors);

        String[] split = invariant.split(" ");

        String operator = split[0];
        String boundAsString = split[1];

        if (operator.equals("<") && boundAsString.equals("0"))
            return null;
        if (operator.equals("<=") && boundAsString.equals("inf"))
            return null;

        Bound bound;
        if (boundAsString.equals("inf"))
            bound = Bound.Infinity;
        else {
            try {
                int intBound = Integer.parseInt(boundAsString);
                bound = new IntBound(intBound);
            } catch (NumberFormatException e) {
                if (constants.containsConstantByName(boundAsString)) {
                    bound = new ConstantBound(constants.getConstantByName(boundAsString));
                } else
                    throw new RuntimeException("A constant which was not declared was used in an invariant.");
            }
        }
        return new ColoredTimeInvariant(operator.equals("<="), bound, color);
    }

    public String getInvariantString() {
        return super.toString();
    }

    public String getInvariantString(boolean displayConstantNames) {
        return super.toString(displayConstantNames);
    }
    

    @Override
    public boolean equals(Object o) {
        ColoredTimeInvariant cti;
        if (!(o instanceof ColoredTimeInvariant))
            return false;
        else
            cti = (ColoredTimeInvariant) o;

        if (!cti.color.equals(this.color))
            return false;
        if (!(cti.isUpperNonstrict() == this.isUpperNonstrict()))
            return false;
        return cti.upperBound().value() == this.upperBound().value();
    }

    public boolean equalsOnlyColor(Object o) {
        ColoredTimeInvariant cti;
        if (!(o instanceof ColoredTimeInvariant))
            return false;
        else
            cti = (ColoredTimeInvariant) o;

        return cti.color.equals(this.color);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + upperBound().hashCode();
        result = 31 * result + color.hashCode();
        return result;
    }

    @Override
    public ColoredTimeInvariant copy(){
        return new ColoredTimeInvariant(isUpperIncluded, upperBound().copy(), color);
    }
}
