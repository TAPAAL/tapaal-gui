package pipe.gui.widgets;

import dk.aau.cs.model.CPN.AllToken;
import dk.aau.cs.model.tapn.TimedToken;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TokenTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        Component superRenderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        if(table.getModel().getValueAt(row, col) instanceof TimedToken) {
            setText(((TimedToken) table.getModel().getValueAt(row, col)).getFormattedTokenString());
        }
        return superRenderer;
    }
}
