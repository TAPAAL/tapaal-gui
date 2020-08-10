package dk.aau.cs.model.CPN;

import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;

import java.math.BigDecimal;

public class ColoredToken extends TimedToken {

    private Color color;

    public ColoredToken(TimedPlace place, Color color) {
        super(place);
        this.color = color;
    }

    public ColoredToken(TimedPlace place, Color color, BigDecimal age) {
        super(place, age);
        this.color = color;
    }

    public Color color() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {return color;}

    public ColoredToken clone() {
        return (ColoredToken) super.clone();
    }

    @Override
    public String toString() {
        String token = "<";
        if (color.getTuple() != null &&  color.getTuple().size() != 0) {
            for (Color element : color.getTuple()) {
                token += element.getColorName() + ", ";
            }
        }
        else {
            return token + color.getColorName() + ">";
        }
        token = token.substring(0, token.length()-2);
        return token + ">";
    }

    public String toStringForPNML() {
        String token = "[";
        if (color.getTuple() != null && color.getTuple().size() != 0) {
            for (Color element : color.getTuple()) {
                token += "(" + element.getColorName() + ")";
            }
            return token + "]";
        } else {
            return token += "(" + color.getColorName() + ")]";
        }
    }

}