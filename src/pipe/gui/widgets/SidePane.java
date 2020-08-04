package pipe.gui.widgets;

import javax.swing.*;

public interface SidePane {
    void moveUp(int index);
    void moveDown(int index);
    JList getJList();
}
