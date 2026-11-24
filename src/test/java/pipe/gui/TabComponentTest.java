package pipe.gui;

import org.junit.jupiter.api.Test;
import pipe.gui.petrinet.PetriNetTab;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabComponentTest {

    @Test
    void changedIndicatorCanBeToggledWithoutChangingTabTitle() {
        var pane = new TestTabbedPane();
        pane.addTab("Example.tapn", new JPanel());
        var header = (TabComponent) pane.getTabComponentAt(0);

        header.setChanged(true);
        assertTrue(header.getComponent(0).isVisible());
        assertTrue(((JComponent) header.getComponent(0)).getToolTipText().contains("Unsaved"));

        header.setChanged(false);
        assertFalse(header.getComponent(0).isVisible());
        org.junit.jupiter.api.Assertions.assertEquals("Example.tapn", pane.getTitleAt(0));
    }

    private static final class TestTabbedPane extends net.tapaal.swinghelpers.ExtendedJTabbedPane<JPanel> {
        @Override
        public Component generator() {
            return new TabComponent(this) {
                @Override
                protected void closeTab(PetriNetTab tab) {
                }
            };
        }
    }
}
