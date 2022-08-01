package net.tapaal.gui.petrinet.widgets;

import javax.swing.*;

public interface SidePane {
    void moveUp(int index);
    void moveDown(int index);
    JList getJList();
}
