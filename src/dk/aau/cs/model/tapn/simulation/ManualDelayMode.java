package dk.aau.cs.model.tapn.simulation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.IntervalOperations;

public class ManualDelayMode implements DelayMode{

	@Override
	public BigDecimal GetDelay(TimedTransition transition,
			TimeInterval dInterval, BigDecimal delayGranularity) {

		//Ask the user
		ChooseDelayPanel panel;

		JOptionPane.showInputDialog(panel = new ChooseDelayPanel(transition, dInterval, delayGranularity));
		
		BigDecimal result = panel.getResult();
		
		//If the user writes something not valid, fix it
		BigDecimal lowerAsNumber = IntervalOperations.getRatBound(dInterval.lowerBound()).getBound();
		BigDecimal upperAsNumber = IntervalOperations.getRatBound(dInterval.upperBound()).getBound();
		
		if(dInterval.IsLowerBoundNonStrict() && result.compareTo(lowerAsNumber) < 0){
			result = lowerAsNumber;
		} else if(!dInterval.IsLowerBoundNonStrict() && result.compareTo(lowerAsNumber) <= 0){
			result = lowerAsNumber.add(delayGranularity);
		} else if(dInterval.IsUpperBoundNonStrict() && result.compareTo(upperAsNumber) > 0){
			result = upperAsNumber;
		} else if(!dInterval.IsUpperBoundNonStrict() && result.compareTo(upperAsNumber) >= 0){
			result = upperAsNumber.subtract(delayGranularity);
		}

		return result;
	}

	private class ChooseDelayPanel extends JPanel{
		private static final long serialVersionUID = -1564890407314003743L;

		JSpinner js;
		Comparable<BigDecimal> lower;
		Comparable<BigDecimal> upper;

		public ChooseDelayPanel(TimedTransition transition, 
				TimeInterval dInterval, BigDecimal delayGranularity) {
			super(new BorderLayout());
			BigDecimal value;
			BigDecimal lowerAsNumber = IntervalOperations.getRatBound(dInterval.lowerBound()).getBound();
			BigDecimal upperAsNumber = IntervalOperations.getRatBound(dInterval.upperBound()).getBound();

			value = lowerAsNumber;

			if(!dInterval.IsLowerBoundNonStrict()){
				value = value.add(delayGranularity);
			}

			SpinnerModel model = new DelaySpinnerModel(value, lower, upper, BigDecimal.ONE, dInterval); 

			js = new JSpinner(model);
			JSpinner.NumberEditor editor = new JSpinner.NumberEditor(js);

			editor.getTextField().setFormatterFactory(new AbstractFormatterFactory() {
				public AbstractFormatter getFormatter(JFormattedTextField tf) {
					NumberFormatter formatter = new CustomNumberFormatter(); 
					DecimalFormat decimalFormat = new DecimalFormat("#.#####"); 
					formatter.setFormat(decimalFormat); 
					formatter.setAllowsInvalid(false);
					return formatter;
				}
			});

			js.setEditor(editor);
			
			this.add(js, BorderLayout.NORTH);
		}

		public BigDecimal getResult(){
			return (BigDecimal) js.getValue();
		}
	}

	public class CustomNumberFormatter extends 	NumberFormatter{
		
		public Object stringToValue(String string) throws ParseException {
			string = string.replace(DecimalFormatSymbols.getInstance().getDecimalSeparator(), '.');
			
			if(string == null || string.isEmpty() || string.equals(".")){
				return null;
			} 
			if(string.contains(".") && string.substring(string.indexOf(".")+1).length() > 5){
				throw new ParseException(string, 0);
			}
			try{
				return new CustomBigDecimal(string);
			}
			catch (NumberFormatException e) {
				throw new ParseException(string, 0);
			}
		}
		
		@Override
		public String valueToString(Object arg) throws ParseException {
			String result = super.valueToString(arg);
			
			if(arg instanceof CustomBigDecimal){
				CustomBigDecimal number = (CustomBigDecimal) arg;
				if(number.getTrailingSepatator()){
					result += DecimalFormatSymbols.getInstance().getDecimalSeparator();
				}
				if(number.getLeadingSeparator()){
					result = result.substring(1, result.length());
				}
			}
			return result;
		}
	}
	
	private class CustomBigDecimal extends BigDecimal{
		boolean trailingSeparator = false;
		boolean leadingSeparator = false;
		
		public boolean getLeadingSeparator() {
			return leadingSeparator;
		}

		public CustomBigDecimal(String string) {
			super(string.endsWith(".") ? 
					string.substring(0, string.length()-1) : string);
			
			if(string.endsWith(".")){
				System.out.println("The string: " + string + " matches");
				trailingSeparator = true;
			}
			if(string.startsWith(".")){
				leadingSeparator = true;
			}
		}
		
		public boolean getTrailingSepatator(){
			return trailingSeparator;
		}
	}

	private class DelaySpinnerModel extends SpinnerNumberModel{
		TimeInterval dInterval;

		public DelaySpinnerModel(Number value, Comparable minimum, Comparable maximum, Number stepSize, TimeInterval dInterval) {
			super(value, minimum, maximum, stepSize);
			this.dInterval = dInterval;
		}

		@Override
		public Object getNextValue() {
			BigDecimal currentValue = (BigDecimal) getValue();
			if(dInterval.isIncluded(currentValue.add(BigDecimal.ONE))){
				return currentValue.add(BigDecimal.ONE);
			} else {
				return null;
			}
		}

		@Override
		public Object getPreviousValue() {
			BigDecimal currentValue = (BigDecimal) getValue();
			if(dInterval.isIncluded(currentValue.subtract(BigDecimal.ONE))){
				return currentValue.subtract(BigDecimal.ONE);
			} else {
				return null;
			}
		}
	}
}
