package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import dk.aau.cs.model.CPN.Color;
import pipe.gui.Constants;

public class TimedToken {
	private final TimedPlace place;
	protected BigDecimal age;
    private Color color;


    public TimedToken(TimedPlace place, Color color) {
		this(place, BigDecimal.ZERO, color);
	}

	public TimedToken(TimedPlace place, BigDecimal age, Color color) {
		this.place = place;
		this.age = age;
		this.color = color;
	}

    public Color color() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {return color;}
    public TimedPlace place() {
		return place;
	}

	public BigDecimal age() {
		return age;
	}
	
	public void setAge(BigDecimal age) {
		this.age = age;		
	}
	
	public TimedToken clone() {
		return new TimedToken(place, age, color); // age is immutable so ok to pass it to constructor
	}

	public TimedToken delay(BigDecimal delay) {
		return new TimedToken(place, age.add(delay), color);
	}
    @Override
	public String toString() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Constants.AGE_DECIMAL_PRECISION);

        return String.format("(%s, %s, %s)", place.toString(), df.format(age), color.toString());
	}
    //This is for colors
    public String toStringForPNML() {
        String token = "";
        if (color.getTuple() != null && color.getTuple().size() != 0) {
            for (Color element : color.getTuple()) {
                token += "(" + element.getColorName() + ")";
            }
            return token + "";
        } else {
            return token += "(" + color.getColorName() + ")";
        }
    }

    public String getFormattedTokenString(){
        return color.getColorName();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		result = prime * result + ((place == null) ? 0 : place.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TimedToken))
			return false;
		TimedToken other = (TimedToken) obj;
		if (age == null) {
			if (other.age != null)
				return false;
		} else if (age.compareTo(other.age) != 0){
			return false;
		}
		if(color == null) {
		    if (other.color != null) {
		        return false;
            }
        } else if(!color.equals(other.color)){
		    return false;
        }
		if (place == null) {
			if (other.place != null)
				return false;
		} else if (!place.equals(other.place))
			return false;
		return true;
	}

}
