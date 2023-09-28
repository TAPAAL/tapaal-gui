package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.CPN.Variable;

import javax.swing.*;
import java.awt.*;

public class ColorComboBoxRenderer extends JLabel implements ListCellRenderer {
    final JComboBox comboBox;
    final int adjust;

    public ColorComboBoxRenderer(JComboBox comboBox) {
        this(comboBox, 0);
    }

    public ColorComboBoxRenderer(JComboBox comboBox, int adjust) {
        this.comboBox = comboBox;
        this.adjust = adjust;
    }

    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {

        if (value instanceof JSeparator){
            return new JSeparator(JSeparator.HORIZONTAL);
        }
        else if (value instanceof Variable){
            String text = ((Variable)value).getName();

            setText(ellipsis(text, maxChars(text, list, adjust)));
            setFont(list.getFont());
        }
        else if (value != null) {
            String text = value.toString();
    
            setText(ellipsis(text, maxChars(text, list, adjust)));
            setFont(list.getFont());
        }

        return this;
    }
    
    // Calculates max possible chars that can be shown on list - padding
    private int maxChars(String text, JList list, int adjust) {    
        FontMetrics metrics = list.getFontMetrics(list.getFont());
        
        int width = 0;
        int comboBoxWidth = comboBox.getWidth();
        
        for (int i = 0; i < text.length(); ++i) {
            int cWidth = metrics.charWidth(text.charAt(i));
            
            if (width + cWidth > comboBoxWidth) {
                return i - adjust;
            }
            
            width += cWidth;
        }
        
        return text.length();
    }

    
    // Inspired by: https://stackoverflow.com/questions/3597550/ideal-method-to-truncate-a-string-with-ellipsis
    public static String ellipsis(final String text, int length)
    {
        if (length > 3 && text.length() > length) {
            return text.substring(0, length - 3) + "...";
        }

        return text;
    }
}
