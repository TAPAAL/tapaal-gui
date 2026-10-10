package pipe.gui;

import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TabComponentTest {

    @Test
    void leftClickingTabTitleSelectsTab() {
        JTabbedPane pane = new JTabbedPane();
        pane.addTab("first", new JPanel());
        pane.addTab("second", new JPanel());

        TabComponent tabComponent = new TabComponent(pane) {
            @Override
            protected void closeTab(pipe.gui.petrinet.PetriNetTab tab) {
            }
        };
        pane.setTabComponentAt(1, tabComponent);

        JLabel title = (JLabel) tabComponent.getComponent(0);
        title.dispatchEvent(new MouseEvent(
            title,
            MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(),
            0,
            1,
            1,
            1,
            false,
            MouseEvent.BUTTON1
        ));

        assertEquals(1, pane.getSelectedIndex());
    }
}
