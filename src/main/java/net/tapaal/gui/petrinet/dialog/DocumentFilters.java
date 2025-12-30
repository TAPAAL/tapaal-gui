package net.tapaal.gui.petrinet.dialog;

import javax.swing.JTextField;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class DocumentFilters extends DocumentFilter {
    private static final String INT_REGEX = "^\\d*$";
    private static final String DOUBLE_REGEX = "^\\d*\\.?\\d*$";

    public static DocumentFilter createIntegerFilter(int maxLength) {
        return createFilter(INT_REGEX, maxLength);
    }

    public static DocumentFilter createDoubleFilter(int maxLength) {
        return createFilter(DOUBLE_REGEX, maxLength);
    }

    private static DocumentFilter createFilter(String regex, int maxLength) {
        return new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                String newValue = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()))
                    .insert(offset, string)
                    .toString();
                if (newValue.matches(regex) && newValue.length() <= maxLength) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                String newValue = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()))
                    .replace(offset, offset + length, text)
                    .toString();
                if (newValue.matches(regex) && newValue.length() <= maxLength) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        };
    }

    public static void applyIntegerFilter(JTextField textField, int maxLength) {
        ((PlainDocument)textField.getDocument()).setDocumentFilter(createIntegerFilter(maxLength));
    }

    public static void applyIntegerFilter(JTextField textField, int min, int max) {
        ((PlainDocument)textField.getDocument()).setDocumentFilter(createIntegerFilter(min, max));
    }

    public static DocumentFilter createIntegerFilter(int min, int max) {
        return new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                String newValue = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()))
                    .insert(offset, string)
                    .toString();
                if (isValid(newValue, min, max)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                String newValue = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()))
                    .replace(offset, offset + length, text)
                    .toString();
                if (isValid(newValue, min, max)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean isValid(String value, int min, int max) {
                if (value.isEmpty()) return true;
                if (!value.matches(INT_REGEX)) return false;
                try {
                    long intVal = Long.parseLong(value);
                    return intVal >= min && intVal <= max;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };
    }

    public static void applyDoubleFilter(JTextField textField, int maxLength) {
        ((PlainDocument)textField.getDocument()).setDocumentFilter(createDoubleFilter(maxLength));
    }

    public static void applyIntegerFilter(JTextField textField) {
        applyIntegerFilter(textField, 9);
    }

    public static void applyDoubleFilter(JTextField textField) {
        applyDoubleFilter(textField, 9);
    }
}
