package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;

import javax.swing.*;
import java.awt.*;

public class ColortypeListCellRenderer extends JLabel implements ListCellRenderer<Object> {
    // Custom cell renderer for the file list to only display the name of the
    private final static int MAXIMUM_TO_DISPLAY = 5;
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        if(value instanceof ColorType && !(value instanceof ProductType)){

            ColorType ct = (ColorType)value;
            if(ct.getColors().size() <= MAXIMUM_TO_DISPLAY){
                setText(ct.toString());
            } else{
                String out = "<html>" + ct.getName() + "<b> is </b>[";
                int i = 0;
                for (Color element : ct.getColors()) {
                    i++;
                    out += element.getColorName() + ", ";
                    if(i > MAXIMUM_TO_DISPLAY -1 && i < ct.getColors().size() - 1){
                        out += "... ,";
                        break;
                    }
                }
                if(i < ct.getColors().size() - 1){
                    out += ct.getColors().get(ct.getColors().size() -1 );
                } else{
                    out = out.substring(0, out.length() - 2);
                }
                out += "]" + "</html>";
                setText(out);
            }
        } else{
            setText(value.toString());
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);

        return this;
    }
}


