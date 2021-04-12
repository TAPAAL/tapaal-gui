package net.tapaal.swinghelpers;

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;
import javax.swing.text.InternationalFormatter;

public class CustomJSpinner extends JSpinner{

	@Override
	public Object getValue() {
		//this is a hack.
		//for some reason when the value in the Jspinner is 0, the value is a Long instead of an Integer.
		//we always want an Integer. 
		Object originalValue = getModel().getValue();
		int val;				
		if (originalValue instanceof Long) {
			val = (int)((Long) originalValue).longValue();
		}
		else {
			val = (Integer) originalValue;
		}
		return new Integer(val);
	}
	
	public CustomJSpinner(Integer value, final Integer minimumValue, final Integer maximumValue) {
		JSpinner.NumberEditor jsEditor =
				(JSpinner.NumberEditor)this.getEditor();
			final JFormattedTextField textField = jsEditor.getTextField();
			final DocumentFilter digitOnlyFilter = new DocumentFilter() {
				@Override
				public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
					//this method is only called by programatic editing of the textbox! 
				}

				@Override
				public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {                 
					String old = fb.getDocument().getText(0, fb.getDocument().getLength());
					StringBuilder newString = new StringBuilder(old);
					newString.replace(offset, length+offset, "");
					if (stringIsValidInteger(newString.toString())) {
						super.remove(fb, offset, length);
					}
				}

				@Override
				public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
					String old = fb.getDocument().getText(0, fb.getDocument().getLength());
					StringBuilder newString = new StringBuilder(old);
					newString.replace(offset, length+offset, text);            	 			
					if (stringIsValidInteger(newString.toString())) {                	
						super.replace(fb, offset, length, text, attrs);
					}
				}
				private boolean stringIsValidInteger(String text) {
					Integer intValue = null;
					if (text.equals("")){
						return true;
					}
					if (!Pattern.matches("^([1-9]([0-9])*)?|0$",text)){
						return false;
					}
					try {
						intValue = Integer.parseInt(text);
					}
					catch (NumberFormatException e) {
						return false;
					}
					if (intValue < minimumValue || intValue > maximumValue) {
						return false;
					}
					return true;
				}
			};
			NumberFormat format = NumberFormat.getIntegerInstance();
			format.setGroupingUsed(false);
			textField.setFormatterFactory(new DefaultFormatterFactory(
					new InternationalFormatter(format){
						@Override
						protected DocumentFilter getDocumentFilter() {
							return digitOnlyFilter;
						}
					}));
			if (value != null) {
				this.setValue(value);
			}
			//this changelistener is added to listen for changes on account of the up and down arrows. 
			this.addChangeListener(e -> {
				//if currentValue is less than minValue of larger than maxValue then make it min or max.
				if ((Integer)((JSpinner)e.getSource()).getValue() < minimumValue) {
					((JSpinner)e.getSource()).setValue(minimumValue);
				}
				else if ((Integer)((JSpinner)e.getSource()).getValue() > maximumValue) {
					((JSpinner)e.getSource()).setValue(maximumValue);
				}
			});
			SwingHelper.setPreferredWidth(this, 100);
			
			final JSpinner spinner = this;

			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
				if(ke.getID()==KeyEvent.KEY_PRESSED && ke.getKeyCode() == KeyEvent.VK_ENTER &&
						KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == textField)
				{
					try
					{
						spinner.commitEdit();
						KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(spinner, ke);
					}catch(Exception e){}
				}
				return false;
			});
	}
	
	public CustomJSpinner(Integer value, final JButton okButton) {
		this(value);
		if (okButton != null) {
			JSpinner.NumberEditor jsEditor =
					(JSpinner.NumberEditor)this.getEditor();
			JFormattedTextField textField = jsEditor.getTextField();
			//this actionlistener makes the okButtons click event fire 			
			//when enter is pressed in the JSpinner. 
			textField.addActionListener(e -> {
				okButton.requestFocusInWindow();
				okButton.doClick();
			});
		}
	}

	public CustomJSpinner(Integer value) {
		this(value,0,Integer.MAX_VALUE);
	} 
}
