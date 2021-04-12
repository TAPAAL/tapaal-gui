package dk.aau.cs.model.CPN;

import dk.aau.cs.model.tapn.*;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColoredTimeInterval extends TimeInterval {

    public static final ColoredTimeInterval ZERO_INF_COLOR_DOT = new ColoredTimeInterval(true,
            new IntBound(0), Bound.Infinity, false, new Color(new ColorType("dot"), 0, "dot"));

    private Color color;

    public ColoredTimeInterval(boolean isLowerIncluded, Bound lower, Bound upper, boolean isUpperIncluded, Color color) {
        super(isLowerIncluded, lower, upper, isUpperIncluded);
        this.color = color;
    }

    public static final ColoredTimeInterval ZERO_INF_DYN_COLOR(Color color) {
        return new ColoredTimeInterval(true,
                new IntBound(0), Bound.Infinity, false, color);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @SuppressWarnings("Duplicates")
    public static ColoredTimeInterval parse(String interval, ConstantStore constants, Vector<Color> colors) {
        Color color;
        if (colors.size() == 1)
            color = colors.get(0);
        else
            color = new Color(colors.get(0).getColorType(), 0, colors);

        Pattern pattern = Pattern.compile("^(\\[|\\()\\s*(\\w+)\\s*,\\s*(\\w+)(\\]|\\))$");
        Matcher matcher = pattern.matcher(interval);
        matcher.find();

        String leftBracket = matcher.group(1);
        String lowerBoundAsString = matcher.group(2);
        String upperBoundAsString = matcher.group(3);
        String rightBracket = matcher.group(4);

        if (!(leftBracket.equals("[") || leftBracket.equals("(")))
            return null;
        if (!(rightBracket.equals("]") || rightBracket.equals(")")))
            return null;

        Bound lowerBound = null;
        try {
            int intLower = Integer.parseInt(lowerBoundAsString);
            lowerBound = new IntBound(intLower);
        } catch (NumberFormatException e) {
            if (constants.containsConstantByName(lowerBoundAsString)) {
                lowerBound = new ConstantBound(constants.getConstantByName(lowerBoundAsString));
            } else
                throw new RuntimeException("A constant which was not declared was used in an time interval of an arc.");
        }

        Bound upperBound = null;
        if (upperBoundAsString.equals("inf"))
            upperBound = Bound.Infinity;
        else {
            try {
                int intBound = Integer.parseInt(upperBoundAsString);
                upperBound = new IntBound(intBound);
            } catch (NumberFormatException e) {
                if (constants.containsConstantByName(upperBoundAsString)) {
                    upperBound = new ConstantBound(constants.getConstantByName(upperBoundAsString));
                } else
                    throw new RuntimeException(
                            "A constant which was not declared was used in an time interval of an arc.");
            }
        }

        return new ColoredTimeInterval(leftBracket.equals("[") ? true : false,
                lowerBound, upperBound, rightBracket.equals("]") ? true : false, color);
    }


    @Override
   public String toString() {
        String print = "";
        if (color != null) {
            if (color.getTuple() != null) {
                for (Color color1 : color.getTuple()) {
                    print += color1.getColorName() + ", ";
                }
                print.substring(0, print.length()-2);
                print += " \u2192 " + super.toString();
                return print;
            } else {
                print += color.getColorName() + " \u2192 " + super.toString();
                return print;
            }
        } else {
            print += super.toString();
            return print;
        }
    }

    public String getInterval() {
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        ColoredTimeInterval cti;
        if (o instanceof ColoredTimeInterval) {
            cti = (ColoredTimeInterval)o;
        } else {
            return false;
        }

        if (!(cti.isLowerIncluded == this.isLowerIncluded))
            return false;

        if (cti.lowerBound().value() != this.lowerBound().value())
            return false;

        if (cti.upperBound().value() != this.upperBound().value())
            return false;

        if (cti.isUpperIncluded != this.isUpperIncluded)
            return false;

        if (!cti.color.equals(this.color))
            return false;

        return true;
    }

    public boolean equalsOnlyColor(Object o) {
        ColoredTimeInterval cti;
        if (o instanceof ColoredTimeInterval) {
            cti = (ColoredTimeInterval)o;
        } else {
            return false;
        }
        if (!cti.color.equals(this.color))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + lowerBound().hashCode();
        result = 31 * result + upperBound().hashCode();
        result = 31 * result + color.hashCode();
        return result;
    }
}