package pipe.dataLayer.colors;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import pipe.gui.Pipe;

public class ColoredToken {
	private BigDecimal age;
	private int color;
	
	public ColoredToken(){
	}
	
	public ColoredToken(int value){
		this.setColor(value);
	}
	
	public ColoredToken(BigDecimal age, int color){
		this.setAge(age);
		this.setColor(color);
	}

	public void setAge(BigDecimal age) {
		this.age = age;
	}

	public BigDecimal getAge() {
		return age;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		return String.format("(%1s,%2d)", df.format(getAge()), getColor());
	}
	

}
