package pipe.gui.widgets;

import javax.swing.*;

public interface SidePane {
    public void moveUp(int index);
    public void moveDown(int index);
    public JList getJList();
}
