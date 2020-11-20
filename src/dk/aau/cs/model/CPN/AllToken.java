package dk.aau.cs.model.CPN;

import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import pipe.gui.Pipe;

import java.text.DecimalFormat;
import java.util.Vector;

public class AllToken extends TimedToken {
    TimedPlace place;
    ColorType colorType;
    Vector<Object> elements;
    public AllToken(TimedPlace place, ColorType colorType, Vector<Object> elements) {
        super(place, null);
        this.place = place;
        this.colorType = colorType;
        this.elements = elements;
    }

    @Override
    public String toString() {
        /*String token = "<";
        if (color.getTuple() != null &&  color.getTuple().size() != 0) {
            for (Color element : color.getTuple()) {
                token += element.getColorName() + ", ";
            }
        }
        else {
            return token + color.getColorName() + ">";
        }
        token = token.substring(0, token.length()-2);
        return token + ">";*/
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);

        return String.format("(%s, %s, %s)", place.toString(), df.format(age), colorType.toString());
    }
    //This is for colors
    @Override
    public String toStringForPNML() {
        return colorType.getName() + ".all";
    }
    @Override
    public String getFormattedTokenString(){
        return colorType.getName() + ".all";
    }

    public Color color() {
        return null;
    }

    public Vector<Object> getElements(){
        return elements;
    }
}
