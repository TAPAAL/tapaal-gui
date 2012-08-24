	package dk.aau.cs.model.tapn.simulation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.BorderFactory;
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

import com.sun.tools.hat.internal.model.JavaBoolean;

import pipe.gui.CreateGui;
import pipe.gui.widgets.EscapableDialog;

import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.IntervalOperations;

public class ManualDelayMode implements DelayMode{
	
	TimeInterval dInterval; 
	JButton okButton;
	boolean okPressed = false;
	JDialog dialog;

	@Override
	public BigDecimal GetDelay(TimedTransition transition,
			TimeInterval dInterval, BigDecimal delayGranularity) {
		this.dInterval = dInterval;

		//Ask the user
		ChooseDelayPanel panel;
		panel = new ChooseDelayPanel(transition, dInterval, delayGranularity);
		dialog = new EscapableDialog(CreateGui.getApp(), "Choose delay", true);
		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		BigDecimal result;
		if(okPressed){
			result = panel.getResult();
		} else {
			result = null;
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
			super(new GridBagLayout());
			BigDecimal value = IntervalOperations.getRatBound(dInterval.lowerBound()).getBound();

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
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(5, 5, 0, 5);
			this.add(js, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(5, 5, 0, 5);
			this.add(createButtonPanel(), gbc);
		}

		private JPanel createButtonPanel() {
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
			
			okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					okPressed = true;
					dialog.setVisible(false);
				}
			});
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					okPressed = false;
					dialog.setVisible(false);
					
				}
			});
			
			buttonPanel.add(cancelButton);
			buttonPanel.add(okButton);
			buttonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			
			return buttonPanel;
		}
		
		public void updateOkButton(BigDecimal result){
			if(result != null && dInterval.isIncluded(result)){
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}

		public BigDecimal getResult(){
			return (BigDecimal) js.getValue();
		}
		
		public class CustomNumberFormatter extends 	NumberFormatter{
			
			public Object stringToValue(String string) throws ParseException {
				BigDecimal result = null;
				try{	
					string = string.replace(DecimalFormatSymbols.getInstance().getDecimalSeparator(), '.');
					if(string == null || string.isEmpty() || string.equals(".")){
						return null;
					} 
					if(string.contains(".") && string.substring(string.indexOf(".")+1).length() > 5){
						throw new ParseException(string, 0);
					}
						
					result = new CustomBigDecimal(string);
					return  result;

				} catch (NumberFormatException e) {
					throw new ParseException(string, 0);
				} finally {
					updateOkButton(result);
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
			
			public boolean getTrailingSepatator(){
				return trailingSeparator;
			}
			
			public CustomBigDecimal(String string) {
				super(string.endsWith(".") ? 
						string.substring(0, string.length()-1) : string);
				
				if(string.endsWith(".")){
					trailingSeparator = true;
				}
				if(string.startsWith(".")){
					leadingSeparator = true;
				}
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
}
