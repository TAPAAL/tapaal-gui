package pipe.gui.widgets;

import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DecimalOnlyDocumentFilter extends DocumentFilter{
	
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
			//this method is only called by programmatic editing of the text box! 
		}

		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {                 
			String old = fb.getDocument().getText(0, fb.getDocument().getLength());
			StringBuffer newString = new StringBuffer(old);
			newString.replace(offset, length+offset, "");
			if (stringIsValidDecimal(newString.toString())) {
				super.remove(fb, offset, length);
			}
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			String old = fb.getDocument().getText(0, fb.getDocument().getLength());
			StringBuffer newString = new StringBuffer(old);
			newString.replace(offset, length+offset, text);            	 			
			if (stringIsValidDecimal(newString.toString())) {                	
				super.replace(fb, offset, length, text, attrs);
			}
		}
		private boolean stringIsValidDecimal(String text) {
			return Pattern.matches("^(([1-9]([0-9])*)?|0)(\\.([0-9])*)?$",text);
		}
	}

