<<<<<<< HEAD
package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.CPN.Variable;

import javax.swing.*;
import java.awt.*;

public class ColorComboBoxRenderer extends JLabel implements ListCellRenderer {
    final JComboBox comboBox;
    public ColorComboBoxRenderer(JComboBox comboBox) {
        this.comboBox = comboBox;
    }

    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
        if(value instanceof JSeparator){
            return  new JSeparator(JSeparator.HORIZONTAL);
        }
        else if(value instanceof Variable){
            setText(ellipsis(((Variable)value).getName(), comboBox.getWidth() / 7));

            setFont(list.getFont());
        }
        else if(value != null) {
            setText(ellipsis(value.toString(), comboBox.getWidth() / 7));

            setFont(list.getFont());
        }

        return this;
    }
    //From here https://stackoverflow.com/questions/3597550/ideal-method-to-truncate-a-string-with-ellipsis


    public static String ellipsis(final String text, int length)
    {
        if(length > 3) {
            // The letters [iIl1] are slim enough to only count as half a character.
            length += Math.ceil(text.replaceAll("[^iIl]", "").length() / 2.0d);

            if (text.length() > length) {
                return text.substring(0, length - 3) + "...";
            }
        }

        return text;
    }
}
=======
package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.CPN.Variable;

import javax.swing.*;
import java.awt.*;

public class ColorComboBoxRenderer extends JLabel implements ListCellRenderer {
    final JComboBox comboBox;
    public ColorComboBoxRenderer(JComboBox comboBox) {
        this.comboBox = comboBox;
    }

    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
        if(value instanceof JSeparator){
            return  new JSeparator(JSeparator.HORIZONTAL);
        }
        else if(value instanceof Variable){
            setText(ellipsis(((Variable)value).getName(), comboBox.getWidth() / 7));

            setFont(list.getFont());
        }
        else if(value != null) {
            setText(ellipsis(value.toString(), comboBox.getWidth() / 7));

            setFont(list.getFont());
        }

        return this;
    }
    //From here https://stackoverflow.com/questions/3597550/ideal-method-to-truncate-a-string-with-ellipsis


    public static String ellipsis(final String text, int length)
    {
        if(length > 3) {
            // The letters [iIl1] are slim enough to only count as half a character.
            length += Math.ceil(text.replaceAll("[^iIl]", "").length() / 2.0d);

            if (text.length() > length) {
                return text.substring(0, length - 3) + "...";
            }
        }

        return text;
    }
}
>>>>>>> origin/cpn
