package pipe.gui.widgets;

import java.text.DecimalFormatSymbols;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DecimalOnlyDocumentFilter extends DocumentFilter{
		
		int numberOfDecimalPlaces;
	
		public DecimalOnlyDocumentFilter() {
			this(-1);
		}
	
		public DecimalOnlyDocumentFilter(int numberOfDecimalPlaces){
			this.numberOfDecimalPlaces = numberOfDecimalPlaces;
		}
	
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
			char localDecimalseparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
			Pattern pattern = Pattern.compile("^(([1-9]([0-9])*)?|0)(" + Pattern.quote(Character.toString(localDecimalseparator)) + "([0-9]*))?$");
			Matcher m = pattern.matcher(text);
			return m.matches() && (numberOfDecimalPlaces < 0 || m.group(5) == null || m.group(5).length() <= numberOfDecimalPlaces);
		}
	}

