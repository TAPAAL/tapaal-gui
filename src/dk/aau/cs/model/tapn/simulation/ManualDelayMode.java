	package dk.aau.cs.model.tapn.simulation;

import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Formatter.BigDecimalLayoutForm;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.NumberFormatter;

import pipe.gui.CreateGui;
import pipe.gui.widgets.EscapableDialog;

import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.IntervalOperations;

public class ManualDelayMode implements DelayMode{
	
	private static ManualDelayMode instance;
	
	public static ManualDelayMode getInstance(){
		if(instance == null){
			instance = new ManualDelayMode();
		}
		return instance;
	}
	
	private ManualDelayMode(){};
	
	TimeInterval dInterval; 
	JButton okButton;
	boolean okPressed = false;
	JDialog dialog;
	
	public String toString() {
		return name();
	}
	
	public static String name(){
		return "Manual delay";
	}

	@Override
	public BigDecimal GetDelay(TimedTransition transition,
			TimeInterval dInterval, BigDecimal delayGranularity) {
		this.dInterval = dInterval;

		//Ask the user
		ChooseDelayPanel panel;
		dialog = new EscapableDialog(CreateGui.getApp(), "Choose delay", true);
		panel = new ChooseDelayPanel(transition, dInterval, delayGranularity);
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

		private JSpinner spinner;

		public ChooseDelayPanel(TimedTransition transition, 
				TimeInterval dInterval, BigDecimal delayGranularity) {
			super(new GridBagLayout());
			JPanel buttonPanel = createButtonPanel();
			BigDecimal value = IntervalOperations.getRatBound(dInterval.lowerBound()).getBound();

			if(!dInterval.IsLowerBoundNonStrict()){
				value = value.add(delayGranularity);
				value = value.stripTrailingZeros();
			}

			SpinnerModel model = new DelaySpinnerModel(value, BigDecimal.ONE, dInterval); 

			spinner = new JSpinner(model);
			JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
			editor.getTextField().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					okButton.requestFocus();
					okButton.doClick();
				}
			});

			editor.getTextField().setFormatterFactory(new AbstractFormatterFactory() {
				public AbstractFormatter getFormatter(JFormattedTextField tf) {
					NumberFormatter formatter = new CustomNumberFormatter(); 
					DecimalFormat decimalFormat = new DecimalFormat("#.#####"); 
					formatter.setFormat(decimalFormat); 
					formatter.setAllowsInvalid(false);
					return formatter;
				}
			});

			spinner.setEditor(editor);
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(5, 5, 0, 5);
			this.add(new JLabel("Choose delay from the interval: " + dInterval), gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(5, 5, 0, 5);
			this.add(spinner, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(5, 5, 0, 5);
			this.add(buttonPanel, gbc);
		}

		private JPanel createButtonPanel() {
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
			
			okButton = new JButton("OK");
			okButton.setPreferredSize(new java.awt.Dimension(100, 25));
			okButton.setMinimumSize(new java.awt.Dimension(100, 25));
			okButton.setMaximumSize(new java.awt.Dimension(100, 25));
			dialog.getRootPane().setDefaultButton(okButton);
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					okPressed = true;
					dialog.setVisible(false);
				}
			});
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
			cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
			cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
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
		
		private void updateOkButton(BigDecimal result){
			if(result != null && dInterval.isIncluded(result)){
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}

		public BigDecimal getResult(){
			return (BigDecimal) spinner.getValue();
		}
		
		private class CustomNumberFormatter extends 	NumberFormatter{
			
			public Object stringToValue(String string) throws ParseException {
				BigDecimal result = null;
				try{	
					string = string.replace(DecimalFormatSymbols.getInstance().getDecimalSeparator(), '.');
					if(string == null || string.isEmpty() || string.equals(".")){
						result = null;
					} else if(string.contains(".") && string.substring(string.indexOf(".")+1).length() > 5){
						throw new ParseException(string, 0);
					} else {
						result = new CustomBigDecimal(string);
					}
				} catch (NumberFormatException e) {
					throw new ParseException(string, 0);
				}
				
				updateOkButton(result);
				return result;
			}

			public String valueToString(Object arg0) throws ParseException {
				if(arg0 != null){
					return arg0.toString().replace('.', DecimalFormatSymbols.getInstance().getDecimalSeparator());
				} else {
					return null;
				}
			}
		}
		
		private class CustomBigDecimal extends BigDecimal{
			String stringRepres;
			
			public CustomBigDecimal(String string) {
				super(string.endsWith(".") && !string.matches(".*\\..*\\..*") ? 
						string.substring(0, string.length()-1) : string);
				stringRepres = string;
			}
			
			public String toString() {
				return stringRepres;
			}
		}

		private class DelaySpinnerModel extends SpinnerNumberModel{
			TimeInterval dInterval;

			public DelaySpinnerModel(Number value, Number stepSize, TimeInterval dInterval) {
				super(value, null, null, stepSize);
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
