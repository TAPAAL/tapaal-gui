package pipe.dataLayer.colors;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import pipe.gui.Pipe;
import pipe.gui.undo.TokenValueEdit;
import pipe.gui.undo.UndoableEdit;

public class ColoredToken {
	private BigDecimal age;
	private IntOrConstant color = new IntOrConstant();
	private boolean displayValues = false;
	
	public ColoredToken(){
		age = new BigDecimal(0, new MathContext(Pipe.AGE_DECIMAL_PRECISION));
	}
	
	public ColoredToken(int color){
		this();
		this.setColor(new IntOrConstant(color));
	}
	
	public ColoredToken(IntOrConstant color){
		this();
		this.setColor(color);
	}
	
	public ColoredToken(BigDecimal age, int color){
		this.setAge(age);
		this.setColor(new IntOrConstant(color));
	}

	public ColoredToken(BigDecimal age, IntOrConstant color) {
		this.age = age;
		this.color = color;
	}

	public ColoredToken(ColoredToken token) {
		this.age = token.getAge();
		this.color = new IntOrConstant(token.getColor());
	}

	private void setAge(BigDecimal age) {
		this.age = age;
	}

	public BigDecimal getAge() {
		return age;
	}
	
	public UndoableEdit setColor(IntOrConstant newValue) {
		if(newValue.getValue() < 0) throw new IllegalArgumentException();
		IntOrConstant old = this.color;
		this.color = newValue;
		
		return new TokenValueEdit(this,old,newValue);		
	}

	public IntOrConstant getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		return String.format("(%1$s,%2$s)", df.format(getAge()), getColor().toString(displayValues));
	}
	
	public void doTimeDelay(BigDecimal delay){
		this.age = age.add(delay);
	}

	public void displayValues(boolean showValues) {
		this.displayValues = showValues;
		
	}

	public void updateConstantName(String oldName, String newName) {
		color.updateConstantName(oldName, newName);		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColoredToken other = (ColoredToken) obj;
		if (age == null) {
			if (other.age != null)
				return false;
		} else if (!age.equals(other.age))
			return false;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		return true;
	}
	

}
